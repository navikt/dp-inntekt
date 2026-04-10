package no.nav.dagpenger.inntekt

import com.github.navikt.tbd_libs.naisful.naisApp
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.ServerReady
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import io.prometheus.metrics.tracer.initializer.SpanContextSupplier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.inntekt.Config.inntektApiConfig
import no.nav.dagpenger.inntekt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.inntekt.db.PostgresInntektStore
import no.nav.dagpenger.inntekt.dpbehandling.DpBehandlingKlient
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentKtorClient
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.httpClient
import no.nav.dagpenger.inntekt.oppslag.pdl.PdlGraphQLRepository
import no.nav.dagpenger.inntekt.oppslag.pdl.pdlGraphQLClientFactory
import no.nav.dagpenger.inntekt.serder.inntektObjectMapper
import no.nav.dagpenger.inntekt.subsumsjonbrukt.KafkaSubsumsjonBruktDataConsumer
import no.nav.dagpenger.inntekt.subsumsjonbrukt.Vaktmester
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

private val LOGGER = KotlinLogging.logger {}
private val configuration = Config.config

private val meterRegistry =
    PrometheusMeterRegistry(
        PrometheusConfig.DEFAULT,
        PrometheusRegistry.defaultRegistry,
        Clock.SYSTEM,
        SpanContextSupplier.getSpanContext(),
    )

fun main() {
    runBlocking {
        val config = configuration.inntektApiConfig
        PostgresDataSourceBuilder.runMigration()
        val dataSource = PostgresDataSourceBuilder.dataSource
        val postgresInntektStore = PostgresInntektStore(dataSource)
        val pdlPersonOppslag =
            PdlGraphQLRepository(
                client =
                    pdlGraphQLClientFactory(
                        url = config.pdl.url,
                        oidcProvider = Config.pdlTokenProvider,
                    ),
            )
        val enhetsregisterClient =
            EnhetsregisterClient(
                baseUrl = config.enhetsregisteretUrl.url,
                httpClient = httpClient(),
            )
        val inntektskomponentHttpClient =
            InntektkomponentKtorClient(
                config.application.hentinntektListeUrl,
                Config.inntektsKomponentTokenProvider,
            )
        val dpBehandlingKlient =
            DpBehandlingKlient(
                hentOboTokenForDpBehandling = Config.dpBehandlingTokenProvider,
                dpBehandlingBaseUrl = config.dpBehandling.url,
                httpKlient = httpClient(),
            )
        val cachedInntektsGetter = BehandlingsInntektsGetter(inntektskomponentHttpClient, postgresInntektStore)
        // Marks inntekt as used
        val subsumsjonBruktDataConsumer =
            KafkaSubsumsjonBruktDataConsumer(
                KafkaSubsumsjonBruktDataConsumer.kafkaConsumer(config.application.id),
                config.inntektBruktDataTopic,
                postgresInntektStore,
            )
        val helsesjekker =
            listOf(
                postgresInntektStore,
                subsumsjonBruktDataConsumer,
            )

        naisApp(
            port = config.application.httpPort,
            meterRegistry = meterRegistry,
            objectMapper = inntektObjectMapper,
            applicationLogger = LoggerFactory.getLogger("ApplicationLogger"),
            callLogger = LoggerFactory.getLogger("CallLogger"),
            aliveCheck = aliveCeck(helsesjekker),
            readyCheck = readyCheck(postgresInntektStore),
            statusPagesConfig = { statusPagesConfig() },
        ) {
            monitor.subscribe(ServerReady) {
                val exceptionHandler =
                    CoroutineExceptionHandler { _, throwable ->
                        LOGGER.error(throwable) { "Exception caught" }
                    }
                val scope = CoroutineScope(Dispatchers.Default + exceptionHandler + SupervisorJob())
                val job =
                    scope.launch {
                        subsumsjonBruktDataConsumer.listen()
                    }
                monitor.subscribe(ApplicationStopped) {
                    LOGGER.info { "Forsøker å lukke datasource og jobber..." }
                    job.cancel()
                    subsumsjonBruktDataConsumer.stop()
                    dataSource.close()
                    LOGGER.info { "Lukket datasource" }
                }
            }
            inntektApi(
                config = configuration,
                inntektskomponentHttpClient = inntektskomponentHttpClient,
                inntektStore = postgresInntektStore,
                behandlingsInntektsGetter = cachedInntektsGetter,
                personOppslag = pdlPersonOppslag,
                enhetsregisterClient = enhetsregisterClient,
                dpBehandlingKlient = dpBehandlingKlient,
            )
        }.start(wait = true)

        // Cleans up unused inntekt on a regular interval
        Vaktmester(dataSource).also {
            fixedRateTimer(
                name = "vaktmester",
                initialDelay = TimeUnit.MINUTES.toMillis(10),
                period = TimeUnit.HOURS.toMillis(12),
                action = {
                    LOGGER.info { "Vaktmesteren rydder.. SLÅTT AVV" }
                    // it.rydd()
                    LOGGER.info { "Vaktmesteren er ferdig.. for denne gang" }
                },
            )
        }
    }
}

private fun readyCheck(postgresInntektStore: PostgresInntektStore): () -> Boolean = { postgresInntektStore.status() == HealthStatus.UP }

private fun aliveCeck(helsesjekker: List<HealthCheck>): () -> Boolean =
    {
        helsesjekker.all { it.status() == HealthStatus.UP }.also { isAlive ->
            if (!isAlive) {
                LOGGER.warn {
                    "En eller flere helsesjekker er nede! Helsejekker status: ${
                        helsesjekker.joinToString { hc ->
                            "${hc::class.simpleName}=${hc.status()}"
                        }
                    }"
                }
            }
        }
    }

package no.nav.dagpenger.inntekt

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.Config.inntektApiConfig
import no.nav.dagpenger.inntekt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.inntekt.db.PostgresInntektStore
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentKtorClient
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.httpClient
import no.nav.dagpenger.inntekt.oppslag.pdl.PdlGraphQLRepository
import no.nav.dagpenger.inntekt.oppslag.pdl.pdlGraphQLClientFactory
import no.nav.dagpenger.inntekt.subsumsjonbrukt.KafkaSubsumsjonBruktDataConsumer
import no.nav.dagpenger.inntekt.subsumsjonbrukt.Vaktmester
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

private val LOGGER = KotlinLogging.logger {}
private val configuration = Config.config

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
        val cachedInntektsGetter = BehandlingsInntektsGetter(inntektskomponentHttpClient, postgresInntektStore)
        // Marks inntekt as used
        val subsumsjonBruktDataConsumer =
            KafkaSubsumsjonBruktDataConsumer(config, postgresInntektStore)
                .apply {
                    listen()
                }.also {
                    Runtime.getRuntime().addShutdownHook(
                        Thread {
                            it.stop()
                        },
                    )
                }

        // Provides a HTTP API for getting inntekt
        embeddedServer(Netty, port = config.application.httpPort) {
            inntektApi(
                configuration,
                inntektskomponentHttpClient,
                postgresInntektStore,
                cachedInntektsGetter,
                pdlPersonOppslag,
                enhetsregisterClient,
                listOf(
                    postgresInntektStore as HealthCheck,
                    subsumsjonBruktDataConsumer as HealthCheck,
                ),
            )
        }.start().also {
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    it.stop(5, 60, TimeUnit.SECONDS)
                },
            )
        }
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

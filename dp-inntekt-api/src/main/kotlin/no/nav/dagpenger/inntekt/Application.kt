package no.nav.dagpenger.inntekt

import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.Config.inntektApiConfig
import no.nav.dagpenger.inntekt.db.KronetilleggUttrekk
import no.nav.dagpenger.inntekt.db.PostgresInntektStore
import no.nav.dagpenger.inntekt.db.dataSourceFrom
import no.nav.dagpenger.inntekt.db.migrate
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentKtorClient
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.httpClient
import no.nav.dagpenger.inntekt.oppslag.pdl.PdlGraphQLRepository
import no.nav.dagpenger.inntekt.oppslag.pdl.pdlGraphQLClientFactory
import no.nav.dagpenger.inntekt.subsumsjonbrukt.KafkaSubsumsjonBruktDataConsumer
import no.nav.dagpenger.inntekt.subsumsjonbrukt.Vaktmester
import no.nav.dagpenger.oidc.StsOidcClient
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

private val LOGGER = KotlinLogging.logger {}
private val configuration = Config.config

fun main() {
    runBlocking {
        val config = configuration.inntektApiConfig
        migrate(config)
        DefaultExports.initialize()
        val dataSource = dataSourceFrom(config)
        val authApiKeyVerifier =
            AuthApiKeyVerifier(
                apiKeyVerifier = ApiKeyVerifier(config.application.apiSecret),
                clients = config.application.allowedApiKeys,
            )
        val postgresInntektStore = PostgresInntektStore(dataSource)
        val stsOidcClient =
            StsOidcClient(
                config.application.oicdStsUrl,
                config.application.username,
                config.application.password,
            )
        val pdlPersonOppslag =
            PdlGraphQLRepository(
                client =
                    pdlGraphQLClientFactory(
                        url = config.pdl.url,
                        oidcProvider = { runBlocking { stsOidcClient.oidcToken().access_token } },
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
                stsOidcClient,
            )
        val cachedInntektsGetter = BehandlingsInntektsGetter(inntektskomponentHttpClient, postgresInntektStore)
        // Marks inntekt as used
        val subsumsjonBruktDataConsumer =
            KafkaSubsumsjonBruktDataConsumer(config, postgresInntektStore).apply {
                listen()
            }.also {
                Runtime.getRuntime().addShutdownHook(
                    Thread {
                        it.stop()
                    },
                )
            }

        val kronetilleggUttrekk = KronetilleggUttrekk(dataSource)
        // Provides a HTTP API for getting inntekt
        embeddedServer(Netty, port = config.application.httpPort) {
            inntektApi(
                configuration,
                inntektskomponentHttpClient,
                postgresInntektStore,
                cachedInntektsGetter,
                pdlPersonOppslag,
                authApiKeyVerifier,
                enhetsregisterClient,
                kronetilleggUttrekk,
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

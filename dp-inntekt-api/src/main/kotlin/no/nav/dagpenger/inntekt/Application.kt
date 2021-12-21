package no.nav.dagpenger.inntekt

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.db.PostgresInntektStore
import no.nav.dagpenger.inntekt.db.dataSourceFrom
import no.nav.dagpenger.inntekt.db.migrate
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentHttpClient
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.httpClient
import no.nav.dagpenger.inntekt.oppslag.pdl.PdlGraphQLClientFactory
import no.nav.dagpenger.inntekt.oppslag.pdl.PdlGraphQLRepository
import no.nav.dagpenger.inntekt.rpc.InntektGrpcServer
import no.nav.dagpenger.inntekt.subsumsjonbrukt.KafkaSubsumsjonBruktDataConsumer
import no.nav.dagpenger.inntekt.subsumsjonbrukt.Vaktmester
import no.nav.dagpenger.ktor.auth.ApiKeyVerifier
import no.nav.dagpenger.oidc.StsOidcClient
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

private val LOGGER = KotlinLogging.logger {}
private val config = Configuration()

@KtorExperimentalAPI
fun main() {
    runBlocking {
        migrate(config)
        DefaultExports.initialize()

        val dataSource = dataSourceFrom(config)
        val authApiKeyVerifier = AuthApiKeyVerifier(
            apiKeyVerifier = ApiKeyVerifier(config.application.apiSecret),
            clients = config.application.allowedApiKeys
        )
        val postgresInntektStore = PostgresInntektStore(dataSource)
        val stsOidcClient = StsOidcClient(
            config.application.oicdStsUrl,
            config.application.username,
            config.application.password
        )

        val pdlPersonOppslag = PdlGraphQLRepository(
            client = PdlGraphQLClientFactory(
                url = config.pdl.url,
                oidcProvider = { runBlocking { stsOidcClient.oidcToken().access_token } }
            )
        )
        val enhetsregisterClient = EnhetsregisterClient(
            baseUrl = config.enhetsregisteretUrl.url,
            httpClient = httpClient()
        )

        val inntektskomponentHttpClient = InntektskomponentHttpClient(
            config.application.hentinntektListeUrl,
            stsOidcClient
        )
        val cachedInntektsGetter = BehandlingsInntektsGetter(inntektskomponentHttpClient, postgresInntektStore)

        val jwkProvider = JwkProviderBuilder(URL(config.application.jwksUrl))
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

        // Marks inntekt as used
        val subsumsjonBruktDataConsumer =
            KafkaSubsumsjonBruktDataConsumer(config, postgresInntektStore).apply {
                listen()
            }.also {
                Runtime.getRuntime().addShutdownHook(
                    Thread {
                        it.stop()
                    }
                )
            }

        // Provides a gRPC server for getting inntekt
        InntektGrpcServer(
            port = 50051,
            inntektStore = postgresInntektStore,
            apiKeyVerifier = authApiKeyVerifier
        ).also {
            launch {
                it.start()
                it.blockUntilShutdown()
            }
        }

        // Provides a HTTP API for getting inntekt
        embeddedServer(Netty, port = config.application.httpPort) {
            inntektApi(
                inntektskomponentHttpClient,
                postgresInntektStore,
                cachedInntektsGetter,
                pdlPersonOppslag,
                authApiKeyVerifier,
                jwkProvider,
                enhetsregisterClient,
                listOf(
                    postgresInntektStore as HealthCheck,
                    subsumsjonBruktDataConsumer as HealthCheck
                )
            )
        }.start().also {
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    it.stop(5, 60, TimeUnit.SECONDS)
                }
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
                }
            )
        }
    }
}

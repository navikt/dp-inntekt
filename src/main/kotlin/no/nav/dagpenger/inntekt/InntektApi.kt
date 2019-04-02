package no.nav.dagpenger.inntekt

import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.JsonEncodingException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.hotspot.DefaultExports
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.oidc.StsOidcClient
import no.nav.dagpenger.inntekt.v1.inntekt
import org.slf4j.event.Level
import java.net.URI
import java.util.concurrent.TimeUnit

private val LOGGER = KotlinLogging.logger {}

fun main() {
    val env = Environment()

    val inntektskomponentHttpClient = InntektskomponentHttpClient(
        env.hentinntektListeUrl,
        StsOidcClient(env.oicdStsUrl, env.username, env.password)
    )

    DefaultExports.initialize()
    val application = embeddedServer(Netty, port = env.httpPort) {
        inntektApi(env, inntektskomponentHttpClient)
    }
    application.start(wait = false)
    Runtime.getRuntime().addShutdownHook(Thread {
        application.stop(5, 60, TimeUnit.SECONDS)
    })
}

fun Application.inntektApi(env: Environment, inntektskomponentHttpClient: InntektskomponentClient) {

    install(DefaultHeaders)

    install(StatusPages) {
        exception<Throwable> { cause ->
            LOGGER.error("Request failed!", cause)
            val error = Problem(
                type = URI("urn:dp:error:inntekt"),
                title = "Uhåndtert feil!"
            )
            call.respond(HttpStatusCode.InternalServerError, error)
        }
        exception<InntektskomponentenHttpClientException> { cause ->
            LOGGER.error("Request failed against inntektskomponenet", cause)
            val error = Problem(
                type = URI("urn:dp:error:inntekt"),
                title = "Feilet mot inntektskomponentent!",
                status = cause.status
            )
            call.respond(HttpStatusCode.fromValue(cause.status), error)
        }
        exception<JsonEncodingException> { cause ->
            LOGGER.error("Request was malformed", cause)
            val error = Problem(
                type = URI("urn:dp:error:inntekt:parameter"),
                title = "Klarte ikke å lese inntektsparameterene",
                status = 400
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
    }
    install(CallLogging) {
        level = Level.INFO

        filter { call ->
            !call.request.path().startsWith("/isAlive") &&
                !call.request.path().startsWith("/isReady") &&
                !call.request.path().startsWith("/metrics")
        }
    }
    install(ContentNegotiation) {
        moshi(moshiInstance)
    }

    routing {
        inntekt(inntektskomponentHttpClient)
        naischecks()
    }
}

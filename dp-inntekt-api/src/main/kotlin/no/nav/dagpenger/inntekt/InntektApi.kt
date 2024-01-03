package no.nav.dagpenger.inntekt

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.natpryce.konfig.Configuration
import de.huxhorn.sulky.ulid.ULID
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.http.withCharset
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.db.IllegalInntektIdException
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentenHttpClientException
import no.nav.dagpenger.inntekt.oppslag.PersonNotFoundException
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import no.nav.dagpenger.inntekt.v1.enhetsregisteret
import no.nav.dagpenger.inntekt.v1.inntekt
import no.nav.dagpenger.inntekt.v1.opptjeningsperiodeApi
import no.nav.dagpenger.inntekt.v1.uklassifisertInntekt
import org.slf4j.event.Level
import java.net.URI

private val LOGGER = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall.inntektApi")

internal fun Application.inntektApi(
    config: Configuration = Config.config,
    inntektskomponentHttpClient: InntektskomponentClient,
    inntektStore: InntektStore,
    behandlingsInntektsGetter: BehandlingsInntektsGetter,
    personOppslag: PersonOppslag,
    enhetsregisterClient: EnhetsregisterClient,
    healthChecks: List<HealthCheck>,
    collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry,
) {
    install(DefaultHeaders)
    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry, Clock.SYSTEM)
    }

    install(Authentication) {
        jwt("azure") {
            azureAdJWT(config)
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            LOGGER.error("Request failed!", cause)
            val error =
                Problem(
                    type = URI("urn:dp:error:inntekt"),
                    title = "Uhåndtert feil!",
                )
            call.respond(HttpStatusCode.InternalServerError, error)
        }
        exception<InntektNotFoundException> { call, cause ->
            LOGGER.warn("Request failed!", cause)
            val problem =
                Problem(
                    type = URI("urn:dp:error:inntekt"),
                    title = "Kunne ikke finne inntekt i databasen",
                    status = 404,
                    detail = cause.message,
                )
            call.respond(HttpStatusCode.NotFound, problem)
        }
        exception<IllegalInntektIdException> { call, cause ->
            LOGGER.warn("Request failed!", cause)
            val problem =
                Problem(
                    type = URI("urn:dp:error:inntekt"),
                    title = "InntektsId må være en gyldig ULID",
                    detail = cause.message,
                    status = HttpStatusCode.BadRequest.value,
                )
            call.respond(HttpStatusCode.BadRequest, problem)
        }
        exception<InntektskomponentenHttpClientException> { call, cause ->
            val statusCode =
                if (HttpStatusCode.fromValue(cause.status)
                        .isSuccess()
                ) {
                    HttpStatusCode.InternalServerError
                } else {
                    HttpStatusCode.fromValue(
                        cause.status,
                    )
                }
            sikkerLogg.error(cause) { "Request failed against inntektskomponenten" }
            LOGGER.error("Request failed against inntektskomponenten")
            val error =
                Problem(
                    type = URI("urn:dp:error:inntektskomponenten"),
                    title = "Innhenting av inntekt mot a-inntekt feilet. Prøv igjen senere",
                    status = statusCode.value,
                    detail = cause.detail,
                )
            call.respond(statusCode, error)
        }
        exception<JsonParseException> { call, cause ->
            LOGGER.warn("Request was malformed", cause)
            val error =
                Problem(
                    type = URI("urn:dp:error:inntekt:parameter"),
                    title = "Klarte ikke å lese parameterene",
                    status = 400,
                )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        exception<MismatchedInputException> { call, cause ->
            LOGGER.warn("Request does not match expected json", cause)
            val error =
                Problem(
                    type = URI("urn:dp:error:inntekt:parameter"),
                    title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                    status = 400,
                )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        exception<BadRequestException> { call, cause ->
            LOGGER.warn("Request does not match expected json", cause)
            val error =
                Problem(
                    type = URI("urn:dp:error:inntekt:parameter"),
                    title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                    status = 400,
                )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        exception<IllegalArgumentException> { call, cause ->
            LOGGER.warn("Request does not match expected json", cause)
            val error =
                Problem(
                    type = URI("urn:dp:error:inntekt:parameter"),
                    title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                    status = 400,
                )
            call.respond(HttpStatusCode.BadRequest, error)
        }

        exception<PersonNotFoundException> { call, cause ->
            LOGGER.error("Could not find person", cause)
            sikkerLogg.error("Could not find person ${cause.ident}", cause)
            val error =
                Problem(
                    type = URI("urn:dp:error:inntekt:person"),
                    title = "Kunne ikke finne inntekt for ukjent person",
                    status = 400,
                )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        exception<CookieNotSetException> { call, cause ->
            LOGGER.warn("Unauthorized call", cause)
            val statusCode = HttpStatusCode.Unauthorized
            val error =
                Problem(
                    type = URI("urn:dp:error:inntekt:auth"),
                    title = "Ikke innlogget",
                    detail = "${cause.message}",
                    status = statusCode.value,
                )
            call.respond(statusCode, error)
        }
    }

    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        generate { newRequestId() }
        verify { it.isNotEmpty() }
    }

    install(CallLogging) {
        level = Level.INFO
        disableDefaultColors()
        callIdMdc("callId")

        filter { call ->
            !call.request.path().startsWith("/isAlive") &&
                !call.request.path().startsWith("/isReady") &&
                !call.request.path().startsWith("/metrics")
        }
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json.withCharset(Charsets.UTF_8), JacksonConverter(jacksonObjectMapper))
    }

    routing {
        route("/v1") {
            route("/inntekt") {
                uklassifisertInntekt(inntektskomponentHttpClient, inntektStore, personOppslag)
            }
            opptjeningsperiodeApi(inntektStore)
            enhetsregisteret(enhetsregisterClient)
        }
        route("v2") {
            route("/inntekt") {
                authenticate("azure") {
                    inntekt(behandlingsInntektsGetter, personOppslag)
                }
            }
        }
        naischecks(healthChecks)
    }
}

private val ulid = ULID()

private fun newRequestId(): String = "dp-inntekt-api-${ulid.nextULID()}"

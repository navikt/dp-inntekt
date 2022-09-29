package no.nav.dagpenger.inntekt

import com.auth0.jwk.JwkProvider
import com.natpryce.konfig.Configuration
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import de.huxhorn.sulky.ulid.ULID
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.features.callIdMdc
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.http.isSuccess
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.authorization
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.inntekt.Config.application
import no.nav.dagpenger.inntekt.db.IllegalInntektIdException
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.KronetilleggUttrekk
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentenHttpClientException
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.dagpenger.inntekt.v1.enhetsregisteret
import no.nav.dagpenger.inntekt.v1.inntekt
import no.nav.dagpenger.inntekt.v1.opptjeningsperiodeApi
import no.nav.dagpenger.inntekt.v1.uklassifisertInntekt
import no.nav.dagpenger.inntekt.v1.uttrekk
import no.nav.dagpenger.ktor.auth.ApiKeyCredential
import no.nav.dagpenger.ktor.auth.ApiKeyVerifier
import no.nav.dagpenger.ktor.auth.ApiPrincipal
import no.nav.dagpenger.ktor.auth.apiKeyAuth
import org.slf4j.event.Level
import java.net.URI

private val LOGGER = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal fun Application.inntektApi(
    config: Configuration = Config.config,
    inntektskomponentHttpClient: InntektskomponentClient,
    inntektStore: InntektStore,
    behandlingsInntektsGetter: BehandlingsInntektsGetter,
    personOppslag: PersonOppslag,
    apiAuthApiKeyVerifier: AuthApiKeyVerifier,
    jwkProvider: JwkProvider,
    enhetsregisterClient: EnhetsregisterClient,
    kronetilleggUttrekk: KronetilleggUttrekk,
    healthChecks: List<HealthCheck>,
    collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
) {
    install(DefaultHeaders)

    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry, Clock.SYSTEM)
    }

    install(Authentication) {
        apiKeyAuth("apikey") {
            apiKeyName = "X-API-KEY"
            validate { apikeyCredential: ApiKeyCredential ->
                withLoggingContext("request_type" to "API") {
                    when {
                        apiAuthApiKeyVerifier.verify(apikeyCredential.value) -> ApiPrincipal(apikeyCredential)
                        else -> null
                    }
                }
            }
        }

        jwt(name = "jwt") {
            realm = "dp-inntekt-api"
            verifier(jwkProvider, config.application.jwksIssuer) {
                acceptNotBefore(10)
                acceptIssuedAt(10)
            }
            authHeader { call ->
                val cookie = call.request.cookies["ID_token"]
                    ?: throw CookieNotSetException("Cookie with name ID_token not found")
                HttpAuthHeader.Single("Bearer", cookie)
            }
            skipWhen {
                sikkerLogg.info { "Request har auth header: ${it.request.authorization()}" }
                it.request.authorization() != null
            }
            validate { credentials ->
                return@validate JWTPrincipal(credentials.payload)
            }
        }

        jwt("azure") {
            azureAdJWT(config)
        }
    }

    install(StatusPages) {
        exception<Throwable> { cause ->
            LOGGER.error("Request failed!", cause)
            val error = Problem(
                type = URI("urn:dp:error:inntekt"),
                title = "Uhåndtert feil!"
            )
            call.respond(HttpStatusCode.InternalServerError, error)
        }
        exception<InntektNotFoundException> { cause ->
            LOGGER.warn("Request failed!", cause)
            val problem = Problem(
                type = URI("urn:dp:error:inntekt"),
                title = "Kunne ikke finne inntekt i databasen",
                status = 404,
                detail = cause.message
            )
            call.respond(HttpStatusCode.NotFound, problem)
        }
        exception<IllegalInntektIdException> { cause ->
            LOGGER.warn("Request failed!", cause)
            val problem = Problem(
                type = URI("urn:dp:error:inntekt"),
                title = "InntektsId må være en gyldig ULID",
                detail = cause.message,
                status = HttpStatusCode.BadRequest.value
            )
            call.respond(HttpStatusCode.BadRequest, problem)
        }
        exception<InntektskomponentenHttpClientException> { cause ->
            val statusCode =
                if (HttpStatusCode.fromValue(cause.status)
                    .isSuccess()
                ) HttpStatusCode.InternalServerError else HttpStatusCode.fromValue(
                    cause.status
                )
            sikkerLogg.error(cause) { "Request failed against inntektskomponenten" }
            LOGGER.error("Request failed against inntektskomponenten")
            val error = Problem(
                type = URI("urn:dp:error:inntektskomponenten"),
                title = "Innhenting av inntekt mot a-inntekt feilet. Prøv igjen senere",
                status = statusCode.value,
                detail = cause.detail
            )
            call.respond(statusCode, error)
        }
        exception<JsonEncodingException> { cause ->
            LOGGER.warn("Request was malformed", cause)
            val error = Problem(
                type = URI("urn:dp:error:inntekt:parameter"),
                title = "Klarte ikke å lese parameterene",
                status = 400
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        exception<JsonDataException> { cause ->
            LOGGER.warn("Request does not match expected json", cause)
            val error = Problem(
                type = URI("urn:dp:error:inntekt:parameter"),
                title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                status = 400
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        exception<IllegalArgumentException> { cause ->
            LOGGER.warn("Request does not match expected json", cause)
            val error = Problem(
                type = URI("urn:dp:error:inntekt:parameter"),
                title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                status = 400
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        exception<CookieNotSetException> { cause ->
            LOGGER.warn("Unauthorized call", cause)
            val statusCode = HttpStatusCode.Unauthorized
            val error = Problem(
                type = URI("urn:dp:error:inntekt:auth"),
                title = "Ikke innlogget",
                detail = "${cause.message}",
                status = statusCode.value
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
        moshi(moshiInstance)
    }

    routing {
        route("/v1") {
            route("/inntekt") {
                authenticate("apikey") {
                    inntekt(behandlingsInntektsGetter)
                }
                uklassifisertInntekt(inntektskomponentHttpClient, inntektStore, personOppslag)
            }
            opptjeningsperiodeApi(inntektStore)
            enhetsregisteret(enhetsregisterClient)
        }
        route("v2") {
            route("/inntekt") {
                authenticate("azure") {
                    inntekt(behandlingsInntektsGetter)
                    uttrekk(kronetilleggUttrekk)
                }
            }
        }
        naischecks(healthChecks)
    }
}

data class AuthApiKeyVerifier(private val apiKeyVerifier: ApiKeyVerifier, private val clients: List<String>) {
    fun verify(payload: String): Boolean {
        sikkerLogg.info { "Verifiserer ApiKey" }
        return clients.map { apiKeyVerifier.verify(payload, it) }.firstOrNull { it } ?: false
    }
}

private val ulid = ULID()
private fun newRequestId(): String = "dp-inntekt-api-${ulid.nextULID()}"

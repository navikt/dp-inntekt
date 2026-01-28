package no.nav.dagpenger.inntekt

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import no.nav.dagpenger.inntekt.db.IllegalInntektIdException
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentenHttpClientException
import no.nav.dagpenger.inntekt.oppslag.PersonNotFoundException
import java.net.URI

private val LOGGER = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall.inntektApi")

fun StatusPagesConfig.statusPagesConfig() {
    exception<Throwable> { call, cause ->
        LOGGER.error(cause) { "Request failed!" }
        val error =
            Problem(
                type = URI("urn:dp:error:inntekt"),
                title = "Uhåndtert feil!",
            )
        call.respond(HttpStatusCode.InternalServerError, error)
    }
    exception<InntektNotFoundException> { call, cause ->
        LOGGER.warn(cause) { "Request failed!" }
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
        LOGGER.warn(cause) { "Request failed!" }
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
            if (HttpStatusCode
                    .fromValue(cause.status)
                    .isSuccess()
            ) {
                HttpStatusCode.InternalServerError
            } else {
                HttpStatusCode.fromValue(
                    cause.status,
                )
            }
        sikkerLogg.error(cause) { "Request failed against inntektskomponenten" }
        LOGGER.error { "Request failed against inntektskomponenten" }
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
        LOGGER.warn(cause) { "Request was malformed" }
        val error =
            Problem(
                type = URI("urn:dp:error:inntekt:parameter"),
                title = "Klarte ikke å lese parameterene",
                status = 400,
            )
        call.respond(HttpStatusCode.BadRequest, error)
    }
    exception<MismatchedInputException> { call, cause ->
        LOGGER.warn(cause) { "Request does not match expected json" }
        val error =
            Problem(
                type = URI("urn:dp:error:inntekt:parameter"),
                title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                status = 400,
            )
        call.respond(HttpStatusCode.BadRequest, error)
    }
    exception<BadRequestException> { call, cause ->
        LOGGER.warn(cause) { "Request does not match expected json" }
        val error =
            Problem(
                type = URI("urn:dp:error:inntekt:parameter"),
                title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                status = 400,
            )
        call.respond(HttpStatusCode.BadRequest, error)
    }
    exception<IllegalArgumentException> { call, cause ->
        LOGGER.warn(cause) { "Request does not match expected json" }
        val error =
            Problem(
                type = URI("urn:dp:error:inntekt:parameter"),
                title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                status = 400,
            )
        call.respond(HttpStatusCode.BadRequest, error)
    }

    exception<PersonNotFoundException> { call, cause ->
        LOGGER.error(cause) { "Could not find person" }
        sikkerLogg.error(cause) { "Could not find person ${cause.ident}" }
        val error =
            Problem(
                type = URI("urn:dp:error:inntekt:person"),
                title = "Kunne ikke finne inntekt for ukjent person",
                status = 400,
            )
        call.respond(HttpStatusCode.BadRequest, error)
    }
    exception<CookieNotSetException> { call, cause ->
        LOGGER.warn(cause) { "Unauthorized call" }
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

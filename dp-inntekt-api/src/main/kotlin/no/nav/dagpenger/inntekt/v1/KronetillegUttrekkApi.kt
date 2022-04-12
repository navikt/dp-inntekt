package no.nav.dagpenger.inntekt.v1

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.KronetilleggUttrekk

private val logger = KotlinLogging.logger {}

internal fun Route.uttrekk(kronetilleggUttrekk: KronetilleggUttrekk) {

    route("{inntektID}") {
        get("harDagpenger") {
            logger.info { "Requesten har disse dataene: ${call.request.local}"}
            val inntektId = InntektId(this.call.parameters["inntektID"] ?: throw IllegalArgumentException("inntektID mangler i parameter"))
            val uttrekk = kronetilleggUttrekk.utrekkFra(inntektId)
            call.respond(uttrekk)
        }
    }
}

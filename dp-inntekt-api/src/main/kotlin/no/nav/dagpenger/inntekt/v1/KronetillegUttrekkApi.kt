package no.nav.dagpenger.inntekt.v1

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.KronetilleggUttrekk

internal fun Route.uttrekk(kronetilleggUttrekk: KronetilleggUttrekk) {
    route("{inntektID}") {
        get("harDagpenger") {
            val inntektId =
                InntektId(
                    this.call.parameters["inntektID"] ?: throw IllegalArgumentException("inntektID mangler i parameter"),
                )
            val uttrekk = kronetilleggUttrekk.utrekkFra(inntektId)
            call.respond(uttrekk)
        }
    }
}

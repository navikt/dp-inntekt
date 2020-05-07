package no.nav.dagpenger.inntekt.v1

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import java.time.LocalDate
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import no.nav.dagpenger.inntekt.v1.models.InntjeningsperiodeParametre
import no.nav.dagpenger.inntekt.v1.models.InntjeningsperiodeResultat

fun Route.opptjeningsperiodeApi(inntektStore: InntektStore) {

    route("is-samme-inntjeningsperiode") {
        post {
            val parametere = call.receive<InntjeningsperiodeParametre>()

            val gammelBeregningsdato = inntektStore.getBeregningsdato(InntektId(parametere.inntektsId))

            val resultat = Opptjeningsperiode(gammelBeregningsdato).sammeOpptjeningsPeriode(
                Opptjeningsperiode(
                    LocalDate.parse(parametere.beregningsdato)
                )
            )

            val response = InntjeningsperiodeResultat(resultat, parametere)

            call.respond(HttpStatusCode.OK, response)
        }
    }
}

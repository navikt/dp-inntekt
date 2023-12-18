package no.nav.dagpenger.inntekt.v1

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import no.nav.dagpenger.inntekt.v1.models.InntjeningsperiodeParametre
import no.nav.dagpenger.inntekt.v1.models.InntjeningsperiodeResultat
import java.time.LocalDate

fun Route.opptjeningsperiodeApi(inntektStore: InntektStore) {
    route("samme-inntjeningsperiode") {
        post {
            withContext(Dispatchers.IO) {
                val parametere = call.receive<InntjeningsperiodeParametre>()

                val gammelBeregningsdato = inntektStore.getBeregningsdato(InntektId(parametere.inntektsId))

                val resultat =
                    Opptjeningsperiode(gammelBeregningsdato).sammeOpptjeningsPeriode(
                        Opptjeningsperiode(
                            LocalDate.parse(parametere.beregningsdato),
                        ),
                    )
                val response = InntjeningsperiodeResultat(sammeInntjeningsPeriode = resultat)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}

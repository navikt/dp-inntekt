package no.nav.dagpenger.inntekt.v1

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.StoreInntektCommand
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.klassifisering.Inntekt
import no.nav.dagpenger.inntekt.klassifisering.klassifiserInntekter
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode

fun Route.klassifisertInntekt(inntektskomponentClient: InntektskomponentClient, inntektStore: InntektStore) {
    authenticate {
        route("") {
            post {
                val request = call.receive<InntektRequest>()

                val opptjeningsperiode = Opptjeningsperiode(request.beregningsDato)

                val parameters = Inntektparametre(
                    aktørId = request.aktørId,
                    vedtakId = request.vedtakId.toString(),
                    beregningsdato = request.beregningsDato
                )

                val storedInntekt = inntektStore.getInntektId(parameters)?.let { inntektStore.getInntekt(it) }
                    ?: inntektStore.storeInntekt(StoreInntektCommand(inntektparametre = parameters,
                        inntekt = inntektskomponentClient.getInntekt(toInntektskomponentRequest(request, opptjeningsperiode))))

                val klassifisertInntekt = storedInntekt.let {
                    Inntekt(
                        it.inntektId.id,
                        klassifiserInntekter(it.inntekt),
                        it.manueltRedigert,
                        opptjeningsperiode.sisteAvsluttendeKalenderMåned
                    )
                }

                call.respond(HttpStatusCode.OK, klassifisertInntekt)
            }
        }

        route("/{inntektId}") {
            post {
                val request = call.receive<InntektByIdRequest>()
                val inntektId = call.parameters["inntektId"].runCatching {
                    InntektId(call.parameters["inntektId"]!!)
                }.getOrThrow()
                val opptjeningsperiode = Opptjeningsperiode(request.beregningsDato)
                val aktor = Aktoer(
                    aktoerType = AktoerType.AKTOER_ID,
                    identifikator = request.aktørId
                )

                val storedInntekt = inntektStore.getInntekt(inntektId)

                if (storedInntekt.inntekt.ident != aktor) {
                    throw InntektNotAuthorizedException("Aktøren har ikke tilgang til denne inntekten.")
                }

                val klassifisertInntekt = storedInntekt.let {
                    Inntekt(
                        inntektsId = it.inntektId.id,
                        inntektsListe = klassifiserInntekter(it.inntekt),
                        manueltRedigert = it.manueltRedigert,
                        sisteAvsluttendeKalenderMåned = opptjeningsperiode.sisteAvsluttendeKalenderMåned
                    )
                }

                call.respond(HttpStatusCode.OK, klassifisertInntekt)
            }
        }
    }
}

class InntektNotAuthorizedException(override val message: String) : RuntimeException(message)

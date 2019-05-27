package no.nav.dagpenger.inntekt.v1

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentRequest
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.klassifisering.Inntekt
import no.nav.dagpenger.inntekt.klassifisering.klassifiserInntekter
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import java.time.LocalDate

fun Route.inntekt(inntektskomponentClient: InntektskomponentClient, inntektStore: InntektStore) {
    authenticate {
        route("inntekt") {
            post {
                val request = call.receive<InntektRequest>()

                val opptjeningsperiode: Opptjeningsperiode = Opptjeningsperiode(request.beregningsDato)

                val storedInntekt = inntektStore.getInntektId(request)?.let { inntektStore.getInntekt(it) }
                    ?: inntektStore.insertInntekt(
                        request,
                        inntektskomponentClient.getInntekt(toInntektskomponentRequest(request, opptjeningsperiode))
                    )

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
    }

    route("inntekt/uklassifisert/{aktørId}/{vedtakId}/{beregningsDato}") {
        get {

            val request = try {
                InntektRequest(
                    aktørId = call.parameters["aktørId"]!!,
                    vedtakId = call.parameters["vedtakId"]!!.toLong(),
                    beregningsDato = LocalDate.parse(call.parameters["beregningsDato"]!!)
                )
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to parse parameters", e)
            }

            val storedInntekt = inntektStore.getInntektId(request)?.let { inntektStore.getInntekt(it) }
                ?: throw InntektNotFoundException("Inntekt with for $request not found.")
            call.respond(HttpStatusCode.OK, storedInntekt)
        }
    }

    route("inntekt/uklassifisert/uncached/{aktørId}/{vedtakId}/{beregningsDato}") {
        get {

            val request = try {
                InntektRequest(
                    aktørId = call.parameters["aktørId"]!!,
                    vedtakId = call.parameters["vedtakId"]!!.toLong(),
                    beregningsDato = LocalDate.parse(call.parameters["beregningsDato"]!!)
                )
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to parse parameters", e)
            }

            val opptjeningsperiode = Opptjeningsperiode(request.beregningsDato)
            val uncachedInntekt =
                inntektskomponentClient.getInntekt(toInntektskomponentRequest(request, opptjeningsperiode))

            call.respond(HttpStatusCode.OK, uncachedInntekt)
        }
    }

    route("inntekt/uklassifisert") {
        post {
            val request = call.receive<InntektRequest>()
            val storedInntekt = inntektStore.getInntektId(request)?.let { inntektStore.getInntekt(it) }
                ?: throw InntektNotFoundException("Inntekt with for $request not found.")
            call.respond(HttpStatusCode.OK, storedInntekt)
        }
    }

    route("inntekt/uklassifisert/update") {
        post {
            val request = call.receive<StoredInntekt>()
            val storedInntekt = inntektStore.redigerInntekt(request)
            call.respond(HttpStatusCode.OK, storedInntekt)
        }
    }
}

data class InntektRequest(
    val aktørId: String,
    val vedtakId: Long,
    val beregningsDato: LocalDate
)

val toInntektskomponentRequest: (InntektRequest, Opptjeningsperiode) -> InntektkomponentRequest =
    { inntektRequest: InntektRequest, opptjeningsperiode: Opptjeningsperiode ->
        val sisteAvsluttendeKalendermåned = opptjeningsperiode.sisteAvsluttendeKalenderMåned
        val førsteMåned = opptjeningsperiode.førsteMåned
        InntektkomponentRequest(inntektRequest.aktørId, førsteMåned, sisteAvsluttendeKalendermåned)
}
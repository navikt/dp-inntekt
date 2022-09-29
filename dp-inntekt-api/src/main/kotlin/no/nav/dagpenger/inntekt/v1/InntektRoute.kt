package no.nav.dagpenger.inntekt.v1

import io.ktor.application.call
import io.ktor.features.callId
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import no.nav.dagpenger.inntekt.BehandlingsInntektsGetter
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.RegelKontekst
import java.time.LocalDate

fun Route.inntekt(behandlingsInntektsGetter: BehandlingsInntektsGetter) {
    route("spesifisert") {
        post {
            withContext(IO) {
                val request = call.receive<InntektRequestMedFnr>()

                val spesifisertInntekt =
                    behandlingsInntektsGetter.getSpesifisertInntekt(
                        Inntektparametre(
                            aktørId = request.aktørId,
                            regelkontekst = request.regelkontekst,
                            beregningsdato = request.beregningsDato,
                            fødselnummer = request.fødselsnummer
                        ),
                        call.callId
                    )

                call.respond(HttpStatusCode.OK, spesifisertInntekt)
            }
        }
    }
    route("klassifisert") {
        post {
            withContext(IO) {
                val request = call.receive<InntektRequestMedFnr>()
                val klassifisertInntekt =
                    behandlingsInntektsGetter.getKlassifisertInntekt(
                        Inntektparametre(
                            aktørId = request.aktørId,
                            regelkontekst = request.regelkontekst,
                            beregningsdato = request.beregningsDato,
                            fødselnummer = request.fødselsnummer
                        ),
                        call.callId
                    )
                call.respond(HttpStatusCode.OK, klassifisertInntekt)
            }
        }
    }
}

data class InntektRequestMedFnr(
    val aktørId: String,
    val regelkontekst: RegelKontekst,
    val fødselsnummer: String? = null,
    val beregningsDato: LocalDate
)

package no.nav.dagpenger.inntekt.v1

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
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

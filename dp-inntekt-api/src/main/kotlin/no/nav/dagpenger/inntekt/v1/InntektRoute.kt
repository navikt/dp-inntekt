package no.nav.dagpenger.inntekt.v1

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import no.nav.dagpenger.inntekt.BehandlingsInntektsGetter
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import java.time.LocalDate

fun Route.inntekt(
    behandlingsInntektsGetter: BehandlingsInntektsGetter,
    personOppslag: PersonOppslag,
) {
    route("klassifisert") {
        post {
            withContext(IO) {
                val request = call.receive<InntektRequestMedFnr>()
                val person = personOppslag.hentPerson(request.ident)
                val klassifisertInntekt =
                    behandlingsInntektsGetter.getKlassifisertInntekt(
                        Inntektparametre(
                            aktørId = person.aktørId,
                            regelkontekst = request.regelkontekst,
                            beregningsdato = request.beregningsDato,
                            fødselsnummer = person.fødselsnummer,
                        ),
                        call.callId,
                    )
                call.respond(HttpStatusCode.OK, klassifisertInntekt)
            }
        }
        get("{inntektId}") {
            withContext(IO) {
                val inntektId = call.parameters["inntektId"]?.let { InntektId(it) } ?: throw MissingRequestParameterException("inntektId")
                val inntekt = behandlingsInntektsGetter.getKlassifisertInntekt(inntektId = inntektId)
                call.respond(HttpStatusCode.OK, inntekt)
            }
        }
    }
}

data class InntektRequestMedFnr(
    val aktørId: String?,
    val regelkontekst: RegelKontekst,
    val fødselsnummer: String? = null,
    val beregningsDato: LocalDate,
) {
    init {
        require(aktørId != null || fødselsnummer !== null) {
            "Enten aktørId eller fødselsnummer må være satt"
        }
    }

    val ident: String
        get() {
            return (aktørId ?: fødselsnummer)!!
        }
}

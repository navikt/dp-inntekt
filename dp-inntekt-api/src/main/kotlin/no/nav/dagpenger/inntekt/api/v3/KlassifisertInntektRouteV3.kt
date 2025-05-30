package no.nav.dagpenger.inntekt.api.v3

import io.ktor.http.HttpStatusCode
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
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import java.time.LocalDate
import java.time.YearMonth

fun Route.inntektV3(
    behandlingsInntektsGetter: BehandlingsInntektsGetter,
    personOppslag: PersonOppslag,
) {
    route("/klassifisert") {
        post {
            withContext(IO) {
                val inntektRequestDto = call.receive<InntektRequestDto>()
                val person = personOppslag.hentPerson(inntektRequestDto.personIdentifikator)
                val inntektparametre =
                    Inntektparametre(
                        aktørId = person.aktørId,
                        fødselsnummer = person.fødselsnummer,
                        regelkontekst = inntektRequestDto.regelkontekst,
                        beregningsdato = inntektRequestDto.beregningsDato,
                    ).apply {
                        opptjeningsperiode.førsteMåned = inntektRequestDto.periodeFraOgMed
                        opptjeningsperiode.sisteAvsluttendeKalenderMåned = inntektRequestDto.periodeTilOgMed
                    }
                val klassifisertInntekt =
                    behandlingsInntektsGetter.getKlassifisertInntekt(
                        inntektparametre,
                        call.callId,
                    )
                call.respond(HttpStatusCode.OK, klassifisertInntekt)
            }
        }
    }
}

data class InntektRequestDto(
    val personIdentifikator: String,
    val regelkontekst: RegelKontekst,
    val beregningsDato: LocalDate,
    val periodeFraOgMed: YearMonth,
    val periodeTilOgMed: YearMonth,
)

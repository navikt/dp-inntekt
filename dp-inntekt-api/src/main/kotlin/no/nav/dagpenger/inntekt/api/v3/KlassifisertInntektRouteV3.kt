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
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.ManueltRedigert
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import java.time.LocalDate
import java.time.YearMonth

fun Route.inntektV3(
    behandlingsInntektsGetter: BehandlingsInntektsGetter,
    personOppslag: PersonOppslag,
    inntektStore: InntektStore,
) {
    route("/klassifisert") {
        post {
            withContext(IO) {
                val klassifisertInntektRequestDto = call.receive<KlassifisertInntektRequestDto>()
                val person = personOppslag.hentPerson(klassifisertInntektRequestDto.personIdentifikator)
                val inntektparametre =
                    Inntektparametre(
                        aktørId = person.aktørId,
                        fødselsnummer = person.fødselsnummer,
                        regelkontekst = klassifisertInntektRequestDto.regelkontekst,
                        beregningsdato = klassifisertInntektRequestDto.beregningsDato,
                    ).apply {
                        opptjeningsperiode.førsteMåned = klassifisertInntektRequestDto.periodeFraOgMed
                        opptjeningsperiode.sisteAvsluttendeKalenderMåned = klassifisertInntektRequestDto.periodeTilOgMed
                    }
                val klassifisertInntekt =
                    behandlingsInntektsGetter.getKlassifisertInntekt(
                        inntektparametre,
                        call.callId,
                    )
                val manueltRedigert = inntektStore.getManueltRedigert(InntektId(klassifisertInntekt.inntektsId))

                call.respond(
                    HttpStatusCode.OK,
                    mapToKlassifisertInntektResponseDto(klassifisertInntekt, manueltRedigert),
                )
            }
        }
    }
}

data class KlassifisertInntektRequestDto(
    val personIdentifikator: String,
    val regelkontekst: RegelKontekst,
    val beregningsDato: LocalDate,
    val periodeFraOgMed: YearMonth,
    val periodeTilOgMed: YearMonth,
)

data class KlassifisertInntektResponseDto(
    val inntektsId: String,
    val inntektsListe: List<KlassifisertInntektMåned>,
    val manueltRedigert: Boolean? = false,
    val begrunnelseManueltRedigert: String? = null,
    val sisteAvsluttendeKalenderMåned: YearMonth,
)

private fun mapToKlassifisertInntektResponseDto(
    inntekt: Inntekt,
    manueltRedigert: ManueltRedigert?,
): KlassifisertInntektResponseDto =
    KlassifisertInntektResponseDto(
        inntektsId = inntekt.inntektsId,
        inntektsListe = inntekt.inntektsListe,
        manueltRedigert = inntekt.manueltRedigert,
        begrunnelseManueltRedigert = manueltRedigert?.begrunnelse,
        sisteAvsluttendeKalenderMåned = inntekt.sisteAvsluttendeKalenderMåned,
    )

package no.nav.dagpenger.inntekt.api.v1.models

import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType.NATURLIG_IDENT
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Avvik
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.mapping.Inntektsmottaker
import no.nav.dagpenger.inntekt.mapping.Virksomhet
import java.time.LocalDateTime.now
import java.time.YearMonth

data class InntekterDto(
    val virksomheter: List<Virksomhet>,
    val mottaker: Inntektsmottaker,
    val periode: PeriodeDto,
)

data class PeriodeDto(
    val fraOgMed: YearMonth,
    val tilOgMed: YearMonth,
)

fun InntekterDto.mapToStoredInntekt(inntektId: String): StoredInntekt =
    StoredInntekt(
        inntektId = InntektId(id = inntektId),
        inntekt = mapToInntektkomponentResponse(this),
        manueltRedigert = true,
        timestamp = now(),
    )

private fun mapToInntektkomponentResponse(inntekterDto: InntekterDto): InntektkomponentResponse {
    val inntektPerÅrOgMåned = mutableMapOf<YearMonth, Pair<MutableList<Inntekt>?, MutableList<Avvik>?>>()
    inntekterDto.virksomheter.forEach { virksomhet ->
        virksomhet.inntekter?.map { inntektMaaned ->
            val inntekter = inntektPerÅrOgMåned[inntektMaaned.aarMaaned]?.first ?: mutableListOf()
            inntekter.add(
                Inntekt(
                    inntektMaaned.belop,
                    inntektMaaned.fordel,
                    inntektMaaned.beskrivelse,
                    inntektMaaned.inntektskilde,
                    inntektMaaned.inntektsstatus,
                    inntektMaaned.inntektsperiodetype ?: "Maaned",
                    inntektMaaned.leveringstidspunkt,
                    inntektMaaned.opptjeningsland,
                    inntektMaaned.opptjeningsperiode,
                    inntektMaaned.skattemessigBosattLand,
                    inntektMaaned.utbetaltIMaaned,
                    inntektMaaned.opplysningspliktig,
                    inntektMaaned.inntektsinnsender,
                    inntektMaaned.virksomhet,
                    inntektMaaned.inntektsmottaker,
                    inntektMaaned.inngaarIGrunnlagForTrekk,
                    inntektMaaned.utloeserArbeidsgiveravgift,
                    inntektMaaned.informasjonsstatus,
                    inntektMaaned.inntektType,
                    inntektMaaned.tilleggsinformasjon,
                ),
            )
            inntektPerÅrOgMåned.put(
                inntektMaaned.aarMaaned,
                Pair(inntekter, inntektPerÅrOgMåned[inntektMaaned.aarMaaned]?.second),
            )
        }
        virksomhet.avvikListe.map {
            val avvik = inntektPerÅrOgMåned[it.avvikPeriode]?.second ?: mutableListOf()
            avvik.add(it)
            inntektPerÅrOgMåned.put(
                it.avvikPeriode,
                Pair(inntektPerÅrOgMåned[it.avvikPeriode]?.first, avvik),
            )
        }
    }

    return InntektkomponentResponse(
        inntektPerÅrOgMåned.map { (yearMonth, inntekter) ->
            ArbeidsInntektMaaned(
                aarMaaned = yearMonth,
                arbeidsInntektInformasjon = ArbeidsInntektInformasjon(inntekter.first),
                avvikListe = inntekter.second,
            )
        },
        Aktoer(NATURLIG_IDENT, inntekterDto.mottaker.pnr ?: throw IllegalArgumentException("Fødselsenummer mangler")),
    )
}

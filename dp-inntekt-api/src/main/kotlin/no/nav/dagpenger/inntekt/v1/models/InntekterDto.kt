package no.nav.dagpenger.inntekt.v1.models

import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.mapping.Inntektsmottaker
import no.nav.dagpenger.inntekt.mapping.Virksomhet
import java.time.LocalDateTime.now

data class InntekterDto(
    val virksomheter: List<Virksomhet>,
    val mottaker: Inntektsmottaker,
)

fun InntekterDto.mapToStoredInntekt(inntektId: String): StoredInntekt =
    StoredInntekt(
        inntektId = InntektId(id = inntektId),
        inntekt = mapToInntektkomponentResponse(this),
        manueltRedigert = true,
        timestamp = now(),
    )

private fun mapToInntektkomponentResponse(inntekterDto: InntekterDto): InntektkomponentResponse {
    val arbeidsInntektMaaneder: MutableList<ArbeidsInntektMaaned> = mutableListOf()

    /*
    Dette må mappes om fra gruppering på virksomhet, til gruppering på aarMaaned (dvs hvert aarMaaned inneholder
    inntekt for alle virksomhetene som har utbetalt inntekt for gitt aarMaaned). Strukturen som kommer fra frontend er
    gruppert på virksomhet, og alle utbetalte inntekter for alle aarMaaned ligger inne i hver virksomhet.

    Vi mistenker også at Avvik i datastrukturen som sendes til frontend (og som frontend da sender tilbake til backend),
    ikke inneholder all informasjon og/eller ligger på feil sted i strukturen.

    Løsningsforslag:

    Lag en Map<YearMonth, List<Inntekt>>(), og map InntektMaaned-objekter til Inntekt-objekter, og legg i riktig
    "bucket" i mappen -> opprett et ArbeidsInntektMaaned-objekt per key i map (key == maanedAar,
    liste i value == arbeidsInntektInformasjon.

    Hvis vi blir skikkelig tøffe: https://kotlinlang.org/docs/collection-grouping.html

    I tillegg må vi se på mapping av avvik når vi har fått det på riktig plass i InntektDto.
     */

    inntekterDto.virksomheter.forEach { virksomhet ->
        arbeidsInntektMaaneder.addAll(
            virksomhet.inntekter?.map {
                ArbeidsInntektMaaned(
                    it.aarMaaned,
                    virksomhet.avvikListe,
                    ArbeidsInntektInformasjon(
                        virksomhet.inntekter.map { inntektMaaned ->
                            Inntekt(
                                inntektMaaned.belop,
                                inntektMaaned.inntektskilde,
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
                            )
                        },
                    ),
                )
            } ?: listOf(),
        )
    }

    return InntektkomponentResponse(
        arbeidsInntektMaaneder,
        mapToAktoerNaturligIdent(inntekterDto.mottaker.pnr),
    )
}

private fun mapToAktoerNaturligIdent(fnr: String?): Aktoer =
    Aktoer(AktoerType.NATURLIG_IDENT, fnr ?: throw IllegalArgumentException("Fødselsenummer mangler"))

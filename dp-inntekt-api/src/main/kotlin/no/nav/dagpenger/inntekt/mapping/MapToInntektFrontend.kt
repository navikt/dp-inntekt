package no.nav.dagpenger.inntekt.mapping

import no.nav.dagpenger.inntekt.api.v1.models.InntekterDto
import no.nav.dagpenger.inntekt.api.v1.models.PeriodeDto
import no.nav.dagpenger.inntekt.db.StoredInntektMedMetadata
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Avvik
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Periode
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import java.math.BigDecimal
import java.time.YearMonth

fun InntektkomponentResponse.mapToFrontend(
    person: Inntektsmottaker,
    organisasjoner: List<Organisasjon>,
    storedInntektMedMetadata: StoredInntektMedMetadata,
): InntekterDto {
    val inntekt = arbeidsInntektMaaned
    val virksomheter: MutableList<Virksomhet> = mutableListOf()

    inntekt?.forEach { arbeidsInntektMaaned ->
        val inntektsInformasjon = arbeidsInntektMaaned.arbeidsInntektInformasjon
        inntektsInformasjon?.inntektListe?.forEach { inntekt ->
            val virksomhet = inntekt.virksomhet
            val virksomhetNavn = organisasjoner.find { it.organisasjonsnummer == virksomhet?.identifikator }?.navn ?: ""
            val inntekter = mutableListOf<InntektMaaned>()
            inntekter.add(
                InntektMaaned(
                    belop = inntekt.beloep,
                    inntektskilde = inntekt.inntektskilde,
                    aarMaaned = arbeidsInntektMaaned.aarMaaned,
                    fordel = inntekt.fordel,
                    beskrivelse = inntekt.beskrivelse,
                    inntektsstatus = inntekt.inntektsstatus,
                    inntektsperiodetype = inntekt.inntektsperiodetype,
                    utbetaltIMaaned = inntekt.utbetaltIMaaned,
                    inntektType = inntekt.inntektType,
                    leveringstidspunkt = inntekt.leveringstidspunkt,
                    opptjeningsland = inntekt.opptjeningsland,
                    opptjeningsperiode = inntekt.opptjeningsperiode,
                    skattemessigBosattLand = inntekt.skattemessigBosattLand,
                    inntektsinnsender = inntekt.inntektsinnsender,
                    virksomhet = inntekt.virksomhet,
                    inntektsmottaker = inntekt.inntektsmottaker,
                    inngaarIGrunnlagForTrekk = inntekt.inngaarIGrunnlagForTrekk,
                    utloeserArbeidsgiveravgift = inntekt.utloeserArbeidsgiveravgift,
                    informasjonsstatus = inntekt.informasjonsstatus,
                    tilleggsinformasjon = inntekt.tilleggsinformasjon,
                ),
            )

            val eksisterendeVirksomhet =
                virksomheter.find { it.virksomhetsnummer == virksomhet?.identifikator }
            if (eksisterendeVirksomhet != null) {
                eksisterendeVirksomhet.inntekter?.addAll(inntekter)
                eksisterendeVirksomhet.periode =
                    InntektPeriode(
                        fraOgMed = eksisterendeVirksomhet.inntekter!!.minOf { it.aarMaaned },
                        tilOgMed = eksisterendeVirksomhet.inntekter.maxOf { it.aarMaaned },
                    )
                eksisterendeVirksomhet.totalBelop = eksisterendeVirksomhet.inntekter.sumOf { it.belop }
            } else {
                virksomheter.add(
                    Virksomhet(
                        virksomhetsnummer = virksomhet?.identifikator ?: "",
                        virksomhetsnavn = virksomhetNavn,
                        periode =
                            InntektPeriode(
                                fraOgMed = arbeidsInntektMaaned.aarMaaned,
                                tilOgMed = arbeidsInntektMaaned.aarMaaned,
                            ),
                        inntekter = inntekter,
                        avvikListe = mutableListOf(),
                    ),
                )
            }
        }

        arbeidsInntektMaaned.avvikListe?.forEach { avvik ->
            val virksomhet = virksomheter.find { it.virksomhetsnummer == avvik.virksomhet?.identifikator }
            if (virksomhet != null) {
                virksomhet.avvikListe.add(avvik)
            } else {
                virksomheter.add(
                    Virksomhet(
                        virksomhetsnummer = avvik.virksomhet?.identifikator ?: "",
                        virksomhetsnavn = "",
                        periode = null,
                        inntekter = null,
                        avvikListe = mutableListOf(avvik),
                    ),
                )
            }
        }
    }

    return InntekterDto(
        virksomheter = virksomheter,
        mottaker = person,
        periode = getPeriode(storedInntektMedMetadata),
        begrunnelse = storedInntektMedMetadata.begrunnelse,
    )
}

private fun getPeriode(storedInntektMedMetadata: StoredInntektMedMetadata): PeriodeDto {
    val opptjeningsperiode = Opptjeningsperiode(beregningsdato = storedInntektMedMetadata.beregningsdato)
    return PeriodeDto(
        fraOgMed = storedInntektMedMetadata.storedInntektPeriode?.fraOgMed ?: opptjeningsperiode.førsteMåned,
        tilOgMed = storedInntektMedMetadata.storedInntektPeriode?.tilOgMed ?: opptjeningsperiode.sisteAvsluttendeKalenderMåned,
    )
}

data class Virksomhet(
    val virksomhetsnummer: String,
    val virksomhetsnavn: String,
    var periode: InntektPeriode?,
    val inntekter: MutableList<InntektMaaned>?,
    var totalBelop: BigDecimal? = inntekter?.sumOf { it.belop } ?: BigDecimal.ZERO,
    val avvikListe: MutableList<Avvik>,
)

data class InntektPeriode(
    val fraOgMed: YearMonth,
    val tilOgMed: YearMonth,
)

data class InntektMaaned(
    val belop: BigDecimal,
    val fordel: String,
    val beskrivelse: InntektBeskrivelse,
    val inntektskilde: String,
    val inntektsstatus: String,
    val inntektsperiodetype: String?,
    val leveringstidspunkt: YearMonth? = null,
    val opptjeningsland: String? = null,
    val opptjeningsperiode: Periode? = null,
    val skattemessigBosattLand: String? = null,
    val utbetaltIMaaned: YearMonth,
    val opplysningspliktig: Aktoer? = null,
    val inntektsinnsender: Aktoer? = null,
    val virksomhet: Aktoer? = null,
    val inntektsmottaker: Aktoer? = null,
    val inngaarIGrunnlagForTrekk: Boolean? = null,
    val utloeserArbeidsgiveravgift: Boolean? = null,
    val informasjonsstatus: String? = null,
    val inntektType: InntektType,
    val tilleggsinformasjon: TilleggInformasjon? = null,
    val aarMaaned: YearMonth,
)

data class Organisasjon(
    val organisasjonsnummer: String,
    val navn: String,
)

package no.nav.dagpenger.inntekt.mapping

import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Avvik
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import java.math.BigDecimal
import java.time.YearMonth

fun InntektkomponentResponse.mapToFrontend(
    person: Inntektsmottaker,
    organisasjonsInfoListe: MutableList<OrganisasjonNavnOgIdMapping>,
): InntekterResponse {
    val inntekt = arbeidsInntektMaaned
    val virksomhetListe: MutableList<Virksomhet> = mutableListOf()

    inntekt?.forEach { arbeidsInntektMaaned ->
        val inntektsInformasjon = arbeidsInntektMaaned.arbeidsInntektInformasjon
        inntektsInformasjon?.inntektListe?.forEach { inntekt ->
            val virksomhet = inntekt.virksomhet
            val virksomhetNavn =
                organisasjonsInfoListe.find { it.organisasjonsnummer == virksomhet?.identifikator }?.organisasjonNavn
                    ?: ""
            val inntekter = mutableListOf<InntektMaaned>()
            inntekter.add(
                InntektMaaned(
                    belop = inntekt.beloep,
                    inntektskilde = inntekt.inntektskilde,
                    redigert = false,
                    begrunnelse = inntekt.beskrivelse.name,
                    aarMaaned = arbeidsInntektMaaned.aarMaaned,
                    fordel = inntekt.fordel,
                    beskrivelse = inntekt.beskrivelse,
                    inntektsstatus = inntekt.inntektsstatus,
                    utbetaltIMaaned = inntekt.utbetaltIMaaned,
                    inntektType = inntekt.inntektType,
                    leveringstidspunkt = inntekt.leveringstidspunkt,
                    opptjeningsland = inntekt.opptjeningsland,
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
                virksomhetListe.find { it.virksomhetsnummer == virksomhet?.identifikator }
            if (eksisterendeVirksomhet != null) {
                eksisterendeVirksomhet.inntekter?.addAll(inntekter)
                eksisterendeVirksomhet.periode =
                    InntektPeriode(
                        fra = eksisterendeVirksomhet.inntekter!!.minOf { it.aarMaaned },
                        til = eksisterendeVirksomhet.inntekter.maxOf { it.aarMaaned },
                    )
                eksisterendeVirksomhet.totalBeløp = eksisterendeVirksomhet.inntekter.sumOf { it.belop }
            } else {
                virksomhetListe.add(
                    Virksomhet(
                        virksomhetsnummer = virksomhet?.identifikator ?: "",
                        virksomhetsnavn = virksomhetNavn,
                        periode =
                            InntektPeriode(
                                fra = arbeidsInntektMaaned.aarMaaned,
                                til = arbeidsInntektMaaned.aarMaaned,
                            ),
                        inntekter = inntekter,
                        avvikListe =
                            arbeidsInntektMaaned.avvikListe?.filter { it.virksomhet?.identifikator == virksomhet?.identifikator }
                                ?: emptyList(),
                    ),
                )
            }
        }
    }

    return InntekterResponse(
        virksomhetsinntekt = virksomhetListe,
        mottaker = person,
    )
}

data class InntekterResponse(
    val virksomhetsinntekt: List<Virksomhet>,
    val mottaker: Inntektsmottaker,
)

data class Virksomhet(
    val virksomhetsnummer: String,
    val virksomhetsnavn: String,
    var periode: InntektPeriode?,
    val inntekter: MutableList<InntektMaaned>?,
    var totalBeløp: BigDecimal? = inntekter?.sumOf { it.belop } ?: BigDecimal.ZERO,
    val avvikListe: List<Avvik>,
)

data class InntektPeriode(
    val fra: YearMonth,
    val til: YearMonth,
)

data class InntektMaaned(
    val belop: BigDecimal,
    val fordel: String,
    val beskrivelse: InntektBeskrivelse,
    val inntektskilde: String,
    val inntektsstatus: String,
    val leveringstidspunkt: YearMonth? = null,
    val opptjeningsland: String? = null,
    val skattemessigBosattLand: String? = null,
    val utbetaltIMaaned: YearMonth,
    val inntektsinnsender: Aktoer? = null,
    val virksomhet: Aktoer? = null,
    val inntektsmottaker: Aktoer? = null,
    val inngaarIGrunnlagForTrekk: Boolean? = null,
    val utloeserArbeidsgiveravgift: Boolean? = null,
    val informasjonsstatus: String? = null,
    val inntektType: InntektType,
    val tilleggsinformasjon: TilleggInformasjon? = null,
    val redigert: Boolean,
    val begrunnelse: String,
    val aarMaaned: YearMonth,
)

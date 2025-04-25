package no.nav.dagpenger.inntekt.mapping

import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Avvik
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import java.math.BigDecimal
import java.time.YearMonth

fun mapToInntektFrontend(
    inntektResponse: InntektkomponentResponse,
    person: Inntektsmottaker,
): InntektForVirksomhetMedPersonInformasjon {
    val inntekt = inntektResponse.arbeidsInntektMaaned
    var virksomhetListe: MutableList<InntektForVirksomhet> = mutableListOf()

    inntekt?.forEach { arbeidsInntektMaaned ->
        val inntektsInformasjon = arbeidsInntektMaaned.arbeidsInntektInformasjon
        inntektsInformasjon?.inntektListe?.forEach { inntekt ->
            val virksomhet = inntekt.virksomhet
            // TODO: Replace with actual logic to get virksomhetNavn
            val virksomhetNavn = "testNavn"
            val inntekter = mutableListOf<InntektVirksomhetMaaned>()
            inntekter.add(
                InntektVirksomhetMaaned(
                    belop = inntekt.beloep,
                    inntektsKilde = inntekt.inntektskilde,
                    // TODO: finn ut om inntekten er redigert
                    redigert = false,
                    begrunnelse = inntekt.beskrivelse.name,
                    aarMaaned = arbeidsInntektMaaned.aarMaaned,
                    fordel = inntekt.fordel,
                    beskrivelse = inntekt.beskrivelse,
                    inntektsStatus = inntekt.inntektsstatus,
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
                virksomhetListe.find { it.virksomhet == virksomhet?.identifikator }
            if (eksisterendeVirksomhet != null) {
                eksisterendeVirksomhet.inntekter?.addAll(inntekter)
                eksisterendeVirksomhet.periode =
                    InntektForVirksomhetPeriode(
                        fra = eksisterendeVirksomhet.inntekter!!.minOf { it.aarMaaned },
                        til = eksisterendeVirksomhet.inntekter.maxOf { it.aarMaaned },
                    )
                eksisterendeVirksomhet.totalBeløp = eksisterendeVirksomhet.inntekter.sumOf { it.belop }
            } else {
                virksomhetListe.add(
                    InntektForVirksomhet(
                        virksomhet = virksomhet!!.identifikator,
                        virksomhetNavn = virksomhetNavn,
                        periode =
                            InntektForVirksomhetPeriode(
                                fra = arbeidsInntektMaaned.aarMaaned,
                                til = arbeidsInntektMaaned.aarMaaned,
                            ),
                        inntekter = inntekter,
                        avvikListe =
                            arbeidsInntektMaaned.avvikListe?.filter { it.virksomhet?.identifikator == virksomhet.identifikator }
                                ?: emptyList(),
                    ),
                )
            }
        }
    }

    return InntektForVirksomhetMedPersonInformasjon(
        inntektVirksomhetMaaned = virksomhetListe,
        mottaker = person,
    )
}

data class InntektForVirksomhetMedPersonInformasjon(
    val inntektVirksomhetMaaned: List<InntektForVirksomhet>,
    val mottaker: Inntektsmottaker,
)

data class InntektForVirksomhet(
    val virksomhet: String,
    val virksomhetNavn: String,
    var periode: InntektForVirksomhetPeriode?,
    val inntekter: MutableList<InntektVirksomhetMaaned>?,
    var totalBeløp: BigDecimal? = inntekter?.sumOf { it.belop } ?: BigDecimal.ZERO,
    val avvikListe: List<Avvik>,
)

data class InntektForVirksomhetPeriode(
    val fra: YearMonth,
    val til: YearMonth,
)

data class InntektVirksomhetMaaned(
    val belop: BigDecimal,
    val fordel: String,
    val beskrivelse: InntektBeskrivelse,
    val inntektsKilde: String,
    val inntektsStatus: String,
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

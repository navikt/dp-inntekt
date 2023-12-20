package no.nav.dagpenger.inntekt.mapping

import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Avvik
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Periode
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.SpesielleInntjeningsforhold
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjonsDetaljer
import no.nav.dagpenger.inntekt.v1.Aktør
import no.nav.dagpenger.inntekt.v1.AktørType
import no.nav.dagpenger.inntekt.v1.InntektId
import no.nav.dagpenger.inntekt.v1.Postering
import no.nav.dagpenger.inntekt.v1.PosteringsType
import no.nav.dagpenger.inntekt.v1.SpesifisertInntekt
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals

private val storedInntekt =
    StoredInntekt(
        inntektId = no.nav.dagpenger.inntekt.db.InntektId("01DGCVFS44PT6B6ZGEYH2WXVMA"),
        inntekt =
            InntektkomponentResponse(
                listOf(
                    ArbeidsInntektMaaned(
                        YearMonth.of(2019, 5),
                        null,
                        ArbeidsInntektInformasjon(
                            listOf(
                                Inntekt(
                                    beloep = BigDecimal.ONE,
                                    fordel = "fordel",
                                    beskrivelse = InntektBeskrivelse.AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS,
                                    inntektskilde = "kilde",
                                    inntektsstatus = "status",
                                    inntektsperiodetype = "periodetype",
                                    inntektType = InntektType.LOENNSINNTEKT,
                                    utbetaltIMaaned = YearMonth.of(2019, 5),
                                ),
                            ),
                        ),
                    ),
                    ArbeidsInntektMaaned(
                        YearMonth.of(2019, 6),
                        listOf(
                            Avvik(
                                ident = Aktoer(AktoerType.AKTOER_ID, "11111111"),
                                opplysningspliktig = Aktoer(AktoerType.AKTOER_ID, "21111111"),
                                virksomhet = Aktoer(AktoerType.ORGANISASJON, "31111111"),
                                avvikPeriode = YearMonth.of(2019, 6),
                                tekst = "",
                            ),
                        ),
                        ArbeidsInntektInformasjon(
                            listOf(
                                Inntekt(
                                    BigDecimal.ONE,
                                    fordel = "fordel",
                                    beskrivelse = InntektBeskrivelse.FASTLOENN,
                                    inntektskilde = "kilde",
                                    inntektsstatus = "status",
                                    inntektsperiodetype = "periodetype",
                                    inntektType = InntektType.LOENNSINNTEKT,
                                    utbetaltIMaaned = YearMonth.of(2019, 6),
                                ),
                                Inntekt(
                                    BigDecimal.ONE,
                                    fordel = "fordel",
                                    beskrivelse = InntektBeskrivelse.FASTLOENN,
                                    inntektskilde = "kilde",
                                    inntektsstatus = "status",
                                    inntektsperiodetype = "periodetype",
                                    inntektType = InntektType.LOENNSINNTEKT,
                                    utbetaltIMaaned = YearMonth.of(2019, 6),
                                    leveringstidspunkt = YearMonth.of(2019, 3),
                                    opptjeningsland = "Norge",
                                    opptjeningsperiode =
                                        Periode(
                                            LocalDate.of(2019, 2, 3),
                                            LocalDate.of(2019, 5, 12),
                                        ),
                                    skattemessigBosattLand = "Norge",
                                    opplysningspliktig = Aktoer(AktoerType.AKTOER_ID, "11111111"),
                                    inntektsinnsender = Aktoer(AktoerType.AKTOER_ID, "22222222"),
                                    virksomhet = Aktoer(AktoerType.ORGANISASJON, "33333333"),
                                    inntektsmottaker = Aktoer(AktoerType.AKTOER_ID, "44444444"),
                                    inngaarIGrunnlagForTrekk = true,
                                    utloeserArbeidsgiveravgift = true,
                                    informasjonsstatus = "informasjon",
                                    tilleggsinformasjon =
                                        TilleggInformasjon(
                                            "kategori",
                                            TilleggInformasjonsDetaljer(
                                                "detaljetyper",
                                                SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
                                            ),
                                        ),
                                ),
                            ),
                        ),
                    ),
                ),
                ident = Aktoer(AktoerType.AKTOER_ID, "aktorId"),
            ),
        manueltRedigert = false,
        timestamp = LocalDateTime.of(2019, 7, 14, 1, 1, 1, 1),
    )

private val spesifisertInntekt =
    SpesifisertInntekt(
        inntektId = InntektId("01DGCVFS44PT6B6ZGEYH2WXVMA"),
        avvik =
            listOf(
                no.nav.dagpenger.inntekt.v1.Avvik(
                    ident = Aktør(AktørType.AKTOER_ID, "11111111"),
                    opplysningspliktig = Aktør(AktørType.AKTOER_ID, "21111111"),
                    virksomhet = Aktør(AktørType.ORGANISASJON, "31111111"),
                    avvikPeriode = YearMonth.of(2019, 6),
                    tekst = "",
                ),
            ),
        posteringer =
            listOf(
                Postering(
                    posteringsMåned = YearMonth.of(2019, 5),
                    beløp = BigDecimal.ONE,
                    fordel = "fordel",
                    inntektskilde = "kilde",
                    inntektsstatus = "status",
                    inntektsperiodetype = "periodetype",
                    utbetaltIMåned = YearMonth.of(2019, 5),
                    posteringsType = PosteringsType.L_AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS,
                ),
                Postering(
                    posteringsMåned = YearMonth.of(2019, 6),
                    beløp = BigDecimal.ONE,
                    fordel = "fordel",
                    inntektskilde = "kilde",
                    inntektsstatus = "status",
                    inntektsperiodetype = "periodetype",
                    utbetaltIMåned = YearMonth.of(2019, 6),
                    posteringsType = PosteringsType.L_FASTLØNN,
                ),
                Postering(
                    posteringsMåned = YearMonth.of(2019, 6),
                    beløp = BigDecimal.ONE,
                    fordel = "fordel",
                    inntektskilde = "kilde",
                    inntektsstatus = "status",
                    inntektsperiodetype = "periodetype",
                    utbetaltIMåned = YearMonth.of(2019, 6),
                    leveringstidspunkt = YearMonth.of(2019, 3),
                    opptjeningsland = "Norge",
                    opptjeningsperiode =
                        no.nav.dagpenger.inntekt.v1.Periode(
                            LocalDate.of(2019, 2, 3),
                            LocalDate.of(2019, 5, 12),
                        ),
                    skattemessigBosattLand = "Norge",
                    opplysningspliktig = Aktør(AktørType.AKTOER_ID, "11111111"),
                    inntektsinnsender = Aktør(AktørType.AKTOER_ID, "22222222"),
                    virksomhet = Aktør(AktørType.ORGANISASJON, "33333333"),
                    inntektsmottaker = Aktør(AktørType.AKTOER_ID, "44444444"),
                    inngårIGrunnlagForTrekk = true,
                    utløserArbeidsgiveravgift = true,
                    informasjonsstatus = "informasjon",
                    posteringsType = PosteringsType.L_FASTLØNN_H,
                ),
            ),
        sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 9),
        ident = Aktør(AktørType.AKTOER_ID, "aktorId"),
        manueltRedigert = false,
        timestamp = LocalDateTime.of(2019, 7, 14, 1, 1, 1, 1),
    )

class MapToSpesifisertInntektTest {
    @Test
    fun `Map inntekt to spesifisertInntekt`() {
        val mappedSpesifisertInntekt = mapToSpesifisertInntekt(storedInntekt, YearMonth.of(2019, 9))

        assertEquals(spesifisertInntekt, mappedSpesifisertInntekt)
    }
}

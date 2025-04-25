package no.nav.dagpenger.inntekt.mapping

import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

val inntektkomponentResponse =
    InntektkomponentResponse(
        ident =
            Aktoer(
                aktoerType = AktoerType.AKTOER_ID,
                identifikator = "2044350291600",
            ),
        arbeidsInntektMaaned =
            listOf(
                ArbeidsInntektMaaned(
                    aarMaaned = YearMonth.of(2025, 1),
                    arbeidsInntektInformasjon =
                        ArbeidsInntektInformasjon(
                            inntektListe =
                                listOf(
                                    Inntekt(
                                        beloep = BigDecimal(50000),
                                        fordel = "kontantytelse",
                                        virksomhet =
                                            Aktoer(
                                                aktoerType = AktoerType.ORGANISASJON,
                                                identifikator = "896929119",
                                            ),
                                        beskrivelse = InntektBeskrivelse.FASTLOENN,
                                        inntektType = InntektType.LOENNSINNTEKT,
                                        inntektskilde = "A-ordningen",
                                        inntektsstatus = "LoependeInnrapportert",
                                        opptjeningsland = "NO",
                                        utbetaltIMaaned = YearMonth.of(2025, 1),
                                        inntektsmottaker =
                                            Aktoer(
                                                aktoerType = AktoerType.AKTOER_ID,
                                                identifikator = "2044350291600",
                                            ),
                                        informasjonsstatus = "InngaarAlltid",
                                        opplysningspliktig =
                                            Aktoer(
                                                aktoerType = AktoerType.ORGANISASJON,
                                                identifikator = "963743254",
                                            ),
                                        inntektsperiodetype = "Maaned",
                                        tilleggsinformasjon =
                                            TilleggInformasjon(
                                                kategori = "NorskKontinentalsokkel",
                                                tilleggsinformasjonDetaljer = null,
                                            ),
                                        skattemessigBosattLand = "NO",
                                        inngaarIGrunnlagForTrekk = true,
                                        utloeserArbeidsgiveravgift = true,
                                    ),
                                ),
                        ),
                    avvikListe = emptyList(),
                ),
                ArbeidsInntektMaaned(
                    aarMaaned = YearMonth.of(2025, 2),
                    arbeidsInntektInformasjon =
                        ArbeidsInntektInformasjon(
                            inntektListe =
                                listOf(
                                    Inntekt(
                                        beloep = BigDecimal(50000),
                                        fordel = "kontantytelse",
                                        virksomhet =
                                            Aktoer(
                                                aktoerType = AktoerType.ORGANISASJON,
                                                identifikator = "896929119",
                                            ),
                                        beskrivelse = InntektBeskrivelse.FASTLOENN,
                                        inntektType = InntektType.LOENNSINNTEKT,
                                        inntektskilde = "A-ordningen",
                                        inntektsstatus = "LoependeInnrapportert",
                                        opptjeningsland = "NO",
                                        utbetaltIMaaned = YearMonth.of(2025, 2),
                                        inntektsmottaker =
                                            Aktoer(
                                                aktoerType = AktoerType.AKTOER_ID,
                                                identifikator = "2044350291600",
                                            ),
                                        informasjonsstatus = "InngaarAlltid",
                                        opplysningspliktig =
                                            Aktoer(
                                                aktoerType = AktoerType.ORGANISASJON,
                                                identifikator = "963743254",
                                            ),
                                        inntektsperiodetype = "Maaned",
                                        tilleggsinformasjon =
                                            TilleggInformasjon(
                                                kategori = "NorskKontinentalsokkel",
                                                tilleggsinformasjonDetaljer = null,
                                            ),
                                        skattemessigBosattLand = "NO",
                                        inngaarIGrunnlagForTrekk = true,
                                        utloeserArbeidsgiveravgift = true,
                                    ),
                                ),
                        ),
                    avvikListe = emptyList(),
                ),
                ArbeidsInntektMaaned(
                    aarMaaned = YearMonth.of(2025, 3),
                    arbeidsInntektInformasjon =
                        ArbeidsInntektInformasjon(
                            inntektListe =
                                listOf(
                                    Inntekt(
                                        beloep = BigDecimal(50000),
                                        fordel = "kontantytelse",
                                        virksomhet =
                                            Aktoer(
                                                aktoerType = AktoerType.ORGANISASJON,
                                                identifikator = "896929120",
                                            ),
                                        beskrivelse = InntektBeskrivelse.FASTLOENN,
                                        inntektType = InntektType.LOENNSINNTEKT,
                                        inntektskilde = "A-ordningen",
                                        inntektsstatus = "LoependeInnrapportert",
                                        opptjeningsland = "NO",
                                        utbetaltIMaaned = YearMonth.of(2025, 3),
                                        inntektsmottaker =
                                            Aktoer(
                                                aktoerType = AktoerType.AKTOER_ID,
                                                identifikator = "2044350291600",
                                            ),
                                        informasjonsstatus = "InngaarAlltid",
                                        opplysningspliktig =
                                            Aktoer(
                                                aktoerType = AktoerType.ORGANISASJON,
                                                identifikator = "963743254",
                                            ),
                                        inntektsperiodetype = "Maaned",
                                        tilleggsinformasjon =
                                            TilleggInformasjon(
                                                kategori = "NorskKontinentalsokkel",
                                                tilleggsinformasjonDetaljer = null,
                                            ),
                                        skattemessigBosattLand = "NO",
                                        inngaarIGrunnlagForTrekk = true,
                                        utloeserArbeidsgiveravgift = true,
                                    ),
                                ),
                        ),
                    avvikListe = emptyList(),
                ),
            ),
    )

val mottaker =
    Inntektsmottaker(
        pnr = "2044350291600",
        navn = "Ola Nordmann",
    )

class MapToInntektFrontendTest {
    @Test
    fun `Map inntekt til InntektForVirksomhetMedPersonInformasjon`() {
        val mappedToInntektFrontend =
            mapToInntektFrontend(
                inntektkomponentResponse,
                mottaker,
            )

        assertEquals(2, mappedToInntektFrontend.inntektVirksomhetMaaned.size)
        assertEquals(mottaker, mappedToInntektFrontend.mottaker)

        assertTrue { mappedToInntektFrontend.inntektVirksomhetMaaned.any { it.virksomhet == "896929119" } }
        assertTrue { mappedToInntektFrontend.inntektVirksomhetMaaned.any { it.virksomhet == "896929120" } }
        assertFalse { mappedToInntektFrontend.inntektVirksomhetMaaned.any { it.virksomhet == "8969291001" } }

        assertEquals(
            2,
            mappedToInntektFrontend.inntektVirksomhetMaaned
                .filter { it.virksomhet == "896929119" }[0]
                .inntekter
                ?.size,
        )

        assertEquals(
            BigDecimal(100000),
            mappedToInntektFrontend.inntektVirksomhetMaaned
                .filter { it.virksomhet == "896929119" }[0]
                .totalBeløp,
        )

        assertEquals(
            1,
            mappedToInntektFrontend.inntektVirksomhetMaaned
                .filter { it.virksomhet == "896929120" }[0]
                .inntekter
                ?.size,
        )

        assertEquals(
            BigDecimal(50000),
            mappedToInntektFrontend.inntektVirksomhetMaaned
                .filter { it.virksomhet == "896929120" }[0]
                .totalBeløp,
        )
    }
}

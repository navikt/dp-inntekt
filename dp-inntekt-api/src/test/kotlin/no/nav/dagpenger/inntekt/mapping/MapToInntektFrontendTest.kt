package no.nav.dagpenger.inntekt.mapping

import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.StoredInntektMedMetadata
import no.nav.dagpenger.inntekt.db.StoredInntektPeriode
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate.now
import java.time.LocalDateTime
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

val organisasjoner =
    mutableListOf(
        Organisasjon(
            organisasjonsnummer = "896929119",
            navn = "Test Org 119",
        ),
        Organisasjon(
            organisasjonsnummer = "896929120",
            navn = "Test Org 120",
        ),
    )

class MapToInntektFrontendTest {
    @Test
    fun `Map inntekt til InntektForVirksomhetMedPersonInformasjon`() {
        val mappedToInntektFrontend =
            inntektkomponentResponse.mapToFrontend(
                person = mottaker,
                organisasjoner = organisasjoner,
                StoredInntektMedMetadata(
                    InntektId(ULID().nextULID().toString()),
                    inntektkomponentResponse,
                    true,
                    LocalDateTime.now(),
                    "01234567890",
                    now(),
                    StoredInntektPeriode(YearMonth.now(), YearMonth.now()),
                    "Dette er en begrunnelse.",
                ),
            )

        mappedToInntektFrontend.begrunnelse shouldBe "Dette er en begrunnelse."
        assertEquals(2, mappedToInntektFrontend.virksomheter.size)
        assertEquals(mottaker, mappedToInntektFrontend.mottaker)

        assertTrue { mappedToInntektFrontend.virksomheter.any { it.virksomhetsnummer == "896929119" } }
        assertTrue { mappedToInntektFrontend.virksomheter.any { it.virksomhetsnummer == "896929120" } }
        assertFalse { mappedToInntektFrontend.virksomheter.any { it.virksomhetsnummer == "8969291001" } }

        assertEquals(
            2,
            mappedToInntektFrontend.virksomheter
                .filter { it.virksomhetsnummer == "896929119" }[0]
                .inntekter
                ?.size,
        )

        assertEquals(
            BigDecimal(100000),
            mappedToInntektFrontend.virksomheter
                .filter { it.virksomhetsnummer == "896929119" }[0]
                .totalBelop,
        )

        assertEquals(
            1,
            mappedToInntektFrontend.virksomheter
                .filter { it.virksomhetsnummer == "896929120" }[0]
                .inntekter
                ?.size,
        )

        assertEquals(
            BigDecimal(50000),
            mappedToInntektFrontend.virksomheter
                .filter { it.virksomhetsnummer == "896929120" }[0]
                .totalBelop,
        )
    }

    @Test
    fun `Map inntekt til Inntekt med tom virksomhetsdata og få tom navn`() {
        val inntektkomponentResponseMedTomVirksomhet =
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
                                                virksomhet = null,
                                                beskrivelse = InntektBeskrivelse.FASTLOENN,
                                                inntektType = InntektType.LOENNSINNTEKT,
                                                inntektskilde = "A-ordningen",
                                                inntektsstatus = "LoependeInnrapportert",
                                                utbetaltIMaaned = YearMonth.of(2025, 1),
                                                inntektsperiodetype = "Maaned",
                                            ),
                                        ),
                                ),
                            avvikListe = emptyList(),
                        ),
                        ArbeidsInntektMaaned(
                            aarMaaned = YearMonth.of(2025, 1),
                            arbeidsInntektInformasjon =
                                ArbeidsInntektInformasjon(
                                    inntektListe =
                                        listOf(
                                            Inntekt(
                                                beloep = BigDecimal(2),
                                                fordel = "kontantytelse",
                                                virksomhet = null,
                                                beskrivelse = InntektBeskrivelse.FASTLOENN,
                                                inntektType = InntektType.LOENNSINNTEKT,
                                                inntektskilde = "A-ordningen",
                                                inntektsstatus = "LoependeInnrapportert",
                                                utbetaltIMaaned = YearMonth.of(2025, 1),
                                                inntektsperiodetype = "Maaned",
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

        val mapTilFrontendMedNullVirksomhet =
            inntektkomponentResponseMedTomVirksomhet.mapToFrontend(
                mottaker,
                organisasjoner,
                StoredInntektMedMetadata(
                    InntektId(ULID().nextULID().toString()),
                    inntektkomponentResponse,
                    true,
                    LocalDateTime.now(),
                    "01234567890",
                    now(),
                    StoredInntektPeriode(YearMonth.now(), YearMonth.now()),
                    "Dette er en begrunnelse.",
                ),
            )
        assertEquals(3, mapTilFrontendMedNullVirksomhet.virksomheter.size)
        assertEquals(2, mapTilFrontendMedNullVirksomhet.virksomheter.filter { it.virksomhetsnummer == "" }.size)
        assertEquals(1, mapTilFrontendMedNullVirksomhet.virksomheter.filter { it.virksomhetsnummer == "896929120" }.size)

        jacksonObjectMapper.writeValueAsString(mapTilFrontendMedNullVirksomhet)
        println(
            "mapTilFrontendMedNullVirksomhetjson: ${
                jacksonObjectMapper.writeValueAsString(
                    mapTilFrontendMedNullVirksomhet,
                )
            }",
        )
    }
}

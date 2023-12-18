package no.nav.dagpenger.inntekt.mapping

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.inntekt.db.InntektId
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
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.SpesielleInntjeningsforhold
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjonsDetaljer
import no.nav.dagpenger.inntekt.moshiInstance
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONCompareResult
import org.skyscreamer.jsonassert.comparator.CustomComparator
import org.skyscreamer.jsonassert.comparator.JSONCompareUtil.getKeys
import org.skyscreamer.jsonassert.comparator.JSONCompareUtil.qualify
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

private val rawInntekt =
    InntektkomponentResponse(
        listOf(
            ArbeidsInntektMaaned(
                YearMonth.of(2019, 5),
                null,
                ArbeidsInntektInformasjon(
                    listOf(
                        Inntekt(
                            BigDecimal.ONE,
                            "fordel",
                            InntektBeskrivelse.AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS,
                            "kilde",
                            "status",
                            "periodetype",
                            inntektType = InntektType.LOENNSINNTEKT,
                            utbetaltIMaaned = YearMonth.of(2019, 5),
                        ),
                    ),
                ),
            ),
            ArbeidsInntektMaaned(
                YearMonth.of(2019, 6),
                null,
                ArbeidsInntektInformasjon(
                    listOf(
                        Inntekt(
                            BigDecimal.ONE,
                            "fordel",
                            InntektBeskrivelse.FASTLOENN,
                            "kilde",
                            "status",
                            "periodetype",
                            inntektType = InntektType.LOENNSINNTEKT,
                            utbetaltIMaaned = YearMonth.of(2019, 6),
                        ),
                        Inntekt(
                            BigDecimal.ONE,
                            "fordel",
                            InntektBeskrivelse.ANNET,
                            "kilde",
                            "status",
                            "periodetype",
                            inntektType = InntektType.LOENNSINNTEKT,
                            utbetaltIMaaned = YearMonth.of(2019, 6),
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
        Aktoer(AktoerType.AKTOER_ID, "aktorId"),
    )

private val inntektsmottaker = Inntektsmottaker("12345678912", "Navn Navnesen")

private val inntektMedVerdikode =
    GUIArbeidsInntektInformasjon(
        listOf(
            InntektMedVerdikode(
                BigDecimal.ONE,
                "fordel",
                InntektBeskrivelse.FASTLOENN,
                "kilde",
                "status",
                "periodetype",
                inntektType = InntektType.NAERINGSINNTEKT,
                utbetaltIMaaned = YearMonth.of(2019, 6),
                verdikode = "Hyre - Annet",
            ),
        ),
    )

@Suppress("ktlint:standard:max-line-length")
internal class KategoriseringTest {
    @Test
    fun `mapToGUIInntekt adds correct verdikode`() {
        val storedInntekt =
            StoredInntekt(
                InntektId(ULID().nextULID()),
                rawInntekt,
                false,
                LocalDateTime.now(),
            )
        val guiInntekt = mapToGUIInntekt(storedInntekt, Opptjeningsperiode(LocalDate.now()), inntektsmottaker)
        assertEquals(
            "Aksjer/grunnfondsbevis til underkurs",
            guiInntekt.inntekt.arbeidsInntektMaaned?.first()?.arbeidsInntektInformasjon?.inntektListe?.first()?.verdikode,
        )
        assertEquals(
            "Fastlønn",
            guiInntekt.inntekt.arbeidsInntektMaaned?.first {
                it.aarMaaned ==
                    YearMonth.of(
                        2019,
                        6,
                    )
            }?.arbeidsInntektInformasjon?.inntektListe?.first()?.verdikode,
        )
        assertEquals(
            "Hyre - Annet",
            guiInntekt.inntekt.arbeidsInntektMaaned?.filter {
                it.aarMaaned ==
                    YearMonth.of(
                        2019,
                        6,
                    )
            }?.first()?.arbeidsInntektInformasjon?.inntektListe?.last()?.verdikode,
        )
    }

    @Test
    fun `Skal legger fra og til dato`() {
        val storedInntekt =
            StoredInntekt(
                InntektId(ULID().nextULID()),
                rawInntekt,
                false,
                LocalDateTime.now(),
            )
        val guiInntekt = mapToGUIInntekt(storedInntekt, Opptjeningsperiode(LocalDate.now()), inntektsmottaker)

        assertNotNull(guiInntekt.inntekt.fraDato)
        assertNotNull(guiInntekt.inntekt.tilDato)
    }

    @Test
    fun `mapToGUIInntekt does not modify other fields than kategori`() {
        val storedInntekt =
            StoredInntekt(
                InntektId(ULID().nextULID()),
                rawInntekt,
                false,
                LocalDateTime.now(),
            )
        val guiInntekt = mapToGUIInntekt(storedInntekt, Opptjeningsperiode(LocalDate.now()), inntektsmottaker)

        val beforeJson = moshiInstance.adapter(InntektkomponentResponse::class.java).toJson(rawInntekt)
        val mappedJson = moshiInstance.adapter(GUIInntektsKomponentResponse::class.java).toJson(guiInntekt.inntekt)

        JSONAssert.assertEquals(
            mappedJson,
            beforeJson,
            AttributeIgnoringComparator(
                JSONCompareMode.STRICT,
                setOf("verdikode", "fraDato", "tilDato"),
                Customization("") { _, _ -> true },
            ),
        )
    }

    @Test
    fun `mapFromGUIInntekt removes verdikode and updates to beskrivelse, type and tilleggsinformasjon correctly`() {
        val guiInntekt =
            GUIInntekt(
                InntektId(ULID().nextULID()),
                LocalDateTime.now(),
                GUIInntektsKomponentResponse(
                    YearMonth.now(),
                    YearMonth.now(),
                    listOf(
                        GUIArbeidsInntektMaaned(
                            YearMonth.of(2019, 6),
                            listOf(avvik()),
                            inntektMedVerdikode,
                        ),
                    ),
                    Aktoer(AktoerType.AKTOER_ID, "3333333333"),
                ),
                false,
                false,
            )

        val mappedInntekt = mapToStoredInntekt(guiInntekt)

        assertEquals(
            InntektBeskrivelse.ANNET,
            mappedInntekt.inntekt.arbeidsInntektMaaned?.first()?.arbeidsInntektInformasjon?.inntektListe?.first()?.beskrivelse,
        )
        assertEquals(
            InntektType.LOENNSINNTEKT,
            mappedInntekt.inntekt.arbeidsInntektMaaned?.first()?.arbeidsInntektInformasjon?.inntektListe?.first()?.inntektType,
        )
        assertEquals(
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
            mappedInntekt.inntekt.arbeidsInntektMaaned?.first()?.arbeidsInntektInformasjon?.inntektListe?.first()?.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.spesielleInntjeningsforhold,
        )
    }

    @Test
    fun `mapFromGUIInntekt does not modify other fields than beskrivelse, type and tilleggsinformasjon`() {
        val guiInntekt =
            GUIInntekt(
                InntektId(ULID().nextULID()),
                LocalDateTime.now(),
                GUIInntektsKomponentResponse(
                    YearMonth.now(),
                    YearMonth.now(),
                    listOf(
                        GUIArbeidsInntektMaaned(
                            YearMonth.of(2019, 6),
                            listOf(avvik()),
                            inntektMedVerdikode,
                        ),
                    ),
                    Aktoer(AktoerType.AKTOER_ID, "3333333333"),
                ),
                false,
                false,
            )

        val mappedInntekt = mapToStoredInntekt(guiInntekt)

        val beforeJson = moshiInstance.adapter(GUIInntektsKomponentResponse::class.java).toJson(guiInntekt.inntekt)
        val mappedJson = moshiInstance.adapter(InntektkomponentResponse::class.java).toJson(mappedInntekt.inntekt)

        JSONAssert.assertEquals(
            beforeJson,
            mappedJson,
            AttributeIgnoringComparator(
                JSONCompareMode.LENIENT,
                setOf("verdikode", "fraDato", "tilDato"),
                Customization("**.beskrivelse") { _, _ -> true },
                Customization("**.inntektType") { _, _ -> true },
            ),
        )
    }

    private fun avvik() =
        Avvik(
            Aktoer(AktoerType.AKTOER_ID, "1111111"),
            Aktoer(AktoerType.AKTOER_ID, "2222222222"),
            null,
            YearMonth.of(2019, 6),
            "tekst",
        )

    @Test
    fun `map to and from GUIInntekt results in original inntekt`() {
        val storedInntekt =
            StoredInntekt(
                InntektId(ULID().nextULID()),
                rawInntekt,
                false,
                LocalDateTime.now(),
            )

        val mapFromGUIInntekt = mapToStoredInntekt(mapToGUIInntekt(storedInntekt, Opptjeningsperiode(LocalDate.now()), null))
        assertEquals(storedInntekt.manueltRedigert, mapFromGUIInntekt.manueltRedigert)
        assertEquals(storedInntekt.inntekt, mapFromGUIInntekt.inntekt)
        assertEquals(storedInntekt.inntektId, mapFromGUIInntekt.inntektId)
    }
}

/* Workaround to be able to use strict mode when one json is missing a field (like kategori)
    see https://stackoverflow.com/a/44328139
*/
class AttributeIgnoringComparator(
    mode: JSONCompareMode,
    var attributesToIgnore: Set<String>,
    vararg customizations: Customization,
) : CustomComparator(
        mode,
        *customizations,
    ) {
    override fun checkJsonObjectKeysExpectedInActual(
        prefix: String,
        expected: JSONObject,
        actual: JSONObject,
        result: JSONCompareResult,
    ) {
        val expectedKeys = getKeys(expected)
        expectedKeys.removeAll(attributesToIgnore)
        for (key in expectedKeys) {
            var expectedValue = expected.get(key)
            if (actual.has(key)) {
                var actualValue = actual.get(key)
                compareValues(qualify(prefix, key), expectedValue, actualValue, result)
            } else {
                result.missing(prefix, key)
            }
        }
    }
}

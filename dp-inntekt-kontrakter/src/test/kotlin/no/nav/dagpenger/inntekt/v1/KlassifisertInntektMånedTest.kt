package no.nav.dagpenger.inntekt.v1

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

class KlassifisertInntektMånedTest {
    val senesteMåned = YearMonth.of(2019, 3)

    val testInntektsListe =
        (0..38).toList().map {
            KlassifisertInntektMåned(
                senesteMåned.minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(1000),
                        InntektKlasse.ARBEIDSINNTEKT,
                    ),
                    KlassifisertInntekt(
                        BigDecimal(2000),
                        InntektKlasse.DAGPENGER_FANGST_FISKE,
                    ),
                ),
            )
        }

    val testInntekt = Inntekt("id", testInntektsListe, sisteAvsluttendeKalenderMåned = senesteMåned)

    @Test
    fun `sum with empty list of inntektsklasserToSum returns 0`() {
        assertEquals(BigDecimal(0), testInntektsListe.sumInntekt(emptyList()))
    }

    @Test
    fun `empty inntekt returns 0`() {
        assertEquals(BigDecimal(0), emptyList<KlassifisertInntektMåned>().sumInntekt(InntektKlasse.values().toList()))
    }

    @Test
    fun ` should sum arbeidsinntekt correctly`() {
        assertEquals(
            BigDecimal(12000),
            testInntekt.splitIntoInntektsPerioder().first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT)),
        )

        assertEquals(
            BigDecimal(36000),
            testInntekt.splitIntoInntektsPerioder().all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT)),
        )
    }

    @Test
    fun ` should sum dp fangst fiske correctly `() {
        assertEquals(
            BigDecimal(24000),
            testInntekt.splitIntoInntektsPerioder().first.sumInntekt(listOf(InntektKlasse.DAGPENGER_FANGST_FISKE)),
        )

        assertEquals(
            BigDecimal(72000),
            testInntekt.splitIntoInntektsPerioder().all().sumInntekt(listOf(InntektKlasse.DAGPENGER_FANGST_FISKE)),
        )
    }

    @Test
    fun ` should sum multiple inntektsklasser`() {
        assertEquals(
            BigDecimal(36000),
            testInntekt.splitIntoInntektsPerioder().first.sumInntekt(
                listOf(
                    InntektKlasse.DAGPENGER_FANGST_FISKE,
                    InntektKlasse.ARBEIDSINNTEKT,
                ),
            ),
        )

        assertEquals(
            BigDecimal(108000),
            testInntekt.splitIntoInntektsPerioder().all().sumInntekt(
                listOf(
                    InntektKlasse.DAGPENGER_FANGST_FISKE,
                    InntektKlasse.ARBEIDSINNTEKT,
                ),
            ),
        )
    }

    @Test
    fun ` should return 0 when no inntekt matches `() {
        assertEquals(
            BigDecimal(0),
            testInntektsListe.sumInntekt(listOf(InntektKlasse.SYKEPENGER)),
        )
    }

    @Test
    fun `all combines three inntektsperioder correctly`() {
        assertEquals(
            emptyList<KlassifisertInntektMåned>(),
            Triple(
                emptyList<KlassifisertInntektMåned>(),
                emptyList<KlassifisertInntektMåned>(),
                emptyList<KlassifisertInntektMåned>(),
            ).all(),
        )

        val first = (1..5).toList().map { KlassifisertInntektMåned(senesteMåned, emptyList()) }
        val second = (1..7).toList().map { KlassifisertInntektMåned(senesteMåned.minusMonths(2), emptyList()) }
        val third = listOf(KlassifisertInntektMåned(senesteMåned.plusMonths(1), emptyList()))
        val all = Triple(first, second, third).all()

        assertEquals(13, all.size)
        assertEquals(5, all.filter { it.årMåned == senesteMåned }.size)
        assertEquals(7, all.filter { it.årMåned == senesteMåned.minusMonths(2) }.size)
        assertEquals(1, all.filter { it.årMåned == senesteMåned.plusMonths(1) }.size)
    }
}

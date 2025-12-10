package no.nav.dagpenger.inntekt.v1

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth

class InntektTest {
    private val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)

    val testInntektsListe =
        (0..38).toList().map {
            KlassifisertInntektMåned(
                sisteAvsluttendeKalenderMåned.minusMonths(it.toLong()),
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

    val testInntekt =
        Inntekt(
            "id",
            testInntektsListe,
            sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
            hentetTidspunkt = java.time.LocalDateTime.now(),
        )

    @Test
    fun `filtering period of last three months affects sum of inntekt`() {
        val filteredInntekt =
            testInntekt.filterPeriod(
                sisteAvsluttendeKalenderMåned.minusMonths(2),
                sisteAvsluttendeKalenderMåned.minusMonths(0),
            )
        assertEquals(
            BigDecimal(9000),
            filteredInntekt.splitIntoInntektsPerioder().first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT)),
        )
        assertEquals(
            BigDecimal(33000),
            filteredInntekt.splitIntoInntektsPerioder().all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT)),
        )
    }

    @Test
    fun `filtering period not overlapping exisiting months does not affect sum `() {
        val filteredInntekt =
            testInntekt.filterPeriod(
                sisteAvsluttendeKalenderMåned.minusMonths(48),
                sisteAvsluttendeKalenderMåned.minusMonths(37),
            )
        assertEquals(
            testInntekt.splitIntoInntektsPerioder().first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT)),
            filteredInntekt.splitIntoInntektsPerioder().first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT)),
        )
        assertEquals(
            testInntekt.splitIntoInntektsPerioder().all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT)),
            filteredInntekt.splitIntoInntektsPerioder().all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT)),
        )
    }

    @Test
    fun `filter period throws exception if from-argument is more recent than to`() {
        assertThrows<IllegalArgumentException> {
            testInntekt.filterPeriod(
                YearMonth.of(2019, 5),
                YearMonth.of(2019, 4),
            )
        }
    }

    @Test
    fun `inntektsPerioder splits up inntekt correctly`() {
        val inntekt =
            Inntekt(
                "id",
                inntektsListe =
                    (0..36).toList().map {
                        KlassifisertInntektMåned(
                            sisteAvsluttendeKalenderMåned.minusMonths(it.toLong()),
                            listOf(
                                KlassifisertInntekt(
                                    BigDecimal(1000),
                                    InntektKlasse.ARBEIDSINNTEKT,
                                ),
                            ),
                        )
                    },
                sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                hentetTidspunkt = LocalDateTime.now(),
            )

        val (first, second, third) = inntekt.splitIntoInntektsPerioder()

        assertEquals(12, first.size)
        assertEquals(12, second.size)
        assertEquals(12, third.size)

        assertTrue(first.all { it.årMåned in YearMonth.of(2018, 4)..YearMonth.of(2019, 3) })
        assertEquals(YearMonth.of(2019, 3), first.last().årMåned)
        assertEquals(YearMonth.of(2018, 4), first.first().årMåned)

        assertTrue(second.all { it.årMåned in YearMonth.of(2017, 4)..YearMonth.of(2018, 3) })
        assertEquals(YearMonth.of(2018, 3), second.last().årMåned)
        assertEquals(YearMonth.of(2017, 4), second.first().årMåned)

        assertTrue(third.all { it.årMåned in YearMonth.of(2016, 4)..YearMonth.of(2017, 3) })
        assertEquals(YearMonth.of(2017, 3), third.last().årMåned)
        assertEquals(YearMonth.of(2016, 4), third.first().årMåned)
    }

    @Test
    fun `inntektsPerioder correctly splits up inntekt for only last year`() {
        val senesteMåned = YearMonth.of(2019, 3)
        val onlyInntektLastYear =
            Inntekt(
                "id",
                inntektsListe =
                    (0..11).toList().map {
                        KlassifisertInntektMåned(
                            senesteMåned.minusMonths(it.toLong()),
                            listOf(KlassifisertInntekt(BigDecimal(1000), InntektKlasse.ARBEIDSINNTEKT)),
                        )
                    },
                sisteAvsluttendeKalenderMåned = senesteMåned,
                hentetTidspunkt = LocalDateTime.now(),
            )
        val (first, second, third) = onlyInntektLastYear.splitIntoInntektsPerioder()

        assertEquals(12, first.size)
        assertEquals(12, second.size)
        assertEquals(12, third.size)

        assertTrue(first.all { it.årMåned in YearMonth.of(2018, 4)..YearMonth.of(2019, 3) })
        assertEquals(YearMonth.of(2019, 3), first.last().årMåned)
        assertEquals(YearMonth.of(2018, 4), first.first().årMåned)

        assertTrue(second.all { it.årMåned in YearMonth.of(2017, 4)..YearMonth.of(2018, 3) })
        assertEquals(YearMonth.of(2018, 3), second.last().årMåned)
        assertEquals(YearMonth.of(2017, 4), second.first().årMåned)

        assertTrue(third.all { it.årMåned in YearMonth.of(2016, 4)..YearMonth.of(2017, 3) })
        assertEquals(YearMonth.of(2017, 3), third.last().årMåned)
        assertEquals(YearMonth.of(2016, 4), third.first().årMåned)

        assertTrue(first.all { it.klassifiserteInntekter.isNotEmpty() })
        assertTrue(second.all { it.klassifiserteInntekter.isEmpty() })
        assertTrue(third.all { it.klassifiserteInntekter.isEmpty() })
    }

    @Test
    fun `inntektsPerioder correctly splits up inntekt missing earliest year`() {
        val senesteMåned = YearMonth.of(2019, 3)
        val noInntektThirdPeriod =
            Inntekt(
                "id",
                inntektsListe =
                    (0..23).toList().map {
                        KlassifisertInntektMåned(
                            senesteMåned.minusMonths(it.toLong()),
                            listOf(KlassifisertInntekt(BigDecimal(1000), InntektKlasse.ARBEIDSINNTEKT)),
                        )
                    },
                sisteAvsluttendeKalenderMåned = senesteMåned,
                hentetTidspunkt = LocalDateTime.now(),
            )

        val (first, second, third) = noInntektThirdPeriod.splitIntoInntektsPerioder()

        assertEquals(12, first.size)
        assertEquals(12, second.size)
        assertEquals(12, third.size)

        assertTrue(first.all { it.årMåned in YearMonth.of(2018, 4)..YearMonth.of(2019, 3) })
        assertEquals(YearMonth.of(2019, 3), first.last().årMåned)
        assertEquals(YearMonth.of(2018, 4), first.first().årMåned)

        assertTrue(second.all { it.årMåned in YearMonth.of(2017, 4)..YearMonth.of(2018, 3) })
        assertEquals(YearMonth.of(2018, 3), second.last().årMåned)
        assertEquals(YearMonth.of(2017, 4), second.first().årMåned)

        assertTrue(third.all { it.årMåned in YearMonth.of(2016, 4)..YearMonth.of(2017, 3) })
        assertEquals(YearMonth.of(2017, 3), third.last().årMåned)
        assertEquals(YearMonth.of(2016, 4), third.first().årMåned)

        assertTrue(first.all { it.klassifiserteInntekter.isNotEmpty() })
        assertTrue(second.all { it.klassifiserteInntekter.isNotEmpty() })
        assertTrue(third.all { it.klassifiserteInntekter.isEmpty() })
    }

    @Test
    fun `inntektsPerioder correctly splits up noncontinous inntekt`() {
        val nonContinous =
            Inntekt(
                "id",
                inntektsListe =
                    ((0..5).toList() + (10..24).toList()).map {
                        KlassifisertInntektMåned(
                            sisteAvsluttendeKalenderMåned.minusMonths(it.toLong()),
                            listOf(KlassifisertInntekt(BigDecimal(1000), InntektKlasse.ARBEIDSINNTEKT)),
                        )
                    },
                sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
                hentetTidspunkt = LocalDateTime.now(),
            )

        val (first, second, third) = nonContinous.splitIntoInntektsPerioder()

        assertEquals(12, first.size)
        assertEquals(12, second.size)
        assertEquals(12, third.size)

        assertTrue(first.all { it.årMåned in YearMonth.of(2018, 4)..YearMonth.of(2019, 3) })
        assertEquals(YearMonth.of(2019, 3), first.last().årMåned)
        assertEquals(YearMonth.of(2018, 4), first.first().årMåned)

        assertTrue(second.all { it.årMåned in YearMonth.of(2017, 4)..YearMonth.of(2018, 3) })
        assertEquals(YearMonth.of(2018, 3), second.last().årMåned)
        assertEquals(YearMonth.of(2017, 4), second.first().årMåned)

        assertTrue(third.all { it.årMåned in YearMonth.of(2016, 4)..YearMonth.of(2017, 3) })
        assertEquals(YearMonth.of(2017, 3), third.last().årMåned)
        assertEquals(YearMonth.of(2016, 4), third.first().årMåned)

        assertEquals(8, first.filter { it.klassifiserteInntekter.isNotEmpty() }.size)
        assertEquals(12, second.filter { it.klassifiserteInntekter.isNotEmpty() }.size)
        assertEquals(1, third.filter { it.klassifiserteInntekter.isNotEmpty() }.size)
    }
}

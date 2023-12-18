package no.nav.dagpenger.inntekt.opptjeningsperiode

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ktlint:standard:max-line-length")
class OpptjeningsperiodeTest {
    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 5])
    fun `Hvis 5 juli er en fredag Så er arbeidsgivers rapporteringsfrist fredag 5 juli - Dersom beregningsdato er i perioden 1-5 juli - Så er mai siste avsluttede kalendermåned fordi 5 juli ikke er passert`(
        dag: Int,
    ) {
        val inntjeningsperiode = Opptjeningsperiode(LocalDate.of(2019, 7, dag))
        assertEquals(YearMonth.of(2019, 5), inntjeningsperiode.sisteAvsluttendeKalenderMåned)
        assertEquals(YearMonth.of(2016, 6), inntjeningsperiode.førsteMåned)
    }

    @Test
    fun `Hvis 5 juli er en fredag Så er arbeidsgivers rapporteringsfrist fredag 5 juli - Dersom beregningsdato er fra og med 6 juli (selv om det er helg) - Så er juni siste avsluttede kalendermåned fordi 5 juli er passert `() {
        val inntjeningsperiode = Opptjeningsperiode(LocalDate.of(2019, 7, 6))
        assertEquals(YearMonth.of(2019, 6), inntjeningsperiode.sisteAvsluttendeKalenderMåned)
        assertEquals(YearMonth.of(2016, 7), inntjeningsperiode.førsteMåned)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7])
    fun `Hvis 5 oktober er en lørdag (helg) Så er arbeidsgivers rapporteringsfrist mandag 7 oktober, og fristen er først passert fra og med tirsdag 8 oktober`(
        dag: Int,
    ) {
        // Dersom beregningsdato er i perioden 1.-7. oktober
        // Så er oktober siste avsluttede kalendermåned fordi 7. oktober ikke er passert
        assertEquals(
            YearMonth.of(2019, 8),
            Opptjeningsperiode(LocalDate.of(2019, 10, dag)).sisteAvsluttendeKalenderMåned,
        )
    }

    @Test
    fun `Hvis 5 oktober er en lørdag (helg) Så er arbeidsgivers rapporteringsfrist mandag 7 oktober, og fristen er først passert fra og med tirsdag 8 oktober`() {
        // Dersom beregningsdato er fra og med 8. oktober
        // Så er september siste avsluttede kalendermåned fordi 7. oktober er passert
        assertEquals(YearMonth.of(2019, 9), Opptjeningsperiode(LocalDate.of(2019, 10, 8)).sisteAvsluttendeKalenderMåned)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7, 9, 10])
    fun `Hvis 5  april er skjærtorsdag (bevegelig helligdag) Så er arbeidsgivers rapporteringsfrist tirsdag 10 april, Og fristen er først passert fra og med onsdag 11 april`(
        dag: Int,
    ) {
        // Dersom beregningsdato er i perioden 1.-10. april
        // Så er februar siste avsluttede kalendermåned fordi 10. april ikke er passert
        assertEquals(
            YearMonth.of(2012, 2),
            Opptjeningsperiode(LocalDate.of(2012, 4, dag)).sisteAvsluttendeKalenderMåned,
        )
    }

    @Test
    fun `Hvis 5  april er skjærtorsdag (bevegelig helligdag) Så er arbeidsgivers rapporteringsfrist tirsdag 10 april, Og fristen er først passert fra og med onsdag 11 april`() {
        assertEquals(YearMonth.of(2012, 3), Opptjeningsperiode(LocalDate.of(2012, 4, 11)).sisteAvsluttendeKalenderMåned)
    }

    @Test
    fun ` Bergingsdato som ikke går over 5 i neste måned bør ha samme inntjenningsperiode `() {
        val inntjeningsperiode1 = Opptjeningsperiode(LocalDate.of(2019, 7, 6))
        val inntjeningsperiode2 = Opptjeningsperiode(LocalDate.of(2019, 7, 31))

        assertTrue { inntjeningsperiode1.sammeOpptjeningsPeriode(inntjeningsperiode2) }
    }

    @Test
    fun ` Bergingsdato som går over 5 i neste måned bør ikke ha samme inntjenningsperiode `() {
        val inntjeningsperiode1 = Opptjeningsperiode(LocalDate.of(2019, 7, 6))
        val inntjeningsperiode2 = Opptjeningsperiode(LocalDate.of(2019, 8, 6))

        assertFalse { inntjeningsperiode1.sammeOpptjeningsPeriode(inntjeningsperiode2) }
    }
}

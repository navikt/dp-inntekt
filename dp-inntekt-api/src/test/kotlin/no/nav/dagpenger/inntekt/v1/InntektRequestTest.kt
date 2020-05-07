package no.nav.dagpenger.inntekt.v1

import java.time.LocalDate
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InntektRequestTest {
    @Test
    fun ` Map to InntektkomponentenRequest`() {
        val request = InntektRequest(
                aktørId = "1234",
                vedtakId = 1223,
                beregningsDato = LocalDate.of(2019, 4, 3)
        )

        val opptjeningsperiode = Opptjeningsperiode(request.beregningsDato)
        val inntektskomponentRequest = toInntektskomponentRequest(request, opptjeningsperiode)

        assertEquals("1234", inntektskomponentRequest.aktørId)
        assertEquals("2016-03", inntektskomponentRequest.månedFom.toString())
        assertEquals("2019-02", inntektskomponentRequest.månedTom.toString())
    }
}

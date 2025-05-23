package no.nav.dagpenger.inntekt.api.v1

import no.nav.dagpenger.inntekt.oppslag.Person
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class InntektRequestTest {
    @Test
    fun ` Map to InntektkomponentenRequest`() {
        val opptjeningsperiode = Opptjeningsperiode(LocalDate.of(2019, 4, 3))
        val person =
            Person(
                fødselsnummer = "",
                aktørId = "1234",
                fornavn = "",
                mellomnavn = null,
                etternavn = "",
            )
        val inntektskomponentRequest = toInntektskomponentRequest(person, opptjeningsperiode)

        assertEquals("1234", inntektskomponentRequest.aktørId)
        assertEquals("2016-03", inntektskomponentRequest.månedFom.toString())
        assertEquals("2019-02", inntektskomponentRequest.månedTom.toString())
    }
}

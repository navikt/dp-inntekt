package no.nav.dagpenger.inntekt.mapping

import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.SpesielleInntjeningsforhold
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VerdikoderKtTest {
    @Test
    fun `test posteringstype-mapping for Feriepenger`() {
        val posteringsTypeInfo = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, null)

        assertEquals("Feriepenger", verdiKode(posteringsTypeInfo))
    }

    @Test
    fun `test at inntjeningsforhold uten mapping er det samme som null`() {
        val posteringsTypeWithNull = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, null)
        val posteringsTypeWithoutMapping =
            DatagrunnlagKlassifisering(
                InntektType.LOENNSINNTEKT,
                InntektBeskrivelse.FERIEPENGER,
                SpesielleInntjeningsforhold.STATSANSATT_UTLANDET,
            )

        assertEquals(verdiKode(posteringsTypeWithNull), verdiKode(posteringsTypeWithoutMapping))
    }

    @Test
    fun `test at ukjent inntjeningsforhold er det samme som null`() {
        val posteringsTypeWithNull = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, null)
        val posteringsTypeWithUnknown =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, SpesielleInntjeningsforhold.UNKNOWN)

        assertEquals(verdiKode(posteringsTypeWithNull), verdiKode(posteringsTypeWithUnknown))
    }
}

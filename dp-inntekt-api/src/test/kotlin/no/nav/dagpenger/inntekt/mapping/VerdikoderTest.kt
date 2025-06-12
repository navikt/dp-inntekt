package no.nav.dagpenger.inntekt.mapping

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType.LOENNSINNTEKT
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType.YTELSE_FRA_OFFENTLIGE
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.SpesielleInntjeningsforhold
import org.junit.jupiter.api.Test

@Suppress("ktlint:standard:max-line-length")
internal class VerdikoderTest {
    @Test
    fun `posteringstype-mapping for Feriepenger returnerer forventet verdi`() {
        val posteringsTypeInfo =
            DatagrunnlagKlassifisering(LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, null)

        verdiKode(posteringsTypeInfo) shouldBe "Feriepenger"
    }

    @Test
    fun `verdikode for DatagrunnlagKlassifisering med inntjeningsforhold uten mapping skal være lik verdikode for DatagrunnlagKlassifisering som inntjeningsforhold lik null`() {
        val posteringsTypeWithNull =
            DatagrunnlagKlassifisering(LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, null)
        val posteringsTypeWithoutMapping =
            DatagrunnlagKlassifisering(
                LOENNSINNTEKT,
                InntektBeskrivelse.FERIEPENGER,
                SpesielleInntjeningsforhold.STATSANSATT_UTLANDET,
            )

        verdiKode(posteringsTypeWithoutMapping) shouldBe verdiKode(posteringsTypeWithNull)
    }

    @Test
    fun `verdikode for DatagrunnlagKlassifisering med ukjent inntjeningsforhold skal være lik verdikode for DatagrunnlagKlassifisering med inntjeningsforhold lik null`() {
        val posteringsTypeWithNull =
            DatagrunnlagKlassifisering(LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, null)
        val posteringsTypeWithUnknown =
            DatagrunnlagKlassifisering(
                LOENNSINNTEKT,
                InntektBeskrivelse.FERIEPENGER,
                SpesielleInntjeningsforhold.UNKNOWN,
            )

        verdiKode(posteringsTypeWithUnknown) shouldBe verdiKode(posteringsTypeWithNull)
    }

    @Test
    fun `utledInntektType utleder InntektBeskrivelse til forventet InntektType`() {
        utledInntektType(InntektBeskrivelse.FORELDREPENGER) shouldBe YTELSE_FRA_OFFENTLIGE
    }
}

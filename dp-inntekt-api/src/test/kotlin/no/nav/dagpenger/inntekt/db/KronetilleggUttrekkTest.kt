package no.nav.dagpenger.inntekt.db

import no.nav.dagpenger.inntekt.DataSource
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.withMigratedDb
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

internal class KronetilleggUttrekkTest {

    @Test
    fun `skal finne inntekter med dagpenger som grunnlag`() {
        val inntektMedOrdinæreDagpenger = storeInntektCommand(inntektMedOrdinæreDagpenger)
        val inntektMedOrdinæreDagpengerMedFiskHyre = storeInntektCommand(inntektMedOrdinæreDagpengerMedFiskeHyre)
        val inntektMedFangOgFiskDagpenger = storeInntektCommand(inntektMedFangstOgFiskDagpenger)
        val inntektUten = storeInntektCommand(inntektUtenDagpenger)
        withMigratedDb {
            val uttrekk = KronetilleggUttrekk(DataSource.instance)
            val store = PostgresInntektStore(DataSource.instance)
            store.storeInntekt(inntektMedOrdinæreDagpenger).also {
                assertTrue(uttrekk.utrekkFra(it.inntektId).svar)
            }
            store.storeInntekt(inntektMedOrdinæreDagpengerMedFiskHyre).also {
                assertTrue(uttrekk.utrekkFra(it.inntektId).svar)
            }
            store.storeInntekt(inntektMedFangOgFiskDagpenger).also {
                assertTrue(uttrekk.utrekkFra(it.inntektId).svar)
            }
            store.storeInntekt(inntektUten).also {
                assertFalse(uttrekk.utrekkFra(it.inntektId).svar)
            }
        }
    }

    private val inntektMedOrdinæreDagpenger = Inntekt(
        beloep = BigDecimal(2000),
        fordel = "?",
        beskrivelse = InntektBeskrivelse.DAGPENGER_VED_ARBEIDSLOESHET,
        inntektskilde = "skatt",
        inntektsstatus = "?",
        inntektsperiodetype = "?",
        inntektType = InntektType.YTELSE_FRA_OFFENTLIGE,
        utbetaltIMaaned = YearMonth.now()
    )
    private val inntektMedOrdinæreDagpengerMedFiskeHyre = Inntekt(
        beloep = BigDecimal(2000),
        fordel = "?",
        beskrivelse = InntektBeskrivelse.DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
        inntektskilde = "skatt",
        inntektsstatus = "?",
        inntektsperiodetype = "?",
        inntektType = InntektType.YTELSE_FRA_OFFENTLIGE,
        utbetaltIMaaned = YearMonth.now()
    )

    private val inntektMedFangstOgFiskDagpenger = Inntekt(
        beloep = BigDecimal(2000),
        fordel = "?",
        beskrivelse = InntektBeskrivelse.DAGPENGER_TIL_FISKER,
        inntektskilde = "skatt",
        inntektsstatus = "?",
        inntektsperiodetype = "?",
        inntektType = InntektType.NAERINGSINNTEKT,
        utbetaltIMaaned = YearMonth.now()
    )

    private val inntektUtenDagpenger = Inntekt(
        beloep = BigDecimal(2000),
        fordel = "?",
        beskrivelse = InntektBeskrivelse.FASTLOENN,
        inntektskilde = "skatt",
        inntektsstatus = "?",
        inntektsperiodetype = "?",
        inntektType = InntektType.LOENNSINNTEKT,
        utbetaltIMaaned = YearMonth.now()
    )

    private fun storeInntektCommand(inntekt: Inntekt) = StoreInntektCommand(
        inntektparametre = Inntektparametre("1234", LocalDate.now(), RegelKontekst("1234", "vedtak")),
        inntekt = InntektkomponentResponse(
            ident = Aktoer(AktoerType.AKTOER_ID, "1235"),
            arbeidsInntektMaaned = listOf(
                ArbeidsInntektMaaned(
                    aarMaaned = YearMonth.now(), emptyList(),
                    arbeidsInntektInformasjon = ArbeidsInntektInformasjon(
                        inntektListe = listOf(
                            inntekt
                        )
                    )
                )
            )
        )
    )
}

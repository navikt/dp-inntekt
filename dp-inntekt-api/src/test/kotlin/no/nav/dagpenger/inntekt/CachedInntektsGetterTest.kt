package no.nav.dagpenger.inntekt

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.db.StoreInntektCommand
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentRequest
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

internal class CachedInntektsGetterTest {

    private val inntektskomponentClientMock: InntektskomponentClient = mockk()
    private val inntektStoreMock: InntektStore = mockk()

    private val emptyInntektsKomponentResponse = InntektkomponentResponse(
        emptyList(),
        Aktoer(AktoerType.AKTOER_ID, "1234")
    )

    @Test
    fun `Get cached inntekt for known behandlingsKey`() {
        val parameters = Inntektparametre(
            aktørId = "1234",
            fødselsnummer = "1234",
            regelkontekst = RegelKontekst("112233", "vedtak"),
            beregningsdato = LocalDate.of(2019, 5, 6)
        )

        val knownStoredInntekt = StoredInntekt(
            InntektId("01DGPKY3CCN8H87WNTBK696TKV"),
            emptyInntektsKomponentResponse,
            false
        )

        every {
            inntektStoreMock.getInntektId(parameters)
        } returns knownStoredInntekt.inntektId

        every {
            inntektStoreMock.getInntekt(knownStoredInntekt.inntektId)
        } returns knownStoredInntekt

        val cachedInntektsGetter = BehandlingsInntektsGetter(inntektskomponentClientMock, inntektStoreMock)
        val storedInntektResult = runBlocking { cachedInntektsGetter.getBehandlingsInntekt(parameters) }

        verify(exactly = 0) { runBlocking { inntektskomponentClientMock.getInntekt(any()) } }
        assertEquals(emptyInntektsKomponentResponse.ident, storedInntektResult.inntekt.ident)
    }

    @Test
    fun `Get new inntekt for uknown behandlingsKey`() {
        val parameters = Inntektparametre(
            aktørId = "5678",
            fødselsnummer = "5678",
            regelkontekst = RegelKontekst("546787", "vedtak"),
            beregningsdato = LocalDate.of(2019, 4, 26)
        )

        every {
            runBlocking {
                inntektskomponentClientMock.getInntekt(
                    InntektkomponentRequest(
                        aktørId = "5678",
                        fødselsnummer = "5678",
                        månedFom = YearMonth.of(2016, 4),
                        månedTom = YearMonth.of(2019, 3)
                    )
                )
            }
        } returns emptyInntektsKomponentResponse

        every {
            inntektStoreMock.getInntektId(parameters)
        } returns null

        every {
            inntektStoreMock.storeInntekt(
                command = StoreInntektCommand(
                    inntektparametre = parameters,
                    inntekt = emptyInntektsKomponentResponse,
                    manueltRedigert = null
                ),
                created = any()
            )
        } returns StoredInntekt(InntektId("01DH179R2HW0FYEP1FABAXV150"), emptyInntektsKomponentResponse, false)

        val cachedInntektsGetter = BehandlingsInntektsGetter(inntektskomponentClientMock, inntektStoreMock)
        val storedInntektResult = runBlocking { cachedInntektsGetter.getBehandlingsInntekt(parameters) }

        verify(exactly = 1) { runBlocking { inntektskomponentClientMock.getInntekt(any()) } }
        verify(exactly = 1) { runBlocking { inntektStoreMock.storeInntekt(any(), any()) } }
        assertEquals(emptyInntektsKomponentResponse.ident, storedInntektResult.inntekt.ident)
    }
}

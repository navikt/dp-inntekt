package no.nav.dagpenger.inntekt.v1

import de.huxhorn.sulky.ulid.ULID
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.KronetilleggUttrekk
import no.nav.dagpenger.inntekt.db.Uttrekk
import no.nav.dagpenger.inntekt.v1.TestApplication.handleAuthenticatedAzureAdRequest
import no.nav.dagpenger.inntekt.v1.TestApplication.mockInntektApi
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class UttrekkRouteTest {
    @Test
    fun `skal svare om dagpenger er med i inntektsgrunnlaget()`() {
        val uttrekk = mockk<KronetilleggUttrekk>()
        val id = ULID().nextULID()
        every {
            uttrekk.utrekkFra(InntektId(id))
        } returns Uttrekk(id.toString(), true)

        testApp(uttrekk) {
            handleAuthenticatedAzureAdRequest(HttpMethod.Get, "/v2/inntekt/$id/harDagpenger").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("""{"id":"$id","svar":true}""", response.content)
            }
        }
    }

    @Test
    fun `ikke eksisterende inntekt id gir 404`() {
        val id = ULID().nextULID()
        val uttrekk = mockk<KronetilleggUttrekk>()
        every {
            uttrekk.utrekkFra(InntektId(id))
        } throws InntektNotFoundException("fant ikke")
        testApp(uttrekk) {
            handleAuthenticatedAzureAdRequest(HttpMethod.Get, "/v2/inntekt/$id/harDagpenger").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    private fun testApp(
        uttrekk: KronetilleggUttrekk,
        callback: TestApplicationEngine.() -> Unit,
    ) {
        withTestApplication(
            mockInntektApi(
                kronetilleggUttrekk = uttrekk,
            ),
        ) { callback() }
    }
}

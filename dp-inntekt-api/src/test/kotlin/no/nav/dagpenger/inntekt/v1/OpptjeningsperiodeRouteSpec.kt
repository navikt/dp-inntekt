package no.nav.dagpenger.inntekt.v1

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.v1.TestApplication.mockInntektApi
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class OpptjeningsperiodeRouteSpec {
    @Test
    fun `Same Inntjeningsperiode API specification test - Should match json field names and formats`() {
        val inntektStore: InntektStore = mockk()

        every {
            runBlocking {
                inntektStore.getBeregningsdato(any())
            }
        } returns LocalDate.of(2019, 2, 27)

        withTestApplication(mockInntektApi(inntektStore = inntektStore)) {
            handleRequest(HttpMethod.Post, "/v1/samme-inntjeningsperiode") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(
                    """
                    {
                      "beregningsdato": "2019-02-27",
                      "inntektsId": "01ARZ3NDEKTSV4RRFFQ69G5FAV"
                    }
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val expectedJsonResult =
                    """{"sammeInntjeningsPeriode":true}"""
                assertEquals(expectedJsonResult, response.content)
            }
        }
    }
}

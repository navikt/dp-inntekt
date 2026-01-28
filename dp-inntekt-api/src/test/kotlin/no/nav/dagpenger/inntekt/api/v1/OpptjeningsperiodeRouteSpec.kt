package no.nav.dagpenger.inntekt.api.v1

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.inntekt.api.v1.TestApplication.autentisert
import no.nav.dagpenger.inntekt.api.v1.TestApplication.mockInntektApi
import no.nav.dagpenger.inntekt.db.InntektStore
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

        mockInntektApi(inntektStore = inntektStore) {
            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "/v1/samme-inntjeningsperiode",
                    body =
                        """
                        {
                          "beregningsdato": "2019-02-27",
                          "inntektsId": "01ARZ3NDEKTSV4RRFFQ69G5FAV"
                        }
                        """.trimIndent(),
                )

            assertEquals(HttpStatusCode.OK, response.status)
            val expectedJsonResult =
                """{"sammeInntjeningsPeriode":true}"""
            assertEquals(expectedJsonResult, response.bodyAsText())
        }
    }
}

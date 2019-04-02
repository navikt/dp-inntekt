package no.nav.dagpenger.inntekt.v1

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.inntekt.Environment
import no.nav.dagpenger.inntekt.InntektskomponentClient
import no.nav.dagpenger.inntekt.InntektskomponentenHttpClientException
import no.nav.dagpenger.inntekt.Problem
import no.nav.dagpenger.inntekt.inntektApi
import no.nav.dagpenger.inntekt.moshiInstance
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

val validJson = """
{
	"aktørId": "1234",
    "vedtakId": 1,
    "beregningsDato": "2019-01-08"
}
""".trimIndent()

val jsonMissingFields = """
{
	"aktørId": "1234",
}
""".trimIndent()

class InntektApiTest {

    private val inntektskomponentClientMock: InntektskomponentClient = mockk()

    init {
        every { inntektskomponentClientMock.getInntekt("1234", any(), any()) } returns HentInntektListeResponse(
            emptyList(),
            Aktoer(AktoerType.AKTOER_ID, "1234")
        )
        every {
            inntektskomponentClientMock.getInntekt(
                "5678",
                any(),
                any()
            )
        } throws InntektskomponentenHttpClientException(400, "Bad request")
    }

    @Test
    fun ` should be able to Http GET isReady, isAlive and metrics endpoint `() =
        testApp {

            with(handleRequest(HttpMethod.Get, "isAlive")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
            }
            with(handleRequest(HttpMethod.Get, "isReady")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
            }
            with(handleRequest(HttpMethod.Get, "metrics")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
            }
        }

    @Test
    fun `post request with good json`() = testApp {
        handleRequest(HttpMethod.Post, "/v1/inntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(validJson)
        }.apply {
            assertTrue(requestHandled)
            Assertions.assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun ` Should respond on unhandled errors and return in rfc7807 problem details standard `() = testApp {
        handleRequest(HttpMethod.Post, "/v1/inntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(
                """
                {
                    "aktørId": "5678",
                    "vedtakId": 1,
                    "beregningsDato": "2019-01-08"
                }
            """.trimIndent()
            )
        }.apply {
            assertTrue(requestHandled)
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
            val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
            assertEquals("Feilet mot inntektskomponentent!", problem?.title)
            assertEquals("urn:dp:error:inntekt", problem?.type.toString())
            assertEquals(400, problem?.status)
        }
    }

    @Test
    fun ` should forward Http status from inntektskomponenten and return in rfc7807 problem details standard `() = testApp {
        handleRequest(HttpMethod.Post, "/v1/inntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(
                """
                {
                    "aktørId": "5678",
                    "vedtakId": 1,
                    "beregningsDato": "2019-01-08"
                }
            """.trimIndent()
            )
        }.apply {
            assertTrue(requestHandled)
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
            val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
            assertEquals("Feilet mot inntektskomponentent!", problem?.title)
            assertEquals("urn:dp:error:inntekt", problem?.type.toString())
            assertEquals(400, problem?.status)
        }
    }

    @Test
    fun `post request with bad json`() = testApp {
        handleRequest(HttpMethod.Post, "/v1/inntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(jsonMissingFields)
        }.apply {
            assertTrue(requestHandled)
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun ` Errors should return in rfc7807 problem details standard on bad request`() = testApp {
        handleRequest(HttpMethod.Post, "/v1/inntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(jsonMissingFields)
        }.apply {
            assertTrue(requestHandled)
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
            val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
            assertEquals("Klarte ikke å lese inntektsparameterene", problem?.title)
            assertEquals("urn:dp:error:inntekt:parameter", problem?.type.toString())
            assertEquals(400, problem?.status)
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        val env = Environment("", "", "", "")
        withTestApplication({
            (inntektApi(env, inntektskomponentClientMock))
        }) { callback() }
    }
}
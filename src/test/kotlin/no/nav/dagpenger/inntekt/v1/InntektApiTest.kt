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

import no.nav.dagpenger.inntekt.Problem
import no.nav.dagpenger.inntekt.brreg.enhetsregisteret.EnhetsregisteretHttpClient
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.inntektApi
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentRequest

import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentenHttpClientException
import no.nav.dagpenger.inntekt.moshiInstance
import no.nav.dagpenger.inntekt.oppslag.PersonNameHttpClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InntektApiTest {
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

    private val inntektskomponentClientMock: InntektskomponentClient = mockk()
    private val enhetsregisteretHttpClientMock: EnhetsregisteretHttpClient = mockk()
    private val personNameHttpClientMock: PersonNameHttpClient = mockk()
    private val inntektStoreMock: InntektStore = mockk(relaxed = true)
    private val inntektPath = "/v1/inntekt"

    init {
        every { inntektskomponentClientMock.getInntekt(InntektkomponentRequest("1234", YearMonth.of(2015, 11), YearMonth.of(2019, 1))) } returns InntektkomponentResponse(
                emptyList(),
                Aktoer(AktoerType.AKTOER_ID, "1234")
        )
        every {
            inntektskomponentClientMock.getInntekt(InntektkomponentRequest("5678", YearMonth.of(2015, 11), YearMonth.of(2019, 1)))
        } throws InntektskomponentenHttpClientException(400, "Bad request")

        every {
            inntektStoreMock.getInntektId(any())
        } returns null
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
    fun `Get klassifisert inntekt should return 200 ok`() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(validJson)
        }.apply {
            assertTrue(requestHandled)
            Assertions.assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun ` Should respond on unhandled errors and return in rfc7807 problem details standard `() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
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
            assertEquals("Feilet mot inntektskomponenten!", problem?.title)
            assertEquals("urn:dp:error:inntektskomponenten", problem?.type.toString())
            assertEquals(400, problem?.status)
        }
    }

    @Test
    fun ` should forward Http status from inntektskomponenten and return in rfc7807 problem details standard `() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
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
            assertEquals("Feilet mot inntektskomponenten!", problem?.title)
            assertEquals("urn:dp:error:inntektskomponenten", problem?.type.toString())
            assertEquals(400, problem?.status)
        }
    }

    @Test
    fun `post request with bad json`() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(jsonMissingFields)
        }.apply {
            assertTrue(requestHandled)
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun ` Errors should return in rfc7807 problem details standard on bad request`() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(jsonMissingFields)
        }.apply {
            assertTrue(requestHandled)
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
            val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
            assertEquals("Klarte ikke å lese parameterene", problem?.title)
            assertEquals("urn:dp:error:inntekt:parameter", problem?.type.toString())
            assertEquals(400, problem?.status)
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({
            (inntektApi(
                inntektskomponentClientMock,
                inntektStoreMock,
                enhetsregisteretHttpClientMock,
                personNameHttpClientMock))
        }) { callback() }
    }
}
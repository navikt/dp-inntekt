package no.nav.dagpenger.inntekt.v1

import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.string.shouldContain
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.inntekt.AuthApiKeyVerifier
import no.nav.dagpenger.inntekt.Problem
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentRequest
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentenHttpClientException
import no.nav.dagpenger.inntekt.moshiInstance
import no.nav.dagpenger.inntekt.oppslag.OppslagClient
import no.nav.dagpenger.ktor.auth.ApiKeyVerifier
import org.junit.jupiter.api.Test

class KlassifisertInntektApiTest {
    val validJson = """
        {
        	"aktørId": "1234",
            "vedtakId": 1,
            "beregningsDato": "2019-01-08"
        }
        """.trimIndent()

    val validJsonById = """
        {
        	"aktørId": "1234",
            "beregningsDato": "2019-01-08"
        }
        """.trimIndent()

    val jsonMissingFields = """
        {
        	"aktørId": "1234",
        }
        """.trimIndent()

    private val apiKeyVerifier = ApiKeyVerifier("secret")
    private val authApiKeyVerifier = AuthApiKeyVerifier(apiKeyVerifier, listOf("test-client"))
    private val apiKey = apiKeyVerifier.generate("test-client")
    private val inntektskomponentClientMock: InntektskomponentClient = mockk()
    private val oppslagClientMock: OppslagClient = mockk()
    private val inntektStoreMock: InntektStore = mockk(relaxed = true)
    private val inntektPath = "/v1/inntekt"

    init {
        every {
            runBlocking {
                inntektskomponentClientMock.getInntekt(
                    InntektkomponentRequest(
                        "1234",
                        YearMonth.of(2016, 1),
                        YearMonth.of(2018, 12)
                    )
                )
            }
        } returns InntektkomponentResponse(
            emptyList(),
            Aktoer(AktoerType.AKTOER_ID, "1234")
        )
        every {
            runBlocking {
                inntektskomponentClientMock.getInntekt(
                    InntektkomponentRequest(
                        "5678",
                        YearMonth.of(2016, 1),
                        YearMonth.of(2018, 12)
                    )
                )
            }
        } throws InntektskomponentenHttpClientException(400, "Bad request")

        every {
            inntektStoreMock.getInntektId(any())
        } returns null
        every {
            oppslagClientMock.finnNaturligIdent(any())
        } returns "12345678912"
    }

    @Test
    fun ` should be able to Http GET isReady, isAlive and metrics endpoint `() =
        testApp {

            with(handleRequest(HttpMethod.Get, "isAlive")) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            with(handleRequest(HttpMethod.Get, "isReady")) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            with(handleRequest(HttpMethod.Get, "metrics")) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

    @Test
    fun `Get klassifisert inntekt should return 200 ok`() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            setBody(validJson)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `Get klassifisert inntekt by ID should return 200 ok`() = testApp {
        val inntektId = InntektId(ULID().nextULID())

        every {
            runBlocking {
                inntektStoreMock.getInntekt(inntektId)
            }
        } returns StoredInntekt(
            inntektId = inntektId,
            inntekt = InntektkomponentResponse(
                arbeidsInntektMaaned = emptyList(),
                ident = Aktoer(AktoerType.AKTOER_ID, "1234")
            ),
            manueltRedigert = false,
            timestamp = LocalDateTime.now()
        )

        handleRequest(HttpMethod.Post, "$inntektPath/${inntektId.id}") {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            setBody(validJson)
        }.apply {
            assertTrue(requestHandled)
            response.content.shouldContain(inntektId.id)
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `Get klassifisert inntekt by ID should verify that aktorId matched the inntekt`() = testApp {
        val inntektId = InntektId(ULID().nextULID())

        every {
            runBlocking {
                inntektStoreMock.getInntekt(inntektId)
            }
        } returns StoredInntekt(
            inntektId = inntektId,
            inntekt = InntektkomponentResponse(
                arbeidsInntektMaaned = emptyList(),
                ident = Aktoer(AktoerType.AKTOER_ID, "3245")
            ),
            manueltRedigert = false,
            timestamp = LocalDateTime.now()
        )

        handleRequest(HttpMethod.Post, "$inntektPath/${inntektId.id}") {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            setBody(validJson)
        }.apply {
            assertTrue(requestHandled)
            val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
            assertEquals("AktørId i request stemmer ikke med aktørId på inntekten du spør etter.", problem?.title)
            assertEquals("urn:dp:error:inntekt", problem?.type.toString())
            assertEquals(401, problem?.status)
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `Get klassifisert inntekt by invalid ULID should return 400 Bad Request`() = testApp {
        handleRequest(HttpMethod.Post, "$inntektPath/123") {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            setBody(validJsonById)
        }.apply {
            assertTrue(requestHandled)
            val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
            assertEquals("InntektsId må være en gyldig ULID", problem?.title)
            assertEquals("urn:dp:error:inntekt", problem?.type.toString())
            assertEquals(400, problem?.status)
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `Get klassifisert inntekt by non-existing ID should return 404 Not Found`() = testApp {
        val inntektId = InntektId(ULID().nextULID())

        every {
            runBlocking {
                inntektStoreMock.getInntekt(inntektId)
            }
        } throws InntektNotFoundException("Test inntekt not found")

        handleRequest(HttpMethod.Post, "$inntektPath/${inntektId.id}") {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            setBody(validJsonById)
        }.apply {
            assertTrue(requestHandled)
            val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
            assertEquals("Kunne ikke finne inntekt i databasen", problem?.title)
            assertEquals("urn:dp:error:inntekt", problem?.type.toString())
            assertEquals(404, problem?.status)
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun ` Should respond on unhandled errors and return in rfc7807 problem details standard `() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
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
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
            assertEquals("Innhenting av inntekt mot a-inntekt feilet. Prøv igjen senere", problem?.title)
            assertEquals("urn:dp:error:inntektskomponenten", problem?.type.toString())
            assertEquals(400, problem?.status)
        }
    }

    @Test
    fun ` should forward Http status from inntektskomponenten and return in rfc7807 problem details standard `() =
        testApp {
            handleRequest(HttpMethod.Post, inntektPath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader("X-API-KEY", apiKey)
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
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
                assertEquals("Innhenting av inntekt mot a-inntekt feilet. Prøv igjen senere", problem?.title)
                assertEquals("urn:dp:error:inntektskomponenten", problem?.type.toString())
                assertEquals(400, problem?.status)
            }
        }

    @Test
    fun `post request with bad json`() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            setBody(jsonMissingFields)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun ` Errors should return in rfc7807 problem details standard on bad request`() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            setBody(jsonMissingFields)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
            assertEquals("Klarte ikke å lese parameterene", problem?.title)
            assertEquals("urn:dp:error:inntekt:parameter", problem?.type.toString())
            assertEquals(400, problem?.status)
        }
    }

    @Test
    fun `Should get unauthorized without api key header`() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(validJson)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `Should get unauthorized without api wrong api key`() = testApp {
        handleRequest(HttpMethod.Post, inntektPath) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", "blabla")
            setBody(validJson)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                oppslagClient = oppslagClientMock,
                apiAuthApiKeyVerifier = authApiKeyVerifier
            )
        ) { callback() }
    }
}

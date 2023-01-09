package no.nav.dagpenger.inntekt.v1

import de.huxhorn.sulky.ulid.ULID
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import no.bekk.bekkopen.person.FodselsnummerCalculator.getFodselsnummerForDate
import no.nav.dagpenger.inntekt.ApiKeyVerifier
import no.nav.dagpenger.inntekt.AuthApiKeyVerifier
import no.nav.dagpenger.inntekt.BehandlingsInntektsGetter
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.v1.TestApplication.handleAuthenticatedAzureAdRequest
import no.nav.dagpenger.inntekt.v1.TestApplication.mockInntektApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlin.test.assertEquals

internal class InntektRouteSpec {

    private val fnr = getFodselsnummerForDate(Date.from(LocalDate.now().minusYears(20).atStartOfDay(ZoneId.systemDefault()).toInstant())).personnummer
    private val ulid = ULID().nextULID()
    private val aktørId = "1234"
    private val beregningsdato = LocalDate.of(2019, 1, 8)
    private val validJson =
        """
        {
        	"aktørId": "$aktørId",
            "regelkontekst": { "id" : "1", "type" : "vedtak" },
            "beregningsDato": "$beregningsdato"
        }
        """.trimIndent()

    private val validJsonWithVedtakIdAsUlid =
        """
        {
            "aktørId": "$aktørId",
            "regelkontekst": { "id" : "$ulid", "type" : "vedtak" },
            "beregningsDato": "2019-01-08"
        }
        """.trimIndent()

    private val validJsonWithFnr =
        """
        {
            "aktørId": "$aktørId",
            "regelkontekst": { "id" : "$ulid", "type" : "vedtak" },
            "fødselsnummer": "$fnr",
            "beregningsDato": "$beregningsdato"
        }
        """.trimIndent()

    private val inntektParametre = Inntektparametre(
        aktørId = "$aktørId",
        regelkontekst = RegelKontekst("1", "vedtak"),
        beregningsdato = beregningsdato

    )

    private val vedtakIdUlidParametre = Inntektparametre(
        aktørId = aktørId,
        regelkontekst = RegelKontekst(ulid, "vedtak"),
        beregningsdato = beregningsdato
    )

    private val fnrParametre = Inntektparametre(
        aktørId = aktørId,
        regelkontekst = RegelKontekst(ulid, "vedtak"),
        fødselnummer = fnr,
        beregningsdato = beregningsdato
    )

    private val jsonMissingFields =
        """
        {
        	"aktørId": "1234",
        }
        """.trimIndent()

    private val behandlingsInntektsGetterMock: BehandlingsInntektsGetter = spyk(BehandlingsInntektsGetter(mockk(relaxed = true), mockk(relaxed = true)))

    private val spesifisertPath = "/inntekt/spesifisert"
    private val klassifisertPath = "/inntekt/klassifisert"
    private val spesifisertInntektPathV1 = "/v1/$spesifisertPath"
    private val spesifisertInntektPathV2 = "/v2/$spesifisertPath"
    private val klassifisertInntektPathV1 = "/v1/$klassifisertPath"
    private val klassifisertInntektPathV2 = "/v2/$klassifisertPath"

    private val apiKeyVerifier = ApiKeyVerifier("secret")
    private val authApiKeyVerifier = AuthApiKeyVerifier(apiKeyVerifier, listOf("test-client"))
    private val apiKey = apiKeyVerifier.generate("test-client")

    private val callId = "string-ulid"
    private val inntektId = InntektId(ULID().nextULID())
    private val emptyInntekt = InntektkomponentResponse(emptyList(), Aktoer(AktoerType.AKTOER_ID, "1234"))

    private val storedInntekt = StoredInntekt(
        inntektId,
        emptyInntekt,
        false
    )

    init {
        coEvery {
            behandlingsInntektsGetterMock.getBehandlingsInntekt(any(), any())
        } returns storedInntekt
    }

    @ParameterizedTest
    @ValueSource(strings = ["/v2/inntekt/spesifisert", "/v2/inntekt/klassifisert"])
    fun `skal ikke autentisere på v2 hvis ikke auth token er med `(endpoint: String) = testApp {
        handleRequest(HttpMethod.Post, endpoint) {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(validJson)
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/v2/inntekt/spesifisert", "/v2/inntekt/klassifisert"])
    fun `skal autentisere på v2 hvis auth token er med `(endpoint: String) = testApp {
        handleAuthenticatedAzureAdRequest(HttpMethod.Post, endpoint) {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(validJson)
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `Spesifisert inntekt API specification test - Should match json field names and formats`() = testApp {
        handleRequest(HttpMethod.Post, spesifisertInntektPathV1) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            addHeader("X-Request-Id", callId)
            setBody(validJson)
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getSpesifisertInntekt(inntektParametre, callId) }
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getBehandlingsInntekt(inntektParametre, callId) }
        }
    }

    @Test
    fun `Klassifisert inntekt API specification test - Should match json field names and formats`() = testApp {
        handleRequest(HttpMethod.Post, klassifisertInntektPathV1) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            addHeader("X-Request-Id", callId)
            setBody(validJson)
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getBehandlingsInntekt(inntektParametre, callId) }
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getKlassifisertInntekt(inntektParametre, callId) }
        }
    }

    @Test
    fun `Spesifisert Requests with vedtakId as string works and does store data`() = testApp {
        handleRequest(HttpMethod.Post, spesifisertInntektPathV1) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            addHeader("X-Request-Id", callId)
            setBody(validJsonWithVedtakIdAsUlid)
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getBehandlingsInntekt(vedtakIdUlidParametre, callId) }
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getSpesifisertInntekt(vedtakIdUlidParametre, callId) }
        }
    }

    @Test
    fun `Klassifisert Requests with fødselsnummer works and does store data`() = testApp {
        handleRequest(HttpMethod.Post, klassifisertInntektPathV1) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            addHeader("X-Request-Id", callId)
            setBody(validJsonWithFnr)
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getBehandlingsInntekt(fnrParametre, callId) }
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getSpesifisertInntekt(fnrParametre, callId) }
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getKlassifisertInntekt(fnrParametre, callId) }
        }
    }

    @Test
    fun `Klassifisert Requests with vedtakId as string works and does store data`() = testApp {
        handleRequest(HttpMethod.Post, klassifisertInntektPathV1) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            addHeader("X-Request-Id", callId)
            setBody(validJsonWithVedtakIdAsUlid)
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getBehandlingsInntekt(vedtakIdUlidParametre, callId) }
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getSpesifisertInntekt(vedtakIdUlidParametre, callId) }
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getKlassifisertInntekt(vedtakIdUlidParametre, callId) }
        }
    }

    @Test
    fun `Spesifisert Requests with fødselsnummer works and does store data`() = testApp {
        handleRequest(HttpMethod.Post, spesifisertInntektPathV1) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            addHeader("X-Request-Id", callId)
            setBody(validJsonWithFnr)
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getBehandlingsInntekt(fnrParametre, callId) }
            coVerify(exactly = 1) { behandlingsInntektsGetterMock.getSpesifisertInntekt(fnrParametre, callId) }
        }
    }

    @Test
    fun `Spesifisert request fails on post request with missing fields`() = testApp {
        handleRequest(HttpMethod.Post, spesifisertInntektPathV1) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            setBody(jsonMissingFields)
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `Klassifisert request fails on post request with missing fields`() = testApp {
        handleRequest(HttpMethod.Post, spesifisertInntektPathV1) {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader("X-API-KEY", apiKey)
            setBody(jsonMissingFields)
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication(
            mockInntektApi(
                behandlingsInntektsGetter = behandlingsInntektsGetterMock,
                apiAuthApiKeyVerifier = authApiKeyVerifier
            )
        ) { callback() }
    }
}

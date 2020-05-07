package no.nav.dagpenger.inntekt

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.oppslag.OppslagClient
import no.nav.dagpenger.inntekt.v1.mockInntektApi
import no.nav.dagpenger.ktor.auth.ApiKeyVerifier
import org.junit.jupiter.api.Test

class NaisChecksTest {
    private val apiKeyVerifier = ApiKeyVerifier("secret")
    private val authApiKeyVerifier = AuthApiKeyVerifier(apiKeyVerifier, listOf("test-client"))
    private val inntektskomponentClientMock: InntektskomponentClient = mockk()
    private val oppslagClientMock: OppslagClient = mockk()
    private val inntektStoreMock: InntektStore = mockk(
        relaxed = true,
        moreInterfaces = *arrayOf(HealthCheck::class)
    )
    private val inntektStoreMockHealthCheck = inntektStoreMock as HealthCheck

    init {
        every {
            inntektStoreMockHealthCheck.status()
        } returns HealthStatus.DOWN
    }

    @Test
    fun ` should get fault on isAlive endpoint `() {
        testApp {
            with(handleRequest(HttpMethod.Get, "isAlive")) {
                assertEquals(HttpStatusCode.ServiceUnavailable, response.status())
            }
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                oppslagClient = oppslagClientMock,
                apiAuthApiKeyVerifier = authApiKeyVerifier,
                healthChecks = listOf(
                    inntektStoreMockHealthCheck
                )
            )
        ) { callback() }
    }
}

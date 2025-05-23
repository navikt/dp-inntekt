package no.nav.dagpenger.inntekt

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.inntekt.api.v1.TestApplication.mockInntektApi
import no.nav.dagpenger.inntekt.api.v1.TestApplication.withMockAuthServerAndTestApplication
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NaisChecksTest {
    private val inntektskomponentClientMock: InntektskomponentClient = mockk()
    private val personOppslagMock: PersonOppslag = mockk()
    private val inntektStoreMock: InntektStore =
        mockk(
            relaxed = true,
            moreInterfaces = arrayOf(HealthCheck::class),
        )
    private val inntektStoreMockHealthCheck = inntektStoreMock as HealthCheck

    init {
        every {
            inntektStoreMockHealthCheck.status()
        } returns HealthStatus.DOWN
    }

    @Test
    fun ` should get fault on isAlive endpoint `() {
        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
                healthChecks =
                    listOf(
                        inntektStoreMockHealthCheck,
                    ),
            ),
        ) {
            val response = client.get("isAlive")
            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }
    }
}

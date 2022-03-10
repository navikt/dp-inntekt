package no.nav.dagpenger.inntekt.v1

import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.dagpenger.inntekt.v1.TestApplication.mockInntektApi
import org.junit.Test

internal class EnhetsregistrerRouteTest {

    @Test
    fun `hent organisasjon `() {
        val enhetsregisterClient: EnhetsregisterClient = mockk()
        coEvery { enhetsregisterClient.hentEnhet("123456789") } returns "{}"
        withTestApplication(
            mockInntektApi(
                enhetsregisterClient = enhetsregisterClient
            )
        ) {
            handleRequest(HttpMethod.Get, "v1/enhetsregisteret/enhet/123456789") {
            }.apply {
                response.status() shouldBe HttpStatusCode.OK
                response.headers["Cache-Control"] shouldBe "max-age=86400"
            }
        }
    }

    @Test
    fun `hent organisasjon på feil `() {
        val enhetsregisterClient: EnhetsregisterClient = mockk()
        coEvery { enhetsregisterClient.hentEnhet("123456789") } throws RuntimeException("Feilet")
        withTestApplication(
            mockInntektApi(
                enhetsregisterClient = enhetsregisterClient
            )
        ) {
            handleRequest(HttpMethod.Get, "v1/enhetsregisteret/enhet/123456789") {
            }.apply {
                response.status() shouldBe HttpStatusCode.BadGateway
            }
        }
    }
}

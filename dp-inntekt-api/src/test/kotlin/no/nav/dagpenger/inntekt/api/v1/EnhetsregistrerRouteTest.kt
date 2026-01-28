package no.nav.dagpenger.inntekt.api.v1

import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.dagpenger.inntekt.api.v1.TestApplication.mockInntektApi
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import org.junit.jupiter.api.Test

internal class EnhetsregistrerRouteTest {
    @Test
    fun `hent organisasjon `() {
        val enhetsregisterClient: EnhetsregisterClient = mockk()
        coEvery { enhetsregisterClient.hentEnhet("123456789") } returns "{}"
        mockInntektApi(
            enhetsregisterClient = enhetsregisterClient,
        ) {
            val response = it.client.get("v1/enhetsregisteret/enhet/123456789")
            response.status shouldBe HttpStatusCode.OK
            response.headers["Cache-Control"] shouldBe "max-age=86400"
        }
    }

    @Test
    fun `hent organisasjon på feil `() {
        val enhetsregisterClient: EnhetsregisterClient = mockk()
        coEvery { enhetsregisterClient.hentEnhet("123456789") } throws RuntimeException("Feilet")
        mockInntektApi(
            enhetsregisterClient = enhetsregisterClient,
        ) {
            val response = it.client.get("v1/enhetsregisteret/enhet/123456789")
            response.status shouldBe HttpStatusCode.BadGateway
        }
    }
}

package no.nav.dagpenger.inntekt.oppslag.enhetsregister

import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

internal class EnhetsregisterClientTest {
    @Test
    fun ` Hvis orgnummer ikke er underenhet `() =
        runBlocking {
            val response =
                EnhetsregisterClient(
                    "http://localhost/enhetsregisteret",
                    httpClient(
                        engine =
                            MockEngine { request ->
                                request.method shouldBe HttpMethod.Get
                                request.url.toString() shouldBe "http://localhost/enhetsregisteret/api/enheter/123456789"
                                respondOk(
                                    enhetOkResposne,
                                )
                            },
                    ),
                ).hentEnhet("123456789")

            response shouldBe enhetOkResposne
        }

    @Test
    fun ` Hvis orgnummer er underenhet `() =
        runBlocking {
            val response =
                EnhetsregisterClient(
                    "http://localhost/enhetsregisteret",
                    httpClient(
                        engine =
                            MockEngine { request ->
                                when (request.url.toString()) {
                                    "http://localhost/enhetsregisteret/api/enheter/123456789" ->
                                        respondError(
                                            HttpStatusCode.NotFound,
                                        )
                                    "http://localhost/enhetsregisteret/api/underenheter/123456789" ->
                                        respondOk(
                                            enhetOkResposne,
                                        )
                                    else ->
                                        respondError(
                                            HttpStatusCode.InternalServerError,
                                        )
                                }
                            },
                    ),
                ).hentEnhet("123456789")

            response shouldBe enhetOkResposne
        }

    @Language("json")
    private val enhetOkResposne =
        """{
                          "organisasjonsnummer": "123456789",
                          "navn": "FIRMA AS"
                    }"""
}

package no.nav.dagpenger.inntekt.oppslag.pdl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.ContentPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import wiremock.com.google.common.net.HttpHeaders
import kotlin.test.assertNotNull

internal class PdlGraphQLRepositoryTest {

    @Test
    fun `Hent person basert på aktør id `() = runBlocking {
        wireMockServer.addPdlResponse(
            matchingJsonPath("$.variables.ident", EqualToPattern("aktørId")),
            WireMock.okJson(okResponse)
        )

        val pdl = PdlGraphQLRepository(
            PdlGraphQLClientFactory(
                url = "${wireMockServer.baseUrl()}/graphql",
                oidcProvider = { TOKEN }
            )
        )

        val person = pdl.hentPerson("aktørId")

        assertNotNull(person)

        person.fødselsnummer shouldBe "fødselsnummer"
        person.fornavn shouldBe "Donald"
        person.mellomnavn shouldBe "Pres"
        person.etternavn shouldBe "Struts"
        person.sammensattNavn() shouldBe "Struts, Donald Pres"
    }

    @Test
    fun `Feil i respons skal føre til tomt svar `() = runBlocking {
        wireMockServer.addPdlResponse(
            matchingJsonPath("$.variables.ident", EqualToPattern("queryFeilet")),
            WireMock.okJson(error)
        )

        val pdl = PdlGraphQLRepository(
            PdlGraphQLClientFactory(
                url = "${wireMockServer.baseUrl()}/graphql",
                oidcProvider = { TOKEN }
            )
        )

        pdl.hentPerson("queryFeilet") shouldBe null
    }

    @Language("JSON")
    private val okResponse =
        """
        {"data":{"hentIdenter":{"identer": [{"ident": "fødselsnummer", "gruppe": "FOLKEREGISTERIDENT"}]}, "hentPerson": {"navn": [
        {
          "fornavn": "Donald",
          "mellomnavn": "Pres",
          "etternavn": "Struts"
        }
      ]}}}
        """.trimIndent()

    @Language("JSON")
    private val error =
        """
        { "data" : null, "errors": [{"message":  "feilet"}]}
        """.trimIndent()

    companion object {
        private const val TOKEN = "token"
        val wireMockServer by lazy {
            WireMockServer(WireMockConfiguration.options().dynamicPort())
        }

        @BeforeAll
        @JvmStatic
        fun setup() {
            wireMockServer.start()
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            wireMockServer.stop()
        }

        fun WireMockServer.addPdlResponse(requestBody: ContentPattern<*>, response: ResponseDefinitionBuilder) {
            this.addStubMapping(
                WireMock.post(WireMock.urlEqualTo("/graphql"))
                    .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo("application/json"))
                    .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/json"))
                    .withHeader(HttpHeaders.AUTHORIZATION, WireMock.equalTo("Bearer $TOKEN"))
                    .withHeader("TEMA", WireMock.equalTo("DAG"))
                    .withHeader("Nav-Consumer-Token", WireMock.equalTo("Bearer $TOKEN"))
                    .withRequestBody(requestBody)
                    .willReturn(response)
                    .build()
            )
        }
    }
}

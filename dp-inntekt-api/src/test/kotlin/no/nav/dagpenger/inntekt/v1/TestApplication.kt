package no.nav.dagpenger.inntekt.v1

import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.overriding
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.testApplication
import io.mockk.mockk
import io.prometheus.client.CollectorRegistry
import no.nav.dagpenger.inntekt.BehandlingsInntektsGetter
import no.nav.dagpenger.inntekt.Config
import no.nav.dagpenger.inntekt.HealthCheck
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.inntektApi
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.security.mock.oauth2.MockOAuth2Server

@Suppress("ktlint:standard:function-naming")
internal object TestApplication {
    private const val ISSUER_ID = "default"
    val mockOAuth2Server: MockOAuth2Server by lazy {
        MockOAuth2Server().also {
            it.start()
        }
    }
    val testOAuthToken: String by lazy {
        mockOAuth2Server.issueToken(
            issuerId = ISSUER_ID,
            subject = "user",
        ).serialize()
    }

    private fun config(): Configuration {
        return Config.config overriding
            ConfigurationMap(
                mapOf(
                    "AZURE_OPENID_CONFIG_JWKS_URI" to mockOAuth2Server.jwksUrl(ISSUER_ID).toString(),
                    "AZURE_OPENID_CONFIG_ISSUER" to mockOAuth2Server.issuerUrl(ISSUER_ID).toString(),
                    "AZURE_APP_CLIENT_ID" to ISSUER_ID,
                ),
            )
    }

    internal fun mockInntektApi(
        inntektskomponentClient: InntektskomponentClient = mockk(),
        inntektStore: InntektStore = mockk(),
        behandlingsInntektsGetter: BehandlingsInntektsGetter = mockk(),
        personOppslag: PersonOppslag = mockk(),
        enhetsregisterClient: EnhetsregisterClient = mockk(relaxed = true),
        healthChecks: List<HealthCheck> = emptyList(),
    ): Application.() -> Unit =
        fun Application.() {
            inntektApi(
                config = config(),
                inntektskomponentHttpClient = inntektskomponentClient,
                inntektStore = inntektStore,
                behandlingsInntektsGetter = behandlingsInntektsGetter,
                personOppslag = personOppslag,
                enhetsregisterClient = enhetsregisterClient,
                healthChecks = healthChecks,
                collectorRegistry = CollectorRegistry(true),
            )
        }

    internal fun withMockAuthServerAndTestApplication(
        moduleFunction: Application.() -> Unit = mockInntektApi(),
        test: suspend ApplicationTestBuilder.() -> Unit,
    ) {
        return testApplication {
            application(moduleFunction)
            test()
        }
    }

    internal suspend fun ApplicationTestBuilder.autentisert(
        endepunkt: String,
        token: String = testOAuthToken,
        httpMethod: HttpMethod = HttpMethod.Post,
        body: String? = null,
        callId: String? = null,
    ): HttpResponse =
        client.request(endepunkt) {
            this.method = httpMethod
            body?.let {
                this.setBody(TextContent(it, ContentType.Application.Json))
                this.header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            this.header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            this.header(HttpHeaders.Authorization, "Bearer $token")
            callId?.let {
                this.header("X-Request-Id", it)
            }
        }

    internal fun TestApplicationEngine.handleAuthenticatedAzureAdRequest(
        method: HttpMethod,
        uri: String,
        test: TestApplicationRequest.() -> Unit = {},
    ): TestApplicationCall {
        return this.handleRequest(method, uri) {
            addHeader("Authorization", "Bearer $testOAuthToken")
            test()
        }
    }
}

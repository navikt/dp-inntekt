package no.nav.dagpenger.inntekt.v1

import com.auth0.jwk.JwkProvider
import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.overriding
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import io.prometheus.client.CollectorRegistry
import no.nav.dagpenger.inntekt.AuthApiKeyVerifier
import no.nav.dagpenger.inntekt.BehandlingsInntektsGetter
import no.nav.dagpenger.inntekt.Config
import no.nav.dagpenger.inntekt.HealthCheck
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.KronetilleggUttrekk
import no.nav.dagpenger.inntekt.inntektApi
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.security.mock.oauth2.MockOAuth2Server

internal object TestApplication {
    private const val ISSUER_ID = "default"

    val mockOAuth2Server: MockOAuth2Server by lazy {
        MockOAuth2Server().also {
            it.start()
        }
    }

    val testOAuthToken: String by lazy {
        mockOAuth2Server.issueToken(
            issuerId = ISSUER_ID

        ).serialize()
    }

    internal fun config(): Configuration {
        return Config.config overriding ConfigurationMap(
            mapOf(
                "AZURE_OPENID_CONFIG_JWKS_URI" to mockOAuth2Server.jwksUrl(ISSUER_ID).toString(),
                "AZURE_OPENID_CONFIG_ISSUER" to mockOAuth2Server.issuerUrl(ISSUER_ID).toString(),
                "AZURE_APP_CLIENT_ID" to ISSUER_ID
            )
        )
    }

    internal fun mockInntektApi(
        inntektskomponentClient: InntektskomponentClient = mockk(),
        inntektStore: InntektStore = mockk(),
        behandlingsInntektsGetter: BehandlingsInntektsGetter = mockk(),
        personOppslag: PersonOppslag = mockk(),
        apiAuthApiKeyVerifier: AuthApiKeyVerifier = mockk(relaxed = true),
        jwkProvider: JwkProvider = mockk(relaxed = true),
        enhetsregisterClient: EnhetsregisterClient = mockk(relaxed = true),
        kronetilleggUttrekk: KronetilleggUttrekk = mockk(relaxed = true),
        healthChecks: List<HealthCheck> = emptyList()
    ): Application.() -> Unit {
        return fun Application.() {
            inntektApi(
                config = config(),
                inntektskomponentHttpClient = inntektskomponentClient,
                inntektStore = inntektStore,
                behandlingsInntektsGetter = behandlingsInntektsGetter,
                personOppslag = personOppslag,
                apiAuthApiKeyVerifier = apiAuthApiKeyVerifier,
                jwkProvider = jwkProvider,
                enhetsregisterClient = enhetsregisterClient,
                healthChecks = healthChecks,
                kronetilleggUttrekk = kronetilleggUttrekk,
                collectorRegistry = CollectorRegistry(true)
            )
        }
    }

    internal fun <R> withMockAuthServerAndTestApplication(
        moduleFunction: Application.() -> Unit,
        test: TestApplicationEngine.() -> R
    ): R = withTestApplication(moduleFunction, test)

    internal fun TestApplicationEngine.handleAuthenticatedAzureAdRequest(
        method: HttpMethod,
        uri: String,
        test: TestApplicationRequest.() -> Unit = {}
    ): TestApplicationCall {
        return this.handleRequest(method, uri) {
            addHeader("Authorization", "Bearer $testOAuthToken")
            test()
        }
    }
}

package no.nav.dagpenger.inntekt.api.v1

import com.github.navikt.tbd_libs.naisful.NaisEndpoints
import com.github.navikt.tbd_libs.naisful.standardApiModule
import com.github.navikt.tbd_libs.naisful.test.TestContext
import com.github.navikt.tbd_libs.naisful.test.plainTestApp
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
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.mockk.mockk
import kotlinx.coroutines.delay
import no.nav.dagpenger.inntekt.BehandlingsInntektsGetter
import no.nav.dagpenger.inntekt.Config
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.dpbehandling.DpBehandlingKlient
import no.nav.dagpenger.inntekt.inntektApi
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.dagpenger.inntekt.serder.inntektObjectMapper
import no.nav.dagpenger.inntekt.statusPagesConfig
import no.nav.security.mock.oauth2.MockOAuth2Server

@Suppress("ktlint:standard:function-naming")
internal object TestApplication {
    private const val ISSUER_ID = "default"
    const val TEST_OAUTH_USER = "user"

    val mockOAuth2Server: MockOAuth2Server by lazy {
        MockOAuth2Server().also {
            it.start()
        }
    }
    val testOAuthToken: String by lazy {
        mockOAuth2Server
            .issueToken(
                issuerId = ISSUER_ID,
                subject = TEST_OAUTH_USER,
            ).serialize()
    }

    private fun config(): Configuration =
        Config.config overriding
            ConfigurationMap(
                mapOf(
                    "AZURE_OPENID_CONFIG_JWKS_URI" to mockOAuth2Server.jwksUrl(ISSUER_ID).toString(),
                    "AZURE_OPENID_CONFIG_ISSUER" to mockOAuth2Server.issuerUrl(ISSUER_ID).toString(),
                    "AZURE_APP_CLIENT_ID" to ISSUER_ID,
                ),
            )

    internal fun mockInntektApi(
        inntektskomponentClient: InntektskomponentClient = mockk(),
        inntektStore: InntektStore = mockk(),
        behandlingsInntektsGetter: BehandlingsInntektsGetter = mockk(),
        personOppslag: PersonOppslag = mockk(),
        enhetsregisterClient: EnhetsregisterClient = mockk(relaxed = true),
        dpBehandlingKlient: DpBehandlingKlient = mockk(relaxed = true),
        block: suspend (TestContext) -> Unit,
    ) {
        withMockAuthServerAndTestApplication({
            inntektApi(
                config = config(),
                inntektskomponentHttpClient = inntektskomponentClient,
                inntektStore = inntektStore,
                behandlingsInntektsGetter = behandlingsInntektsGetter,
                personOppslag = personOppslag,
                enhetsregisterClient = enhetsregisterClient,
                dpBehandlingKlient = dpBehandlingKlient,
            )
        }) {
            block(this)
        }
    }

    private fun withMockAuthServerAndTestApplication(
        moduleFunction: Application.() -> Unit,
        test: suspend TestContext.() -> Unit,
    ) {
        val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

        return plainTestApp(
            testApplicationModule = {
                standardApiModule(
                    meterRegistry = meterRegistry,
                    objectMapper = inntektObjectMapper,
                    callLogger = environment.log,
                    naisEndpoints = NaisEndpoints.Default,
                    callIdHeaderName = HttpHeaders.XRequestId,
                    preStopHook = { delay(5000) },
                    statusPagesConfig = { statusPagesConfig() },
                )
                moduleFunction()
            },
            testClientObjectMapper = inntektObjectMapper,
        ) {
            test()
        }
    }

    internal suspend fun TestContext.autentisert(
        httpMethod: HttpMethod = HttpMethod.Post,
        endepunkt: String,
        body: String? = null,
        token: String = testOAuthToken,
        callId: String? = null,
    ): HttpResponse =
        client.request(endepunkt) {
            this.method = httpMethod
            body?.let { this.setBody(TextContent(it, ContentType.Application.Json)) }
            callId?.let {
                this.header(HttpHeaders.XRequestId, it)
            }
            this.header(HttpHeaders.Authorization, "Bearer $token")
            this.header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            this.header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
}

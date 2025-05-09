package no.nav.dagpenger.inntekt.inntektskomponenten.v1

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.okForContentType
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.YearMonth
import kotlin.test.assertEquals

@Suppress("ktlint:standard:max-line-length")
internal class InntektskomponentHttpClientTest {
    companion object {
        val dummyTokenProvider = { "oidc" }
        val server: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun start() {
            server.start()
        }

        @AfterAll
        @JvmStatic
        fun stop() {
            server.stop()
        }
    }

    @BeforeEach
    fun configure() {
        configureFor(server.port())
    }

    @Test
    fun `fetch uklassifisert inntekt on 200 ok and measure latency metrics`() {
        val body =
            InntektskomponentHttpClientTest::class.java
                .getResource("/test-data/example-inntekt-payload.json")?.readText()

        stubFor(
            post(urlEqualTo("/v1/hentinntektliste"))
                .withHeader("Authorization", authorizationBearer)
                .withHeader("Nav-Consumer-Id", dpInntektApi)
                .withHeader("Nav-Call-Id", AnythingPattern())
                .willReturn(
                    okForContentType("application/json", body),
                ),
        )

        val inntektskomponentClient =
            InntektkomponentKtorClient(
                server.url("/v1/hentinntektliste"),
                dummyTokenProvider,
            )

        val hentInntektListeResponse =
            runBlocking {
                inntektskomponentClient.getInntekt(
                    InntektkomponentRequest(
                        aktørId = "",
                        fødselsnummer = "",
                        månedFom = YearMonth.of(2017, 3),
                        månedTom = YearMonth.of(2019, 1),
                    ),
                )
            }

        assertEquals("99999999999", hentInntektListeResponse.ident.identifikator)
    }

    @Test
    fun `fetch uklassifisert inntekt with spesielleinntjeningsforhold 200 ok`() {
        val body =
            InntektskomponentHttpClientTest::class.java
                .getResource("/test-data/example-inntekt-spesielleinntjeningsforhold.json").readText()

        stubFor(
            post(urlEqualTo("/v1/hentinntektliste"))
                .withHeader("Authorization", authorizationBearer)
                .withHeader("Nav-Consumer-Id", dpInntektApi)
                .withHeader("Nav-Call-Id", AnythingPattern())
                .willReturn(
                    okForContentType("application/json", body),
                ),
        )

        val inntektskomponentClient =
            InntektkomponentKtorClient(
                server.url("/v1/hentinntektliste"),
                dummyTokenProvider,
            )

        val hentInntektListeResponse =
            runBlocking {
                inntektskomponentClient.getInntekt(
                    InntektkomponentRequest(
                        aktørId = "",
                        fødselsnummer = "",
                        månedFom = YearMonth.of(2017, 3),
                        månedTom = YearMonth.of(2019, 1),
                    ),
                )
            }

        assertEquals("8888888888", hentInntektListeResponse.ident.identifikator)
        assertEquals(
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
            hentInntektListeResponse.arbeidsInntektMaaned?.first()?.arbeidsInntektInformasjon?.inntektListe?.first()?.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.spesielleInntjeningsforhold,
        )
    }

    @Test
    fun `fetch uklassifisert inntekt on 500 server error`() {
        stubFor(
            post(urlEqualTo("/v1/hentinntektliste"))
                .withHeader("Authorization", authorizationBearer)
                .withHeader("Nav-Consumer-Id", dpInntektApi)
                .withHeader("Nav-Call-Id", AnythingPattern())
                .willReturn(
                    WireMock.serverError(),
                ),
        )

        val inntektskomponentClient =
            InntektkomponentKtorClient(
                server.url("/v1/hentinntektliste"),
                dummyTokenProvider,
            )

        val result =
            runCatching {
                runBlocking {
                    inntektskomponentClient.getInntekt(
                        InntektkomponentRequest(
                            aktørId = "",
                            fødselsnummer = "",
                            månedFom = YearMonth.of(2017, 3),
                            månedTom = YearMonth.of(2019, 1),
                        ),
                    )
                }
            }

        result.isFailure.shouldBeTrue()
        result.shouldBeClientException<InntektskomponentenHttpClientException>(
            status = 500,
        )
    }

    @Test
    fun `fetch uklassifisert inntekt with timeout`() {
        stubFor(
            post(urlEqualTo("/v1/hentinntektliste"))
                .withHeader("Authorization", authorizationBearer)
                .withHeader("Nav-Consumer-Id", dpInntektApi)
                .withHeader("Nav-Call-Id", AnythingPattern())
                .willReturn(
                    WireMock.serviceUnavailable().withFixedDelay(500),
                ),
        )

        val inntektskomponentClient =
            InntektkomponentKtorClient(
                hentInntektlisteUrl = server.url("/v1/hentinntektliste"),
                timeouts = InntektskomponentClient.ConnectionTimeout(readTimeout = Duration.ofMillis(5)),
                azureAdTokenProvider = dummyTokenProvider,
            )

        val result =
            runCatching {
                runBlocking {
                    inntektskomponentClient.getInntekt(
                        InntektkomponentRequest(
                            aktørId = "",
                            fødselsnummer = "",
                            månedFom = YearMonth.of(2017, 3),
                            månedTom = YearMonth.of(2019, 1),
                        ),
                    )
                }
            }

        result.isFailure.shouldBeTrue()
        result.shouldBeClientException<InntektskomponentenHttpClientException>(
            status = 500,
            message = "Tidsavbrudd mot inntektskomponenten.",
        )
    }

    @Test
    fun `fetch uklassifisert inntekt on 500 server error with body`() {
        stubFor(
            post(urlEqualTo("/v1/hentinntektliste"))
                .withHeader("Authorization", authorizationBearer)
                .withHeader("Nav-Consumer-Id", dpInntektApi)
                .withHeader("Nav-Call-Id", AnythingPattern())
                .willReturn(
                    WireMock.serverError().withBody(errorFromInntekskomponenten),
                ),
        )

        val inntektskomponentClient =
            InntektkomponentKtorClient(
                server.url("/v1/hentinntektliste"),
                dummyTokenProvider,
            )

        val result =
            runCatching {
                runBlocking {
                    inntektskomponentClient.getInntekt(
                        InntektkomponentRequest(
                            aktørId = "",
                            fødselsnummer = "",
                            månedFom = YearMonth.of(2017, 3),
                            månedTom = YearMonth.of(2019, 1),
                        ),
                    )
                }
            }

        result.isFailure.shouldBeTrue()
        result.shouldBeClientException<InntektskomponentenHttpClientException>(
            status = 500,
            message = "Failed to fetch inntekt. Problem message: Feil i filtrering: En feil oppstod i filteret DagpengerGrunnlagA-Inntekt, Regel no.nav.inntektskomponenten.filter.regler.dagpenger.DagpengerHovedregel støtter ikke inntekter av type no.nav.inntektskomponenten.domain.Loennsinntekt",
            detail = "Feil i filtrering: En feil oppstod i filteret DagpengerGrunnlagA-Inntekt, Regel no.nav.inntektskomponenten.filter.regler.dagpenger.DagpengerHovedregel støtter ikke inntekter av type no.nav.inntektskomponenten.domain.Loennsinntekt",
        )
    }

    private val dpInntektApi = EqualToPattern("dp-inntekt-api")

    private val authorizationBearer = EqualToPattern("Bearer oidc")

    private val errorFromInntekskomponenten =
        """
        {
          "timestamp": "2019-06-12T13:59:13.233+0200",
          "status": "500",
          "error": "Internal Server Error",
          "message": "Feil i filtrering: En feil oppstod i filteret DagpengerGrunnlagA-Inntekt, Regel no.nav.inntektskomponenten.filter.regler.dagpenger.DagpengerHovedregel støtter ikke inntekter av type no.nav.inntektskomponenten.domain.Loennsinntekt",
          "path": "/hentinntektliste"
        }
        """.trimIndent()
}

private inline fun <reified T : InntektskomponentenHttpClientException> Result<InntektkomponentResponse>.shouldBeClientException(
    status: Int? = null,
    message: String? = null,
    detail: String? = null,
) {
    val exception = exceptionOrNull() as T
    status?.let { exception.status shouldBe status }
    message?.let { exception.message shouldBe message }
    detail?.let { exception.detail shouldBe detail }
}

package no.nav.dagpenger.inntekt.inntektskomponenten.v1

import de.huxhorn.sulky.ulid.ULID
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.JacksonConverter
import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.core.metrics.Summary
import no.nav.dagpenger.inntekt.serder.inntektObjectMapper
import kotlin.time.Duration.Companion.seconds

private val logg = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall")
private val ulid = ULID()
const val INNTEKTSKOMPONENT_CLIENT_SECONDS_METRICNAME = "inntektskomponent_client_seconds"
private val clientLatencyStats: Summary =
    Summary
        .builder()
        .name(INNTEKTSKOMPONENT_CLIENT_SECONDS_METRICNAME)
        .quantile(0.5, 0.05) // Add 50th percentile (= median) with 5% tolerated error
        .quantile(0.9, 0.01) // Add 90th percentile with 1% tolerated error
        .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
        .help("Latency arena-adapter regel client, in seconds")
        .register()
const val INNTEKTSKOMPONENT_FETCH_ERROR = "inntektskomponent_fetch_error"
private val clientFetchErrors =
    Counter
        .builder()
        .name(INNTEKTSKOMPONENT_FETCH_ERROR)
        .help("Number of times fetching form inntektskomponenten has failed")
        .register()
const val INNTEKTSKOMPONENT_STATUS_CODES = "inntektskomponent_status_codes"
private val inntektskomponentStatusCodesCounter =
    Counter
        .builder()
        .name(INNTEKTSKOMPONENT_STATUS_CODES)
        .help("Number of times inntektskomponenten has returned a specific status code")
        .labelNames("status_code")
        .register()

internal class InntektkomponentKtorClient(
    private val hentInntektlisteUrl: String,
    private val azureAdTokenProvider: () -> String,
    private val timeouts: InntektskomponentClient.ConnectionTimeout = InntektskomponentClient.ConnectionTimeout(),
    engine: HttpClientEngine =
        CIO.create {
            requestTimeout = 30.seconds.inWholeMilliseconds
        },
) : InntektskomponentClient {
    private val httpClient =
        HttpClient(engine) {
            expectSuccess = true
            install(Logging) {
                level = LogLevel.INFO
            }
            install(HttpTimeout) {
                connectTimeoutMillis = timeouts.connectionTimeout.toMillis()
                requestTimeoutMillis = timeouts.readTimeout.toMillis()
                socketTimeoutMillis = timeouts.connectionTimeout.toMillis()
            }
            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(inntektObjectMapper))
            }
            defaultRequest {
                header("Nav-Consumer-Id", "dp-inntekt-api")
            }
        }

    override suspend fun getInntekt(
        request: InntektkomponentRequest,
        callId: String?,
    ): InntektkomponentResponse {
        val requestBody = request.tilInntektListeRequest()

        val externalCallId = callId ?: ulid.nextULID()
        withLoggingContext(mapOf("callId" to externalCallId)) {
            val timer = clientLatencyStats.startTimer()
            val response =
                try {
                    httpClient.post(urlString = hentInntektlisteUrl) {
                        header("Nav-Call-Id", externalCallId)
                        header(HttpHeaders.ContentType, ContentType.Application.Json)
                        header(HttpHeaders.Authorization, "Bearer ${azureAdTokenProvider()}")
                        setBody(requestBody)
                    }
                } catch (error: ServerResponseException) {
                    val statusKode = error.response.status.value
                    inntektskomponentStatusCodesCounter.labelValues(statusKode.toString()).inc()
                    clientFetchErrors.inc()
                    val feilmelding =
                        kotlin
                            .runCatching {
                                inntektObjectMapper.readTree(error.response.bodyAsText()).get("message").asText()
                            }.getOrElse { error.message }
                    throw InntektskomponentenHttpClientException(
                        statusKode,
                        "Failed to fetch inntekt. Problem message: $feilmelding",
                        feilmelding,
                    ).also {
                        logg.error(it) { it }
                        sikkerLogg.error(it) { "Oppslag mot inntektskomponenten feilet. Request=$requestBody" }
                    }
                } catch (timeout: HttpRequestTimeoutException) {
                    val detail = "Tidsavbrudd mot inntektskomponenten. Brukte ${timer.observeDuration().seconds}"
                    clientFetchErrors.inc()
                    logg.error(timeout) { detail }
                    throw InntektskomponentenHttpClientException(
                        500,
                        "Tidsavbrudd mot inntektskomponenten.",
                        detail,
                    )
                } finally {
                    timer.observeDuration()
                }
            inntektskomponentStatusCodesCounter.labelValues(response.status.value.toString()).inc()

            return response.body()
        }
    }

    private fun InntektkomponentRequest.tilInntektListeRequest() =
        HentInntektListeRequest(
            "DagpengerGrunnlagA-Inntekt",
            "Dagpenger",
            Aktoer(AktoerType.AKTOER_ID, this.aktørId),
            this.månedFom,
            this.månedTom,
        )
}

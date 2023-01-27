package no.nav.dagpenger.inntekt.inntektskomponenten.v1

import com.github.kittinunf.fuel.core.awaitResponseResult
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import de.huxhorn.sulky.ulid.ULID
import io.prometheus.client.Counter
import io.prometheus.client.Summary
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.inntekt.moshiInstance
import no.nav.dagpenger.oidc.OidcClient
import java.time.YearMonth

private val logg = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall")
private val jsonResponseAdapter = moshiInstance.adapter(InntektkomponentResponse::class.java)
private val jsonRequestRequestAdapter = moshiInstance.adapter(HentInntektListeRequest::class.java)
private val jsonMapAdapter = moshiInstance.adapter(Map::class.java)
private val ulid = ULID()
const val INNTEKTSKOMPONENT_CLIENT_SECONDS_METRICNAME = "inntektskomponent_client_seconds"
private val clientLatencyStats: Summary = Summary.build()
    .name(INNTEKTSKOMPONENT_CLIENT_SECONDS_METRICNAME)
    .quantile(0.5, 0.05) // Add 50th percentile (= median) with 5% tolerated error
    .quantile(0.9, 0.01) // Add 90th percentile with 1% tolerated error
    .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
    .help("Latency arena-adapter regel client, in seconds")
    .register()
const val INNTEKTSKOMPONENT_FETCH_ERROR = "inntektskomponent_fetch_error"
private val clientFetchErrors = Counter.build()
    .name(INNTEKTSKOMPONENT_FETCH_ERROR)
    .help("Number of times fetching form inntektskomponenten has failed")
    .register()
const val INNTEKTSKOMPONENT_STATUS_CODES = "inntektskomponent_status_codes"
private val inntektskomponentStatusCodesCounter = Counter.build()
    .name(INNTEKTSKOMPONENT_STATUS_CODES)
    .help("Number of times inntektskomponenten has returned a specific status code")
    .labelNames("status_code")
    .register()

class InntektskomponentHttpClient(
    private val hentInntektlisteUrl: String,
    private val oidcClient: OidcClient
) : InntektskomponentClient {
    override suspend fun getInntekt(
        request: InntektkomponentRequest,
        timeouts: InntektskomponentClient.ConnectionTimeout,
        callId: String?
    ): InntektkomponentResponse {
        val requestBody = HentInntektListeRequest(
            "DagpengerGrunnlagA-Inntekt",
            "Dagpenger",
            Aktoer(AktoerType.AKTOER_ID, request.aktørId),
            request.månedFom,
            request.månedTom
        )
        val jsonBody = jsonRequestRequestAdapter.toJson(requestBody)
        val timer = clientLatencyStats.startTimer()
        val externalCallId = callId ?: ulid.nextULID()
        withLoggingContext(
            "callId" to externalCallId
        ) {
            logg.info("Fetching new inntekt for ${request.copy(fødselsnummer = "<REDACTED>")}")

            try {
                val (_, response, result) = with(hentInntektlisteUrl.httpPost()) {
                    timeout(timeouts.connectionTimeout.toMillis().toInt())
                    timeoutRead(timeouts.readTimeout.toMillis().toInt())

                    authentication().bearer(oidcClient.oidcToken().access_token)
                    header("Nav-Consumer-Id" to "dp-inntekt-api")
                    header("Nav-Call-Id" to externalCallId)
                    body(jsonBody)
                    awaitResponseResult(moshiDeserializerOf(jsonResponseAdapter))
                }

                inntektskomponentStatusCodesCounter.labels(response.statusCode.toString()).inc()

                return result.fold(
                    {
                        it
                    },
                    { error ->
                        val resp = error.response.body().asString("application/json")
                        val detail = runCatching {
                            jsonMapAdapter.fromJson(resp)
                        }.let {
                            it.getOrNull()?.get("message")?.toString() ?: error.message
                        }

                        clientFetchErrors.inc()

                        throw InntektskomponentenHttpClientException(
                            if (response.statusCode == -1) 500 else response.statusCode, // we did not get a response status code, ie timeout/network issues
                            "Failed to fetch inntekt. Status code ${response.statusCode}. Response message: ${response.responseMessage}. Problem message: $detail",
                            detail
                        ).also {
                            logg.error(it) { it }
                            sikkerLogg.error(it) { "Oppslag mot inntektskomponenten feilet. Request=$requestBody" }
                        }
                    }
                )
            } finally {
                timer.observeDuration()
            }
        }
    }
}

data class HentInntektListeRequest(
    val ainntektsfilter: String,
    val formaal: String,
    val ident: Aktoer,
    val maanedFom: YearMonth,
    val maanedTom: YearMonth
)

class InntektskomponentenHttpClientException(
    val status: Int,
    override val message: String,
    val detail: String? = null
) : RuntimeException(message)

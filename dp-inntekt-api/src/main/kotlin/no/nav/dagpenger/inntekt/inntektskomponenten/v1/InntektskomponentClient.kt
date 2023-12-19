package no.nav.dagpenger.inntekt.inntektskomponenten.v1

import java.time.Duration
import java.time.YearMonth

interface InntektskomponentClient {
    suspend fun getInntekt(
        request: InntektkomponentRequest,
        callId: String? = null,
    ): InntektkomponentResponse

    data class ConnectionTimeout(
        val connectionTimeout: Duration = Duration.ofSeconds(15),
        val readTimeout: Duration = Duration.ofSeconds(15),
    )
}

data class HentInntektListeRequest(
    val ainntektsfilter: String,
    val formaal: String,
    val ident: Aktoer,
    val maanedFom: YearMonth,
    val maanedTom: YearMonth,
)

class InntektskomponentenHttpClientException(
    val status: Int,
    override val message: String,
    val detail: String? = null,
) : RuntimeException(message)

package no.nav.dagpenger.inntekt.inntektskomponenten.v1

import java.time.Duration

interface InntektskomponentClient {
    suspend fun getInntekt(
        request: InntektkomponentRequest,
        timeouts: ConnectionTimeout = ConnectionTimeout(),
        callId: String? = null
    ): InntektkomponentResponse

    data class ConnectionTimeout(
        val connectionTimeout: Duration = Duration.ofSeconds(15),
        val readTimeout: Duration = Duration.ofSeconds(15)
    )
}

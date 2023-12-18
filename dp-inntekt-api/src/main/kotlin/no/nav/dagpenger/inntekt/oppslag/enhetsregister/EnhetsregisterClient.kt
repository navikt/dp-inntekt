package no.nav.dagpenger.inntekt.oppslag.enhetsregister

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector
import java.time.Duration

class EnhetsregisterClient(
    private val baseUrl: String,
    private val httpClient: HttpClient,
) {
    suspend fun hentEnhet(orgnummer: String): String {
        return withContext(Dispatchers.IO) {
            try {
                httpClient.get("$baseUrl/api/enheter/$orgnummer").body()
            } catch (e: ClientRequestException) {
                when (e.response.status.value) {
                    404 -> httpClient.get("$baseUrl/api/underenheter/$orgnummer").body()
                    else -> throw e
                }
            }
        }
    }
}

internal fun httpClient(
    engine: HttpClientEngine = Apache.create { customizeClient { setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault())) } },
): HttpClient {
    return HttpClient(engine) {
        expectSuccess = true
        install(HttpTimeout) {
            connectTimeoutMillis = Duration.ofSeconds(5).toMillis()
            requestTimeoutMillis = Duration.ofSeconds(15).toMillis()
            socketTimeoutMillis = Duration.ofSeconds(15).toMillis()
        }

        install(Logging) {
            level = LogLevel.INFO
        }
    }
}

package no.nav.dagpenger.inntekt.dpbehandling

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import java.util.UUID

class DpBehandlingKlient(
    val hentOboTokenForDpBehandling: (token: String) -> String,
    val dpBehandlingBaseUrl: String,
    val httpKlient: HttpClient,
) {
    fun rekjørBehandling(
        ident: String,
        behandlingId: UUID,
        opplysningId: UUID,
        token: String,
    ) {
        sikkerlogg.info {
            "Rekjør behandling i dp-behandling" +
                "BehandlingId: $behandlingId " +
                "OpplysningId: $opplysningId "
        }

        val oboToken = hentOboTokenForDpBehandling(token)

        val requestBody =
            RekjørBehandlingDTO(
                ident = ident,
                opplysninger = listOf(opplysningId.toString()),
            )

        runBlocking {
            val response: HttpResponse =
                httpKlient.post("$dpBehandlingBaseUrl/behandling/$behandlingId/rekjor") {
                    accept(ContentType.Application.Json)
                    header("Authorization", "Bearer $oboToken")
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

            check(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                "Feil ved rekjøring av behandling i DP behandling. " +
                    "Statuskode: ${response.status} " +
                    "BehandlingId: $behandlingId " +
                    "OpplysningId: $opplysningId"
            }
        }
    }

    private companion object {
        private val sikkerlogg = KotlinLogging.logger("tjenestekall.dpbehandling")
    }
}

data class RekjørBehandlingDTO(
    val ident: String,
    val opplysninger: List<String>,
)

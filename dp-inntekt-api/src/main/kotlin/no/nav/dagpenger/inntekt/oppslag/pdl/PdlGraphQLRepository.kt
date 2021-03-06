package no.nav.dagpenger.inntekt.oppslag.pdl

import com.expediagroup.graphql.client.jackson.GraphQLClientJacksonSerializer
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.oppslag.Person
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.pdl.HentPerson
import no.nav.pdl.hentperson.Navn
import java.net.URL
import java.util.UUID

private val log = KotlinLogging.logger { }
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

class PdlGraphQLRepository(
    private val client: GraphQLKtorClient
) : PersonOppslag {

    override suspend fun hentPerson(aktørId: String): Person? {
        val query = HentPerson(HentPerson.Variables(ident = aktørId))
        val result = client.execute(query)

        return if (result.errors?.isNotEmpty() == true) {
            log.error { "Feil i GraphQL-responsen: ${result.errors}" }
            null
        } else {
            result.toPerson()
        }
    }

    private fun GraphQLClientResponse<HentPerson.Result>.toPerson(): Person? {
        val navn: Navn? = data?.hentPerson?.navn?.firstOrNull()
        val fødselsnummer = data?.hentIdenter?.identer?.firstOrNull()?.ident
        return fødselsnummer?.let { fnr ->
            navn?.let { navn ->
                Person(
                    fødselsnummer = fnr,
                    etternavn = navn.etternavn,
                    mellomnavn = navn.mellomnavn,
                    fornavn = navn.fornavn
                )
            }
        }
    }
}

fun PdlGraphQLClientFactory(
    url: String,
    oidcProvider: () -> String
): GraphQLKtorClient {
    val client = HttpClient(engineFactory = CIO) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = sikkerlogg.info { message }
            }

            level = LogLevel.HEADERS
        }

        defaultRequest {
            oidcProvider().also {
                header(HttpHeaders.Authorization, "Bearer $it")
                header("Nav-Consumer-Token", "Bearer $it")
            }

            header(HttpHeaders.UserAgent, "dp-inntekt-api")
            header(HttpHeaders.Accept, "application/json")
            header("Tema", "DAG")
            header("Nav-Call-Id", UUID.randomUUID())
        }
    }

    return GraphQLKtorClient(url = URL(url), httpClient = client, serializer = GraphQLClientJacksonSerializer())
}

package no.nav.dagpenger.inntekt.oppslag.pdl

import com.expediagroup.graphql.client.jackson.GraphQLClientJacksonSerializer
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.oppslag.Person
import no.nav.dagpenger.inntekt.oppslag.PersonNotFoundException
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.pdl.HentPerson
import no.nav.pdl.enums.IdentGruppe
import no.nav.pdl.hentperson.Navn
import java.net.URL
import java.util.UUID

private val log = KotlinLogging.logger { }
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

class PdlGraphQLRepository(
    private val client: GraphQLKtorClient,
) : PersonOppslag {
    override suspend fun hentPerson(ident: String): Person {
        val query = HentPerson(HentPerson.Variables(ident = ident))
        val result = client.execute(query)

        return if (result.errors?.isNotEmpty() == true) {
            log.error { "Feil i GraphQL-responsen: ${result.errors}" }
            throw PersonNotFoundException(ident = ident)
        } else {
            result.toPerson()?.also {
                log.debug { "Fikk hentet PDL person" }
            } ?: throw PersonNotFoundException(ident = ident, msg = "Feil ved parsing av Person json")
        }
    }

    private fun GraphQLClientResponse<HentPerson.Result>.toPerson(): Person? {
        val navn: Navn? = data?.hentPerson?.navn?.firstOrNull()
        val fødselsnummer =
            data?.hentIdenter?.identer?.firstOrNull { it.gruppe == IdentGruppe.FOLKEREGISTERIDENT }?.ident
        val aktørId = data?.hentIdenter?.identer?.firstOrNull { it.gruppe == IdentGruppe.AKTORID }?.ident
        return fødselsnummer?.let {
            navn?.let {
                aktørId?.let {
                    Person(
                        fødselsnummer = fødselsnummer,
                        aktørId = aktørId,
                        etternavn = navn.etternavn,
                        mellomnavn = navn.mellomnavn,
                        fornavn = navn.fornavn,
                    )
                }
            }
        }
    }
}

fun pdlGraphQLClientFactory(
    url: String,
    oidcProvider: () -> String,
): GraphQLKtorClient {
    val client =
        HttpClient(engineFactory = CIO) {
            install(Logging) {
                logger =
                    object : Logger {
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

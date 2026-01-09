package no.nav.dagpenger.inntekt.api.v3

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import java.time.LocalDate

fun Route.inntektId(
    inntektStore: InntektStore,
    personOppslag: PersonOppslag,
) {
    authenticate("azure") {
        route("/inntektId/{aktørId}/{kontekstType}/{kontekstId}/{beregningsDato}") {
            get {
                val aktørId = call.parameters["aktørId"]!!
                val kontekstId = call.parameters["kontekstId"]!!
                val kontekstType = call.parameters["kontekstType"]!!
                val beregningsDato = LocalDate.parse(call.parameters["beregningsDato"]!!)
                val person = personOppslag.hentPerson(aktørId)

                inntektStore
                    .getInntektId(
                        Inntektparametre(
                            aktørId = person.aktørId,
                            fødselsnummer = person.fødselsnummer,
                            regelkontekst = RegelKontekst(kontekstId, kontekstType),
                            beregningsdato = beregningsDato,
                        ),
                    )?.let {
                        call.respond(HttpStatusCode.OK, it)
                    } ?: throw RuntimeException("Klarte ikke finne inntektId for Arena-parametere")
            }
        }
    }
}

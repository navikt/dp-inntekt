package no.nav.dagpenger.inntekt.v1

import com.auth0.jwt.exceptions.JWTDecodeException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.prometheus.metrics.core.metrics.Counter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.ManueltRedigert
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.db.StoreInntektCommand
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentRequest
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.mapping.GUIInntekt
import no.nav.dagpenger.inntekt.mapping.Inntektsmottaker
import no.nav.dagpenger.inntekt.mapping.Organisasjon
import no.nav.dagpenger.inntekt.mapping.dataGrunnlagKlassifiseringToVerdikode
import no.nav.dagpenger.inntekt.mapping.mapToDetachedInntekt
import no.nav.dagpenger.inntekt.mapping.mapToFrontend
import no.nav.dagpenger.inntekt.mapping.mapToGUIInntekt
import no.nav.dagpenger.inntekt.mapping.mapToStoredInntekt
import no.nav.dagpenger.inntekt.oppslag.Person
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import no.nav.dagpenger.inntekt.v1.models.InntekterDto
import no.nav.dagpenger.inntekt.v1.models.mapToStoredInntekt
import java.time.LocalDate
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}
const val INNTEKT_KORRIGERING = "inntekt_korrigering"
private val inntektKorrigeringCounter =
    Counter
        .builder()
        .name(INNTEKT_KORRIGERING)
        .help("Antall ganger saksbehandler har korrigert inntekter")
        .register()
const val INNTEKT_OPPFRISKING = "inntekt_oppfrisking"
private val inntektOppfriskingCounter =
    Counter
        .builder()
        .name(INNTEKT_OPPFRISKING)
        .help("Antall ganger saksbehandler har oppdatert inntekter")
        .register()
const val INNTEKT_OPPFRISKING_BRUKT = "inntekt_oppfrisking_brukt"
private val inntektOppfriskingBruktCounter =
    Counter
        .builder()
        .name(INNTEKT_OPPFRISKING_BRUKT)
        .help("Antall ganger saksbehandler har brukt oppdaterte inntekter")
        .register()

fun Route.uklassifisertInntekt(
    inntektskomponentClient: InntektskomponentClient,
    inntektStore: InntektStore,
    personOppslag: PersonOppslag,
    enhetsregisterClient: EnhetsregisterClient,
    coroutineContext: CoroutineContext = Dispatchers.IO,
) {
    authenticate("azure") {
        route("/uklassifisert/{aktørId}/{kontekstType}/{kontekstId}/{beregningsDato}") {
            get {
                withContext(coroutineContext) {
                    call.withInntektRequest("GET /uklassifisert/") {
                        val person = personOppslag.hentPerson(this.aktørId)
                        inntektStore
                            .getInntektId(
                                Inntektparametre(
                                    aktørId = person.aktørId,
                                    fødselsnummer = person.fødselsnummer,
                                    regelkontekst = RegelKontekst(this.kontekstId, this.kontekstType),
                                    beregningsdato = this.beregningsDato,
                                ),
                            )?.let {
                                inntektStore.getInntekt(it)
                            }?.let {
                                val inntektsmottaker = Inntektsmottaker(person.fødselsnummer, person.sammensattNavn())
                                mapToGUIInntekt(it, Opptjeningsperiode(this.beregningsDato), inntektsmottaker)
                            }?.let {
                                call.respond(HttpStatusCode.OK, it)
                            } ?: throw InntektNotFoundException("Inntekt with for $this not found.")
                    }
                }
            }
            post {
                withContext(coroutineContext) {
                    call.withInntektRequest("POST /uklassifisert/") {
                        val person = personOppslag.hentPerson(this.aktørId)
                        val guiInntekt = call.receive<GUIInntekt>()
                        mapToStoredInntekt(guiInntekt)
                            .let {
                                inntektStore.storeInntekt(
                                    StoreInntektCommand(
                                        inntektparametre =
                                            Inntektparametre(
                                                aktørId = person.aktørId,
                                                fødselsnummer = person.fødselsnummer,
                                                regelkontekst = RegelKontekst(this.kontekstId, this.kontekstType),
                                                beregningsdato = this.beregningsDato,
                                            ),
                                        inntekt = it.inntekt,
                                        manueltRedigert =
                                            ManueltRedigert.from(
                                                guiInntekt.redigertAvSaksbehandler,
                                                call.getSubject(),
                                            ),
                                    ),
                                )
                            }.let {
                                call.respond(
                                    HttpStatusCode.OK,
                                    mapToGUIInntekt(
                                        it,
                                        Opptjeningsperiode(this.beregningsDato),
                                        guiInntekt.inntektsmottaker,
                                    ),
                                )
                            }.also {
                                inntektKorrigeringCounter.inc()
                            }
                    }
                }
            }
        }

        route("/uklassifisert/{inntektId}") {
            get {
                withContext(coroutineContext) {
                    val inntektId = InntektId(call.parameters["inntektId"]!!)
                    inntektStore
                        .getInntektMedPersonFnr(inntektId)
                        .let {
                            val person = personOppslag.hentPerson(it.fødselsnummer)
                            val inntektsmottaker = Inntektsmottaker(it.fødselsnummer, person.sammensattNavn())
                            val organisasjoner =
                                hentOrganisasjoner(
                                    enhetsregisterClient,
                                    it.inntekt.arbeidsInntektMaaned
                                        ?.flatMap { it.arbeidsInntektInformasjon?.inntektListe.orEmpty() }
                                        ?.filter { inntekt ->
                                            inntekt.virksomhet?.aktoerType == AktoerType.ORGANISASJON &&
                                                (inntekt.opptjeningsland == "NO" || inntekt.opptjeningsland == null)
                                        }?.mapNotNull { it.virksomhet?.identifikator }
                                        ?.toTypedArray()
                                        ?.toList() ?: emptyList(),
                                )
                            it.inntekt.mapToFrontend(inntektsmottaker, organisasjoner)
                        }.let {
                            call.respond(HttpStatusCode.OK, it)
                        }
                }
            }
            post {
                withContext(coroutineContext) {
                    val inntektId = call.parameters["inntektId"]!!
                    call
                        .receive<InntekterDto>()
                        .mapToStoredInntekt(
                            inntektId = inntektId,
                        ).let {
                            val inntektPersonMapping = inntektStore.getInntektPersonMapping(inntektId)
                            inntektStore.storeInntekt(
                                StoreInntektCommand(
                                    inntektparametre =
                                        Inntektparametre(
                                            aktørId = inntektPersonMapping.aktørId,
                                            fødselsnummer = it.inntekt.ident.identifikator,
                                            regelkontekst =
                                                RegelKontekst(
                                                    inntektPersonMapping.kontekstId,
                                                    inntektPersonMapping.kontekstType,
                                                ),
                                            beregningsdato = inntektPersonMapping.beregningsdato,
                                        ),
                                    inntekt = it.inntekt,
                                    manueltRedigert =
                                        ManueltRedigert.from(
                                            true,
                                            call.getSubject(),
                                        ),
                                ),
                            )
                        }.let {
                            call.respond(HttpStatusCode.OK, it.inntektId.id)
                        }.also {
                            inntektKorrigeringCounter.inc()
                        }
                }
            }
        }

        route("/uklassifisert/uncached/{aktørId}/{kontekstType}/{kontekstId}/{beregningsDato}") {
            get {
                val callId = call.callId
                withContext(coroutineContext) {
                    call.withInntektRequest("GET /uklassifisert/uncached/") {
                        val person = personOppslag.hentPerson(this.aktørId)
                        val opptjeningsperiode = Opptjeningsperiode(this.beregningsDato)
                        toInntektskomponentRequest(person, opptjeningsperiode)
                            .let {
                                logger.info { "Henter nye inntekter fra inntektskomponenten" }
                                inntektskomponentClient.getInntekt(it, callId = callId)
                            }.let {
                                logger.info { "Fikk nye inntekter fra inntektskomponenten" }
                                val inntektsmottaker =
                                    Inntektsmottaker(person.fødselsnummer, person.sammensattNavn())
                                mapToGUIInntekt(it, opptjeningsperiode, inntektsmottaker)
                            }.let {
                                call.respond(HttpStatusCode.OK, it)
                            }.also {
                                inntektOppfriskingCounter.inc()
                            }
                    }
                }
            }

            post {
                withContext(coroutineContext) {
                    call.withInntektRequest("POST /uklassifisert/uncached/") {
                        val guiInntekt = call.receive<GUIInntekt>()
                        val person = personOppslag.hentPerson(this.aktørId)
                        mapToDetachedInntekt(guiInntekt)
                            .let {
                                inntektStore.storeInntekt(
                                    StoreInntektCommand(
                                        inntektparametre =
                                            Inntektparametre(
                                                aktørId = person.aktørId,
                                                fødselsnummer = person.fødselsnummer,
                                                regelkontekst = RegelKontekst(this.kontekstId, this.kontekstType),
                                                beregningsdato = this.beregningsDato,
                                            ),
                                        inntekt = it.inntekt,
                                        manueltRedigert =
                                            ManueltRedigert.from(
                                                guiInntekt.redigertAvSaksbehandler,
                                                call.getSubject(),
                                            ),
                                    ),
                                )
                            }.let {
                                call.respond(
                                    HttpStatusCode.OK,
                                    mapToGUIInntekt(
                                        it,
                                        Opptjeningsperiode(this.beregningsDato),
                                        guiInntekt.inntektsmottaker,
                                    ),
                                )
                            }.also {
                                inntektOppfriskingBruktCounter.inc()
                            }
                    }
                }
            }
        }
    }
    route("/verdikoder") {
        get {
            call.respond(HttpStatusCode.OK, dataGrunnlagKlassifiseringToVerdikode.values)
        }
    }
}

private suspend fun hentOrganisasjoner(
    enhetsregisterClient: EnhetsregisterClient,
    organisasjonsNummerListe: List<String>?,
): List<Organisasjon> {
    val organisasjoner = mutableListOf<Organisasjon>()
    organisasjonsNummerListe?.forEach { orgNr ->
        runCatching {
            enhetsregisterClient.hentEnhet(orgNr)
        }.onFailure {
            logger.error(it) { "Feil ved henting av organisasjonsnavn for $it" }
        }.onSuccess {
            val organisasjonsNavnOgIdMapping =
                Organisasjon(
                    organisasjonsnummer = orgNr,
                    navn = it,
                )
            organisasjoner.add(organisasjonsNavnOgIdMapping)
        }
    }

    return organisasjoner
}

private fun ApplicationCall.getSubject(): String {
    return runCatching {
        this.authentication
            .principal<JWTPrincipal>()
            ?.payload
            ?.subject
            ?: throw JWTDecodeException("Unable to get subject from JWT")
    }.getOrElse {
        logger.error(it) { "Unable to get subject" }
        return@getOrElse "UNKNOWN"
    }
}

private inline fun ApplicationCall.withInntektRequest(
    route: String,
    block: InntektRequest.() -> Unit,
) {
    val inntektRequest =
        runCatching {
            InntektRequest(
                aktørId = this.parameters["aktørId"]!!,
                kontekstId = this.parameters["kontekstId"]!!,
                kontekstType = this.parameters["kontekstType"]!!,
                beregningsDato = LocalDate.parse(this.parameters["beregningsDato"]!!),
            )
        }.getOrElse { t -> throw IllegalArgumentException("Failed to parse parameters", t) }

    withLoggingContext(
        "route" to route,
        "kontekstId" to inntektRequest.kontekstId,
        "kontekstType" to inntektRequest.kontekstType,
    ) {
        block(inntektRequest)
    }
}

data class InntektRequest(
    val aktørId: String,
    val kontekstId: String,
    val kontekstType: String,
    val beregningsDato: LocalDate,
)

val toInntektskomponentRequest: (Person, Opptjeningsperiode) -> InntektkomponentRequest =
    { person: Person, opptjeningsperiode: Opptjeningsperiode ->
        val sisteAvsluttendeKalendermåned = opptjeningsperiode.sisteAvsluttendeKalenderMåned
        val førsteMåned = opptjeningsperiode.førsteMåned
        InntektkomponentRequest(person.aktørId, person.fødselsnummer, førsteMåned, sisteAvsluttendeKalendermåned)
    }

package no.nav.dagpenger.inntekt.v1

import com.auth0.jwt.exceptions.JWTDecodeException
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.features.callId
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import io.prometheus.client.Counter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.ManueltRedigert
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.db.StoreInntektCommand
import no.nav.dagpenger.inntekt.inntektKlassifiseringsKoderJsonAdapter
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentRequest
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.mapping.GUIInntekt
import no.nav.dagpenger.inntekt.mapping.Inntektsmottaker
import no.nav.dagpenger.inntekt.mapping.dataGrunnlagKlassifiseringToVerdikode
import no.nav.dagpenger.inntekt.mapping.mapToDetachedInntekt
import no.nav.dagpenger.inntekt.mapping.mapToGUIInntekt
import no.nav.dagpenger.inntekt.mapping.mapToStoredInntekt
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import java.time.LocalDate

private val logger = KotlinLogging.logger {}
const val INNTEKT_KORRIGERING = "inntekt_korrigering"
private val inntektKorrigeringCounter =
    Counter.build().name(INNTEKT_KORRIGERING).help("Antall ganger saksbehandler har korrigert inntekter").register()
const val INNTEKT_OPPFRISKING = "inntekt_oppfrisking"
private val inntektOppfriskingCounter =
    Counter.build().name(INNTEKT_OPPFRISKING).help("Antall ganger saksbehandler har oppdatert inntekter").register()
const val INNTEKT_OPPFRISKING_BRUKT = "inntekt_oppfrisking_brukt"
private val inntektOppfriskingBruktCounter =
    Counter.build().name(INNTEKT_OPPFRISKING_BRUKT).help("Antall ganger saksbehandler har brukt oppdaterte inntekter")
        .register()

fun Route.uklassifisertInntekt(
    inntektskomponentClient: InntektskomponentClient,
    inntektStore: InntektStore,
    personOppslag: PersonOppslag
) {
    authenticate("jwt", "azure") {
        route("/uklassifisert/{aktørId}/{kontekstType}/{kontekstId}/{beregningsDato}") {
            get {
                withContext(Dispatchers.IO) {
                    parseUrlPathParameters().run {
                        inntektStore.getInntektId(
                            Inntektparametre(
                                aktørId = this.aktørId,
                                regelkontekst = RegelKontekst(this.kontekstId, this.kontekstType),
                                beregningsdato = this.beregningsDato
                            )
                        )?.let {
                            inntektStore.getInntekt(it)
                        }?.let {
                            val person = personOppslag.hentPerson(this.aktørId)
                            val inntektsmottaker = Inntektsmottaker(person?.fødselsnummer, person?.sammensattNavn())
                            mapToGUIInntekt(it, Opptjeningsperiode(this.beregningsDato), inntektsmottaker)
                        }?.let {
                            call.respond(HttpStatusCode.OK, it)
                        } ?: throw InntektNotFoundException("Inntekt with for $this not found.")
                    }
                }
            }
            post {
                withContext(Dispatchers.IO) {
                    parseUrlPathParameters().run {
                        val guiInntekt = call.receive<GUIInntekt>()
                        mapToStoredInntekt(guiInntekt).let {
                            inntektStore.storeInntekt(
                                StoreInntektCommand(
                                    inntektparametre = Inntektparametre(
                                        aktørId = this.aktørId,
                                        regelkontekst = RegelKontekst(this.kontekstId, this.kontekstType),
                                        beregningsdato = this.beregningsDato
                                    ),
                                    inntekt = it.inntekt,
                                    manueltRedigert = ManueltRedigert.from(
                                        guiInntekt.redigertAvSaksbehandler,
                                        getSubject()
                                    )
                                )
                            )
                        }.let {
                            call.respond(
                                HttpStatusCode.OK,
                                mapToGUIInntekt(
                                    it,
                                    Opptjeningsperiode(this.beregningsDato),
                                    guiInntekt.inntektsmottaker
                                )
                            )
                        }.also {
                            inntektKorrigeringCounter.inc()
                        }
                    }
                }
            }
        }

        route("/uklassifisert/uncached/{aktørId}/{kontekstType}/{kontekstId}/{beregningsDato}") {
            get {
                val callId = call.callId
                withContext(Dispatchers.IO) {
                    parseUrlPathParameters().run {
                        withLoggingContext(
                            "route" to "/uklassifisert/uncached",
                            "aktørId" to aktørId,
                            "kontekstType" to kontekstType,
                            "kontekstId" to kontekstId,
                            "beregningsDato" to beregningsDato.toString()
                        ) {
                            val opptjeningsperiode = Opptjeningsperiode(this.beregningsDato)
                            toInntektskomponentRequest(this, opptjeningsperiode).let {
                                logger.info { "Henter nye inntekter fra inntektskomponenten" }
                                inntektskomponentClient.getInntekt(it, callId = callId)
                            }.let {
                                logger.info { "Fikk nye inntekter fra inntektskomponenten" }
                                val person = personOppslag.hentPerson(this.aktørId)
                                val inntektsmottaker =
                                    Inntektsmottaker(person?.fødselsnummer, person?.sammensattNavn())
                                mapToGUIInntekt(it, opptjeningsperiode, inntektsmottaker)
                            }.let {
                                call.respond(HttpStatusCode.OK, it)
                            }.also {
                                inntektOppfriskingCounter.inc()
                            }
                        }
                    }
                }
            }

            post {
                withContext(Dispatchers.IO) {
                    parseUrlPathParameters().run {
                        val guiInntekt = call.receive<GUIInntekt>()
                        mapToDetachedInntekt(guiInntekt).let {
                            inntektStore.storeInntekt(
                                StoreInntektCommand(
                                    inntektparametre = Inntektparametre(
                                        aktørId = this.aktørId,
                                        regelkontekst = RegelKontekst(this.kontekstId, this.kontekstType),
                                        beregningsdato = this.beregningsDato
                                    ),
                                    inntekt = it.inntekt,
                                    manueltRedigert = ManueltRedigert.from(
                                        guiInntekt.redigertAvSaksbehandler,
                                        getSubject()
                                    )
                                )
                            )
                        }.let {
                            call.respond(
                                HttpStatusCode.OK,
                                mapToGUIInntekt(
                                    it,
                                    Opptjeningsperiode(this.beregningsDato),
                                    guiInntekt.inntektsmottaker
                                )
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
            withContext(Dispatchers.IO) {
                call.respondText(
                    inntektKlassifiseringsKoderJsonAdapter.toJson(dataGrunnlagKlassifiseringToVerdikode.values),
                    ContentType.Application.Json,
                    HttpStatusCode.OK
                )
            }
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.getSubject(): String {
    return runCatching {
        call.authentication.principal?.let {
            (it as JWTPrincipal).payload.subject
        } ?: throw JWTDecodeException("Unable to get subject from JWT")
    }.getOrElse {
        logger.error(it) { "Unable to get subject" }
        return@getOrElse "UNKNOWN"
    }
}

private fun PipelineContext<Unit, ApplicationCall>.parseUrlPathParameters(): InntektRequest = runCatching {
    InntektRequest(
        aktørId = call.parameters["aktørId"]!!,
        kontekstId = call.parameters["kontekstId"]!!,
        kontekstType = call.parameters["kontekstType"]!!,
        beregningsDato = LocalDate.parse(call.parameters["beregningsDato"]!!)
    )
}.getOrElse { t -> throw IllegalArgumentException("Failed to parse parameters", t) }

data class InntektRequest(
    val aktørId: String,
    val kontekstId: String,
    val kontekstType: String,
    val beregningsDato: LocalDate
)

data class InntektByIdRequest(
    val aktørId: String,
    val beregningsDato: LocalDate
)

val toInntektskomponentRequest: (InntektRequest, Opptjeningsperiode) -> InntektkomponentRequest =
    { inntektRequest: InntektRequest, opptjeningsperiode: Opptjeningsperiode ->
        val sisteAvsluttendeKalendermåned = opptjeningsperiode.sisteAvsluttendeKalenderMåned
        val førsteMåned = opptjeningsperiode.førsteMåned
        InntektkomponentRequest(inntektRequest.aktørId, førsteMåned, sisteAvsluttendeKalendermåned)
    }

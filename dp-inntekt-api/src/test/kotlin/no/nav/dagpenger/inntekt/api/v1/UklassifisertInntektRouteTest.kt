package no.nav.dagpenger.inntekt.api.v1

import com.fasterxml.jackson.module.kotlin.readValue
import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.inntekt.Problem
import no.nav.dagpenger.inntekt.api.v1.TestApplication.TEST_OAUTH_USER
import no.nav.dagpenger.inntekt.api.v1.TestApplication.autentisert
import no.nav.dagpenger.inntekt.api.v1.TestApplication.mockInntektApi
import no.nav.dagpenger.inntekt.api.v1.TestApplication.withMockAuthServerAndTestApplication
import no.nav.dagpenger.inntekt.api.v1.models.FullVirksomhetsInformasjon
import no.nav.dagpenger.inntekt.api.v1.models.InntekterDto
import no.nav.dagpenger.inntekt.db.DetachedInntekt
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektPersonMapping
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.ManueltRedigert
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.db.StoreInntektCommand
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.db.StoredInntektMedMetadata
import no.nav.dagpenger.inntekt.db.StoredInntektPeriode
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentRequest
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.mapping.GUIArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.mapping.GUIArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.mapping.GUIInntekt
import no.nav.dagpenger.inntekt.mapping.GUIInntektsKomponentResponse
import no.nav.dagpenger.inntekt.mapping.InntektMedVerdikode
import no.nav.dagpenger.inntekt.oppslag.Person
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("ktlint:standard:max-line-length")
internal class UklassifisertInntektRouteTest {
    private val aktørId = "12345678912"
    private val fødselsnummer = "fnr"
    private val inntektskomponentClientMock: InntektskomponentClient = mockk()
    private val inntektStoreMock: InntektStore = mockk()
    private val inntektId = InntektId(ULID().nextULID())
    private val token = TestApplication.testOAuthToken
    private val notFoundQuery =
        Inntektparametre(
            aktørId = aktørId,
            fødselsnummer = fødselsnummer,
            regelkontekst = RegelKontekst("1", "VEDTAK"),
            beregningsdato = LocalDate.of(2019, 1, 8),
        )
    private val foundQuery =
        Inntektparametre(
            aktørId = aktørId,
            fødselsnummer = fødselsnummer,
            regelkontekst = RegelKontekst(aktørId, "VEDTAK"),
            beregningsdato = LocalDate.of(2019, 1, 8),
        )
    private val inntektkomponentenFoundRequest =
        InntektkomponentRequest(
            aktørId = aktørId,
            fødselsnummer = fødselsnummer,
            månedFom = YearMonth.of(2016, 1),
            månedTom = YearMonth.of(2018, 12),
        )
    private val personOppslagMock: PersonOppslag = mockk()
    private val emptyInntekt = InntektkomponentResponse(emptyList(), Aktoer(AktoerType.AKTOER_ID, aktørId))
    private val storedInntekt =
        StoredInntekt(
            inntektId = inntektId,
            inntekt = emptyInntekt,
            manueltRedigert = false,
        )
    private val uklassifisertInntekt = "/v1/inntekt/uklassifisert"

    init {
        every {
            inntektStoreMock.getInntektId(notFoundQuery)
        } returns null

        every {
            inntektStoreMock.getInntektId(foundQuery)
        } returns inntektId

        every {
            inntektStoreMock.storeInntekt(
                command =
                    StoreInntektCommand(
                        inntektparametre = foundQuery,
                        inntekt = storedInntekt.inntekt,
                        manueltRedigert = null,
                    ),
                created = any(),
            )
        } returns storedInntekt

        every {
            inntektStoreMock.storeInntekt(
                command =
                    StoreInntektCommand(
                        inntektparametre = foundQuery,
                        inntekt = storedInntekt.inntekt,
                        manueltRedigert = ManueltRedigert.from(true, "user"),
                    ),
                created = any(),
            )
        } returns storedInntekt

        every {
            inntektStoreMock.getInntekt(inntektId)
        } returns
            StoredInntekt(
                inntektId,
                InntektkomponentResponse(emptyList(), Aktoer(AktoerType.AKTOER_ID, aktørId)),
                false,
                LocalDateTime.now(),
            )

        every {
            runBlocking { inntektskomponentClientMock.getInntekt(inntektkomponentenFoundRequest, callId = any()) }
        } returns emptyInntekt

        every {
            runBlocking { personOppslagMock.hentPerson(any()) }
        } returns
            Person(
                aktørId = aktørId,
                fødselsnummer = fødselsnummer,
                fornavn = "Navn",
                etternavn = "Navnesen",
                mellomnavn = null,
            )
    }

    @Test
    fun `GET unknown uklassifisert inntekt should return 404 not found`() =

        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val response =
                autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/${notFoundQuery.aktørId}/${notFoundQuery.regelkontekst.type}/${notFoundQuery.regelkontekst.id}/${notFoundQuery.beregningsdato}",
                    token = token,
                )

            assertEquals(HttpStatusCode.NotFound, response.status)
            val problem = jacksonObjectMapper.readValue<Problem>(response.bodyAsText())
            assertEquals("Kunne ikke finne inntekt i databasen", problem.title)
            assertEquals("urn:dp:error:inntekt", problem.type.toString())
            assertEquals(404, problem.status)
            assertEquals(
                "Inntekt with for InntektRequest(aktørId=$aktørId, kontekstId=1, kontekstType=VEDTAK, beregningsDato=2019-01-08) not found.",
                problem.detail,
            )
        }

    @Test
    fun `GET uklassifisert without auth cookie should return 401 `() =

        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val response =
                client.get(
                    "$uklassifisertInntekt/${notFoundQuery.aktørId}/${notFoundQuery.regelkontekst.type}/${notFoundQuery.regelkontekst.id}/${notFoundQuery.beregningsdato}",
                )

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET uklassifisert inntekt with malformed parameters should return bad request`() =
        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val response =
                autentisert(
                    "$uklassifisertInntekt/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/blabla",
                )

            assertEquals(BadRequest, response.status)
        }

    @Test
    fun `Get request for uklassifisert inntekt should return 200 ok`() =
        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val response =
                autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                )

            assertEquals(OK, response.status)
            val storedInntekt =
                jacksonObjectMapper.readValue<StoredInntekt>(response.bodyAsText())
            assertEquals(storedInntekt.inntektId, inntektId)
        }

    @Test
    fun `Get request for uncached uklassifisert inntekt should return 200 ok`() =
        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val response =
                autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/uncached/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                )
            assertEquals(OK, response.status)
            val uncachedInntekt =
                jacksonObjectMapper.readValue<DetachedInntekt>(response.bodyAsText())
            assertEquals(emptyInntekt.ident, uncachedInntekt.inntekt.ident)
        }

    @Test
    fun `Post uklassifisert inntekt should return 200 ok`() =
        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val guiInntekt =
                GUIInntekt(
                    inntektId = inntektId,
                    timestamp = null,
                    inntekt = GUIInntektsKomponentResponse(null, null, listOf(), Aktoer(AktoerType.AKTOER_ID, aktørId)),
                    manueltRedigert = false,
                    redigertAvSaksbehandler = false,
                )

            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "v1/inntekt/uklassifisert/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                    body = jacksonObjectMapper.writeValueAsString(guiInntekt),
                )
            assertEquals(OK, response.status)
            val uncachedInntekt =
                jacksonObjectMapper.readValue<DetachedInntekt>(response.bodyAsText())
            assertEquals(emptyInntekt.ident, uncachedInntekt.inntekt.ident)
        }

    @Test
    fun `Post uklassifisert inntekt redigert should return 200 ok`() =
        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val guiInntekt =
                GUIInntekt(
                    inntektId = inntektId,
                    timestamp = null,
                    inntekt = GUIInntektsKomponentResponse(null, null, listOf(), Aktoer(AktoerType.AKTOER_ID, aktørId)),
                    manueltRedigert = false,
                    redigertAvSaksbehandler = false,
                )

            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "v1/inntekt/uklassifisert/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                    body = jacksonObjectMapper.writeValueAsString(guiInntekt),
                )
            assertEquals(OK, response.status)
            val storedInntekt =
                jacksonObjectMapper.readValue<StoredInntekt>(response.bodyAsText())
            assertEquals(storedInntekt.inntektId, inntektId)
        }

    @Test
    fun `Post uklassifisert inntekt med feil redigert should return 400 ok`() =

        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val guiInntekt =
                GUIInntekt(
                    inntektId = inntektId,
                    timestamp = null,
                    inntekt =
                        GUIInntektsKomponentResponse(
                            fraDato = null,
                            tilDato = null,
                            arbeidsInntektMaaned =
                                listOf(
                                    GUIArbeidsInntektMaaned(
                                        aarMaaned = YearMonth.of(2019, 1),
                                        avvikListe = listOf(),
                                        arbeidsInntektInformasjon =
                                            GUIArbeidsInntektInformasjon(
                                                inntektListe =
                                                    listOf(
                                                        InntektMedVerdikode(
                                                            beloep = BigDecimal(123),
                                                            inntektskilde = "",
                                                            verdikode = "Bolig",
                                                            utbetaltIMaaned = YearMonth.of(2019, 1),
                                                            beskrivelse = InntektBeskrivelse.BOLIG,
                                                            fordel = null,
                                                            inntektType = InntektType.LOENNSINNTEKT,
                                                            inntektsperiodetype = null,
                                                            inntektsstatus = null,
                                                        ),
                                                    ),
                                            ),
                                    ),
                                ),
                            ident = Aktoer(AktoerType.AKTOER_ID, aktørId),
                        ),
                    manueltRedigert = false,
                    redigertAvSaksbehandler = true,
                )

            val body = jacksonObjectMapper.writeValueAsString(guiInntekt)
            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "v1/inntekt/uklassifisert/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                    body = body.replace(oldValue = "123", newValue = ""),
                )

            response.status shouldBe BadRequest
        }

    @Test
    fun `Post uklassifisert uncached inntekt should return 200 ok`() =
        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val guiInntekt =
                GUIInntekt(
                    inntektId = null,
                    timestamp = null,
                    inntekt = GUIInntektsKomponentResponse(null, null, listOf(), Aktoer(AktoerType.AKTOER_ID, aktørId)),
                    manueltRedigert = false,
                    redigertAvSaksbehandler = true,
                )

            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "v1/inntekt/uklassifisert/uncached/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                    body = jacksonObjectMapper.writeValueAsString(guiInntekt),
                )
            assertEquals(OK, response.status)
            val storedInntekt =
                jacksonObjectMapper.readValue<StoredInntekt>(response.bodyAsText())
            assertEquals(storedInntekt.inntektId, inntektId)
        }

    @Test
    fun `Post uklassifisert uncached inntekt redigert should return 200 ok`() =

        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val guiInntekt =
                GUIInntekt(
                    inntektId = null,
                    timestamp = null,
                    inntekt = GUIInntektsKomponentResponse(null, null, listOf(), Aktoer(AktoerType.AKTOER_ID, aktørId)),
                    manueltRedigert = false,
                    redigertAvSaksbehandler = false,
                )

            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "v1/inntekt/uklassifisert/uncached/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                    body = jacksonObjectMapper.writeValueAsString(guiInntekt),
                )
            assertEquals(OK, response.status)
            val storedInntekt =
                jacksonObjectMapper.readValue<StoredInntekt>(response.bodyAsText())
            assertEquals(storedInntekt.inntektId, inntektId)
        }

    @Test
    fun `Should get verdikode mapping`() =

        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
            ),
        ) {
            val response =
                autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "v1/inntekt/verdikoder",
                )
            assertEquals(OK, response.status)
            assertEquals("application/json; charset=UTF-8", response.headers["Content-Type"])
            assertTrue(runCatching { jacksonObjectMapper.readValue<Set<String>>(response.bodyAsText()) }.isSuccess)
        }

    @Test
    fun `Get request for uklassifisert inntekt med ugyldig inntektID returnerer 400 BAD REQUEST`() =
        withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
                enhetsregisterClient = mockk<EnhetsregisterClient>(),
            ),
        ) {
            val response =
                autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/UGYLDIG_ID",
                )

            response.status shouldBe BadRequest
        }

    @Test
    fun `Get request for uklassifisert inntekt med inntektID returnerer 200 ok`() {
        val enhetsregisterClientMock = mockk<EnhetsregisterClient>(relaxed = true)
        return withMockAuthServerAndTestApplication(
            mockInntektApi(
                inntektskomponentClient = inntektskomponentClientMock,
                inntektStore = inntektStoreMock,
                personOppslag = personOppslagMock,
                enhetsregisterClient = enhetsregisterClientMock,
            ),
        ) {
            val bodyFraEr =
                FullVirksomhetsInformasjon::class.java
                    .getResource("/test-data/example-virksomhet-info.json")!!
                    .readText()
            coEvery { enhetsregisterClientMock.hentEnhet("1111111") } returns bodyFraEr
            val body =
                UklassifisertInntektRouteTest::class.java
                    .getResource("/test-data/example-inntekt-med-inntektId-payload.json")
                    ?.readText()
            every {
                inntektStoreMock.getStoredInntektMedMetadata(inntektId)
            } returns
                StoredInntektMedMetadata(
                    inntektId,
                    inntekt = jacksonObjectMapper.readValue(body!!),
                    manueltRedigert = false,
                    timestamp = LocalDateTime.now(),
                    fødselsnummer = fødselsnummer,
                    beregningsdato = now(),
                    storedInntektPeriode =
                        StoredInntektPeriode(
                            fraOgMed = YearMonth.of(2023, 1),
                            tilOgMed = YearMonth.of(2025, 5),
                        ),
                )

            val response =
                autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/${inntektId.id}",
                )

            response.status shouldBe OK
            val storedInntekt = jacksonObjectMapper.readValue<InntekterDto>(response.bodyAsText())
            storedInntekt.periode.fraOgMed shouldBe YearMonth.of(2023, 1)
            storedInntekt.periode.tilOgMed shouldBe YearMonth.of(2025, 5)
            storedInntekt.virksomheter shouldHaveSize 2
            storedInntekt.virksomheter[0].inntekter?.shouldHaveSize(4)
            storedInntekt.virksomheter.first().virksomhetsnummer shouldBe "1111111"
            storedInntekt.virksomheter.first().virksomhetsnavn shouldBe "Test Org"
            storedInntekt.virksomheter[1].virksomhetsnummer shouldBe "2222222"
            storedInntekt.virksomheter[1].virksomhetsnavn shouldBe ""
        }
    }

    @Test
    fun `Post request for uklassifisert inntekt med inntektId lagrer og returnerer ny ID`() =
        withMockAuthServerAndTestApplication(
            moduleFunction =
                mockInntektApi(
                    inntektskomponentClient = inntektskomponentClientMock,
                    inntektStore = inntektStoreMock,
                ),
        ) {
            val body =
                UklassifisertInntektRouteTest::class.java
                    .getResource("/test-data/expected-uklassifisert-post-body.json")
                    ?.readText()
            val inntekterDto = jacksonObjectMapper.readValue<InntekterDto>(body!!)

            val inntektPersonMapping =
                InntektPersonMapping(
                    inntektId = inntektId,
                    aktørId = "123456789",
                    fnr = null,
                    kontekstId = "kontekstId",
                    beregningsdato = now(),
                    timestamp = LocalDateTime.now(),
                    kontekstType = "kontekstType",
                )
            every { inntektStoreMock.getInntektPersonMapping(any()) } returns inntektPersonMapping

            val storeInntektCommandSlot = slot<StoreInntektCommand>()
            every { inntektStoreMock.storeInntekt(capture(storeInntektCommandSlot), any()) } returns storedInntekt

            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "$uklassifisertInntekt/${inntektId.id}",
                    body = body,
                )

            response.bodyAsText() shouldBe storedInntekt.inntektId.id
            verify(exactly = 1) { inntektStoreMock.storeInntekt(any(), any()) }
            storeInntektCommandSlot.captured.inntektparametre.aktørId shouldBe inntektPersonMapping.aktørId
            storeInntektCommandSlot.captured.inntektparametre.fødselsnummer shouldBe inntekterDto.mottaker.pnr
            storeInntektCommandSlot.captured.inntektparametre.regelkontekst.id shouldBe inntektPersonMapping.kontekstId
            storeInntektCommandSlot.captured.inntektparametre.regelkontekst.type shouldBe inntektPersonMapping.kontekstType
            storeInntektCommandSlot.captured.inntektparametre.beregningsdato shouldBe inntektPersonMapping.beregningsdato
            storeInntektCommandSlot.captured.inntektparametre.opptjeningsperiode.førsteMåned shouldBe YearMonth.of(2000, 12)
            storeInntektCommandSlot.captured.inntektparametre.opptjeningsperiode.sisteAvsluttendeKalenderMåned shouldBe
                YearMonth.of(2025, 4)
            storeInntektCommandSlot.captured.manueltRedigert.shouldNotBeNull()
            storeInntektCommandSlot.captured.manueltRedigert!!.redigertAv shouldBe TEST_OAUTH_USER
            storeInntektCommandSlot.captured.manueltRedigert!!.begrunnelse shouldBe "Dette er en begrunnelse."
        }
}

package no.nav.dagpenger.inntekt.api.v1

import com.fasterxml.jackson.module.kotlin.readValue
import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.content.TextContent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.dagpenger.stpeter.plugin.TilgangAvvistException
import no.nav.dagpenger.inntekt.Problem
import no.nav.dagpenger.inntekt.api.v1.TestApplication.TEST_OAUTH_USER
import no.nav.dagpenger.inntekt.api.v1.TestApplication.autentisert
import no.nav.dagpenger.inntekt.api.v1.TestApplication.mockInntektApi
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
import no.nav.dagpenger.inntekt.dpbehandling.DpBehandlingKlient
import no.nav.dagpenger.inntekt.dpbehandling.OpplysningTypeId
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
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
import no.nav.dagpenger.inntekt.serder.inntektObjectMapper
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.readValue
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID
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
    private val dpBehandlingKlient: DpBehandlingKlient = mockk(relaxed = true)
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

        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
        ) {
            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/${notFoundQuery.aktørId}/${notFoundQuery.regelkontekst.type}/${notFoundQuery.regelkontekst.id}/${notFoundQuery.beregningsdato}",
                    token = token,
                )

            assertEquals(HttpStatusCode.NotFound, response.status)
            val problem = inntektObjectMapper.readValue<Problem>(response.bodyAsText())
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
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
        ) {
            val response =
                it.client.get(
                    "$uklassifisertInntekt/${notFoundQuery.aktørId}/${notFoundQuery.regelkontekst.type}/${notFoundQuery.regelkontekst.id}/${notFoundQuery.beregningsdato}",
                )

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET uklassifisert inntekt with malformed parameters should return bad request`() =

        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
        ) {
            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Get,
                    "$uklassifisertInntekt/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/blabla",
                )

            assertEquals(BadRequest, response.status)
        }

    @Test
    fun `Get request for uklassifisert inntekt should return 200 ok`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
        ) {
            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                )

            assertEquals(OK, response.status)
            val storedInntekt =
                inntektObjectMapper.readValue<StoredInntekt>(response.bodyAsText())
            assertEquals(storedInntekt.inntektId, inntektId)
        }

    @Test
    fun `Get request for uklassifisert inntekt should return 404 when ident is unknown`() {
        val enhetsregisterClientMock = mockk<EnhetsregisterClient>(relaxed = true)
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
            enhetsregisterClient = enhetsregisterClientMock,
            vedTilgangTilPerson = { _, _, _ ->
                throw TilgangAvvistException(
                    title = "Fant ikke NAV-ident",
                    status = HttpStatusCode.NotFound,
                    type = URI("urn:dp:error:stpeter:not-found"),
                    detail = "Ukjent NAV-ident",
                    instance = URI("urn:dp:inntekt:uklassifisert"),
                )
            },
        ) {
            every {
                inntektStoreMock.getStoredInntektMedMetadata(inntektId)
            } returns
                StoredInntektMedMetadata(
                    inntektId = inntektId,
                    inntekt = storedInntekt.inntekt,
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
                it.autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/${inntektId.id}",
                )

            response.status shouldBe HttpStatusCode.NotFound
            val problem = inntektObjectMapper.readValue<Problem>(response.bodyAsText())
            problem.status shouldBe 404
            problem.title shouldBe "Fant ikke NAV-ident"
            problem.type.toString() shouldBe "urn:dp:error:stpeter:not-found"
            problem.detail shouldBe "Ukjent NAV-ident"
            coVerify(exactly = 0) { personOppslagMock.hentPerson(any()) }
            coVerify(exactly = 0) { enhetsregisterClientMock.hentEnhet(any()) }
        }
    }

    @Test
    fun `Get request for uklassifisert inntekt should return 403 when tilgang is denied`() {
        val enhetsregisterClientMock = mockk<EnhetsregisterClient>(relaxed = true)
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
            enhetsregisterClient = enhetsregisterClientMock,
            vedTilgangTilPerson = { _, _, _ ->
                throw TilgangAvvistException(
                    title = "Ingen tilgang",
                    status = HttpStatusCode.Forbidden,
                    type = URI("urn:dp:error:stpeter:forbidden"),
                    detail = "Saksbehandler har ikke tilgang",
                    instance = URI("urn:dp:inntekt:uklassifisert"),
                )
            },
        ) {
            every {
                inntektStoreMock.getStoredInntektMedMetadata(inntektId)
            } returns
                StoredInntektMedMetadata(
                    inntektId = inntektId,
                    inntekt = storedInntekt.inntekt,
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
                it.autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/${inntektId.id}",
                )

            response.status shouldBe HttpStatusCode.Forbidden
            val problem = inntektObjectMapper.readValue<Problem>(response.bodyAsText())
            problem.status shouldBe 403
            problem.title shouldBe "Ingen tilgang"
            problem.type.toString() shouldBe "urn:dp:error:stpeter:forbidden"
            problem.detail shouldBe "Saksbehandler har ikke tilgang"
            coVerify(exactly = 0) { personOppslagMock.hentPerson(any()) }
            coVerify(exactly = 0) { enhetsregisterClientMock.hentEnhet(any()) }
        }
    }

    @Test
    fun `Get request for uncached uklassifisert inntekt should return 200 ok`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
        ) {
            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/uncached/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                )
            assertEquals(OK, response.status)
            val uncachedInntekt =
                inntektObjectMapper.readValue<DetachedInntekt>(response.bodyAsText())
            assertEquals(emptyInntekt.ident, uncachedInntekt.inntekt.ident)
        }

    @Test
    fun `Post uklassifisert inntekt should return 200 ok`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
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
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "v1/inntekt/uklassifisert/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                    body = inntektObjectMapper.writeValueAsString(guiInntekt),
                )
            assertEquals(OK, response.status)
            val uncachedInntekt =
                inntektObjectMapper.readValue<DetachedInntekt>(response.bodyAsText())
            assertEquals(emptyInntekt.ident, uncachedInntekt.inntekt.ident)
        }

    @Test
    fun `Post uklassifisert inntekt redigert should return 200 ok`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
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
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "v1/inntekt/uklassifisert/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                    body = inntektObjectMapper.writeValueAsString(guiInntekt),
                )
            assertEquals(OK, response.status)
            val storedInntekt =
                inntektObjectMapper.readValue<StoredInntekt>(response.bodyAsText())
            assertEquals(storedInntekt.inntektId, inntektId)
        }

    @Test
    fun `Post uklassifisert inntekt med feil redigert should return 400 ok`() =

        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
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

            val body = inntektObjectMapper.writeValueAsString(guiInntekt)
            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "v1/inntekt/uklassifisert/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                    body = body.replace(oldValue = "123", newValue = ""),
                )

            response.status shouldBe BadRequest
        }

    @Test
    fun `Post uklassifisert uncached inntekt should return 200 ok`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
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
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "v1/inntekt/uklassifisert/uncached/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                    body = inntektObjectMapper.writeValueAsString(guiInntekt),
                )
            assertEquals(OK, response.status)
            val storedInntekt =
                inntektObjectMapper.readValue<StoredInntekt>(response.bodyAsText())
            assertEquals(storedInntekt.inntektId, inntektId)
        }

    @Test
    fun `Post uklassifisert uncached inntekt redigert should return 200 ok`() =

        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
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
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "v1/inntekt/uklassifisert/uncached/${foundQuery.aktørId}/${foundQuery.regelkontekst.type}/${foundQuery.regelkontekst.id}/${foundQuery.beregningsdato}",
                    body = inntektObjectMapper.writeValueAsString(guiInntekt),
                )
            assertEquals(OK, response.status)
            val storedInntekt =
                inntektObjectMapper.readValue<StoredInntekt>(response.bodyAsText())
            assertEquals(storedInntekt.inntektId, inntektId)
        }

    @Test
    fun `Should get verdikode mapping`() =

        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
        ) {
            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "v1/inntekt/verdikoder",
                )
            assertEquals(OK, response.status)
            assertEquals("application/json", response.headers["Content-Type"])
            assertTrue(runCatching { inntektObjectMapper.readValue<Set<String>>(response.bodyAsText()) }.isSuccess)
        }

    @Test
    fun `Get request for uklassifisert inntekt med ugyldig inntektID returnerer 400 BAD REQUEST`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
            enhetsregisterClient = mockk<EnhetsregisterClient>(),
        ) {
            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/UGYLDIG_ID",
                )

            response.status shouldBe BadRequest
        }

    @Test
    fun `Get request for uklassifisert inntekt med inntektID returnerer 200 ok`() {
        val enhetsregisterClientMock = mockk<EnhetsregisterClient>(relaxed = true)
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
            enhetsregisterClient = enhetsregisterClientMock,
            vedTilgangTilPerson = { ident, token, block ->
                ident shouldBe fødselsnummer
                token shouldBe this@UklassifisertInntektRouteTest.token
                block()
            },
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
                    inntekt = inntektObjectMapper.readValue(body!!),
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
                it.autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/${inntektId.id}",
                )

            response.status shouldBe OK
            val storedInntekt = inntektObjectMapper.readValue<InntekterDto>(response.bodyAsText())
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
    fun `Post request for uklassifisert inntekt med inntektId lagrer og returnerer ny ID, uten å rekjøre i dp-behandling, når erArena er true`() =

        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
            dpBehandlingKlient = dpBehandlingKlient,
            vedTilgangTilPerson = { ident, token, block ->
                ident shouldBe fødselsnummer
                token shouldBe this@UklassifisertInntektRouteTest.token
                block()
            },
        ) {
            val body =
                UklassifisertInntektRouteTest::class.java
                    .getResource("/test-data/expected-uklassifisert-post-body.json")
                    ?.readText()
            val inntekterDto = inntektObjectMapper.readValue<InntekterDto>(body!!)

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
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "$uklassifisertInntekt/${inntektId.id}?erArena=true",
                    body = body,
                )

            response.bodyAsText() shouldBe storedInntekt.inntektId.id
            verify(exactly = 1) { inntektStoreMock.storeInntekt(any(), any()) }
            storeInntektCommandSlot.captured.inntektparametre.aktørId shouldBe inntektPersonMapping.aktørId
            storeInntektCommandSlot.captured.inntektparametre.fødselsnummer shouldBe inntekterDto.mottaker.pnr
            storeInntektCommandSlot.captured.inntektparametre.regelkontekst.id shouldBe inntektPersonMapping.kontekstId
            storeInntektCommandSlot.captured.inntektparametre.regelkontekst.type shouldBe inntektPersonMapping.kontekstType
            storeInntektCommandSlot.captured.inntektparametre.beregningsdato shouldBe inntektPersonMapping.beregningsdato
            storeInntektCommandSlot.captured.inntektparametre.opptjeningsperiode.førsteMåned shouldBe
                YearMonth.of(
                    2000,
                    12,
                )
            storeInntektCommandSlot.captured.inntektparametre.opptjeningsperiode.sisteAvsluttendeKalenderMåned shouldBe
                YearMonth.of(2025, 4)
            storeInntektCommandSlot.captured.manueltRedigert.shouldNotBeNull()
            storeInntektCommandSlot.captured.manueltRedigert!!.redigertAv shouldBe TEST_OAUTH_USER
            storeInntektCommandSlot.captured.manueltRedigert!!.begrunnelse shouldBe "Dette er en begrunnelse."
            verify(exactly = 0) { dpBehandlingKlient.rekjørBehandling(any(), any(), any(), any()) }
        }

    @Test
    fun `Post request for uklassifisert inntekt should fail closed when stpeter throws technical error`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            dpBehandlingKlient = dpBehandlingKlient,
            vedTilgangTilPerson = { _, _, _ ->
                throw TilgangAvvistException(
                    title = "En ukjent feil oppstod ved evaluering av tilgang",
                    status = HttpStatusCode.Forbidden,
                    type = URI("urn:error:forbidden"),
                    detail = "Uventet svar fra stpeter: status=500, body=boom",
                    instance = URI("urn:dp:stpeter:api:v1:person"),
                )
            },
        ) {
            val body =
                UklassifisertInntektRouteTest::class.java
                    .getResource("/test-data/expected-uklassifisert-post-body.json")
                    ?.readText()

            every { inntektStoreMock.getInntektPersonMapping(any()) } returns
                InntektPersonMapping(
                    inntektId = inntektId,
                    aktørId = "123456789",
                    fnr = fødselsnummer,
                    kontekstId = "kontekstId",
                    beregningsdato = now(),
                    timestamp = LocalDateTime.now(),
                    kontekstType = "kontekstType",
                )

            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "$uklassifisertInntekt/${inntektId.id}?behandlingId=${UUID.randomUUID()}&opplysningId=${UUID.randomUUID()}",
                    body = body!!,
                )

            response.status shouldBe HttpStatusCode.BadGateway
            val problem = inntektObjectMapper.readValue<Problem>(response.bodyAsText())
            problem.status shouldBe 502
            problem.title shouldBe "En teknisk feil oppstod ved evaluering av tilgang"
            problem.type.toString() shouldBe "urn:error:forbidden"
            problem.detail shouldBe "Kunne ikke evaluere tilgang mot stpeter. Prøv igjen senere."
            verify(exactly = 0) { inntektStoreMock.storeInntekt(any(), any()) }
            verify(exactly = 0) { dpBehandlingKlient.rekjørBehandling(any(), any(), any(), any()) }
        }

    @Test
    fun `Post request for uklassifisert inntekt should return 403 when tilgang is denied`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            dpBehandlingKlient = dpBehandlingKlient,
            vedTilgangTilPerson = { _, _, _ ->
                throw TilgangAvvistException(
                    title = "Ingen tilgang",
                    status = HttpStatusCode.Forbidden,
                    type = URI("urn:dp:error:stpeter:forbidden"),
                    detail = "Saksbehandler har ikke tilgang",
                    instance = URI("urn:dp:inntekt:uklassifisert"),
                )
            },
        ) {
            val body =
                UklassifisertInntektRouteTest::class.java
                    .getResource("/test-data/expected-uklassifisert-post-body.json")
                    ?.readText()

            every { inntektStoreMock.getInntektPersonMapping(any()) } returns
                InntektPersonMapping(
                    inntektId = inntektId,
                    aktørId = "123456789",
                    fnr = fødselsnummer,
                    kontekstId = "kontekstId",
                    beregningsdato = now(),
                    timestamp = LocalDateTime.now(),
                    kontekstType = "kontekstType",
                )

            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "$uklassifisertInntekt/${inntektId.id}?behandlingId=${UUID.randomUUID()}&opplysningId=${UUID.randomUUID()}",
                    body = body!!,
                )

            response.status shouldBe HttpStatusCode.Forbidden
            val problem = inntektObjectMapper.readValue<Problem>(response.bodyAsText())
            problem.status shouldBe 403
            problem.title shouldBe "Ingen tilgang"
            problem.type.toString() shouldBe "urn:dp:error:stpeter:forbidden"
            problem.detail shouldBe "Saksbehandler har ikke tilgang"
            verify(exactly = 0) { inntektStoreMock.storeInntekt(any(), any()) }
            verify(exactly = 0) { dpBehandlingKlient.rekjørBehandling(any(), any(), any(), any()) }
        }

    @Test
    fun `Get request for uklassifisert inntekt uten Authorization-header skal returnere 401`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
        ) {
            val response =
                it.client.get("$uklassifisertInntekt/${inntektId.id}")

            response.status shouldBe HttpStatusCode.Unauthorized
        }

    @Test
    fun `Post request for uklassifisert inntekt uten Authorization-header skal returnere 401`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            dpBehandlingKlient = dpBehandlingKlient,
        ) {
            val body =
                UklassifisertInntektRouteTest::class.java
                    .getResource("/test-data/expected-uklassifisert-post-body.json")
                    ?.readText()

            val response =
                it.client.post("$uklassifisertInntekt/${inntektId.id}?erArena=true") {
                    setBody(TextContent(body!!, ContentType.Application.Json))
                }

            response.status shouldBe HttpStatusCode.Unauthorized
            verify(exactly = 0) { inntektStoreMock.storeInntekt(any(), any()) }
        }

    @Test
    fun `Post request for uklassifisrt inntekt med inntektId gir 400 Bad Request uten behandlingId og opplysningId når erArena er false`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            dpBehandlingKlient = dpBehandlingKlient,
        ) {
            val body =
                UklassifisertInntektRouteTest::class.java
                    .getResource("/test-data/expected-uklassifisert-post-body.json")
                    ?.readText()

            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "$uklassifisertInntekt/${inntektId.id}?erArena=false",
                    body = body!!,
                )

            response.status shouldBe BadRequest
        }

    @Test
    fun `Post request for uklassifisert inntekt med inntektId lagrer og returnerer ny ID, med behandlingId og opplysningId`() =
        mockInntektApi(
            inntektskomponentClient = inntektskomponentClientMock,
            inntektStore = inntektStoreMock,
            dpBehandlingKlient = dpBehandlingKlient,
            vedTilgangTilPerson = { ident, token, block ->
                ident shouldBe fødselsnummer
                token shouldBe this@UklassifisertInntektRouteTest.token
                block()
            },
        ) {
            val body =
                UklassifisertInntektRouteTest::class.java
                    .getResource("/test-data/expected-uklassifisert-post-body.json")
                    ?.readText()
            val inntekterDto = inntektObjectMapper.readValue<InntekterDto>(body!!)

            val behandlingId = UUID.randomUUID()
            val opplysningTypeId = OpplysningTypeId(UUID.randomUUID())
            justRun { dpBehandlingKlient.rekjørBehandling(any(), any(), any(), any()) }

            val inntektPersonMapping =
                InntektPersonMapping(
                    inntektId = inntektId,
                    aktørId = "123456789",
                    fnr = fødselsnummer,
                    kontekstId = "kontekstId",
                    beregningsdato = now(),
                    timestamp = LocalDateTime.now(),
                    kontekstType = "kontekstType",
                )
            every { inntektStoreMock.getInntektPersonMapping(any()) } returns inntektPersonMapping

            val storeInntektCommandSlot = slot<StoreInntektCommand>()
            every { inntektStoreMock.storeInntekt(capture(storeInntektCommandSlot), any()) } returns storedInntekt

            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "$uklassifisertInntekt/${inntektId.id}?behandlingId=$behandlingId&opplysningId=$opplysningTypeId",
                    body = body,
                )

            response.bodyAsText() shouldBe storedInntekt.inntektId.id
            verify(exactly = 1) { inntektStoreMock.storeInntekt(any(), any()) }
            storeInntektCommandSlot.captured.inntektparametre.aktørId shouldBe inntektPersonMapping.aktørId
            storeInntektCommandSlot.captured.inntektparametre.fødselsnummer shouldBe inntekterDto.mottaker.pnr
            storeInntektCommandSlot.captured.inntektparametre.regelkontekst.id shouldBe inntektPersonMapping.kontekstId
            storeInntektCommandSlot.captured.inntektparametre.regelkontekst.type shouldBe inntektPersonMapping.kontekstType
            storeInntektCommandSlot.captured.inntektparametre.beregningsdato shouldBe inntektPersonMapping.beregningsdato
            storeInntektCommandSlot.captured.inntektparametre.opptjeningsperiode.førsteMåned shouldBe
                YearMonth.of(
                    2000,
                    12,
                )
            storeInntektCommandSlot.captured.inntektparametre.opptjeningsperiode.sisteAvsluttendeKalenderMåned shouldBe
                YearMonth.of(2025, 4)
            storeInntektCommandSlot.captured.manueltRedigert.shouldNotBeNull()
            storeInntektCommandSlot.captured.manueltRedigert!!.redigertAv shouldBe TEST_OAUTH_USER
            storeInntektCommandSlot.captured.manueltRedigert!!.begrunnelse shouldBe "Dette er en begrunnelse."
            verify(exactly = 1) {
                dpBehandlingKlient.rekjørBehandling(
                    fødselsnummer,
                    behandlingId,
                    opplysningTypeId,
                    token,
                )
            }
        }

    @Test
    fun `Get request for uncached uklassifisert inntekt skal returnere 200 OK og inntekt`() {
        val enhetsregisterClientMock = mockk<EnhetsregisterClient>(relaxed = true)
        val inntektKomponentClientMock = mockk<InntektskomponentClient>(relaxed = true)

        mockInntektApi(
            inntektskomponentClient = inntektKomponentClientMock,
            inntektStore = inntektStoreMock,
            personOppslag = personOppslagMock,
            enhetsregisterClient = enhetsregisterClientMock,
        ) {
            val body =
                UklassifisertInntektRouteTest::class.java
                    .getResource("/test-data/example-inntekt-med-inntektId-payload.json")
                    ?.readText()
            every {
                inntektStoreMock.getStoredInntektMedMetadata(inntektId)
            } returns
                StoredInntektMedMetadata(
                    inntektId,
                    inntekt = inntektObjectMapper.readValue(body!!),
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

            val inntektKomponentResponseFraAInntekt =
                InntektkomponentResponse(
                    arbeidsInntektMaaned =
                        listOf(
                            ArbeidsInntektMaaned(
                                aarMaaned = YearMonth.parse("2019-01"),
                                avvikListe = null,
                                arbeidsInntektInformasjon =
                                    ArbeidsInntektInformasjon(
                                        inntektListe =
                                            listOf(
                                                Inntekt(
                                                    inntektType = InntektType.NAERINGSINNTEKT,
                                                    beloep = BigDecimal(250000),
                                                    fordel = "kontantytelse",
                                                    inntektskilde = "A-ordningen",
                                                    inntektsperiodetype = "Maaned",
                                                    inntektsstatus = "LoependeInnrapportert",
                                                    leveringstidspunkt = YearMonth.parse("2019-02"),
                                                    utbetaltIMaaned = YearMonth.parse("2018-03"),
                                                    opplysningspliktig =
                                                        Aktoer(
                                                            aktoerType = AktoerType.ORGANISASJON,
                                                            identifikator = "1111111",
                                                        ),
                                                    virksomhet =
                                                        Aktoer(
                                                            aktoerType = AktoerType.ORGANISASJON,
                                                            identifikator = "1111111",
                                                        ),
                                                    inntektsmottaker =
                                                        Aktoer(
                                                            aktoerType = AktoerType.NATURLIG_IDENT,
                                                            identifikator = "99999999999",
                                                        ),
                                                    inngaarIGrunnlagForTrekk = true,
                                                    utloeserArbeidsgiveravgift = true,
                                                    informasjonsstatus = "InngaarAlltid",
                                                    beskrivelse = InntektBeskrivelse.LOTT_KUN_TRYGDEAVGIFT,
                                                ),
                                            ),
                                    ),
                            ),
                            ArbeidsInntektMaaned(
                                aarMaaned = YearMonth.parse("2018-03"),
                                avvikListe = null,
                                arbeidsInntektInformasjon =
                                    ArbeidsInntektInformasjon(
                                        inntektListe =
                                            listOf(
                                                Inntekt(
                                                    inntektType = InntektType.NAERINGSINNTEKT,
                                                    beloep = BigDecimal(250000),
                                                    fordel = "kontantytelse",
                                                    inntektskilde = "A-ordningen",
                                                    inntektsperiodetype = "Maaned",
                                                    inntektsstatus = "LoependeInnrapportert",
                                                    leveringstidspunkt = YearMonth.parse("2019-02"),
                                                    utbetaltIMaaned = YearMonth.parse("2018-03"),
                                                    opplysningspliktig =
                                                        Aktoer(
                                                            aktoerType = AktoerType.ORGANISASJON,
                                                            identifikator = "1111111",
                                                        ),
                                                    virksomhet =
                                                        Aktoer(
                                                            aktoerType = AktoerType.ORGANISASJON,
                                                            identifikator = "1111111",
                                                        ),
                                                    inntektsmottaker =
                                                        Aktoer(
                                                            aktoerType = AktoerType.NATURLIG_IDENT,
                                                            identifikator = "99999999999",
                                                        ),
                                                    inngaarIGrunnlagForTrekk = true,
                                                    utloeserArbeidsgiveravgift = true,
                                                    informasjonsstatus = "InngaarAlltid",
                                                    beskrivelse = InntektBeskrivelse.LOTT_KUN_TRYGDEAVGIFT,
                                                ),
                                            ),
                                    ),
                            ),
                        ),
                    ident =
                        Aktoer(
                            aktoerType = AktoerType.NATURLIG_IDENT,
                            identifikator = "-1",
                        ),
                )
            coEvery {
                inntektKomponentClientMock.getInntekt(
                    request = any(),
                    callId = any(),
                )
            } returns inntektKomponentResponseFraAInntekt

            val bodyFraEr =
                FullVirksomhetsInformasjon::class.java
                    .getResource("/test-data/example-virksomhet-info.json")!!
                    .readText()
            coEvery { enhetsregisterClientMock.hentEnhet("1111111") } returns bodyFraEr

            val response =
                it.autentisert(
                    httpMethod = HttpMethod.Get,
                    endepunkt = "$uklassifisertInntekt/uncached/${inntektId.id}",
                )

            assertEquals(OK, response.status)

            val storedInntekt =
                inntektObjectMapper.readValue<InntekterDto>(response.bodyAsText())
            storedInntekt.virksomheter shouldHaveSize 1
            storedInntekt.virksomheter[0].inntekter?.shouldHaveSize((2))
        }
    }
}

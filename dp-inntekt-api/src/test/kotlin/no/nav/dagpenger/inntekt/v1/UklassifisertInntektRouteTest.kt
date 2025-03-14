package no.nav.dagpenger.inntekt.v1

import com.fasterxml.jackson.module.kotlin.readValue
import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.inntekt.Problem
import no.nav.dagpenger.inntekt.db.DetachedInntekt
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.ManueltRedigert
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.db.StoreInntektCommand
import no.nav.dagpenger.inntekt.db.StoredInntekt
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
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import no.nav.dagpenger.inntekt.v1.TestApplication.autentisert
import no.nav.dagpenger.inntekt.v1.TestApplication.mockInntektApi
import no.nav.dagpenger.inntekt.v1.TestApplication.withMockAuthServerAndTestApplication
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
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

            assertEquals(HttpStatusCode.BadRequest, response.status)
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

            assertEquals(HttpStatusCode.OK, response.status)
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
            assertEquals(HttpStatusCode.OK, response.status)
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
            assertEquals(HttpStatusCode.OK, response.status)
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
            assertEquals(HttpStatusCode.OK, response.status)
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

            response.status shouldBe HttpStatusCode.BadRequest
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
            assertEquals(HttpStatusCode.OK, response.status)
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
            assertEquals(HttpStatusCode.OK, response.status)
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
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("application/json; charset=UTF-8", response.headers["Content-Type"])
            assertTrue(runCatching { jacksonObjectMapper.readValue<Set<String>>(response.bodyAsText()) }.isSuccess)
        }
}

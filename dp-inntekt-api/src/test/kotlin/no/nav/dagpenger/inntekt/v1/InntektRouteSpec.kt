package no.nav.dagpenger.inntekt.v1

import de.huxhorn.sulky.ulid.ULID
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import no.bekk.bekkopen.person.FodselsnummerCalculator.getFodselsnummerForDate
import no.nav.dagpenger.inntekt.BehandlingsInntektsGetter
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.oppslag.Person
import no.nav.dagpenger.inntekt.oppslag.PersonNotFoundException
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import no.nav.dagpenger.inntekt.v1.TestApplication.handleAuthenticatedAzureAdRequest
import no.nav.dagpenger.inntekt.v1.TestApplication.mockInntektApi
import no.nav.dagpenger.inntekt.v1.TestApplication.testOAuthToken
import no.nav.dagpenger.inntekt.v1.TestApplication.withMockAuthServerAndTestApplication
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import kotlin.test.assertEquals

internal class InntektRouteSpec {
    private val fnr =
        getFodselsnummerForDate(
            Date.from(
                LocalDate.now().minusYears(20).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ),
        ).personnummer
    private val ulid = ULID().nextULID()
    private val aktørId = "1234"
    private val beregningsdato = LocalDate.of(2019, 1, 8)
    private val validJson =
        """
        {
        	"aktørId": "$aktørId",
            "regelkontekst": { "id" : "1", "type" : "vedtak" },
            "beregningsDato": "$beregningsdato"
        }
        """.trimIndent()

    private val validJsonWithVedtakIdAsUlid =
        """
        {
            "aktørId": "$aktørId",
            "regelkontekst": { "id" : "$ulid", "type" : "vedtak" },
            "beregningsDato": "2019-01-08"
        }
        """.trimIndent()

    private val validJsonWithFnr =
        """
        {
            "regelkontekst": { "id" : "$ulid", "type" : "vedtak" },
            "fødselsnummer": "$fnr",
            "beregningsDato": "$beregningsdato"
        }
        """.trimIndent()

    private val inntektParametre =
        Inntektparametre(
            aktørId = "$aktørId",
            fødselsnummer = "$fnr",
            regelkontekst = RegelKontekst("1", "vedtak"),
            beregningsdato = beregningsdato,
        )

    private val vedtakIdUlidParametre =
        Inntektparametre(
            aktørId = aktørId,
            fødselsnummer = fnr,
            regelkontekst = RegelKontekst(ulid, "vedtak"),
            beregningsdato = beregningsdato,
        )

    private val fnrParametre =
        Inntektparametre(
            aktørId = aktørId,
            regelkontekst = RegelKontekst(ulid, "vedtak"),
            fødselsnummer = fnr,
            beregningsdato = beregningsdato,
        )

    private val jsonMissingFields =
        """
        {
        	"aktørId": "1234",
        }
        """.trimIndent()

    private val jsonUkjentPerson =
        """
        {
            "regelkontekst": { "id" : "$ulid", "type" : "vedtak" },
            "fødselsnummer": "ukjent",
            "beregningsDato": "$beregningsdato"
        }
        """.trimIndent()

    val uKjentInntektsId = ULID().nextULID()
    val kjentInntektsId = ULID().nextULID()
    val kjentInntekt =
        Inntekt(
            kjentInntektsId,
            sisteAvsluttendeKalenderMåned = YearMonth.of(2023, 10),
            inntektsListe =
                listOf(
                    KlassifisertInntektMåned(
                        årMåned = YearMonth.of(2023, 10),
                        klassifiserteInntekter =
                            listOf(
                                KlassifisertInntekt(
                                    beløp = 1000.toBigDecimal(),
                                    inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
                                ),
                            ),
                    ),
                ),
        )

    private val behandlingsInntektsGetterMock: BehandlingsInntektsGetter =
        spyk(BehandlingsInntektsGetter(mockk(relaxed = true), mockk(relaxed = true)))

    private val personOppslagMock =
        mockk<PersonOppslag>().also {
            val person =
                Person(
                    fødselsnummer = fnr,
                    aktørId = aktørId,
                    fornavn = "fornavn",
                    mellomnavn = null,
                    etternavn = "etternav",
                )
            coEvery { it.hentPerson(fnr) } returns person
            coEvery { it.hentPerson(aktørId) } returns person
            coEvery { it.hentPerson("ukjent") } throws PersonNotFoundException("ukjent")
        }

    private val klassifisertInntektPathV2 = "/v2/inntekt/klassifisert"

    private val callId = "string-ulid"
    private val inntektId = InntektId(ULID().nextULID())
    private val emptyInntekt = InntektkomponentResponse(emptyList(), Aktoer(AktoerType.AKTOER_ID, "1234"))

    private val storedInntekt =
        StoredInntekt(
            inntektId,
            emptyInntekt,
            false,
        )

    init {
        coEvery {
            behandlingsInntektsGetterMock.getBehandlingsInntekt(any(), any())
        } returns storedInntekt

        coEvery {
            behandlingsInntektsGetterMock.getKlassifisertInntekt(InntektId(kjentInntektsId))
        } returns kjentInntekt

        coEvery {
            behandlingsInntektsGetterMock.getKlassifisertInntekt(InntektId(uKjentInntektsId))
        } throws InntektNotFoundException("Inntekt not found")
    }

    @Test
    fun `skal ikke autentisere på v2 hvis ikke auth token er med `() =
        testApp {
            handleRequest(HttpMethod.Post, klassifisertInntektPathV2) {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(validJson)
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

    @Test
    fun `skal autentisere på v2 hvis auth token er med `() =
        testApp {
            handleAuthenticatedAzureAdRequest(HttpMethod.Post, klassifisertInntektPathV2) {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(validJson)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

    @Test
    fun `Klassifisert inntekt API specification test - Should match json field names and formats`() =
        testApp {
            handleAuthenticatedAzureAdRequest(HttpMethod.Post, klassifisertInntektPathV2) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader("X-Request-Id", callId)
                setBody(validJson)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                coVerify(exactly = 1) { behandlingsInntektsGetterMock.getBehandlingsInntekt(inntektParametre, callId) }
                coVerify(exactly = 1) { behandlingsInntektsGetterMock.getKlassifisertInntekt(inntektParametre, callId) }
            }
        }

    @Test
    fun `Klassifisert Requests with fødselsnummer works and does store data`() =
        testApp {
            handleAuthenticatedAzureAdRequest(HttpMethod.Post, klassifisertInntektPathV2) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader("X-Request-Id", callId)
                setBody(validJsonWithFnr)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                coVerify(exactly = 1) { behandlingsInntektsGetterMock.getBehandlingsInntekt(fnrParametre, callId) }
                coVerify(exactly = 1) { behandlingsInntektsGetterMock.getSpesifisertInntekt(fnrParametre, callId) }
                coVerify(exactly = 1) { behandlingsInntektsGetterMock.getKlassifisertInntekt(fnrParametre, callId) }
            }
        }

    @Test
    fun `Klassifisert Requests with vedtakId as string works and does store data`() =
        testApp {
            handleAuthenticatedAzureAdRequest(HttpMethod.Post, klassifisertInntektPathV2) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader("X-Request-Id", callId)
                setBody(validJsonWithVedtakIdAsUlid)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                coVerify(exactly = 1) {
                    behandlingsInntektsGetterMock.getBehandlingsInntekt(
                        vedtakIdUlidParametre,
                        callId,
                    )
                }
                coVerify(exactly = 1) {
                    behandlingsInntektsGetterMock.getSpesifisertInntekt(
                        vedtakIdUlidParametre,
                        callId,
                    )
                }
                coVerify(exactly = 1) {
                    behandlingsInntektsGetterMock.getKlassifisertInntekt(
                        vedtakIdUlidParametre,
                        callId,
                    )
                }
            }
        }

    @Test
    fun `Klassifisert request fails on post request with missing fields`() =
        testApp {
            handleAuthenticatedAzureAdRequest(HttpMethod.Post, klassifisertInntektPathV2) {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(jsonMissingFields)
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

    @Test
    fun `Klassifisert request fails on post request with unknown person`() =
        testApp {
            handleAuthenticatedAzureAdRequest(HttpMethod.Post, klassifisertInntektPathV2) {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(jsonUkjentPerson)
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

    @Test
    fun `Hente klassifisert inntekt basert på inntekt ID`() =
        withMockAuthServerAndTestApplication(mockInntektApi(behandlingsInntektsGetter = behandlingsInntektsGetterMock)) {
            val kjentInntektIdresponse =
                client.get("/v2/inntekt/klassifisert/$kjentInntektsId") {
                    autentisert()
                }
            assertEquals(HttpStatusCode.OK, kjentInntektIdresponse.status)
            kjentInntektIdresponse.status shouldBe HttpStatusCode.OK
            val hentetInntekt =
                jacksonObjectMapper.readValue(
                    kjentInntektIdresponse.bodyAsText(),
                    Inntekt::class.java,
                )
            assertSoftly {
                hentetInntekt.inntektsId shouldBe kjentInntekt.inntektsId
                hentetInntekt.sisteAvsluttendeKalenderMåned shouldBe kjentInntekt.sisteAvsluttendeKalenderMåned
                hentetInntekt.inntektsListe shouldBe kjentInntekt.inntektsListe
                hentetInntekt.manueltRedigert shouldBe kjentInntekt.manueltRedigert
            }
            val uKjentInntektIdresponse =
                client.get("/v2/inntekt/klassifisert/$uKjentInntektsId") {
                    autentisert()
                }

            uKjentInntektIdresponse.status shouldBe HttpStatusCode.NotFound

            val ikkeInntektIdResponse =
                client.get("/v2/inntekt/klassifisert/123") {
                    autentisert()
                }

            ikkeInntektIdResponse.status shouldBe HttpStatusCode.BadRequest
        }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication(
            mockInntektApi(
                behandlingsInntektsGetter = behandlingsInntektsGetterMock,
                personOppslag = personOppslagMock,
            ),
        ) { callback() }
    }

    private fun HttpRequestBuilder.autentisert() {
        header(HttpHeaders.Authorization, "Bearer $testOAuthToken")
    }
}

package no.nav.dagpenger.inntekt.api.v3

import com.fasterxml.jackson.module.kotlin.readValue
import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode.Companion.OK
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.inntekt.BehandlingsInntektsGetter
import no.nav.dagpenger.inntekt.api.v1.TestApplication.autentisert
import no.nav.dagpenger.inntekt.api.v1.TestApplication.mockInntektApi
import no.nav.dagpenger.inntekt.api.v1.TestApplication.withMockAuthServerAndTestApplication
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.ManueltRedigert
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.oppslag.Person
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.Test

class KlassifisertInntektRouteV3Test {
    private val fødselsnummer = "12345678901"
    private val aktørId = "12345"
    private val personOppslagMock: PersonOppslag = mockk()
    private val behandlingsInntektsGetterMock: BehandlingsInntektsGetter = mockk()
    private val inntektParametreCapture = mutableListOf<Inntektparametre>()
    private val inntektStoreMock: InntektStore = mockk()
    private val regelkontekst = RegelKontekst("id", "type")
    private val beregningsDato = LocalDate.of(2018, 5, 27)!!
    private val periodeFraOgMed = YearMonth.of(2017, 1)!!
    private val periodeTilOgMed = YearMonth.of(2018, 12)!!
    private val inntektsId = ULID().nextULID()
    private val sisteAvsluttendeKalenderMåned = YearMonth.now()

    init {
        coEvery {
            personOppslagMock.hentPerson(any())
        } returns
            Person(
                fødselsnummer,
                aktørId,
                "Navn",
                null,
                "Navnesen",
            )

        coEvery {
            behandlingsInntektsGetterMock.getKlassifisertInntekt(
                capture(inntektParametreCapture),
                any(),
            )
        } returns createInntekt(true)

        every { inntektStoreMock.getManueltRedigert(any()) } returns mockk(relaxed = true)
    }

    @Test
    fun `Inntektparametre opprettes med forventede verdier`() {
        withMockAuthServerAndTestApplication(
            mockInntektApi(
                personOppslag = personOppslagMock,
                behandlingsInntektsGetter = behandlingsInntektsGetterMock,
                inntektStore = inntektStoreMock,
            ),
        ) {
            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "/v3/inntekt/klassifisert",
                    body =
                        jacksonObjectMapper.writeValueAsString(
                            KlassifisertInntektRequestDto(
                                fødselsnummer,
                                regelkontekst,
                                beregningsDato,
                                periodeFraOgMed,
                                periodeTilOgMed,
                            ),
                        ),
                )

            response.status shouldBe OK
            val inntektparametre = inntektParametreCapture.first()
            inntektparametre.aktørId shouldBe aktørId
            inntektparametre.fødselsnummer shouldBe fødselsnummer
            inntektparametre.regelkontekst shouldBe regelkontekst
            inntektparametre.beregningsdato shouldBe beregningsDato
            inntektparametre.opptjeningsperiode.førsteMåned shouldBe periodeFraOgMed
            inntektparametre.opptjeningsperiode.sisteAvsluttendeKalenderMåned shouldBe periodeTilOgMed
        }
    }

    @Test
    fun `Klassifisert-endepunktet returnerer forventet respons når inntekten er manuelt redigert`() {
        every { inntektStoreMock.getManueltRedigert(any()) } returns
            ManueltRedigert(
                "N313373",
                "Dette er en begrunnelse.",
            )

        withMockAuthServerAndTestApplication(
            mockInntektApi(
                personOppslag = personOppslagMock,
                behandlingsInntektsGetter = behandlingsInntektsGetterMock,
                inntektStore = inntektStoreMock,
            ),
        ) {
            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "/v3/inntekt/klassifisert",
                    body =
                        jacksonObjectMapper.writeValueAsString(
                            KlassifisertInntektRequestDto(
                                fødselsnummer,
                                regelkontekst,
                                beregningsDato,
                                periodeFraOgMed,
                                periodeTilOgMed,
                            ),
                        ),
                )

            response.status shouldBe OK
            val inntektDTO =
                jacksonObjectMapper.readValue<Inntekt>(response.bodyAsText())
            inntektDTO.inntektsId shouldBe inntektsId
            inntektDTO.inntektsListe.shouldNotBeEmpty()
            inntektDTO.manueltRedigert shouldBe true
            inntektDTO.begrunnelseManueltRedigert shouldBe "Dette er en begrunnelse."
            inntektDTO.sisteAvsluttendeKalenderMåned shouldBe sisteAvsluttendeKalenderMåned
        }
    }

    @Test
    fun `Klassifisert-endepunktet returnerer forventet respons når inntekten ikke er manuelt redigert`() {
        every { inntektStoreMock.getManueltRedigert(any()) } returns null

        coEvery {
            behandlingsInntektsGetterMock.getKlassifisertInntekt(
                capture(inntektParametreCapture),
                any(),
            )
        } returns createInntekt(false)

        withMockAuthServerAndTestApplication(
            mockInntektApi(
                personOppslag = personOppslagMock,
                behandlingsInntektsGetter = behandlingsInntektsGetterMock,
                inntektStore = inntektStoreMock,
            ),
        ) {
            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "/v3/inntekt/klassifisert",
                    body =
                        jacksonObjectMapper.writeValueAsString(
                            KlassifisertInntektRequestDto(
                                fødselsnummer,
                                regelkontekst,
                                beregningsDato,
                                periodeFraOgMed,
                                periodeTilOgMed,
                            ),
                        ),
                )

            response.status shouldBe OK
            val klassifisertInntektResponseDto =
                jacksonObjectMapper.readValue<Inntekt>(response.bodyAsText())
            klassifisertInntektResponseDto.inntektsId shouldBe inntektsId
            klassifisertInntektResponseDto.inntektsListe.shouldNotBeEmpty()
            klassifisertInntektResponseDto.manueltRedigert shouldBe false
            klassifisertInntektResponseDto.begrunnelseManueltRedigert shouldBe null
            klassifisertInntektResponseDto.sisteAvsluttendeKalenderMåned shouldBe sisteAvsluttendeKalenderMåned
        }
    }

    @Test
    fun `harInntekt-endepunktet returnerer true når det finnes inntekt`() {
        val inntektskomponent = mockk<InntektskomponentClient>()

        coEvery {
            inntektskomponent.getInntekt(request = any())
        } returns
            InntektkomponentResponse(
                listOf(ArbeidsInntektMaaned(YearMonth.now(), null, null)),
                Aktoer(AktoerType.NATURLIG_IDENT, fødselsnummer),
            )

        withMockAuthServerAndTestApplication(
            mockInntektApi(
                personOppslag = personOppslagMock,
                inntektskomponentClient = inntektskomponent,
            ),
        ) {
            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "/v3/inntekt/harInntekt",
                    body =
                        jacksonObjectMapper.writeValueAsString(
                            HarInntektRequestDto(fødselsnummer, sisteAvsluttendeKalenderMåned),
                        ),
                )

            response.status shouldBe OK
            val inntekt = jacksonObjectMapper.readValue<Boolean>(response.bodyAsText())

            inntekt shouldBe true
        }
    }

    private fun createInntekt(manueltRedigert: Boolean): Inntekt =
        Inntekt(
            inntektsId,
            listOf(KlassifisertInntektMåned(YearMonth.now(), listOf())),
            manueltRedigert,
            null,
            YearMonth.now(),
            LocalDateTime.now(),
        )
}

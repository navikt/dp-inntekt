package no.nav.dagpenger.inntekt.api.v3

import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode.Companion.OK
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.inntekt.BehandlingsInntektsGetter
import no.nav.dagpenger.inntekt.api.v1.TestApplication.autentisert
import no.nav.dagpenger.inntekt.api.v1.TestApplication.mockInntektApi
import no.nav.dagpenger.inntekt.api.v1.TestApplication.withMockAuthServerAndTestApplication
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.oppslag.Person
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test

class KlassifisertInntektRouteV3Test {
    val fødselsnummer = "12345678901"
    val aktørId = "12345"
    private val personOppslagMock: PersonOppslag = mockk()
    private val behandlingsInntektsGetterMock: BehandlingsInntektsGetter = mockk()
    private val inntektParametreCapture = mutableListOf<Inntektparametre>()

    init {
        every {
            runBlocking { personOppslagMock.hentPerson(any()) }
        } returns
            Person(
                fødselsnummer,
                aktørId,
                "Navn",
                null,
                "Navnesen",
            )
        every {
            runBlocking {
                behandlingsInntektsGetterMock.getKlassifisertInntekt(
                    capture(inntektParametreCapture),
                    any(),
                )
            }
        } returns
            mockk(relaxed = true)
    }

    @Test
    fun `Inntektparametre opprettes med forventede verdier`() {
        val regelkontekst = RegelKontekst("id", "type")
        val beregningsDato = LocalDate.of(2018, 5, 27)
        val periodeFraOgMed = YearMonth.of(2017, 1)
        val periodeTilOgMed = YearMonth.of(2018, 12)

        withMockAuthServerAndTestApplication(
            mockInntektApi(
                personOppslag = personOppslagMock,
                behandlingsInntektsGetter = behandlingsInntektsGetterMock,
            ),
        ) {
            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "/v3/inntekt/klassifisert",
                    body =
                        jacksonObjectMapper.writeValueAsString(
                            InntektRequestDto(
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
}

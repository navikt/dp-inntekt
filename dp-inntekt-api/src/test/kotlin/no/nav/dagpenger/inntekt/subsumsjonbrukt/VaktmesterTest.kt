package no.nav.dagpenger.inntekt.subsumsjonbrukt

import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.prometheus.client.CollectorRegistry
import no.nav.dagpenger.inntekt.DataSource
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.ManueltRedigert
import no.nav.dagpenger.inntekt.db.PostgresInntektStore
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.db.StoreInntektCommand
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.withMigratedDb
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime

internal class VaktmesterTest {

    private val parameters = Inntektparametre(
        aktørId = "1234",
        fødselsnummer = "1234",
        regelkontekst = RegelKontekst("1234", "vedtak"),
        beregningsdato = LocalDate.now()
    )

    private val inntekter = InntektkomponentResponse(
        ident = Aktoer(AktoerType.AKTOER_ID, parameters.aktørId!!),
        arbeidsInntektMaaned = emptyList()
    )

    @Test
    fun `Skal ikke slette brukte inntekter`() {

        withMigratedDb {
            val inntektStore = PostgresInntektStore(DataSource.instance)
            val bruktInntekt = inntektStore.storeInntekt(
                StoreInntektCommand(
                    inntektparametre = parameters,
                    inntekt = inntekter
                )

            )
            inntektStore.markerInntektBrukt(bruktInntekt.inntektId)
            val vaktmester = Vaktmester(DataSource.instance)
            vaktmester.rydd()
            inntektStore.getInntekt(bruktInntekt.inntektId) shouldNotBe null
        }
    }

    @Test
    fun `Skal kun slette inntekt som ikke er brukt selvom det er referrert til samme behandlingsnøkler som en annen inntekt som er brukt`() {
        withMigratedDb {
            val inntektStore = PostgresInntektStore(DataSource.instance)
            val ubruktInntekt = inntektStore.storeInntekt(
                StoreInntektCommand(
                    inntektparametre = parameters,
                    inntekt = inntekter

                ),
                created = ZonedDateTime.now().minusMonths(7)

            )
            val bruktInntekt = inntektStore.storeInntekt(
                StoreInntektCommand(
                    inntektparametre = parameters,
                    inntekt = inntekter.copy(
                        arbeidsInntektMaaned = listOf(
                            ArbeidsInntektMaaned(
                                aarMaaned = YearMonth.now(),
                                arbeidsInntektInformasjon = ArbeidsInntektInformasjon(emptyList()),
                                avvikListe = emptyList()
                            )
                        )
                    )

                ),
                created = ZonedDateTime.now().minusMonths(7)
            )
            inntektStore.markerInntektBrukt(bruktInntekt.inntektId)
            val vaktmester = Vaktmester(DataSource.instance)
            vaktmester.rydd()
            inntektStore.getInntektId(parameters) shouldBe bruktInntekt.inntektId
            assertThrows<InntektNotFoundException> { inntektStore.getInntekt(ubruktInntekt.inntektId) }
        }
    }

    @Test
    fun `Skal kun slette ubrukte inntekter som er eldre enn 180 dager`() {
        withMigratedDb {
            val inntektStore = PostgresInntektStore(DataSource.instance)
            val ubruktEldreEnn180Dager = inntektStore.storeInntekt(
                command = StoreInntektCommand(
                    inntektparametre = parameters,
                    inntekt = inntekter
                ),
                created = ZonedDateTime.now().minusMonths(7)

            )
            val ubruktYngreEnn180Dager = inntektStore.storeInntekt(
                command = StoreInntektCommand(
                    inntektparametre = parameters,
                    inntekt = inntekter
                )
            )

            val vaktmester = Vaktmester(DataSource.instance)
            vaktmester.rydd()
            assertThrows<InntektNotFoundException> { inntektStore.getInntekt(ubruktEldreEnn180Dager.inntektId) }
            inntektStore.getInntekt(ubruktYngreEnn180Dager.inntektId) shouldBe ubruktYngreEnn180Dager
        }
        CollectorRegistry.defaultRegistry.metricFamilySamples().asSequence().find { it.name == "inntekt_slettet" }
            ?.let { metric ->
                metric.samples[0].value shouldNotBe null
                metric.samples[0].value shouldBeGreaterThan 0.0
            } ?: AssertionError("Could not find metric")
    }

    @Test
    fun `Skal kun slette manuelt redigerte, ubrukte inntekter som er eldre enn 180 dager`() {
        withMigratedDb {
            val inntektStore = PostgresInntektStore(DataSource.instance)
            val ubruktEldreEnn90Dager = inntektStore.storeInntekt(

                command = StoreInntektCommand(
                    inntektparametre = parameters,
                    inntekt = inntekter,
                    manueltRedigert = ManueltRedigert(
                        redigertAv = "test"
                    )
                ),
                created = ZonedDateTime.now().minusMonths(7)
            )

            val vaktmester = Vaktmester(DataSource.instance)
            vaktmester.rydd()
            assertThrows<InntektNotFoundException> { inntektStore.getInntekt(ubruktEldreEnn90Dager.inntektId) }
            inntektStore.getManueltRedigert(ubruktEldreEnn90Dager.inntektId) shouldBe null
        }
    }
}

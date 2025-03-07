package no.nav.dagpenger.inntekt.subsumsjonbrukt

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.dagpenger.inntekt.Postgres.withMigratedDb
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.ManueltRedigert
import no.nav.dagpenger.inntekt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.inntekt.db.PostgresInntektStore
import no.nav.dagpenger.inntekt.db.RegelKontekst
import no.nav.dagpenger.inntekt.db.StoreInntektCommand
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime

internal class VaktmesterTest {
    private val parameters =
        Inntektparametre(
            aktørId = "1234",
            fødselsnummer = "1234",
            regelkontekst = RegelKontekst("1234", "vedtak"),
            beregningsdato = LocalDate.now(),
        )

    private val inntekter =
        InntektkomponentResponse(
            ident = Aktoer(AktoerType.AKTOER_ID, parameters.aktørId!!),
            arbeidsInntektMaaned = emptyList(),
        )

    @Test
    fun `Skal ikke slette brukte inntekter`() {
        withMigratedDb {
            val inntektStore = PostgresInntektStore(PostgresDataSourceBuilder.dataSource)
            val bruktInntekt =
                inntektStore.storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = parameters,
                        inntekt = inntekter,
                    ),
                )
            inntektStore.markerInntektBrukt(bruktInntekt.inntektId)
            val vaktmester = Vaktmester(PostgresDataSourceBuilder.dataSource)
            vaktmester.rydd()
            inntektStore.getInntekt(bruktInntekt.inntektId) shouldNotBe null
        }
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `Skal kun slette inntekt som ikke er brukt selvom det er referrert til samme behandlingsnøkler som en annen inntekt som er brukt`() {
        withMigratedDb {
            val inntektStore = PostgresInntektStore(PostgresDataSourceBuilder.dataSource)
            val ubruktInntekt =
                inntektStore.storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = parameters,
                        inntekt = inntekter,
                    ),
                    created = ZonedDateTime.now().minusMonths(7),
                )
            val bruktInntekt =
                inntektStore.storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = parameters,
                        inntekt =
                            inntekter.copy(
                                arbeidsInntektMaaned =
                                    listOf(
                                        ArbeidsInntektMaaned(
                                            aarMaaned = YearMonth.now(),
                                            arbeidsInntektInformasjon = ArbeidsInntektInformasjon(emptyList()),
                                            avvikListe = emptyList(),
                                        ),
                                    ),
                            ),
                    ),
                    created = ZonedDateTime.now().minusMonths(7),
                )
            inntektStore.markerInntektBrukt(bruktInntekt.inntektId)
            val vaktmester = Vaktmester(PostgresDataSourceBuilder.dataSource)
            vaktmester.rydd()
            inntektStore.getInntektId(parameters) shouldBe bruktInntekt.inntektId
            assertThrows<InntektNotFoundException> { inntektStore.getInntekt(ubruktInntekt.inntektId) }
        }
    }

    @Test
    fun `Skal kun slette ubrukte inntekter som er eldre enn 180 dager`() {
        withMigratedDb {
            val inntektStore = PostgresInntektStore(PostgresDataSourceBuilder.dataSource)
            val ubruktEldreEnn180Dager =
                inntektStore.storeInntekt(
                    command =
                        StoreInntektCommand(
                            inntektparametre = parameters,
                            inntekt = inntekter,
                        ),
                    created = ZonedDateTime.now().minusMonths(7),
                )
            val ubruktYngreEnn180Dager =
                inntektStore.storeInntekt(
                    command =
                        StoreInntektCommand(
                            inntektparametre = parameters,
                            inntekt = inntekter,
                        ),
                )

            val vaktmester = Vaktmester(PostgresDataSourceBuilder.dataSource)
            vaktmester.rydd()
            assertThrows<InntektNotFoundException> { inntektStore.getInntekt(ubruktEldreEnn180Dager.inntektId) }
            inntektStore.getInntekt(ubruktYngreEnn180Dager.inntektId) shouldBe ubruktYngreEnn180Dager
        }
        PrometheusRegistry.defaultRegistry.scrape().find { it.metadata.name == "inntekt_slettet" }
            ?.let { metric ->
                metric.dataPoints[0].labels shouldNotBe null
                metric.dataPoints[0].scrapeTimestampMillis shouldNotBe null
            } ?: AssertionError("Could not find metric")
    }

    @Test
    fun `Skal kun slette manuelt redigerte, ubrukte inntekter som er eldre enn 180 dager`() {
        withMigratedDb {
            val inntektStore = PostgresInntektStore(PostgresDataSourceBuilder.dataSource)
            val ubruktEldreEnn90Dager =
                inntektStore.storeInntekt(
                    command =
                        StoreInntektCommand(
                            inntektparametre = parameters,
                            inntekt = inntekter,
                            manueltRedigert =
                                ManueltRedigert(
                                    redigertAv = "test",
                                ),
                        ),
                    created = ZonedDateTime.now().minusMonths(7),
                )

            val vaktmester = Vaktmester(PostgresDataSourceBuilder.dataSource)
            vaktmester.rydd()
            assertThrows<InntektNotFoundException> { inntektStore.getInntekt(ubruktEldreEnn90Dager.inntektId) }
            inntektStore.getManueltRedigert(ubruktEldreEnn90Dager.inntektId) shouldBe null
        }
    }
}

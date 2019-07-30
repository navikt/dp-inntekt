package no.nav.dagpenger.inntekt.db

import com.zaxxer.hikari.HikariDataSource
import no.nav.dagpenger.inntekt.BehandlingsKey
import no.nav.dagpenger.inntekt.Configuration
import no.nav.dagpenger.inntekt.dummyConfigs
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.withProps
import org.junit.Test
import org.testcontainers.containers.PostgreSQLContainer
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class PostgresTest {

    @Test
    fun `Migration scripts are applied successfully`() {
        withCleanDb {
            val migrations = migrate(DataSource.instance)
            assertEquals(5, migrations, "Wrong number of migrations")
        }
    }

    @Test
    fun `Migration scripts are idempotent`() {
        withCleanDb {
            migrate(DataSource.instance)

            val migrations = migrate(DataSource.instance)
            assertEquals(0, migrations, "Wrong number of migrations")
        }
    }

    @Test
    fun `Migration of testdata `() {
        withCleanDb {
            val migrations = migrate(DataSource.instance, locations = listOf("db/migration", "db/testdata"))
            assertEquals(9, migrations, "Wrong number of migrations")
        }
    }

    @Test
    fun `JDBC url is set correctly from  config values `() {
        withProps(dummyConfigs) {
            with(hikariConfigFrom(Configuration())) {
                assertEquals("jdbc:postgresql://localhost:5432/dp-inntekt-db", jdbcUrl)
            }
        }
    }
}

internal class PostgresInntektStoreTest {
    @Test
    fun `Successful insert of inntekter`() {
        withMigratedDb {
            with(PostgresInntektStore(DataSource.instance)) {
                val behandlingsKey = BehandlingsKey("1234", 1234, LocalDate.now())
                val hentInntektListeResponse = InntektkomponentResponse(
                    emptyList(),
                    Aktoer(AktoerType.AKTOER_ID, "1234")
                )
                val storedInntekt = insertInntekt(behandlingsKey, hentInntektListeResponse)
                assertNotNull(storedInntekt.inntektId)
                assertTrue("Inntekstliste should be in the same state") { hentInntektListeResponse == storedInntekt.inntekt }

                val storedInntektByRequest = getInntekt(storedInntekt.inntektId)
                assertTrue("Inntekstliste should be in the same state") { storedInntekt == storedInntektByRequest }

                assertNull(getManueltRedigert(storedInntektByRequest.inntektId))
            }
        }
    }

    @Test
    fun `Successful insert of inntekter which is manuelt redigert`() {
        withMigratedDb {
            with(PostgresInntektStore(DataSource.instance)) {
                val behandlingsKey = BehandlingsKey("1234", 1234, LocalDate.now())
                val hentInntektListeResponse = InntektkomponentResponse(
                    emptyList(),
                    Aktoer(AktoerType.AKTOER_ID, "1234")
                )
                val manueltRedigert = ManueltRedigert("user")

                val storedInntekt = insertInntekt(behandlingsKey, hentInntektListeResponse, manueltRedigert)
                assertTrue(storedInntekt.manueltRedigert)

                val storedInntektByRequest = getInntekt(storedInntekt.inntektId)
                assertTrue(storedInntektByRequest.manueltRedigert)

                val storedManueltRedigert = getManueltRedigert(storedInntekt.inntektId)
                assertNotNull(storedManueltRedigert)
                assertEquals(manueltRedigert, storedManueltRedigert)
            }
        }
    }

    @Test
    fun ` Sucessfully get inntekter`() {

        withMigratedDb {
            with(PostgresInntektStore(DataSource.instance)) {
                val hentInntektListeResponse = InntektkomponentResponse(
                    emptyList(),
                    Aktoer(AktoerType.AKTOER_ID, "1234")
                )
                insertInntekt(BehandlingsKey("1234", 12345, LocalDate.now()), hentInntektListeResponse)

                val inntektId = getInntektId(BehandlingsKey("1234", 12345, LocalDate.now()))
                val storedInntekt = inntektId?.let { getInntekt(it) }!!
                assertNotNull(storedInntekt.inntektId)
                assertTrue("Inntekstliste should be in the same state") { hentInntektListeResponse == storedInntekt.inntekt }
                assertFalse("Inntekt is manually edited") { storedInntekt.manueltRedigert }
            }
        }
    }

    @Test
    fun ` Inntekt not present should give null StoredInntekt`() {

        withMigratedDb {
            with(PostgresInntektStore(DataSource.instance)) {
                val inntektId = getInntektId(BehandlingsKey("7890", 7890, LocalDate.now()))
                assertNull(inntektId)
            }
        }
    }

    @Test
    fun `getInntektId should return latest InntektId`() {
        withMigratedDb {
            with(PostgresInntektStore(DataSource.instance)) {
                val hentInntektListeResponse = InntektkomponentResponse(
                    emptyList(),
                    Aktoer(AktoerType.AKTOER_ID, "1234")
                )

                val behandlingsKey = BehandlingsKey("1234", 12345, LocalDate.now())

                insertInntekt(behandlingsKey, hentInntektListeResponse)
                val lastStoredInntekt = insertInntekt(behandlingsKey, hentInntektListeResponse)

                val latestInntektId = getInntektId(behandlingsKey)

                assertEquals(lastStoredInntekt.inntektId, latestInntektId)
            }
        }
    }

    @Test
    fun ` Sucessfully get beregningsdato`() {

        withMigratedDb {
            with(PostgresInntektStore(DataSource.instance)) {
                val hentInntektListeResponse = InntektkomponentResponse(
                    emptyList(),
                    Aktoer(AktoerType.AKTOER_ID, "1234")
                )
                val inntekt = insertInntekt(
                    BehandlingsKey("1234", 12345, LocalDate.of(2019, 4, 14)),
                    hentInntektListeResponse
                )

                val beregningsdato = getBeregningsdato(inntekt.inntektId)

                assertNotNull(beregningsdato)
                assertEquals(LocalDate.of(2019, 4, 14), beregningsdato)
            }
        }
    }

    @Test
    fun ` Getting beregningsdato for unknown inntektId should throw error`() {

        withMigratedDb {
            with(PostgresInntektStore(DataSource.instance)) {
                val result = runCatching {
                    getBeregningsdato(InntektId("12ARZ3NDEKTSV4RRFFQ69G5FBY"))
                }
                assertTrue("Result is not failure") { result.isFailure }
                assertTrue("Result is $result") { result.exceptionOrNull() is InntektNotFoundException }
            }
        }
    }
}

private fun withCleanDb(test: () -> Unit) = DataSource.instance.also { clean(it) }.run { test() }

private fun withMigratedDb(test: () -> Unit) =
    DataSource.instance.also { clean(it) }.also { migrate(it) }.run { test() }

private object PostgresContainer {
    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:11.2").apply {
            start()
        }
    }
}

private object DataSource {
    val instance: HikariDataSource by lazy {
        HikariDataSource().apply {
            username = PostgresContainer.instance.username
            password = PostgresContainer.instance.password
            jdbcUrl = PostgresContainer.instance.jdbcUrl
            connectionTimeout = 1000L
        }
    }
}
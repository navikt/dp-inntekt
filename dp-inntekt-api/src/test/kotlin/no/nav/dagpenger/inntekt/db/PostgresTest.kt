package no.nav.dagpenger.inntekt.db

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import no.nav.dagpenger.inntekt.Configuration
import no.nav.dagpenger.inntekt.DataSource
import no.nav.dagpenger.inntekt.dummyConfigs
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.withCleanDb
import no.nav.dagpenger.inntekt.withMigratedDb
import no.nav.dagpenger.inntekt.withProps
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class PostgresTest {

    @Test
    fun `Migration scripts are applied successfully`() {
        withCleanDb {
            val migrations = migrate(DataSource.instance)
            assertEquals(12, migrations, "Wrong number of migrations")
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
            assertEquals(17, migrations, "Wrong number of migrations")
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
                val parameters = Inntektparametre("1234", LocalDate.now(), RegelKontekst("1234"))
                val hentInntektListeResponse = InntektkomponentResponse(
                    emptyList(),
                    Aktoer(AktoerType.AKTOER_ID, "1234")
                )

                val storedInntekt = storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = parameters,
                        inntekt = hentInntektListeResponse
                    )
                )
                assertNotNull(storedInntekt.inntektId)
                assertTrue("Inntekstliste should be in the same state") { hentInntektListeResponse == storedInntekt.inntekt }

                val storedInntektByRequest = getInntekt(storedInntekt.inntektId)
                assertTrue("Inntekstliste should be in the same state") { storedInntekt == storedInntektByRequest }

                assertNull(getManueltRedigert(storedInntektByRequest.inntektId))
            }
        }
    }

    @Test
    fun ` Should insert different inntekt based on different aktørid and same vedtak id `() {

        val aktørId1 = "1234"
        val aktørId2 = "5678"

        withMigratedDb {

            with(PostgresInntektStore(DataSource.instance)) {

                val aktør1 =
                    Inntektparametre(aktørId = aktørId1, beregningsdato = LocalDate.now(), regelkontekst = RegelKontekst("1234"))
                val aktør2 =
                    Inntektparametre(aktørId = aktørId2, beregningsdato = LocalDate.now(), regelkontekst = RegelKontekst("1234"))
                storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = aktør1,
                        inntekt = InntektkomponentResponse(
                            emptyList(),
                            Aktoer(AktoerType.AKTOER_ID, aktørId1)
                        )
                    )
                )

                storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = aktør2,
                        inntekt = InntektkomponentResponse(
                            emptyList(),
                            Aktoer(AktoerType.AKTOER_ID, aktørId2)
                        )
                    )
                )

                assertSoftly {
                    getInntektId(aktør1) shouldNotBe null
                    getInntektId(aktør2) shouldNotBe null
                    assertNotEquals(getInntektId(aktør2), getInntektId(aktør1))
                    getInntektId(Inntektparametre(aktørId = aktørId2, beregningsdato = LocalDate.now(), regelkontekst = RegelKontekst("464664"))) shouldBe null
                    getInntektId(Inntektparametre(aktørId = "3535535335", beregningsdato = LocalDate.now(), regelkontekst = RegelKontekst("1234"))) shouldBe null
                }
            }
        }
    }

    @Test
    fun `Successful insert of inntekter which is manuelt redigert`() {
        withMigratedDb {
            with(PostgresInntektStore(DataSource.instance)) {
                val parameters = Inntektparametre("1234", LocalDate.now(), RegelKontekst("1234"))
                val hentInntektListeResponse = InntektkomponentResponse(
                    emptyList(),
                    Aktoer(AktoerType.AKTOER_ID, "1234")
                )
                val manueltRedigert = ManueltRedigert("user")

                val storedInntekt = storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = parameters,
                        inntekt = hentInntektListeResponse,
                        manueltRedigert = manueltRedigert
                    )
                )
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

                storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = Inntektparametre(aktørId = "1234", beregningsdato = LocalDate.now(), regelkontekst = RegelKontekst("12345")),
                        inntekt = hentInntektListeResponse
                    )
                )

                val inntektId = getInntektId(Inntektparametre("1234", LocalDate.now(), RegelKontekst("12345")))
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
                val inntektId = getInntektId(Inntektparametre("7890", LocalDate.now(), RegelKontekst("7890")))
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

                val parameters = Inntektparametre("1234", LocalDate.now(), RegelKontekst("12345"))

                storeInntekt(StoreInntektCommand(inntektparametre = parameters, inntekt = hentInntektListeResponse))
                val lastStoredInntekt = storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = parameters,
                        inntekt = hentInntektListeResponse
                    )
                )

                val latestInntektId = getInntektId(parameters)

                assertEquals(lastStoredInntekt.inntektId, latestInntektId)
            }
        }
    }

    @Test
    fun `Should get spesifisert inntekt`() {
        withMigratedDb {
            with(PostgresInntektStore(DataSource.instance)) {
                val hentInntektListeResponse = InntektkomponentResponse(
                    emptyList(),
                    Aktoer(AktoerType.AKTOER_ID, "1234")
                )

                val parameters = Inntektparametre("1234", LocalDate.now(), RegelKontekst("12345"))

                val lastStoredInntekt = storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = parameters,
                        inntekt = hentInntektListeResponse
                    )
                )

                val spesifisertInntekt = getSpesifisertInntekt(lastStoredInntekt.inntektId)
                spesifisertInntekt.inntektId.id shouldBe lastStoredInntekt.inntektId.id
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

                val inntekt = storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = Inntektparametre("1234", LocalDate.of(2019, 4, 14), RegelKontekst("12345")),
                        inntekt = hentInntektListeResponse
                    )
                )

                val beregningsdato = getBeregningsdato(inntekt.inntektId)

                assertNotNull(beregningsdato)
                assertEquals(LocalDate.of(2019, 4, 14), beregningsdato)
            }
        }
    }

    @Test
    fun `Sucessfully get beregningsdato from backup table`() {
        withCleanDb {
            migrate(DataSource.instance, locations = listOf("db/migration", "unit-testdata"))

            with(PostgresInntektStore(DataSource.instance)) {
                val beregningsDatoFromMain = getBeregningsdato(InntektId("01E46501PMY105AFXE4XF088MV"))
                assertNotNull(beregningsDatoFromMain)
                assertEquals(LocalDate.of(2019, 1, 1), beregningsDatoFromMain)

                val beregningsDatoFromBackup = getBeregningsdato(InntektId("01EDBSHDENAHCVBYT02W160E6X"))
                assertNotNull(beregningsDatoFromBackup)
                assertEquals(LocalDate.of(2019, 3, 3), beregningsDatoFromBackup)
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

    @Test
    fun ` Should mark an inntekt as used `() {
        withMigratedDb {
            with(PostgresInntektStore(DataSource.instance)) {
                val hentInntektListeResponse = InntektkomponentResponse(
                    emptyList(),
                    Aktoer(AktoerType.AKTOER_ID, "1234")
                )

                val storedInntekt = storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = Inntektparametre("1234", LocalDate.now(), RegelKontekst("12345")),
                        inntekt = hentInntektListeResponse
                    )
                )
                val updated = markerInntektBrukt(storedInntekt.inntektId)
                updated shouldBe 1
            }
        }
    }
}

internal class InntektsStorePropertyTest : StringSpec() {

    init {
        withMigratedDb {
            val store = PostgresInntektStore(DataSource.instance)

            "Alle inntekter skal kunne hentes når de lagres" {
                checkAll(storeInntekCommandGenerator) { command: StoreInntektCommand ->
                    val stored = store.storeInntekt(command)
                    store.getInntektId(command.inntektparametre) shouldBe stored.inntektId
                }
            }

            "Alle inntekter skal kunne hentes når de lagres med samme vedtak id men forskjellig person uten fødselsnummer" {
                checkAll(storeInntektCommandGeneratorWithSameVedtakidAndBeregningsDato) { command: StoreInntektCommand ->
                    val stored = store.storeInntekt(command)
                    store.getInntektId(command.inntektparametre) shouldBe stored.inntektId
                }
            }
        }
    }

    private val storeInntekCommandGenerator = arb {
        val stringArb = Arb.string(10, 11)
        generateSequence {
            StoreInntektCommand(
                inntektparametre = Inntektparametre(
                    aktørId = stringArb.next(it),
                    regelkontekst = RegelKontekst(stringArb.next(it)),
                    fødselnummer = stringArb.next(it),
                    beregningsdato = Arb.localDateTime(minYear = 2010, maxYear = LocalDate.now().year).next(it).toLocalDate()
                ),
                inntekt = InntektkomponentResponse(
                    arbeidsInntektMaaned = emptyList(),
                    ident = Aktoer(AktoerType.AKTOER_ID, "1234")
                )
            )
        }
    }

    private val storeInntektCommandGeneratorWithSameVedtakidAndBeregningsDato = arb {
        val stringArb = Arb.string(10, 11)
        generateSequence {
            StoreInntektCommand(
                inntektparametre = Inntektparametre(
                    aktørId = stringArb.next(it),
                    regelkontekst = RegelKontekst("12345"),
                    fødselnummer = null,
                    beregningsdato = LocalDate.now()
                ),
                inntekt = InntektkomponentResponse(
                    arbeidsInntektMaaned = emptyList(),
                    ident = Aktoer(AktoerType.AKTOER_ID, "1234")
                )
            )
        }
    }
}

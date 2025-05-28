package no.nav.dagpenger.inntekt.db

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import no.nav.dagpenger.inntekt.Postgres.withCleanDb
import no.nav.dagpenger.inntekt.Postgres.withMigratedDb
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
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
            val migrations = PostgresDataSourceBuilder.runMigration()
            assertEquals(18, migrations, "Wrong number of migrations")
        }
    }

    @Test
    fun `Migration scripts are idempotent`() {
        withMigratedDb {
            val migrations = PostgresDataSourceBuilder.runMigration()
            assertEquals(0, migrations, "Wrong number of migrations")
        }
    }

    @Test
    fun `Migration of testdata `() {
        withCleanDb {
            val migrations = PostgresDataSourceBuilder.runMigration(locations = listOf("db/migration", "db/testdata"))
            assertEquals(23, migrations, "Wrong number of migrations")
        }
    }
}

internal class PostgresInntektStoreTest {
    @Test
    fun `Successful insert of inntekter`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val parameters = Inntektparametre("1234", "1234", LocalDate.now(), RegelKontekst("1234", "vedtak"))
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )

                val storedInntekt =
                    storeInntekt(
                        StoreInntektCommand(
                            inntektparametre = parameters,
                            inntekt = hentInntektListeResponse,
                        ),
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
        val ident1 = "1234"
        val ident2 = "5678"

        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val aktør1 =
                    Inntektparametre(
                        aktørId = ident1,
                        fødselsnummer = ident1,
                        beregningsdato = LocalDate.now(),
                        regelkontekst = RegelKontekst("1234", "vedtak"),
                    )
                val aktør2 =
                    Inntektparametre(
                        aktørId = ident2,
                        fødselsnummer = ident2,
                        beregningsdato = LocalDate.now(),
                        regelkontekst = RegelKontekst("1234", "vedtak"),
                    )
                storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = aktør1,
                        inntekt =
                            InntektkomponentResponse(
                                emptyList(),
                                Aktoer(AktoerType.AKTOER_ID, ident1),
                            ),
                    ),
                )

                storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = aktør2,
                        inntekt =
                            InntektkomponentResponse(
                                emptyList(),
                                Aktoer(AktoerType.AKTOER_ID, ident1),
                            ),
                    ),
                )

                assertSoftly {
                    getInntektId(aktør1) shouldNotBe null
                    getInntektId(aktør2) shouldNotBe null
                    assertNotEquals(getInntektId(aktør2), getInntektId(aktør1))
                    getInntektId(
                        Inntektparametre(
                            aktørId = ident1,
                            fødselsnummer = ident1,
                            beregningsdato = LocalDate.now(),
                            regelkontekst = RegelKontekst("464664", "vedtak"),
                        ),
                    ) shouldBe null
                    getInntektId(
                        Inntektparametre(
                            aktørId = "3535535335",
                            fødselsnummer = "3535535335",
                            beregningsdato = LocalDate.now(),
                            regelkontekst = RegelKontekst("1234", "vedtak"),
                        ),
                    ) shouldBe null
                }
            }
        }
    }

    @Test
    fun ` Should fetch different inntekt based on different konteksttype and same vedtak id and same aktørid`() {
        val ident1 = "1234"

        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val aktør1 =
                    Inntektparametre(
                        aktørId = ident1,
                        fødselsnummer = ident1,
                        beregningsdato = LocalDate.now(),
                        regelkontekst = RegelKontekst("1234", "veiledning"),
                    )
                val aktør2 =
                    Inntektparametre(
                        aktørId = ident1,
                        fødselsnummer = ident1,
                        beregningsdato = LocalDate.now(),
                        regelkontekst = RegelKontekst("1234", "saksbehandling"),
                    )
                storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = aktør1,
                        inntekt =
                            InntektkomponentResponse(
                                emptyList(),
                                Aktoer(AktoerType.AKTOER_ID, ident1),
                            ),
                    ),
                )

                storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = aktør2,
                        inntekt =
                            InntektkomponentResponse(
                                emptyList(),
                                Aktoer(AktoerType.AKTOER_ID, ident1),
                            ),
                    ),
                )

                assertSoftly {
                    getInntektId(aktør1) shouldNotBe null
                    getInntektId(aktør2) shouldNotBe null
                    assertNotEquals(getInntektId(aktør2), getInntektId(aktør1))
                }
            }
        }
    }

    @Test
    fun `Successful insert of inntekter which is manuelt redigert`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val parameters = Inntektparametre("1234", "1234", LocalDate.now(), RegelKontekst("1234", "vedtak"))
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )
                val manueltRedigert = ManueltRedigert("user", "Dette er en begrunnelse.")

                val storedInntekt =
                    storeInntekt(
                        StoreInntektCommand(
                            inntektparametre = parameters,
                            inntekt = hentInntektListeResponse,
                            manueltRedigert = manueltRedigert,
                        ),
                    )
                val storedInntektByRequest = getInntekt(storedInntekt.inntektId)
                val storedManueltRedigert = getManueltRedigert(storedInntekt.inntektId)
                storedManueltRedigert shouldNotBe null
                storedManueltRedigert shouldBe manueltRedigert
                storedInntekt.manueltRedigert shouldBe true
                storedInntektByRequest.manueltRedigert shouldBe true
            }
        }
    }

    @Test
    fun `Lagring av inntekt skal kaste IllegalArgumentException når begrunnelse er lengre enn 1024 tegn`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val parameters = Inntektparametre("1234", "1234", LocalDate.now(), RegelKontekst("1234", "vedtak"))
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )
                val manueltRedigert = ManueltRedigert("user", "A".repeat(1025))

                val exception =
                    shouldThrow<IllegalArgumentException> {
                        storeInntekt(
                            StoreInntektCommand(
                                inntektparametre = parameters,
                                inntekt = hentInntektListeResponse,
                                manueltRedigert = manueltRedigert,
                            ),
                        )
                    }
                exception shouldHaveMessage "Begrunnelsen kan ikke være lengre enn 1024 tegn."
            }
        }
    }

    @Test
    fun `getStoredInntektMedMetadata returnerer forventet resultat når inntekten ikke er manuelt redigert`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val parameters = Inntektparametre("1234", "1234", LocalDate.now(), RegelKontekst("1234", "vedtak"))
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )
                storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = parameters,
                        inntekt = hentInntektListeResponse,
                        manueltRedigert = null,
                    ),
                )
            }
        }
    }

    @Test
    fun `getStoredInntektMedMetadata returnerer forventet resultat når inntekten er manuelt redigert`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val parameters = Inntektparametre("1234", "1234", LocalDate.now(), RegelKontekst("1234", "vedtak"))
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )
                val manueltRedigert = ManueltRedigert("user", "Dette er en begrunnelse.")
                storeInntekt(
                    StoreInntektCommand(
                        inntektparametre = parameters,
                        inntekt = hentInntektListeResponse,
                        manueltRedigert = manueltRedigert,
                    ),
                )
            }
        }
    }

    @Test
    fun ` Sucessfully get inntekter`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )

                storeInntekt(
                    StoreInntektCommand(
                        inntektparametre =
                            Inntektparametre(
                                aktørId = "1234",
                                fødselsnummer = "1234",
                                beregningsdato = LocalDate.now(),
                                regelkontekst = RegelKontekst("12345", "vedtak"),
                            ),
                        inntekt = hentInntektListeResponse,
                    ),
                )

                val inntektId =
                    getInntektId(Inntektparametre("1234", "1234", LocalDate.now(), RegelKontekst("12345", "vedtak")))
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
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val inntektId =
                    getInntektId(Inntektparametre("7890", "7890", LocalDate.now(), RegelKontekst("7890", "vedtak")))
                assertNull(inntektId)
            }
        }
    }

    @Test
    fun `getInntektId should return latest InntektId`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )

                val parameters = Inntektparametre("1234", "1234", LocalDate.now(), RegelKontekst("12345", "vedtak"))

                storeInntekt(StoreInntektCommand(inntektparametre = parameters, inntekt = hentInntektListeResponse))
                val lastStoredInntekt =
                    storeInntekt(
                        StoreInntektCommand(
                            inntektparametre = parameters,
                            inntekt = hentInntektListeResponse,
                        ),
                    )

                val latestInntektId = getInntektId(parameters)

                assertEquals(lastStoredInntekt.inntektId, latestInntektId)
            }
        }
    }

    @Test
    fun `Should get spesifisert inntekt`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )

                val parameters = Inntektparametre("1234", "1234", LocalDate.now(), RegelKontekst("12345", "vedtak"))

                val lastStoredInntekt =
                    storeInntekt(
                        StoreInntektCommand(
                            inntektparametre = parameters,
                            inntekt = hentInntektListeResponse,
                        ),
                    )

                val spesifisertInntekt = getSpesifisertInntekt(lastStoredInntekt.inntektId)
                spesifisertInntekt.inntektId.id shouldBe lastStoredInntekt.inntektId.id
            }
        }
    }

    @Test
    fun ` Sucessfully get beregningsdato`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )

                val inntekt =
                    storeInntekt(
                        StoreInntektCommand(
                            inntektparametre =
                                Inntektparametre(
                                    "1234",
                                    "1234",
                                    LocalDate.of(2019, 4, 14),
                                    RegelKontekst("12345", "vedtak"),
                                ),
                            inntekt = hentInntektListeResponse,
                        ),
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
            PostgresDataSourceBuilder.runMigration(locations = listOf("db/migration", "unit-testdata"))

            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
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
    fun `Sucessfully  migrates from vedtakid and converts to konteksttype`() {
        withCleanDb {
            PostgresDataSourceBuilder.runMigration(locations = listOf("db/migration", "unit-testdata"))

            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val inntektsid =
                    getInntektId(
                        Inntektparametre(
                            "AKTØR_ID",
                            "fnr",
                            LocalDate.of(2019, 1, 1),
                            RegelKontekst("-1337", "forskudd"),
                        ),
                    )
                assertNotNull(inntektsid)
            }
        }
    }

    @Test
    fun ` Getting beregningsdato for unknown inntektId should throw error`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val result =
                    runCatching {
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
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )

                val storedInntekt =
                    storeInntekt(
                        StoreInntektCommand(
                            inntektparametre =
                                Inntektparametre(
                                    "1234",
                                    "1234",
                                    LocalDate.now(),
                                    RegelKontekst("12345", "vedtak"),
                                ),
                            inntekt = hentInntektListeResponse,
                        ),
                    )
                val updated = markerInntektBrukt(storedInntekt.inntektId)
                updated shouldBe 1
            }
        }
    }

    @Test
    fun `Hent inntekt_person_mapping`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )
                val inntektparametre =
                    Inntektparametre(
                        aktørId = "12345",
                        fødselsnummer = "0987654321",
                        beregningsdato = LocalDate.now(),
                        regelkontekst = RegelKontekst("432", "vedtak"),
                    )
                val storedInntekt =
                    storeInntekt(
                        StoreInntektCommand(
                            inntektparametre = inntektparametre,
                            inntekt = hentInntektListeResponse,
                        ),
                    )

                val inntektPersonMapping = getInntektPersonMapping(storedInntekt.inntektId.id)

                inntektPersonMapping.inntektId.id shouldBe storedInntekt.inntektId.id
                inntektPersonMapping.aktørId shouldBe inntektparametre.aktørId
                inntektPersonMapping.fnr shouldBe inntektparametre.fødselsnummer
                inntektPersonMapping.kontekstId shouldBe inntektparametre.regelkontekst.id
                inntektPersonMapping.beregningsdato shouldBe inntektparametre.beregningsdato
                inntektPersonMapping.timestamp shouldNotBe null
                inntektPersonMapping.kontekstType shouldBe inntektparametre.regelkontekst.type
            }
        }
    }

    @Test
    fun `getStoredInntektMedMetadata returnerer forventet respons`() {
        withMigratedDb {
            with(PostgresInntektStore(PostgresDataSourceBuilder.dataSource)) {
                val hentInntektListeResponse =
                    InntektkomponentResponse(
                        emptyList(),
                        Aktoer(AktoerType.AKTOER_ID, "1234"),
                    )
                val inntektparametre = Inntektparametre("1234", "1234", LocalDate.now(), RegelKontekst("12345", "vedtak"))
                val storedInntekt =
                    storeInntekt(
                        StoreInntektCommand(
                            inntektparametre = inntektparametre,
                            inntekt = hentInntektListeResponse,
                        ),
                    )

                val storedInntektMedMetadata = getStoredInntektMedMetadata(storedInntekt.inntektId)

                storedInntektMedMetadata.inntektId shouldBe storedInntekt.inntektId
                storedInntektMedMetadata.inntekt shouldBe hentInntektListeResponse
                storedInntektMedMetadata.manueltRedigert shouldBe false
                storedInntektMedMetadata.fødselsnummer shouldBe "1234"
                storedInntektMedMetadata.timestamp shouldNotBe null
            }
        }
    }
}

internal class InntektsStorePropertyTest : StringSpec() {
    init {
        withMigratedDb {
            val store = PostgresInntektStore(PostgresDataSourceBuilder.dataSource)

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

    private val storeInntekCommandGenerator =
        arbitrary {
            val aktørId = Arb.string(10, 11)
            val kontekstId = Arb.string(5, 40)
            StoreInntektCommand(
                inntektparametre =
                    Inntektparametre(
                        aktørId = aktørId.next(it),
                        regelkontekst = RegelKontekst(kontekstId.next(it), "vedtak"),
                        fødselsnummer = aktørId.next(it),
                        beregningsdato =
                            Arb
                                .localDateTime(minYear = 2010, maxYear = LocalDate.now().year)
                                .next(it)
                                .toLocalDate(),
                    ),
                inntekt =
                    InntektkomponentResponse(
                        arbeidsInntektMaaned = emptyList(),
                        ident = Aktoer(AktoerType.AKTOER_ID, "1234"),
                    ),
            )
        }

    private val storeInntektCommandGeneratorWithSameVedtakidAndBeregningsDato =
        arbitrary {
            val stringArb = Arb.string(10, 11)
            val kontekstId = Arb.string(5, 40)
            StoreInntektCommand(
                inntektparametre =
                    Inntektparametre(
                        aktørId = stringArb.next(it),
                        regelkontekst = RegelKontekst(kontekstId.next(it), "vedtak"),
                        fødselsnummer = stringArb.next(it),
                        beregningsdato = LocalDate.now(),
                    ),
                inntekt =
                    InntektkomponentResponse(
                        arbeidsInntektMaaned = emptyList(),
                        ident = Aktoer(AktoerType.AKTOER_ID, "1234"),
                    ),
            )
        }
}

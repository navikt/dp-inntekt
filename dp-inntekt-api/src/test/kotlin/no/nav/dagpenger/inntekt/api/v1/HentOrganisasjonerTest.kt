package no.nav.dagpenger.inntekt.api.v1

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient
import org.junit.jupiter.api.Test

internal class HentOrganisasjonerTest {
    private val enhetsregisterClient: EnhetsregisterClient = mockk()

    @Test
    fun `hentOrganisasjoner returner en liste med orginfo med navn`() {
        runBlocking {
            coEvery { enhetsregisterClient.hentEnhet("123456789") } returns
                """
                {
                  "navn": "organisasjonsNummer789",
                  "organisasjonsnummer": "123456789"
                }
                """.trimIndent()

            coEvery { enhetsregisterClient.hentEnhet("987654321") } returns
                """
                {
                  "navn": "organisasjonsNummer321",
                  "organisasjonsnummer": "987654321"
                }
                """.trimIndent()

            val result =
                hentOrganisasjoner(
                    enhetsregisterClient,
                    listOf("123456789", "987654321"),
                )

            result shouldHaveSize 2
            result.map { it.organisasjonsnummer } shouldBe listOf("123456789", "987654321")
            result.map { it.navn } shouldBe listOf("organisasjonsNummer789", "organisasjonsNummer321")
        }
    }

    @Test
    fun `hentOrganisasjoner gjør 1 kall når det er flere av samme orgnr`() {
        runBlocking {
            coEvery { enhetsregisterClient.hentEnhet("123456789") } returns
                """{"navn": "organisasjonsNummer789", "organisasjonsnummer": "123456789"}""".trimIndent()

            val result =
                hentOrganisasjoner(
                    enhetsregisterClient,
                    listOf("123456789", "123456789", "123456789"),
                )

            result shouldHaveSize 1
            result.first().organisasjonsnummer shouldBe "123456789"
        }
    }

    @Test
    fun `hentOrganisasjoner håndterer null input`() {
        runBlocking {
            val result = hentOrganisasjoner(enhetsregisterClient, null)

            result.shouldBeEmpty()
        }
    }

    @Test
    fun `hentOrganisasjoner håndterer tom liste som parameter`() {
        runBlocking {
            val result = hentOrganisasjoner(enhetsregisterClient, emptyList())

            result.shouldBeEmpty()
        }
    }

    @Test
    fun `hentOrganisasjoner skiper failed requests og fortsetter`() {
        runBlocking {
            coEvery { enhetsregisterClient.hentEnhet("123456789") } returns
                """{"navn": "organisasjonsNummer789", "organisasjonsnummer": "123456789"}""".trimIndent()

            coEvery { enhetsregisterClient.hentEnhet("999999999") } throws
                RuntimeException("API error")

            coEvery { enhetsregisterClient.hentEnhet("987654321") } returns
                """{"navn": "organisasjonsNummer321", "organisasjonsnummer": "987654321"}""".trimIndent()

            val result =
                hentOrganisasjoner(
                    enhetsregisterClient,
                    listOf("123456789", "999999999", "987654321"),
                )

            result shouldHaveSize 2
            result.map { it.organisasjonsnummer } shouldBe listOf("123456789", "987654321")
        }
    }

    @Test
    fun `hentOrganisasjoner håndterer blandet duplicatater og feil`() {
        runBlocking {
            coEvery { enhetsregisterClient.hentEnhet("111111111") } returns
                """{"navn": "Company A", "organisasjonsnummer": "111111111"}""".trimIndent()

            coEvery { enhetsregisterClient.hentEnhet("222222222") } throws
                RuntimeException("Not found")

            coEvery { enhetsregisterClient.hentEnhet("333333333") } returns
                """{"navn": "Company C", "organisasjonsnummer": "333333333"}""".trimIndent()

            val orgNumbers =
                listOf(
                    "111111111",
                    "111111111",
                    "222222222",
                    "333333333",
                    "333333333",
                )

            val result = hentOrganisasjoner(enhetsregisterClient, orgNumbers)

            result shouldHaveSize 2
            result.map { it.organisasjonsnummer } shouldBe listOf("111111111", "333333333")
        }
    }

    @Test
    fun `hentOrganisasjoner henter orgnavn riktig`() {
        runBlocking {
            val orgName = "Test Organisasjon AS"
            coEvery { enhetsregisterClient.hentEnhet("123456789") } returns
                """
                {
                  "navn": "$orgName",
                  "organisasjonsnummer": "123456789"
                }
                """.trimIndent()

            val result =
                hentOrganisasjoner(
                    enhetsregisterClient,
                    listOf("123456789"),
                )

            result shouldHaveSize 1
            result.first().navn shouldBe orgName
        }
    }
}

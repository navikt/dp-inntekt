package no.nav.dagpenger.inntekt.oppslag

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class PersonTest {

    @Test
    fun `sammensatt navn uten mellomnavn`() {
        val person = Person(
            fødselsnummer = "ikke-relevant",
            fornavn = "Fornøyd",
            etternavn = "Hund",
            aktørId = "ikke-relevant",
            mellomnavn = null
        )
        person.sammensattNavn() shouldBe "Hund, Fornøyd"
    }

    @Test
    fun `sammensatt navn med mellomnavn`() {
        val person =
            Person(
                aktørId = "ikke-relevant",
                fødselsnummer = "ikke-relevant",
                fornavn = "Fornøyd",
                etternavn = "Hund",
                mellomnavn = "Katt",
            )
        person.sammensattNavn() shouldBe "Hund, Fornøyd Katt"
    }
}

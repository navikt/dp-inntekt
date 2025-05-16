package no.nav.dagpenger.inntekt.v1.models

import com.fasterxml.jackson.module.kotlin.readValue
import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType.NATURLIG_IDENT
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import org.junit.jupiter.api.Test
import java.time.YearMonth

class InntekterDtoTest {
    @Test
    fun `mapToStoredInntekt mapper til forventet resultat`() {
        val inntektId = ULID().nextULID()
        val inntekterDto =
            jacksonObjectMapper.readValue<InntekterDto>(
                this::class.java.getResource("/test-data/expected-uklassifisert-post-body.json")?.readText()!!,
            )

        val storedInntekt =
            inntekterDto.mapToStoredInntekt(
                inntektId = inntektId,
            )

        storedInntekt.manueltRedigert shouldBe true
        storedInntekt.timestamp.shouldNotBeNull()
        storedInntekt.inntektId.id shouldBe inntektId
        storedInntekt.inntekt.ident.aktoerType shouldBe NATURLIG_IDENT
        storedInntekt.inntekt.ident.identifikator shouldBe inntekterDto.mottaker.pnr
        storedInntekt.inntekt.arbeidsInntektMaaned?.shouldHaveSize(2)
        storedInntekt.inntekt.arbeidsInntektMaaned?.first().let {
            it?.aarMaaned shouldBe YearMonth.of(2023, 1)
            it?.avvikListe.shouldBeEmpty()
            it?.arbeidsInntektInformasjon?.inntektListe?.shouldHaveSize(2)
        }
        storedInntekt.inntekt.arbeidsInntektMaaned?.get(1).let {
            it?.aarMaaned shouldBe YearMonth.of(2024, 1)
            it?.avvikListe.shouldBeEmpty()
        }
    }
}

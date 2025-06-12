package no.nav.dagpenger.inntekt.api.v1.models

import com.fasterxml.jackson.module.kotlin.readValue
import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType.AKTOER_ID
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType.NATURLIG_IDENT
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType.ORGANISASJON
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Avvik
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse.BIL
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse.SVANGERSKAPSPENGER_FERIEPENGER
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType.LOENNSINNTEKT
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType.YTELSE_FRA_OFFENTLIGE
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Periode
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.SpesielleInntjeningsforhold.LOENN_VED_KONKURS_ELLER_STATSGARANTI_OSV
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjonsDetaljer
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class InntekterDtoTest {
    @Test
    fun `mapToStoredInntekt mapper til forventet resultat`() {
        val inntektId = ULID().nextULID()
        val inntekterDto =
            jacksonObjectMapper.readValue<InntekterDto>(
                this::class.java.getResource("/test-data/expected-uklassifisert-post-body.json")?.readText()!!,
            )

        val storedInntekt = inntekterDto.mapToStoredInntekt(inntektId = inntektId)

        storedInntekt.manueltRedigert shouldBe true
        storedInntekt.timestamp.shouldNotBeNull()
        storedInntekt.inntektId.id shouldBe inntektId
        storedInntekt.inntekt.ident.aktoerType shouldBe NATURLIG_IDENT
        storedInntekt.inntekt.ident.identifikator shouldBe inntekterDto.mottaker.pnr
        storedInntekt.inntekt.arbeidsInntektMaaned?.shouldHaveSize(5)
        storedInntekt.inntekt.arbeidsInntektMaaned?.first().let {
            it?.aarMaaned shouldBe YearMonth.of(2023, 1)
            it?.avvikListe.shouldContainExactly(
                Avvik(
                    ident = Aktoer(AKTOER_ID, "287631"),
                    opplysningspliktig = Aktoer(NATURLIG_IDENT, "06221841830"),
                    virksomhet = Aktoer(ORGANISASJON, "123456789"),
                    avvikPeriode = YearMonth.of(2023, 1),
                    tekst = "Dette er et avvik for 2023-01",
                ),
            )
            it?.arbeidsInntektInformasjon?.inntektListe?.shouldHaveSize(1)
            it?.arbeidsInntektInformasjon?.inntektListe?.shouldContain(
                Inntekt(
                    beloep = BigDecimal("50000.00"),
                    fordel = "kontantytelse",
                    beskrivelse = BIL,
                    inntektskilde = "A-ordningen",
                    inntektsstatus = "LoependeInnrapportert",
                    inntektsperiodetype = "Maaned",
                    leveringstidspunkt = YearMonth.of(2023, 1),
                    opptjeningsland = "NO",
                    opptjeningsperiode = Periode(LocalDate.of(2023, 1, 4), LocalDate.of(2023, 1, 25)),
                    skattemessigBosattLand = "NO",
                    utbetaltIMaaned = YearMonth.of(2023, 2),
                    opplysningspliktig = Aktoer(NATURLIG_IDENT, "06221841830"),
                    inntektsinnsender = Aktoer(ORGANISASJON, "561235623"),
                    virksomhet = Aktoer(ORGANISASJON, "123456789"),
                    inntektsmottaker = Aktoer(AKTOER_ID, "287631"),
                    inngaarIGrunnlagForTrekk = true,
                    utloeserArbeidsgiveravgift = false,
                    informasjonsstatus = "InngaarAlltid",
                    inntektType = LOENNSINNTEKT,
                    tilleggsinformasjon =
                        TilleggInformasjon(
                            kategori = "enKategori",
                            tilleggsinformasjonDetaljer =
                                TilleggInformasjonsDetaljer(
                                    detaljerType = "enDetaljerType",
                                    spesielleInntjeningsforhold = LOENN_VED_KONKURS_ELLER_STATSGARANTI_OSV,
                                ),
                        ),
                ),
            )
        }
        storedInntekt.inntekt.arbeidsInntektMaaned?.get(1).let {
            it?.aarMaaned shouldBe YearMonth.of(2024, 1)
            it?.avvikListe.shouldContainExactly(
                Avvik(
                    ident = Aktoer(AKTOER_ID, "287631"),
                    opplysningspliktig = Aktoer(NATURLIG_IDENT, "06221841830"),
                    virksomhet = Aktoer(ORGANISASJON, "123456789"),
                    avvikPeriode = YearMonth.of(2024, 1),
                    tekst = "Dette er et avvik",
                ),
                Avvik(
                    ident = Aktoer(AKTOER_ID, "287631"),
                    opplysningspliktig = Aktoer(NATURLIG_IDENT, "06221841830"),
                    virksomhet = Aktoer(ORGANISASJON, "987654321"),
                    avvikPeriode = YearMonth.of(2024, 1),
                    tekst = "Dette er et annet avvik",
                ),
            )
            it?.arbeidsInntektInformasjon?.inntektListe?.shouldHaveSize(2)
        }
        storedInntekt.inntekt.arbeidsInntektMaaned?.get(2).let {
            it?.aarMaaned shouldBe YearMonth.of(2025, 2)
            it?.avvikListe shouldBe null
            it?.arbeidsInntektInformasjon?.inntektListe?.shouldHaveSize(1)
            it?.arbeidsInntektInformasjon?.inntektListe?.first().let { inntekt ->
                inntekt?.tilleggsinformasjon shouldBe null
            }
        }
        storedInntekt.inntekt.arbeidsInntektMaaned?.get(3).let {
            it?.aarMaaned shouldBe YearMonth.of(2025, 3)
            it?.avvikListe shouldBe null
            it?.arbeidsInntektInformasjon?.inntektListe?.shouldHaveSize(1)
        }
        storedInntekt.inntekt.arbeidsInntektMaaned?.get(4).let {
            it?.aarMaaned shouldBe YearMonth.of(2000, 12)
            it?.avvikListe?.shouldHaveSize(1)
            it?.arbeidsInntektInformasjon?.inntektListe?.shouldBeEmpty()
        }
    }

    @Test
    fun `mapToStoredInntekt mapper til forventet resultat når inntektType er null`() {
        val inntektId = ULID().nextULID()
        val inntekterDto =
            jacksonObjectMapper.readValue<InntekterDto>(
                this::class.java
                    .getResource("/test-data/expected-uklassifisert-post-body-inntektType-er-null.json")
                    ?.readText()!!,
            )

        val storedInntekt = inntekterDto.mapToStoredInntekt(inntektId = inntektId)

        storedInntekt.inntekt.arbeidsInntektMaaned?.first()?.arbeidsInntektInformasjon?.inntektListe?.first().let {
            it?.beskrivelse shouldBe SVANGERSKAPSPENGER_FERIEPENGER
            it?.inntektType shouldBe YTELSE_FRA_OFFENTLIGE
        }
    }
}

package no.nav.dagpenger.inntekt.serder

import de.huxhorn.sulky.ulid.ULID
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.SpesifisertInntekt
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.klassifiserer.klassifiserOgMapInntekt
import no.nav.dagpenger.inntekt.mapping.GUIInntekt
import no.nav.dagpenger.inntekt.mapping.Inntektsmottaker
import no.nav.dagpenger.inntekt.mapping.mapToGUIInntekt
import no.nav.dagpenger.inntekt.mapping.mapToSpesifisertInntekt
import no.nav.dagpenger.inntekt.moshiInstance
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import java.time.YearMonth

class JsonSanityCheck {

    @ParameterizedTest
    @ValueSource(strings = ["example-inntekt-payload", "example-inntekt-avvik", "example-inntekt-spesielleinntjeningsforhold"])
    fun `moshi og jackson har samme sannhet`(fil: String) {
        val url = "/test-data/$fil.json"
        val json = this.javaClass.getResource(url)?.readText() ?: throw AssertionError("fant ikke $url")
        json shouldNotBe null

        val inntektkomponentResponse =
            moshiInstance.adapter(InntektkomponentResponse::class.java).fromJson(json)!!

        val storedInntekt = StoredInntekt(
            inntektId = InntektId(ULID().nextULID()),
            inntekt = inntektkomponentResponse,
            manueltRedigert = false,
        )

        val spesifisertInntekt = mapToSpesifisertInntekt(storedInntekt, YearMonth.of(2018, 3))
        assertSpesifisertInntekt(spesifisertInntekt)
        assertKlassifisertInntekt(spesifisertInntekt)
        assertGUIInntekt(inntektkomponentResponse)
    }

    private fun assertSpesifisertInntekt(spesifisertInntekt: SpesifisertInntekt) = assertSoftly {
        val spesifisertInntektKlasse = SpesifisertInntekt::class.java
        val moshiAdapter = moshiInstance.adapter(spesifisertInntektKlasse)
        val moshiSpesifisertInntektJson =
            moshiAdapter.toJson(spesifisertInntekt)
        val jacksonSpesifisertInntektJson = jacksonObjectMapper.writeValueAsString(spesifisertInntekt)
        moshiSpesifisertInntektJson shouldEqualJson jacksonSpesifisertInntektJson

        val spesifisertInntektJackson =
            jacksonObjectMapper.readValue(moshiSpesifisertInntektJson, spesifisertInntektKlasse)

        val spesifisertInntektMoshi =
            moshiAdapter.fromJson(jacksonSpesifisertInntektJson)!!

        spesifisertInntektJackson shouldBe spesifisertInntektMoshi
    }

    private fun assertKlassifisertInntekt(spesifisertInntekt: SpesifisertInntekt) = assertSoftly {
        val klassifisertInntekt = klassifiserOgMapInntekt(spesifisertInntekt)
        val inntektClass = Inntekt::class.java
        val moshiAdapter = moshiInstance.adapter(inntektClass)
        val moshiKlassifisertInntektJson =
            moshiAdapter.toJson(klassifisertInntekt)
        val jacksonKlassifisertInntektJson = jacksonObjectMapper.writeValueAsString(klassifisertInntekt)

        moshiKlassifisertInntektJson shouldEqualJson jacksonKlassifisertInntektJson

        val inntektJackson =
            jacksonObjectMapper.readValue(moshiKlassifisertInntektJson, inntektClass)

        val inntektMoshi =
            moshiAdapter.fromJson(jacksonKlassifisertInntektJson)!!

        inntektJackson.inntektsId shouldBe inntektMoshi.inntektsId
        inntektJackson.sisteAvsluttendeKalenderMåned shouldBe inntektMoshi.sisteAvsluttendeKalenderMåned
        inntektJackson.inntektsListe shouldBe inntektMoshi.inntektsListe
        inntektJackson.manueltRedigert shouldBe inntektMoshi.manueltRedigert
    }

    private fun assertGUIInntekt(inntektkomponentResponse: InntektkomponentResponse) = assertSoftly {
        val guiInntekt = mapToGUIInntekt(
            inntektkomponentResponse,
            Opptjeningsperiode(LocalDate.of(2018, 4, 5)),
            Inntektsmottaker("123", "Test Testen"),
        )

        val guiInntektClass = GUIInntekt::class.java
        val moshiAdapter = moshiInstance.adapter(guiInntektClass)
        val moshiGuiInntekt = moshiAdapter.toJson(guiInntekt)
        val jacksonGuiInntekt = jacksonObjectMapper.writeValueAsString(guiInntekt)

        moshiGuiInntekt shouldEqualJson jacksonGuiInntekt

        val guiInntektJackson =
            jacksonObjectMapper.readValue(moshiGuiInntekt, guiInntektClass)

        val guiInntektMoshi =
            moshiAdapter.fromJson(jacksonGuiInntekt)!!

        guiInntektJackson shouldBe guiInntektMoshi
    }
}

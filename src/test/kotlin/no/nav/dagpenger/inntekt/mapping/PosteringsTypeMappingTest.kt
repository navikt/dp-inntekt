package no.nav.dagpenger.inntekt.mapping

import no.nav.dagpenger.events.inntekt.v1.PosteringsType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.SpesielleInntjeningsforhold
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PosteringsTypeMappingTest {
    @Test
    fun `test posteringstype-mapping for Aksjer_grunnfondsbevis til underkurs`() {
        val posteringsType = PosteringsType.L_AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Annen arbeidsinntekt`() {
        val posteringsType = PosteringsType.L_ANNET
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Arbeidsopphold kost`() {
        val posteringsType = PosteringsType.L_ARBEIDSOPPHOLD_KOST
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ARBEIDSOPPHOLD_KOST, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Arbeidsopphold losji`() {
        val posteringsType = PosteringsType.L_ARBEIDSOPPHOLD_LOSJI
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ARBEIDSOPPHOLD_LOSJI, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Beregnet skatt`() {
        val posteringsType = PosteringsType.L_BEREGNET_SKATT
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BEREGNET_SKATT, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Besøksreiser hjemmet annet`() {
        val posteringsType = PosteringsType.L_BESØKSREISER_HJEMMET_ANNET
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BESOEKSREISER_HJEMMET_ANNET, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Besøksreiser hjemmet kilometergodtgjørelse bil`() {
        val posteringsType = PosteringsType.L_BESØKSREISER_HJEMMET_KILOMETERGODTGJØRELSE_BIL
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BESOEKSREISER_HJEMMET_KILOMETERGODTGJOERELSE_BIL,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Betalt utenlandsk skatt`() {
        val posteringsType = PosteringsType.L_BETALT_UTENLANDSK_SKATT
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BETALT_UTENLANDSK_SKATT, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Bil`() {
        val posteringsType = PosteringsType.L_BIL
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BIL, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Bolig`() {
        val posteringsType = PosteringsType.L_BOLIG
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BOLIG, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Bonus`() {
        val posteringsType = PosteringsType.L_BONUS
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BONUS, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Bonus fra forsvaret`() {
        val posteringsType = PosteringsType.L_BONUS_FRA_FORSVARET
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BONUS_FRA_FORSVARET, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Elektronisk kommunikasjon`() {
        val posteringsType = PosteringsType.L_ELEKTRONISK_KOMMUNIKASJON
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ELEKTRONISK_KOMMUNIKASJON, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Fast bilgodtgjørelse`() {
        val posteringsType = PosteringsType.L_FAST_BILGODTGJØRELSE
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FAST_BILGODTGJOERELSE, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Faste tillegg`() {
        val posteringsType = PosteringsType.L_FAST_TILLEGG
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FAST_TILLEGG, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Fastlønn`() {
        val posteringsType = PosteringsType.L_FASTLØNN
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FASTLOENN, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Feriepenger`() {
        val posteringsType = PosteringsType.L_FERIEPENGER
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Fond for idrettsutøvere`() {
        val posteringsType = PosteringsType.L_FOND_FOR_IDRETTSUTØVERE
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FOND_FOR_IDRETTSUTOEVERE, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Foreldrepenger fra folketrygden`() {
        val posteringsType = PosteringsType.Y_FORELDREPENGER
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.FORELDREPENGER, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Helligdagstillegg`() {
        val posteringsType = PosteringsType.L_HELLIGDAGSTILLEGG
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HELLIGDAGSTILLEGG, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Honorar, akkord, prosent eller provisjonslønn`() {
        val posteringsType = PosteringsType.L_HONORAR_AKKORD_PROSENT_PROVISJON
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HONORAR_AKKORD_PROSENT_PROVISJON,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyretillegg`() {
        val posteringsType = PosteringsType.L_HYRETILLEGG
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HYRETILLEGG, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Innbetaling til utenlandsk pensjonsordning`() {
        val posteringsType = PosteringsType.L_INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Kilometergodtgjørelse bil`() {
        val posteringsType = PosteringsType.L_KILOMETERGODTGJØRELSE_BIL
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KILOMETERGODTGJOERELSE_BIL, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Kommunal omsorgslønn og fosterhjemsgodtgjørelse`() {
        val posteringsType = PosteringsType.L_KOMMUNAL_OMSORGSLØNN_OG_FOSTERHJEMSGODTGJØRELSE
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Kost (dager)`() {
        val posteringsType = PosteringsType.L_KOST_DAGER
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOST_DAGER, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Kost (døgn)`() {
        val posteringsType = PosteringsType.L_KOST_DØGN
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOST_DOEGN, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Kostbesparelse i hjemmet`() {
        val posteringsType = PosteringsType.L_KOSTBESPARELSE_I_HJEMMET
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOSTBESPARELSE_I_HJEMMET, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Losji`() {
        val posteringsType = PosteringsType.L_LOSJI
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.LOSJI, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Lønn mv som ikke er skattepliktig i Norge fra utenlandsk diplomatisk eller konsulær stasjon`() {
        val posteringsType = PosteringsType.L_IKKE_SKATTEPLIKTIG_LØNN_FRA_UTENLANDSK_DIPLOM_KONSUL_STASJON
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.IKKE_SKATTEPLIKTIG_LOENN_FRA_UTENLANDSK_DIPLOM_KONSUL_STASJON,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Lønn og godtgjørelse til dagmamma eller praktikant som passer barn i barnets hjem`() {
        val posteringsType = PosteringsType.L_LØNN_FOR_BARNEPASS_I_BARNETS_HJEM
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_FOR_BARNEPASS_I_BARNETS_HJEM,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Lønn og godtgjørelse til privatpersoner for arbeidsoppdrag i oppdragsgivers hjem`() {
        val posteringsType = PosteringsType.L_LØNN_TIL_PRIVATPERSONER_FOR_ARBEID_I_HJEMMET
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_TIL_PRIVATPERSONER_FOR_ARBEID_I_HJEMMET,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Lønn og godtgjørelse utbetalt av veldedig eller allmennyttig institusjon eller organisasjon`() {
        val posteringsType = PosteringsType.L_LØNN_UTBETALT_AV_VELDEDIG_ELLER_ALLMENNYTTIG_INSTITUSJON_ELLER_ORGANISASJON
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_UTBETALT_AV_VELDEDIG_ELLER_ALLMENNYTTIG_INSTITUSJON_ELLER_ORGANISASJON,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Lønn til verge fra Fylkesmannen`() {
        val posteringsType = PosteringsType.L_LØNN_TIL_VERGE_FRA_FYLKESMANNEN
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_TIL_VERGE_FRA_FYLKESMANNEN,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Opsjoner`() {
        val posteringsType = PosteringsType.L_OPSJONER
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.OPSJONER, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Overtidsgodtgjørelse`() {
        val posteringsType = PosteringsType.L_OVERTIDSGODTGJØRELSE
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.OVERTIDSGODTGJOERELSE, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Reise annet`() {
        val posteringsType = PosteringsType.L_REISE_ANNET
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_ANNET, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Reise kost`() {
        val posteringsType = PosteringsType.L_REISE_KOST
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_KOST, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Reise losji`() {
        val posteringsType = PosteringsType.L_REISE_LOSJI
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_LOSJI, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Rentefordel lån`() {
        val posteringsType = PosteringsType.L_RENTEFORDEL_LÅN
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.RENTEFORDEL_LAAN, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Skattepliktig del av visse typer forsikringer`() {
        val posteringsType = PosteringsType.L_SKATTEPLIKTIG_DEL_FORSIKRINGER
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SKATTEPLIKTIG_DEL_FORSIKRINGER,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Skattepliktig personalrabatt`() {
        val posteringsType = PosteringsType.L_SKATTEPLIKTIG_PERSONALRABATT
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SKATTEPLIKTIG_PERSONALRABATT,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Sluttvederlag`() {
        val posteringsType = PosteringsType.L_SLUTTVEDERLAG
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SLUTTVEDERLAG, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Smusstillegg`() {
        val posteringsType = PosteringsType.L_SMUSSTILLEGG
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SMUSSTILLEGG, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Stipend`() {
        val posteringsType = PosteringsType.L_STIPEND
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.STIPEND, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Styrehonorar og godtgjørelse i forbindelse med verv`() {
        val posteringsType = PosteringsType.L_STYREHONORAR_OG_GODTGJØRELSE_VERV
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.STYREHONORAR_OG_GODTGJOERELSE_VERV,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Svangerskapspenger`() {
        val posteringsType = PosteringsType.Y_SVANGERSKAPSPENGER
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SVANGERSKAPSPENGER, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Timelønn`() {
        val posteringsType = PosteringsType.L_TIMELØNN
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TIMELOENN, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tips`() {
        val posteringsType = PosteringsType.L_TIPS
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TIPS, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Trekk i lønn for ferie`() {
        val posteringsType = PosteringsType.L_TREKK_I_LØNN_FOR_FERIE
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Uregelmessige tillegg knyttet til arbeidet tid`() {
        val posteringsType = PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Uregelmessige tillegg knyttet til ikke-arbeidet tid`() {
        val posteringsType = PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Yrkebil tjenestligbehov kilometer`() {
        val posteringsType = PosteringsType.L_YRKEBIL_TJENESTLIGBEHOV_KILOMETER
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.YRKEBIL_TJENESTLIGBEHOV_KILOMETER,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Yrkebil tjenestligbehov listepris`() {
        val posteringsType = PosteringsType.L_YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Dagpenger ved arbeidsløshet`() {
        val posteringsType = PosteringsType.Y_DAGPENGER_VED_ARBEIDSLØSHET
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.DAGPENGER_VED_ARBEIDSLOESHET,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Dagpenger til fisker`() {
        val posteringsType = PosteringsType.N_DAGPENGER_TIL_FISKER
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.DAGPENGER_TIL_FISKER, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Dagpenger til fisker som bare har hyre`() {
        val posteringsType = PosteringsType.Y_DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Sykepenger til fisker`() {
        val posteringsType = PosteringsType.N_SYKEPENGER_TIL_FISKER
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.SYKEPENGER_TIL_FISKER, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Sykepenger til fisker som bare har hyre`() {
        val posteringsType = PosteringsType.Y_SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
            null
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Annet`() {
        val posteringsType = PosteringsType.L_ANNET_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.ANNET,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Bonus`() {
        val posteringsType = PosteringsType.L_BONUS_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BONUS,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Faste tillegg`() {
        val posteringsType = PosteringsType.L_FAST_TILLEGG_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FAST_TILLEGG,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Fastlønn`() {
        val posteringsType = PosteringsType.L_FASTLØNN_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FASTLOENN,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Feriepenger`() {
        val posteringsType = PosteringsType.L_FERIEPENGER_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FERIEPENGER,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Helligdagstillegg`() {
        val posteringsType = PosteringsType.L_HELLIGDAGSTILLEGG_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HELLIGDAGSTILLEGG,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Overtidsgodtgjørelse`() {
        val posteringsType = PosteringsType.L_OVERTIDSGODTGJØRELSE_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.OVERTIDSGODTGJOERELSE,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Sluttvederlag`() {
        val posteringsType = PosteringsType.L_SLUTTVEDERLAG_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SLUTTVEDERLAG,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Timelønn`() {
        val posteringsType = PosteringsType.L_TIMELØNN_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TIMELOENN,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Uregelmessige tillegg knyttet til arbeidet tid`() {
        val posteringsType = PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Hyre - Uregelmessige tillegg knyttet til ikke-arbeidet tid`() {
        val posteringsType = PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Lott det skal beregnes trygdeavgift av`() {
        val posteringsType = PosteringsType.N_LOTT_KUN_TRYGDEAVGIFT
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.LOTT_KUN_TRYGDEAVGIFT, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Trekk i lønn for ferie - Hyre`() {
        val posteringsType = PosteringsType.L_TREKK_I_LØNN_FOR_FERIE_H
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Vederlag lott`() {
        val posteringsType = PosteringsType.N_VEDERLAG
        val posteringsTypeInfo = PosteringsTypeInfo(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.VEDERLAG, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Sykepenger fra folketrygden`() {
        val posteringsType = PosteringsType.Y_SYKEPENGER
        val posteringsTypeInfo =
            PosteringsTypeInfo(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SYKEPENGER, null)

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Annet`() {
        val posteringsType = PosteringsType.L_ANNET_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.ANNET,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Bonus`() {
        val posteringsType = PosteringsType.L_BONUS_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BONUS,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Faste tillegg`() {
        val posteringsType = PosteringsType.L_FAST_TILLEGG_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FAST_TILLEGG,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Fastlønn`() {
        val posteringsType = PosteringsType.L_FASTLØNN_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FASTLOENN,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Feriepenger`() {
        val posteringsType = PosteringsType.L_FERIEPENGER_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FERIEPENGER,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Helligdagstillegg`() {
        val posteringsType = PosteringsType.L_HELLIGDAGSTILLEGG_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HELLIGDAGSTILLEGG,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Overtidsgodtgjørelse`() {
        val posteringsType = PosteringsType.L_OVERTIDSGODTGJØRELSE_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.OVERTIDSGODTGJOERELSE,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Sluttvederlag`() {
        val posteringsType = PosteringsType.L_SLUTTVEDERLAG_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SLUTTVEDERLAG,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Timelønn`() {
        val posteringsType = PosteringsType.L_TIMELØNN_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TIMELOENN,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Uregelmessige tillegg knyttet til arbeidet tid`() {
        val posteringsType = PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Tiltak - Uregelmessige tillegg knyttet til ikke-arbeidet tid`() {
        val posteringsType = PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }

    @Test
    fun `test posteringstype-mapping for Trekk i lønn for ferie - Tiltak`() {
        val posteringsType = PosteringsType.L_TREKK_I_LØNN_FOR_FERIE_T
        val posteringsTypeInfo = PosteringsTypeInfo(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(posteringsType, toPosteringsType(posteringsTypeInfo))
        assertEquals(posteringsTypeInfo, toPosteringsTypeInfo(posteringsType))
    }
}
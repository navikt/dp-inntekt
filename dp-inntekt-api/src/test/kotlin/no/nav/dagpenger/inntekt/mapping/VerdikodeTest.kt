package no.nav.dagpenger.inntekt.mapping

import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.SpesielleInntjeningsforhold
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VerdikodeTest {
    @Test
    fun `test verdikode-mapping for Aksjer_grunnfondsbevis til underkurs`() {
        val verdiKode = "Aksjer/grunnfondsbevis til underkurs"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Annen arbeidsinntekt - Ikke skattepliktig`() {
        val verdiKode = "Annen arbeidsinntekt - Ikke skattepliktig"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, SpesielleInntjeningsforhold.LOENN_OG_ANNEN_GODTGJOERELSE_SOM_IKKE_ER_SKATTEPLIKTIG)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Annen arbeidsinntekt - Utlandet`() {
        val verdiKode = "Annen arbeidsinntekt - Utlandet"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, SpesielleInntjeningsforhold.LOENN_UTBETALT_FRA_DEN_NORSKE_STAT_OPPTJENT_I_UTLANDET)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Annen arbeidsinntekt - Konkurs`() {
        val verdiKode = "Annen arbeidsinntekt - Konkurs"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, SpesielleInntjeningsforhold.LOENN_VED_KONKURS_ELLER_STATSGARANTI_OSV)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Annen arbeidsinntekt`() {
        val verdiKode = "Annen arbeidsinntekt"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Arbeidsopphold kost`() {
        val verdiKode = "Arbeidsopphold kost"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ARBEIDSOPPHOLD_KOST, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Arbeidsopphold losji`() {
        val verdiKode = "Arbeidsopphold losji"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ARBEIDSOPPHOLD_LOSJI, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Beregnet skatt`() {
        val verdiKode = "Beregnet skatt"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BEREGNET_SKATT, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Besøksreiser hjemmet annet`() {
        val verdiKode = "Besøksreiser hjemmet annet"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BESOEKSREISER_HJEMMET_ANNET, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Besøksreiser hjemmet kilometergodtgjørelse bil`() {
        val verdiKode = "Besøksreiser hjemmet kilometergodtgjørelse bil"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BESOEKSREISER_HJEMMET_KILOMETERGODTGJOERELSE_BIL,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Betalt utenlandsk skatt`() {
        val verdiKode = "Betalt utenlandsk skatt"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BETALT_UTENLANDSK_SKATT, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Bil`() {
        val verdiKode = "Bil"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BIL, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Bolig`() {
        val verdiKode = "Bolig"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BOLIG, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Bonus`() {
        val verdiKode = "Bonus"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BONUS, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Bonus fra forsvaret`() {
        val verdiKode = "Bonus fra forsvaret"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BONUS_FRA_FORSVARET, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Elektronisk kommunikasjon`() {
        val verdiKode = "Elektronisk kommunikasjon"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ELEKTRONISK_KOMMUNIKASJON, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Fast bilgodtgjørelse`() {
        val verdiKode = "Fast bilgodtgjørelse"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FAST_BILGODTGJOERELSE, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Faste tillegg`() {
        val verdiKode = "Faste tillegg"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FAST_TILLEGG, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Fastlønn`() {
        val verdiKode = "Fastlønn"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FASTLOENN, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Feriepenger`() {
        val verdiKode = "Feriepenger"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Fond for idrettsutøvere`() {
        val verdiKode = "Fond for idrettsutøvere"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FOND_FOR_IDRETTSUTOEVERE, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Foreldrepenger fra folketrygden`() {
        val verdiKode = "Foreldrepenger fra folketrygden"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.FORELDREPENGER, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Helligdagstillegg`() {
        val verdiKode = "Helligdagstillegg"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HELLIGDAGSTILLEGG, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Honorar, akkord, prosent eller provisjonslønn`() {
        val verdiKode = "Honorar, akkord, prosent eller provisjonslønn"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HONORAR_AKKORD_PROSENT_PROVISJON,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Honorar, akkord, prosent eller provisjonslønn`() {
        val verdiKode = "Hyre - Honorar, akkord, prosent eller provisjonslønn"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HONORAR_AKKORD_PROSENT_PROVISJON,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyretillegg`() {
        val verdiKode = "Hyretillegg"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HYRETILLEGG, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Innbetaling til utenlandsk pensjonsordning`() {
        val verdiKode = "Innbetaling til utenlandsk pensjonsordning"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Kilometergodtgjørelse bil`() {
        val verdiKode = "Kilometergodtgjørelse bil"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KILOMETERGODTGJOERELSE_BIL, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Kommunal omsorgslønn og fosterhjemsgodtgjørelse`() {
        val verdiKode = "Kommunal omsorgslønn og fosterhjemsgodtgjørelse"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Kost (dager)`() {
        val verdiKode = "Kost (dager)"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOST_DAGER, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Kost (døgn)`() {
        val verdiKode = "Kost (døgn)"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOST_DOEGN, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Kostbesparelse i hjemmet`() {
        val verdiKode = "Kostbesparelse i hjemmet"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOSTBESPARELSE_I_HJEMMET, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Losji`() {
        val verdiKode = "Losji"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.LOSJI, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Lønn mv som ikke er skattepliktig i Norge fra utenlandsk diplomatisk eller konsulær stasjon`() {
        val verdiKode = "Lønn mv som ikke er skattepliktig i Norge fra utenlandsk diplomatisk eller konsulær stasjon"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.IKKE_SKATTEPLIKTIG_LOENN_FRA_UTENLANDSK_DIPLOM_KONSUL_STASJON,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Lønn og godtgjørelse til dagmamma eller praktikant som passer barn i barnets hjem`() {
        val verdiKode = "Lønn og godtgjørelse til dagmamma eller praktikant som passer barn i barnets hjem"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_FOR_BARNEPASS_I_BARNETS_HJEM,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Lønn og godtgjørelse til privatpersoner for arbeidsoppdrag i oppdragsgivers hjem`() {
        val verdiKode = "Lønn og godtgjørelse til privatpersoner for arbeidsoppdrag i oppdragsgivers hjem"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_TIL_PRIVATPERSONER_FOR_ARBEID_I_HJEMMET,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Lønn og godtgjørelse utbetalt av veldedig eller allmennyttig institusjon eller organisasjon`() {
        val verdiKode = "Lønn og godtgjørelse utbetalt av veldedig eller allmennyttig institusjon eller organisasjon"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_UTBETALT_AV_VELDEDIG_ELLER_ALLMENNYTTIG_INSTITUSJON_ELLER_ORGANISASJON,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Lønn til verge fra Fylkesmannen`() {
        val verdiKode = "Lønn til verge fra Fylkesmannen"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_TIL_VERGE_FRA_FYLKESMANNEN,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Opsjoner`() {
        val verdiKode = "Opsjoner"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.OPSJONER, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Overtidsgodtgjørelse`() {
        val verdiKode = "Overtidsgodtgjørelse"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.OVERTIDSGODTGJOERELSE, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Reise annet`() {
        val verdiKode = "Reise annet"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_ANNET, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Reise kost`() {
        val verdiKode = "Reise kost"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_KOST, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Reise losji`() {
        val verdiKode = "Reise losji"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_LOSJI, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Rentefordel lån`() {
        val verdiKode = "Rentefordel lån"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.RENTEFORDEL_LAAN, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Skattepliktig del av visse typer forsikringer`() {
        val verdiKode = "Skattepliktig del av visse typer forsikringer"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SKATTEPLIKTIG_DEL_FORSIKRINGER,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Sluttvederlag`() {
        val verdiKode = "Sluttvederlag"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SLUTTVEDERLAG, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Smusstillegg`() {
        val verdiKode = "Smusstillegg"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SMUSSTILLEGG, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Stipend`() {
        val verdiKode = "Stipend"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.STIPEND, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Styrehonorar og godtgjørelse i forbindelse med verv`() {
        val verdiKode = "Styrehonorar og godtgjørelse i forbindelse med verv"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.STYREHONORAR_OG_GODTGJOERELSE_VERV,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Svangerskapspenger`() {
        val verdiKode = "Svangerskapspenger fra folketrygden"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SVANGERSKAPSPENGER, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Timelønn`() {
        val verdiKode = "Timelønn"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TIMELOENN, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Trekk i lønn for ferie`() {
        val verdiKode = "Trekk i lønn for ferie"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Uregelmessige tillegg knyttet til arbeidet tid`() {
        val verdiKode = "Uregelmessige tillegg knyttet til arbeidet tid"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Uregelmessige tillegg knyttet til ikke-arbeidet tid`() {
        val verdiKode = "Uregelmessige tillegg knyttet til ikke-arbeidet tid"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Yrkesbil tjenestlig behov kilometer`() {
        val verdiKode = "Yrkesbil tjenestlig behov kilometer"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.YRKEBIL_TJENESTLIGBEHOV_KILOMETER,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Yrkesbil tjenestlig behov listepris`() {
        val verdiKode = "Yrkesbil tjenestlig behov listepris"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Dagpenger ved arbeidsløshet`() {
        val verdiKode = "Dagpenger ved arbeidsløshet"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.DAGPENGER_VED_ARBEIDSLOESHET,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Dagpenger til fisker`() {
        val verdiKode = "Dagpenger til fisker"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.DAGPENGER_TIL_FISKER, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Dagpenger til fisker som bare har hyre`() {
        val verdiKode = "Dagpenger til fisker som bare har hyre"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Sykepenger til fisker`() {
        val verdiKode = "Sykepenger til fisker"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.SYKEPENGER_TIL_FISKER, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Sykepenger til fisker som bare har hyre`() {
        val verdiKode = "Sykepenger til fisker som bare har hyre"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
            null
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Annet`() {
        val verdiKode = "Hyre - Annet"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.ANNET,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Bonus`() {
        val verdiKode = "Hyre - Bonus"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BONUS,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Faste tillegg`() {
        val verdiKode = "Hyre - Faste tillegg"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FAST_TILLEGG,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Fastlønn`() {
        val verdiKode = "Hyre - Fastlønn"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FASTLOENN,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Feriepenger`() {
        val verdiKode = "Hyre - Feriepenger"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FERIEPENGER,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Helligdagstillegg`() {
        val verdiKode = "Hyre - Helligdagstillegg"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HELLIGDAGSTILLEGG,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Overtidsgodtgjørelse`() {
        val verdiKode = "Hyre - Overtidsgodtgjørelse"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.OVERTIDSGODTGJOERELSE,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Sluttvederlag`() {
        val verdiKode = "Hyre - Sluttvederlag"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SLUTTVEDERLAG,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Timelønn`() {
        val verdiKode = "Hyre - Timelønn"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TIMELOENN,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Uregelmessige tillegg knyttet til arbeidet tid`() {
        val verdiKode = "Hyre - Uregelmessige tillegg knyttet til arbeidet tid"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Uregelmessige tillegg knyttet til ikke-arbeidet tid`() {
        val verdiKode = "Hyre - Uregelmessige tillegg knyttet til ikke-arbeidet tid"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Lott det skal beregnes trygdeavgift av`() {
        val verdiKode = "Lott det skal beregnes trygdeavgift av"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.LOTT_KUN_TRYGDEAVGIFT, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Hyre - Trekk i lønn for ferie`() {
        val verdiKode = "Hyre - Trekk i lønn for ferie"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Vederlag lott`() {
        val verdiKode = "Vederlag lott"
        val datagrunnlag = DatagrunnlagKlassifisering(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.VEDERLAG, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Sykepenger fra folketrygden`() {
        val verdiKode = "Sykepenger fra folketrygden"
        val datagrunnlag =
            DatagrunnlagKlassifisering(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SYKEPENGER, null)

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Annet`() {
        val verdiKode = "Tiltak - Annet"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.ANNET,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Bonus`() {
        val verdiKode = "Tiltak - Bonus"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BONUS,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Faste tillegg`() {
        val verdiKode = "Tiltak - Faste tillegg"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FAST_TILLEGG,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Fastlønn`() {
        val verdiKode = "Tiltak - Fastlønn"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FASTLOENN,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Feriepenger`() {
        val verdiKode = "Tiltak - Feriepenger"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FERIEPENGER,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Helligdagstillegg`() {
        val verdiKode = "Tiltak - Helligdagstillegg"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HELLIGDAGSTILLEGG,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Overtidsgodtgjørelse`() {
        val verdiKode = "Tiltak - Overtidsgodtgjørelse"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.OVERTIDSGODTGJOERELSE,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Sluttvederlag`() {
        val verdiKode = "Tiltak - Sluttvederlag"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SLUTTVEDERLAG,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Timelønn`() {
        val verdiKode = "Tiltak - Timelønn"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TIMELOENN,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Uregelmessige tillegg knyttet til arbeidet tid`() {
        val verdiKode = "Tiltak - Uregelmessige tillegg knyttet til arbeidet tid"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Tiltak - Uregelmessige tillegg knyttet til ikke-arbeidet tid`() {
        val verdiKode = "Tiltak - Uregelmessige tillegg knyttet til ikke-arbeidet tid"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }

    @Test
    fun `test verdikode-mapping for Trekk i lønn for ferie - Tiltak`() {
        val verdiKode = "Tiltak - Trekk i lønn for ferie"
        val datagrunnlag = DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK
        )

        assertEquals(verdiKode, verdiKode(datagrunnlag))
        assertEquals(datagrunnlag, dataGrunnlag(verdiKode))
    }
}

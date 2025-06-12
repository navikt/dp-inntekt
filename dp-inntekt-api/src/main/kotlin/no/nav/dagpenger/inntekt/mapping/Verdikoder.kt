package no.nav.dagpenger.inntekt.mapping

import com.uchuhimo.collections.biMapOf
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.SpesielleInntjeningsforhold

fun verdiKode(datagrunnlagKlassifisering: DatagrunnlagKlassifisering): String {
    if (shouldTryGenericMappingWithoutForhold(datagrunnlagKlassifisering)) {
        return verdiKode(datagrunnlagKlassifisering.copy(forhold = null))
    }

    return dataGrunnlagKlassifiseringToVerdikode[datagrunnlagKlassifisering]
        ?: throw VerdiKodeMappingException("No verdikode found for datagrunnlagKlassifisering=$datagrunnlagKlassifisering")
}

private fun shouldTryGenericMappingWithoutForhold(datagrunnlagKlassifisering: DatagrunnlagKlassifisering) =
    !dataGrunnlagKlassifiseringToVerdikode.contains(datagrunnlagKlassifisering) && datagrunnlagKlassifisering.forhold != null

fun dataGrunnlag(verdiKode: String): DatagrunnlagKlassifisering =
    dataGrunnlagKlassifiseringToVerdikode.inverse[verdiKode]
        ?: throw VerdiKodeMappingException("No datagrunnlag found for verdikode=$verdiKode")

fun utledInntektType(inntektBeskrivelse: InntektBeskrivelse): InntektType =
    dataGrunnlagKlassifiseringToVerdikode.keys
        .find { it.beskrivelse == inntektBeskrivelse }
        ?.type
        ?: throw IllegalArgumentException("Klarte ikke å utlede inntekttype fra inntektbeskrivelse \"$inntektBeskrivelse\".")

class VerdiKodeMappingException(
    message: String,
) : RuntimeException(message)

@Suppress("ktlint:standard:max-line-length")
val dataGrunnlagKlassifiseringToVerdikode =
    biMapOf(
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS,
            null,
        ) to "Aksjer/grunnfondsbevis til underkurs",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, null) to "Annen arbeidsinntekt",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.ANNET,
            SpesielleInntjeningsforhold.LOENN_OG_ANNEN_GODTGJOERELSE_SOM_IKKE_ER_SKATTEPLIKTIG,
        ) to "Annen arbeidsinntekt - Ikke skattepliktig",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.ANNET,
            SpesielleInntjeningsforhold.LOENN_VED_KONKURS_ELLER_STATSGARANTI_OSV,
        ) to "Annen arbeidsinntekt - Konkurs",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.ANNET,
            SpesielleInntjeningsforhold.LOENN_UTBETALT_FRA_DEN_NORSKE_STAT_OPPTJENT_I_UTLANDET,
        ) to "Annen arbeidsinntekt - Utlandet",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ARBEIDSOPPHOLD_KOST, null) to "Arbeidsopphold kost",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ARBEIDSOPPHOLD_LOSJI, null) to "Arbeidsopphold losji",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BEREGNET_SKATT, null) to "Beregnet skatt",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BESOEKSREISER_HJEMMET_ANNET,
            null,
        ) to "Besøksreiser hjemmet annet",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BESOEKSREISER_HJEMMET_KILOMETERGODTGJOERELSE_BIL,
            null,
        ) to "Besøksreiser hjemmet kilometergodtgjørelse bil",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BETALT_UTENLANDSK_SKATT,
            null,
        ) to "Betalt utenlandsk skatt",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BIL, null) to "Bil",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BOLIG, null) to "Bolig",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BONUS, null) to "Bonus",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BONUS_FRA_FORSVARET, null) to "Bonus fra forsvaret",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.ELEKTRONISK_KOMMUNIKASJON,
            null,
        ) to "Elektronisk kommunikasjon",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FAST_BILGODTGJOERELSE, null) to "Fast bilgodtgjørelse",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FAST_TILLEGG, null) to "Faste tillegg",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FASTLOENN, null) to "Fastlønn",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, null) to "Feriepenger",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FOND_FOR_IDRETTSUTOEVERE,
            null,
        ) to "Fond for idrettsutøvere",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HELLIGDAGSTILLEGG, null) to "Helligdagstillegg",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HONORAR_AKKORD_PROSENT_PROVISJON,
            null,
        ) to "Honorar, akkord, prosent eller provisjonslønn",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.ANNET,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Annet",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BONUS,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Bonus",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FAST_TILLEGG,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Faste tillegg",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FASTLOENN,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Fastlønn",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FERIEPENGER,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Feriepenger",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HELLIGDAGSTILLEGG,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Helligdagstillegg",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HONORAR_AKKORD_PROSENT_PROVISJON,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Honorar, akkord, prosent eller provisjonslønn",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.OVERTIDSGODTGJOERELSE,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Overtidsgodtgjørelse",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SLUTTVEDERLAG,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Sluttvederlag",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TIMELOENN,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Timelønn",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Trekk i lønn for ferie",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Uregelmessige tillegg knyttet til arbeidet tid",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Uregelmessige tillegg knyttet til ikke-arbeidet tid",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FERIEPENGER,
            SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
        ) to "Hyre - Feriepenger",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HYRETILLEGG, null) to "Hyretillegg",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING,
            null,
        ) to "Innbetaling til utenlandsk pensjonsordning",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.KILOMETERGODTGJOERELSE_BIL,
            null,
        ) to "Kilometergodtgjørelse bil",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE,
            null,
        ) to "Kommunal omsorgslønn og fosterhjemsgodtgjørelse",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOST_DAGER, null) to "Kost (dager)",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOST_DOEGN, null) to "Kost (døgn)",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.KOSTBESPARELSE_I_HJEMMET,
            null,
        ) to "Kostbesparelse i hjemmet",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.LOSJI, null) to "Losji",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.IKKE_SKATTEPLIKTIG_LOENN_FRA_UTENLANDSK_DIPLOM_KONSUL_STASJON,
            null,
        ) to "Lønn mv som ikke er skattepliktig i Norge fra utenlandsk diplomatisk eller konsulær stasjon",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_FOR_BARNEPASS_I_BARNETS_HJEM,
            null,
        ) to "Lønn og godtgjørelse til dagmamma eller praktikant som passer barn i barnets hjem",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_TIL_PRIVATPERSONER_FOR_ARBEID_I_HJEMMET,
            null,
        ) to "Lønn og godtgjørelse til privatpersoner for arbeidsoppdrag i oppdragsgivers hjem",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_UTBETALT_AV_VELDEDIG_ELLER_ALLMENNYTTIG_INSTITUSJON_ELLER_ORGANISASJON,
            null,
        ) to "Lønn og godtgjørelse utbetalt av veldedig eller allmennyttig institusjon eller organisasjon",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_TIL_VERGE_FRA_FYLKESMANNEN,
            null,
        ) to "Lønn til verge fra Fylkesmannen",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.LOENN_TIL_VERGE_FRA_STATSFORVALTEREN,
            null,
        ) to "Lønn til verge fra Statsforvalteren",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.OPSJONER, null) to "Opsjoner",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.OVERTIDSGODTGJOERELSE, null) to "Overtidsgodtgjørelse",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_ANNET, null) to "Reise annet",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_KOST, null) to "Reise kost",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_LOSJI, null) to "Reise losji",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.RENTEFORDEL_LAAN, null) to "Rentefordel lån",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SKATTEPLIKTIG_DEL_FORSIKRINGER,
            null,
        ) to "Skattepliktig del av visse typer forsikringer",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SKATTEPLIKTIG_GODTGJOERELSE_SAERAVTALE_UTLAND,
            null,
        ) to "Skattepliktig godtgjørelse særavtale utland",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SKATTEPLIKTIG_PERSONALRABATT,
            null,
        ) to "Skattepliktig personalrabatt",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SLUTTVEDERLAG, null) to "Sluttvederlag",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SMUSSTILLEGG, null) to "Smusstillegg",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.STIPEND, null) to "Stipend",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.STYREHONORAR_OG_GODTGJOERELSE_VERV,
            null,
        ) to "Styrehonorar og godtgjørelse i forbindelse med verv",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.ANNET,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Annet",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.BONUS,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Bonus",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FAST_TILLEGG,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Faste tillegg",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FASTLOENN,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Fastlønn",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.FERIEPENGER,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Feriepenger",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.HELLIGDAGSTILLEGG,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Helligdagstillegg",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.OVERTIDSGODTGJOERELSE,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Overtidsgodtgjørelse",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.SLUTTVEDERLAG,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Sluttvederlag",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TIMELOENN,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Timelønn",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Trekk i lønn for ferie",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Uregelmessige tillegg knyttet til arbeidet tid",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
            SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK,
        ) to "Tiltak - Uregelmessige tillegg knyttet til ikke-arbeidet tid",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TIMELOENN, null) to "Timelønn",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TIPS, null) to "Tips",
        DatagrunnlagKlassifisering(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE, null) to "Trekk i lønn for ferie",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
            null,
        ) to "Uregelmessige tillegg knyttet til arbeidet tid",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
            null,
        ) to "Uregelmessige tillegg knyttet til ikke-arbeidet tid",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.YRKEBIL_TJENESTLIGBEHOV_KILOMETER,
            null,
        ) to "Yrkesbil tjenestlig behov kilometer",
        DatagrunnlagKlassifisering(
            InntektType.LOENNSINNTEKT,
            InntektBeskrivelse.YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS,
            null,
        ) to "Yrkesbil tjenestlig behov listepris",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.DAGPENGER_VED_ARBEIDSLOESHET,
            null,
        ) to "Dagpenger ved arbeidsløshet",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.DAGPENGER_VED_ARBEIDSLOESHET_FERIETILLEGG,
            null,
        ) to "Dagpenger ved arbeidsløshet - Ferietillegg",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
            null,
        ) to "Dagpenger til fisker som bare har hyre",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE_FERIETILLEGG,
            null,
        ) to "Dagpenger til fisker som bare har hyre - Ferietillegg",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.FORELDREPENGER,
            null,
        ) to "Foreldrepenger fra folketrygden",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.FORELDREPENGER_FERIEPENGER,
            null,
        ) to "Foreldrepenger fra folketrygden - Feriepenger",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.OMSORGSPENGER,
            null,
        ) to "Omsorgspenger fra folketrygden",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.OMSORGSPENGER_FERIEPENGER,
            null,
        ) to "Omsorgspenger fra folketrygden - Feriepenger",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.OPPLÆRINGSPENGER,
            null,
        ) to "Opplæringspenger fra folketrygden",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.OPPLÆRINGSPENGER_FERIEPENGER,
            null,
        ) to "Opplæringspenger fra folketrygden - Feriepenger",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.PLEIEPENGER,
            null,
        ) to "Pleiepenger fra folketrygden",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.PLEIEPENGER_FERIEPENGER,
            null,
        ) to "Pleiepenger fra folketrygden - Feriepenger",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.SVANGERSKAPSPENGER,
            null,
        ) to "Svangerskapspenger fra folketrygden",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.SVANGERSKAPSPENGER_FERIEPENGER,
            null,
        ) to "Svangerskapspenger fra folketrygden - Feriepenger",
        DatagrunnlagKlassifisering(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SYKEPENGER, null) to "Sykepenger fra folketrygden",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.SYKEPENGER_FERIEPENGER,
            null,
        ) to "Sykepenger fra folketrygden - Feriepenger",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
            null,
        ) to "Sykepenger til fisker som bare har hyre",
        DatagrunnlagKlassifisering(
            InntektType.YTELSE_FRA_OFFENTLIGE,
            InntektBeskrivelse.SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE_FERIEPENGER,
            null,
        ) to "Sykepenger til fisker som bare har hyre - Feriepenger",
        DatagrunnlagKlassifisering(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.DAGPENGER_TIL_FISKER, null) to "Dagpenger til fisker",
        DatagrunnlagKlassifisering(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.SYKEPENGER_TIL_FISKER, null) to "Sykepenger til fisker",
        DatagrunnlagKlassifisering(
            InntektType.NAERINGSINNTEKT,
            InntektBeskrivelse.LOTT_KUN_TRYGDEAVGIFT,
            null,
        ) to "Lott det skal beregnes trygdeavgift av",
        DatagrunnlagKlassifisering(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.VEDERLAG, null) to "Vederlag lott",
    )

@Deprecated(
    message = "Used  by verdikoder, need to be replaced by klassifiserer  logic",
)
data class DatagrunnlagKlassifisering(
    val type: InntektType,
    val beskrivelse: InntektBeskrivelse,
    val forhold: SpesielleInntjeningsforhold? = null,
)

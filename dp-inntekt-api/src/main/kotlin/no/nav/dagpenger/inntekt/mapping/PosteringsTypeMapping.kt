package no.nav.dagpenger.inntekt.mapping

import com.uchuhimo.collections.biMapOf
import no.nav.dagpenger.events.inntekt.v1.PosteringsType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.SpesielleInntjeningsforhold

data class PosteringsTypeGrunnlag(
    val type: InntektType,
    val beskrivelse: InntektBeskrivelse,
    val forhold: SpesielleInntjeningsforhold? = null,
)

fun toPosteringsType(posteringsTypeGrunnlag: PosteringsTypeGrunnlag): PosteringsType {
    if (shouldTryGenericMappingWithoutForhold(posteringsTypeGrunnlag)) {
        return toPosteringsType(posteringsTypeGrunnlag.copy(forhold = null))
    }

    return posteringsTypeMapping[posteringsTypeGrunnlag]
        ?: throw PosteringsTypeMappingException("No posteringsType found for posteringsTypeGrunnlag=$posteringsTypeGrunnlag")
}

private fun shouldTryGenericMappingWithoutForhold(posteringsTypeGrunnlag: PosteringsTypeGrunnlag) =
    !posteringsTypeMapping.contains(posteringsTypeGrunnlag) && posteringsTypeGrunnlag.forhold != null

fun toPosteringsTypeGrunnlag(posteringsType: PosteringsType): PosteringsTypeGrunnlag {
    return posteringsTypeMapping.inverse[posteringsType]
        ?: throw PosteringsTypeMappingException("No posteringsTypeGrunnlag found for posteringsType=$posteringsType")
}

@Suppress("ktlint:standard:max-line-length")
private val posteringsTypeMapping =
    biMapOf(
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS, null) to PosteringsType.L_AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, null) to PosteringsType.L_ANNET, PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ARBEIDSOPPHOLD_KOST, null) to PosteringsType.L_ARBEIDSOPPHOLD_KOST,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ARBEIDSOPPHOLD_LOSJI, null) to PosteringsType.L_ARBEIDSOPPHOLD_LOSJI,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BEREGNET_SKATT, null) to PosteringsType.L_BEREGNET_SKATT,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BESOEKSREISER_HJEMMET_ANNET, null) to PosteringsType.L_BESØKSREISER_HJEMMET_ANNET,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BESOEKSREISER_HJEMMET_KILOMETERGODTGJOERELSE_BIL, null) to PosteringsType.L_BESØKSREISER_HJEMMET_KILOMETERGODTGJØRELSE_BIL,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BETALT_UTENLANDSK_SKATT, null) to PosteringsType.L_BETALT_UTENLANDSK_SKATT,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BIL, null) to PosteringsType.L_BIL,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BOLIG, null) to PosteringsType.L_BOLIG,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BONUS, null) to PosteringsType.L_BONUS,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BONUS_FRA_FORSVARET, null) to PosteringsType.L_BONUS_FRA_FORSVARET,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ELEKTRONISK_KOMMUNIKASJON, null) to PosteringsType.L_ELEKTRONISK_KOMMUNIKASJON,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FAST_BILGODTGJOERELSE, null) to PosteringsType.L_FAST_BILGODTGJØRELSE,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FAST_TILLEGG, null) to PosteringsType.L_FAST_TILLEGG,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FASTLOENN, null) to PosteringsType.L_FASTLØNN,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, null) to PosteringsType.L_FERIEPENGER,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FOND_FOR_IDRETTSUTOEVERE, null) to PosteringsType.L_FOND_FOR_IDRETTSUTØVERE,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.FORELDREPENGER, null) to PosteringsType.Y_FORELDREPENGER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.FORELDREPENGER_FERIEPENGER, null) to PosteringsType.Y_FORELDREPENGER_FERIEPENGER,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HELLIGDAGSTILLEGG, null) to PosteringsType.L_HELLIGDAGSTILLEGG,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HONORAR_AKKORD_PROSENT_PROVISJON, null) to PosteringsType.L_HONORAR_AKKORD_PROSENT_PROVISJON,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HONORAR_AKKORD_PROSENT_PROVISJON, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_HONORAR_AKKORD_PROSENT_PROVISJON_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HYRETILLEGG, null) to PosteringsType.L_HYRETILLEGG,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING, null) to PosteringsType.L_INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KILOMETERGODTGJOERELSE_BIL, null) to PosteringsType.L_KILOMETERGODTGJØRELSE_BIL,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE, null) to PosteringsType.L_KOMMUNAL_OMSORGSLØNN_OG_FOSTERHJEMSGODTGJØRELSE,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOST_DAGER, null) to PosteringsType.L_KOST_DAGER,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOST_DOEGN, null) to PosteringsType.L_KOST_DØGN,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.KOSTBESPARELSE_I_HJEMMET, null) to PosteringsType.L_KOSTBESPARELSE_I_HJEMMET,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.LOSJI, null) to PosteringsType.L_LOSJI,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.IKKE_SKATTEPLIKTIG_LOENN_FRA_UTENLANDSK_DIPLOM_KONSUL_STASJON, null) to PosteringsType.L_IKKE_SKATTEPLIKTIG_LØNN_FRA_UTENLANDSK_DIPLOM_KONSUL_STASJON,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.LOENN_FOR_BARNEPASS_I_BARNETS_HJEM, null) to PosteringsType.L_LØNN_FOR_BARNEPASS_I_BARNETS_HJEM,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.LOENN_TIL_PRIVATPERSONER_FOR_ARBEID_I_HJEMMET, null) to PosteringsType.L_LØNN_TIL_PRIVATPERSONER_FOR_ARBEID_I_HJEMMET,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.LOENN_UTBETALT_AV_VELDEDIG_ELLER_ALLMENNYTTIG_INSTITUSJON_ELLER_ORGANISASJON, null) to PosteringsType.L_LØNN_UTBETALT_AV_VELDEDIG_ELLER_ALLMENNYTTIG_INSTITUSJON_ELLER_ORGANISASJON,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.LOENN_TIL_VERGE_FRA_FYLKESMANNEN, null) to PosteringsType.L_LØNN_TIL_VERGE_FRA_FYLKESMANNEN,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.LOENN_TIL_VERGE_FRA_STATSFORVALTEREN, null) to PosteringsType.L_LØNN_TIL_VERGE_FRA_STATSFORVALTEREN,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.OPSJONER, null) to PosteringsType.L_OPSJONER,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.OVERTIDSGODTGJOERELSE, null) to PosteringsType.L_OVERTIDSGODTGJØRELSE,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_ANNET, null) to PosteringsType.L_REISE_ANNET,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_KOST, null) to PosteringsType.L_REISE_KOST,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.REISE_LOSJI, null) to PosteringsType.L_REISE_LOSJI,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.RENTEFORDEL_LAAN, null) to PosteringsType.L_RENTEFORDEL_LÅN,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SKATTEPLIKTIG_DEL_FORSIKRINGER, null) to PosteringsType.L_SKATTEPLIKTIG_DEL_FORSIKRINGER,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SLUTTVEDERLAG, null) to PosteringsType.L_SLUTTVEDERLAG,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SMUSSTILLEGG, null) to PosteringsType.L_SMUSSTILLEGG,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.STIPEND, null) to PosteringsType.L_STIPEND,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.STYREHONORAR_OG_GODTGJOERELSE_VERV, null) to PosteringsType.L_STYREHONORAR_OG_GODTGJØRELSE_VERV,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SVANGERSKAPSPENGER, null) to PosteringsType.Y_SVANGERSKAPSPENGER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SVANGERSKAPSPENGER_FERIEPENGER, null) to PosteringsType.Y_SVANGERSKAPSPENGER_FERIEPENGER,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TIMELOENN, null) to PosteringsType.L_TIMELØNN,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE, null) to PosteringsType.L_TREKK_I_LØNN_FOR_FERIE,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID, null) to PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID, null) to PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.YRKEBIL_TJENESTLIGBEHOV_KILOMETER, null) to PosteringsType.L_YRKEBIL_TJENESTLIGBEHOV_KILOMETER,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS, null) to PosteringsType.L_YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.DAGPENGER_VED_ARBEIDSLOESHET, null) to PosteringsType.Y_DAGPENGER_VED_ARBEIDSLØSHET,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.DAGPENGER_VED_ARBEIDSLOESHET_FERIETILLEGG, null) to PosteringsType.Y_DAGPENGER_VED_ARBEIDSLØSHET_FERIETILLEGG,
        PosteringsTypeGrunnlag(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.DAGPENGER_TIL_FISKER, null) to PosteringsType.N_DAGPENGER_TIL_FISKER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE, null) to PosteringsType.Y_DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE_FERIETILLEGG, null) to PosteringsType.Y_DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE_FERIETILLEGG,
        PosteringsTypeGrunnlag(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.SYKEPENGER_TIL_FISKER, null) to PosteringsType.N_SYKEPENGER_TIL_FISKER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE, null) to PosteringsType.Y_SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE_FERIEPENGER, null) to PosteringsType.Y_SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE_FERIEPENGER,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_ANNET_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BONUS, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_BONUS_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FAST_TILLEGG, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_FAST_TILLEGG_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FASTLOENN, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_FASTLØNN_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_FERIEPENGER_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HELLIGDAGSTILLEGG, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_HELLIGDAGSTILLEGG_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.OVERTIDSGODTGJOERELSE, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_OVERTIDSGODTGJØRELSE_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SLUTTVEDERLAG, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_SLUTTVEDERLAG_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TIMELOENN, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_TIMELØNN_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID_H,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID_H,
        PosteringsTypeGrunnlag(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.LOTT_KUN_TRYGDEAVGIFT, null) to PosteringsType.N_LOTT_KUN_TRYGDEAVGIFT,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE, SpesielleInntjeningsforhold.HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY) to PosteringsType.L_TREKK_I_LØNN_FOR_FERIE_H,
        PosteringsTypeGrunnlag(InntektType.NAERINGSINNTEKT, InntektBeskrivelse.VEDERLAG, null) to PosteringsType.N_VEDERLAG,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SYKEPENGER, null) to PosteringsType.Y_SYKEPENGER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.SYKEPENGER_FERIEPENGER, null) to PosteringsType.Y_SYKEPENGER_FERIEPENGER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.PLEIEPENGER, null) to PosteringsType.Y_PLEIEPENGER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.PLEIEPENGER_FERIEPENGER, null) to PosteringsType.Y_PLEIEPENGER_FERIEPENGER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.OMSORGSPENGER, null) to PosteringsType.Y_OMSORGSPENGER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.OMSORGSPENGER_FERIEPENGER, null) to PosteringsType.Y_OMSORGSPENGER_FERIEPENGER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.OPPLÆRINGSPENGER, null) to PosteringsType.Y_OPPLÆRINGSPENGER,
        PosteringsTypeGrunnlag(InntektType.YTELSE_FRA_OFFENTLIGE, InntektBeskrivelse.OPPLÆRINGSPENGER_FERIEPENGER, null) to PosteringsType.Y_OPPLÆRINGSPENGER_FERIEPENGER,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_ANNET_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.BONUS, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_BONUS_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FAST_TILLEGG, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_FAST_TILLEGG_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FASTLOENN, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_FASTLØNN_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.FERIEPENGER, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_FERIEPENGER_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.HELLIGDAGSTILLEGG, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_HELLIGDAGSTILLEGG_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.OVERTIDSGODTGJOERELSE, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_OVERTIDSGODTGJØRELSE_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SLUTTVEDERLAG, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_SLUTTVEDERLAG_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TIMELOENN, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_TIMELØNN_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TREKK_I_LOENN_FOR_FERIE, SpesielleInntjeningsforhold.LOENN_VED_ARBEIDSMARKEDSTILTAK) to PosteringsType.L_TREKK_I_LØNN_FOR_FERIE_T,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.TIPS, null) to PosteringsType.L_TIPS,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SKATTEPLIKTIG_PERSONALRABATT, null) to PosteringsType.L_SKATTEPLIKTIG_PERSONALRABATT,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.SKATTEPLIKTIG_GODTGJOERELSE_SAERAVTALE_UTLAND, null) to PosteringsType.L_SKATTEPLIKTIG_GODTGJOERELSE_SAERAVTALE_UTLAND,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, SpesielleInntjeningsforhold.LOENN_OG_ANNEN_GODTGJOERELSE_SOM_IKKE_ER_SKATTEPLIKTIG) to PosteringsType.L_ANNET_IKKE_SKATTEPLIKTIG,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, SpesielleInntjeningsforhold.LOENN_UTBETALT_FRA_DEN_NORSKE_STAT_OPPTJENT_I_UTLANDET) to PosteringsType.L_ANNET_UTLANDET,
        PosteringsTypeGrunnlag(InntektType.LOENNSINNTEKT, InntektBeskrivelse.ANNET, SpesielleInntjeningsforhold.LOENN_VED_KONKURS_ELLER_STATSGARANTI_OSV) to PosteringsType.L_ANNET_KONKURS,
    )

class PosteringsTypeMappingException(message: String) : RuntimeException(message)

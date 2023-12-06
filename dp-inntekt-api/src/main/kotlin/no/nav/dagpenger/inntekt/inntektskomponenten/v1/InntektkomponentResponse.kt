package no.nav.dagpenger.inntekt.inntektskomponenten.v1

import com.fasterxml.jackson.annotation.JsonProperty
import com.squareup.moshi.Json
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

/**
 * Inntekt v3 models
 *
 * as of https://confluence.adeo.no/display/SDFS/tjeneste_v3%3Avirksomhet%3AInntekt_v3 and overlap with
 * https://confluence.adeo.no/display/FEL/Inntektskomponenten+-+Informasjonsmodell+-+RS
 *
 */

data class InntektkomponentRequest(
    val aktørId: String,
    val fødselsnummer: String,
    val månedFom: YearMonth,
    val månedTom: YearMonth,
)

data class InntektkomponentResponse(
    val arbeidsInntektMaaned: List<ArbeidsInntektMaaned>?,
    val ident: Aktoer,
)

data class ArbeidsInntektMaaned(
    val aarMaaned: YearMonth,
    val avvikListe: List<Avvik>?,
    val arbeidsInntektInformasjon: ArbeidsInntektInformasjon?,
)

data class ArbeidsInntektInformasjon(
    val inntektListe: List<Inntekt>?,
)

data class Inntekt(
    val beloep: BigDecimal,
    val fordel: String,
    val beskrivelse: InntektBeskrivelse,
    val inntektskilde: String,
    val inntektsstatus: String,
    val inntektsperiodetype: String,
    val leveringstidspunkt: YearMonth? = null,
    val opptjeningsland: String? = null,
    val opptjeningsperiode: Periode? = null,
    val skattemessigBosattLand: String? = null,
    val utbetaltIMaaned: YearMonth,
    val opplysningspliktig: Aktoer? = null,
    val inntektsinnsender: Aktoer? = null,
    val virksomhet: Aktoer? = null,
    val inntektsmottaker: Aktoer? = null,
    val inngaarIGrunnlagForTrekk: Boolean? = null,
    val utloeserArbeidsgiveravgift: Boolean? = null,
    val informasjonsstatus: String? = null,
    val inntektType: InntektType,
    val tilleggsinformasjon: TilleggInformasjon? = null,
)

data class Periode(
    val startDato: LocalDate,
    val sluttDato: LocalDate,
)

data class Aktoer(
    val aktoerType: AktoerType,
    val identifikator: String,
)

enum class AktoerType {
    AKTOER_ID,
    NATURLIG_IDENT,
    ORGANISASJON,
}

data class Avvik(
    val ident: Aktoer,
    val opplysningspliktig: Aktoer,
    val virksomhet: Aktoer?,
    val avvikPeriode: YearMonth,
    val tekst: String,
)

data class TilleggInformasjon(
    val kategori: String?,
    val tilleggsinformasjonDetaljer: TilleggInformasjonsDetaljer?,
)

data class TilleggInformasjonsDetaljer(
    val detaljerType: String?,
    val spesielleInntjeningsforhold: SpesielleInntjeningsforhold?,
)

enum class InntektType {
    LOENNSINNTEKT,
    NAERINGSINNTEKT,
    PENSJON_ELLER_TRYGD,
    YTELSE_FRA_OFFENTLIGE,
}

/**
 *  @Json = Moshi
 *  @JsonProperty = Jackson
 *
 */
enum class SpesielleInntjeningsforhold {
    @Json(name = "hyreTilMannskapPaaFiskeSmaahvalfangstOgSelfangstfartoey")
    @JsonProperty("hyreTilMannskapPaaFiskeSmaahvalfangstOgSelfangstfartoey")
    HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,

    @Json(name = "loennVedArbeidsmarkedstiltak")
    @JsonProperty("loennVedArbeidsmarkedstiltak")
    LOENN_VED_ARBEIDSMARKEDSTILTAK,

    @Json(name = "loennOgAnnenGodtgjoerelseSomIkkeErSkattepliktig")
    @JsonProperty("loennOgAnnenGodtgjoerelseSomIkkeErSkattepliktig")
    LOENN_OG_ANNEN_GODTGJOERELSE_SOM_IKKE_ER_SKATTEPLIKTIG,

    @Json(name = "loennUtbetaltFraDenNorskeStatOpptjentIUtlandet")
    @JsonProperty("loennUtbetaltFraDenNorskeStatOpptjentIUtlandet")
    LOENN_UTBETALT_FRA_DEN_NORSKE_STAT_OPPTJENT_I_UTLANDET,

    @Json(name = "loennVedKonkursEllerStatsgarantiOsv")
    @JsonProperty("loennVedKonkursEllerStatsgarantiOsv")
    LOENN_VED_KONKURS_ELLER_STATSGARANTI_OSV,

    @Json(name = "skattefriArbeidsinntektBarnUnderTrettenAar")
    @JsonProperty("skattefriArbeidsinntektBarnUnderTrettenAar")
    SKATTEFRI_ARBEIDSINNTEKT_BARN_UNDER_TRETTEN_AAR,

    @Json(name = "statsansattUtlandet")
    @JsonProperty("statsansattUtlandet")
    STATSANSATT_UTLANDET,

    @Json(name = "utenlandskeSjoefolkSomIkkeErSkattepliktig")
    @JsonProperty("utenlandskeSjoefolkSomIkkeErSkattepliktig")
    UTELANDSKE_SJOEFOLK_SOM_IKKE_ER_SKATTEPLIKTIG,
    UNKNOWN,
}

/**
 *  @Json == Moshi
 *  @JsonProperty = Jackson
 *
 */
enum class InntektBeskrivelse {
    @Json(name = "aksjerGrunnfondsbevisTilUnderkurs")
    @JsonProperty("aksjerGrunnfondsbevisTilUnderkurs")
    AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS,

    @Json(name = "annet")
    @JsonProperty("annet")
    ANNET,

    @Json(name = "arbeidsoppholdKost")
    @JsonProperty("arbeidsoppholdKost")
    ARBEIDSOPPHOLD_KOST,

    @Json(name = "arbeidsoppholdLosji")
    @JsonProperty("arbeidsoppholdLosji")
    ARBEIDSOPPHOLD_LOSJI,

    @Json(name = "beregnetSkatt")
    @JsonProperty("beregnetSkatt")
    BEREGNET_SKATT,

    @Json(name = "besoeksreiserHjemmetAnnet")
    @JsonProperty("besoeksreiserHjemmetAnnet")
    BESOEKSREISER_HJEMMET_ANNET,

    @Json(name = "besoeksreiserHjemmetKilometergodtgjoerelseBil")
    @JsonProperty("besoeksreiserHjemmetKilometergodtgjoerelseBil")
    BESOEKSREISER_HJEMMET_KILOMETERGODTGJOERELSE_BIL,

    @Json(name = "betaltUtenlandskSkatt")
    @JsonProperty("betaltUtenlandskSkatt")
    BETALT_UTENLANDSK_SKATT,

    @Json(name = "bil")
    @JsonProperty("bil")
    BIL,

    @Json(name = "bolig")
    @JsonProperty("bolig")
    BOLIG,

    @Json(name = "bonus")
    @JsonProperty("bonus")
    BONUS,

    @Json(name = "bonusFraForsvaret")
    @JsonProperty("bonusFraForsvaret")
    BONUS_FRA_FORSVARET,

    @Json(name = "elektroniskKommunikasjon")
    @JsonProperty("elektroniskKommunikasjon")
    ELEKTRONISK_KOMMUNIKASJON,

    @Json(name = "fastBilgodtgjoerelse")
    @JsonProperty("fastBilgodtgjoerelse")
    FAST_BILGODTGJOERELSE,

    @Json(name = "fastTillegg")
    @JsonProperty("fastTillegg")
    FAST_TILLEGG,

    @Json(name = "fastloenn")
    @JsonProperty("fastloenn")
    FASTLOENN,

    @Json(name = "feriepenger")
    @JsonProperty("feriepenger")
    FERIEPENGER,

    @Json(name = "fondForIdrettsutoevere")
    @JsonProperty("fondForIdrettsutoevere")
    FOND_FOR_IDRETTSUTOEVERE,

    @Json(name = "helligdagstillegg")
    @JsonProperty("helligdagstillegg")
    HELLIGDAGSTILLEGG,

    @Json(name = "honorarAkkordProsentProvisjon")
    @JsonProperty("honorarAkkordProsentProvisjon")
    HONORAR_AKKORD_PROSENT_PROVISJON,

    @Json(name = "hyretillegg")
    @JsonProperty("hyretillegg")
    HYRETILLEGG,

    @Json(name = "innbetalingTilUtenlandskPensjonsordning")
    @JsonProperty("innbetalingTilUtenlandskPensjonsordning")
    INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING,

    @Json(name = "kilometergodtgjoerelseBil")
    @JsonProperty("kilometergodtgjoerelseBil")
    KILOMETERGODTGJOERELSE_BIL,

    @Json(name = "kommunalOmsorgsloennOgFosterhjemsgodtgjoerelse")
    @JsonProperty("kommunalOmsorgsloennOgFosterhjemsgodtgjoerelse")
    KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE,

    @Json(name = "kostDager")
    @JsonProperty("kostDager")
    KOST_DAGER,

    @Json(name = "kostDoegn")
    @JsonProperty("kostDoegn")
    KOST_DOEGN,

    @Json(name = "kostbesparelseIHjemmet")
    @JsonProperty("kostbesparelseIHjemmet")
    KOSTBESPARELSE_I_HJEMMET,

    @Json(name = "losji")
    @JsonProperty("losji")
    LOSJI,

    @Json(name = "ikkeSkattepliktigLoennFraUtenlandskDiplomKonsulStasjon")
    @JsonProperty("ikkeSkattepliktigLoennFraUtenlandskDiplomKonsulStasjon")
    IKKE_SKATTEPLIKTIG_LOENN_FRA_UTENLANDSK_DIPLOM_KONSUL_STASJON,

    @Json(name = "loennForBarnepassIBarnetsHjem")
    @JsonProperty("loennForBarnepassIBarnetsHjem")
    LOENN_FOR_BARNEPASS_I_BARNETS_HJEM,

    @Json(name = "loennTilPrivatpersonerForArbeidIHjemmet")
    @JsonProperty("loennTilPrivatpersonerForArbeidIHjemmet")
    LOENN_TIL_PRIVATPERSONER_FOR_ARBEID_I_HJEMMET,

    @Json(name = "loennUtbetaltAvVeldedigEllerAllmennyttigInstitusjonEllerOrganisasjon")
    @JsonProperty("loennUtbetaltAvVeldedigEllerAllmennyttigInstitusjonEllerOrganisasjon")
    LOENN_UTBETALT_AV_VELDEDIG_ELLER_ALLMENNYTTIG_INSTITUSJON_ELLER_ORGANISASJON,

    @Json(name = "loennTilVergeFraFylkesmannen")
    @JsonProperty("loennTilVergeFraFylkesmannen")
    LOENN_TIL_VERGE_FRA_FYLKESMANNEN,

    @Json(name = "loennTilVergeFraStatsforvalteren")
    @JsonProperty("loennTilVergeFraStatsforvalteren")
    LOENN_TIL_VERGE_FRA_STATSFORVALTEREN,

    @Json(name = "opsjoner")
    @JsonProperty("opsjoner")
    OPSJONER,

    @Json(name = "overtidsgodtgjoerelse")
    @JsonProperty("overtidsgodtgjoerelse")
    OVERTIDSGODTGJOERELSE,

    @Json(name = "reiseAnnet")
    @JsonProperty("reiseAnnet")
    REISE_ANNET,

    @Json(name = "reiseKost")
    @JsonProperty("reiseKost")
    REISE_KOST,

    @Json(name = "reiseLosji")
    @JsonProperty("reiseLosji")
    REISE_LOSJI,

    @Json(name = "rentefordelLaan")
    @JsonProperty("rentefordelLaan")
    RENTEFORDEL_LAAN,

    @Json(name = "skattepliktigDelForsikringer")
    @JsonProperty("skattepliktigDelForsikringer")
    SKATTEPLIKTIG_DEL_FORSIKRINGER,

    @Json(name = "sluttvederlag")
    @JsonProperty("sluttvederlag")
    SLUTTVEDERLAG,

    @Json(name = "smusstillegg")
    @JsonProperty("smusstillegg")
    SMUSSTILLEGG,

    @Json(name = "stipend")
    @JsonProperty("stipend")
    STIPEND,

    @Json(name = "styrehonorarOgGodtgjoerelseVerv")
    @JsonProperty("styrehonorarOgGodtgjoerelseVerv")
    STYREHONORAR_OG_GODTGJOERELSE_VERV,

    @Json(name = "timeloenn")
    @JsonProperty("timeloenn")
    TIMELOENN,

    @Json(name = "tips")
    @JsonProperty("tips")
    TIPS,

    @Json(name = "skattepliktigPersonalrabatt")
    @JsonProperty("skattepliktigPersonalrabatt")
    SKATTEPLIKTIG_PERSONALRABATT,

    @Json(name = "skattepliktigGodtgjoerelseSaeravtaleUtland")
    @JsonProperty("skattepliktigGodtgjoerelseSaeravtaleUtland")
    SKATTEPLIKTIG_GODTGJOERELSE_SAERAVTALE_UTLAND,

    @Json(name = "trekkILoennForFerie")
    @JsonProperty("trekkILoennForFerie")
    TREKK_I_LOENN_FOR_FERIE,

    @Json(name = "uregelmessigeTilleggKnyttetTilArbeidetTid")
    @JsonProperty("uregelmessigeTilleggKnyttetTilArbeidetTid")
    UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,

    @Json(name = "uregelmessigeTilleggKnyttetTilIkkeArbeidetTid")
    @JsonProperty("uregelmessigeTilleggKnyttetTilIkkeArbeidetTid")
    UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,

    @Json(name = "yrkebilTjenestligbehovKilometer")
    @JsonProperty("yrkebilTjenestligbehovKilometer")
    YRKEBIL_TJENESTLIGBEHOV_KILOMETER,

    @Json(name = "yrkebilTjenestligbehovListepris")
    @JsonProperty("yrkebilTjenestligbehovListepris")
    YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS,

    @Json(name = "lottKunTrygdeavgift")
    @JsonProperty("lottKunTrygdeavgift")
    LOTT_KUN_TRYGDEAVGIFT,

    @Json(name = "vederlag")
    @JsonProperty("vederlag")
    VEDERLAG,

    @Json(name = "dagpengerVedArbeidsloeshet")
    @JsonProperty("dagpengerVedArbeidsloeshet")
    DAGPENGER_VED_ARBEIDSLOESHET,

    @Json(name = "ferietilleggDagpengerVedArbeidsloeshet")
    @JsonProperty("ferietilleggDagpengerVedArbeidsloeshet")
    DAGPENGER_VED_ARBEIDSLOESHET_FERIETILLEGG,

    @Json(name = "dagpengerTilFisker")
    @JsonProperty("dagpengerTilFisker")
    DAGPENGER_TIL_FISKER,

    @Json(name = "dagpengerTilFiskerSomBareHarHyre")
    @JsonProperty("dagpengerTilFiskerSomBareHarHyre")
    DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,

    @Json(name = "ferietilleggDagpengerTilFiskerSomBareHarHyre")
    @JsonProperty("ferietilleggDagpengerTilFiskerSomBareHarHyre")
    DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE_FERIETILLEGG,

    @Json(name = "foreldrepenger")
    @JsonProperty("foreldrepenger")
    FORELDREPENGER,

    @Json(name = "feriepengerForeldrepenger")
    @JsonProperty("feriepengerForeldrepenger")
    FORELDREPENGER_FERIEPENGER,

    @Json(name = "svangerskapspenger")
    @JsonProperty("svangerskapspenger")
    SVANGERSKAPSPENGER,

    @Json(name = "feriepengerSvangerskapspenger")
    @JsonProperty("feriepengerSvangerskapspenger")
    SVANGERSKAPSPENGER_FERIEPENGER,

    @Json(name = "sykepenger")
    @JsonProperty("sykepenger")
    SYKEPENGER,

    @Json(name = "feriepengerSykepenger")
    @JsonProperty("feriepengerSykepenger")
    SYKEPENGER_FERIEPENGER,

    @Json(name = "sykepengerTilFisker")
    @JsonProperty("sykepengerTilFisker")
    SYKEPENGER_TIL_FISKER,

    @Json(name = "sykepengerTilFiskerSomBareHarHyre")
    @JsonProperty("sykepengerTilFiskerSomBareHarHyre")
    SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,

    @Json(name = "feriepengerSykepengerTilFiskerSomBareHarHyre")
    @JsonProperty("feriepengerSykepengerTilFiskerSomBareHarHyre")
    SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE_FERIEPENGER,

    @Json(name = "pleiepenger")
    @JsonProperty("pleiepenger")
    PLEIEPENGER,

    @Json(name = "feriepengerPleiepenger")
    @JsonProperty("feriepengerPleiepenger")
    PLEIEPENGER_FERIEPENGER,

    @Json(name = "omsorgspenger")
    @JsonProperty("omsorgspenger")
    OMSORGSPENGER,

    @Json(name = "feriepengerOmsorgspenger")
    @JsonProperty("feriepengerOmsorgspenger")
    OMSORGSPENGER_FERIEPENGER,

    @Json(name = "opplaeringspenger")
    @JsonProperty("opplaeringspenger")
    OPPLÆRINGSPENGER,

    @Json(name = "feriepengerOpplaeringspenger")
    @JsonProperty("feriepengerOpplaeringspenger")
    OPPLÆRINGSPENGER_FERIEPENGER,
}

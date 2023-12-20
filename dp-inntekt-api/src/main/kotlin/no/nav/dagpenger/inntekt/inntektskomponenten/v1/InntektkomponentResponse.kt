package no.nav.dagpenger.inntekt.inntektskomponenten.v1

import com.fasterxml.jackson.annotation.JsonProperty
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
    @JsonProperty("hyreTilMannskapPaaFiskeSmaahvalfangstOgSelfangstfartoey")
    HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,

    @JsonProperty("loennVedArbeidsmarkedstiltak")
    LOENN_VED_ARBEIDSMARKEDSTILTAK,

    @JsonProperty("loennOgAnnenGodtgjoerelseSomIkkeErSkattepliktig")
    LOENN_OG_ANNEN_GODTGJOERELSE_SOM_IKKE_ER_SKATTEPLIKTIG,

    @JsonProperty("loennUtbetaltFraDenNorskeStatOpptjentIUtlandet")
    LOENN_UTBETALT_FRA_DEN_NORSKE_STAT_OPPTJENT_I_UTLANDET,

    @JsonProperty("loennVedKonkursEllerStatsgarantiOsv")
    LOENN_VED_KONKURS_ELLER_STATSGARANTI_OSV,

    @JsonProperty("skattefriArbeidsinntektBarnUnderTrettenAar")
    SKATTEFRI_ARBEIDSINNTEKT_BARN_UNDER_TRETTEN_AAR,

    @JsonProperty("statsansattUtlandet")
    STATSANSATT_UTLANDET,

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
    @JsonProperty("aksjerGrunnfondsbevisTilUnderkurs")
    AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS,

    @JsonProperty("annet")
    ANNET,

    @JsonProperty("arbeidsoppholdKost")
    ARBEIDSOPPHOLD_KOST,

    @JsonProperty("arbeidsoppholdLosji")
    ARBEIDSOPPHOLD_LOSJI,

    @JsonProperty("beregnetSkatt")
    BEREGNET_SKATT,

    @JsonProperty("besoeksreiserHjemmetAnnet")
    BESOEKSREISER_HJEMMET_ANNET,

    @JsonProperty("besoeksreiserHjemmetKilometergodtgjoerelseBil")
    BESOEKSREISER_HJEMMET_KILOMETERGODTGJOERELSE_BIL,

    @JsonProperty("betaltUtenlandskSkatt")
    BETALT_UTENLANDSK_SKATT,

    @JsonProperty("bil")
    BIL,

    @JsonProperty("bolig")
    BOLIG,

    @JsonProperty("bonus")
    BONUS,

    @JsonProperty("bonusFraForsvaret")
    BONUS_FRA_FORSVARET,

    @JsonProperty("elektroniskKommunikasjon")
    ELEKTRONISK_KOMMUNIKASJON,

    @JsonProperty("fastBilgodtgjoerelse")
    FAST_BILGODTGJOERELSE,

    @JsonProperty("fastTillegg")
    FAST_TILLEGG,

    @JsonProperty("fastloenn")
    FASTLOENN,

    @JsonProperty("feriepenger")
    FERIEPENGER,

    @JsonProperty("fondForIdrettsutoevere")
    FOND_FOR_IDRETTSUTOEVERE,

    @JsonProperty("helligdagstillegg")
    HELLIGDAGSTILLEGG,

    @JsonProperty("honorarAkkordProsentProvisjon")
    HONORAR_AKKORD_PROSENT_PROVISJON,

    @JsonProperty("hyretillegg")
    HYRETILLEGG,

    @JsonProperty("innbetalingTilUtenlandskPensjonsordning")
    INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING,

    @JsonProperty("kilometergodtgjoerelseBil")
    KILOMETERGODTGJOERELSE_BIL,

    @JsonProperty("kommunalOmsorgsloennOgFosterhjemsgodtgjoerelse")
    KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE,

    @JsonProperty("kostDager")
    KOST_DAGER,

    @JsonProperty("kostDoegn")
    KOST_DOEGN,

    @JsonProperty("kostbesparelseIHjemmet")
    KOSTBESPARELSE_I_HJEMMET,

    @JsonProperty("losji")
    LOSJI,

    @JsonProperty("ikkeSkattepliktigLoennFraUtenlandskDiplomKonsulStasjon")
    IKKE_SKATTEPLIKTIG_LOENN_FRA_UTENLANDSK_DIPLOM_KONSUL_STASJON,

    @JsonProperty("loennForBarnepassIBarnetsHjem")
    LOENN_FOR_BARNEPASS_I_BARNETS_HJEM,

    @JsonProperty("loennTilPrivatpersonerForArbeidIHjemmet")
    LOENN_TIL_PRIVATPERSONER_FOR_ARBEID_I_HJEMMET,

    @JsonProperty("loennUtbetaltAvVeldedigEllerAllmennyttigInstitusjonEllerOrganisasjon")
    LOENN_UTBETALT_AV_VELDEDIG_ELLER_ALLMENNYTTIG_INSTITUSJON_ELLER_ORGANISASJON,

    @JsonProperty("loennTilVergeFraFylkesmannen")
    LOENN_TIL_VERGE_FRA_FYLKESMANNEN,

    @JsonProperty("loennTilVergeFraStatsforvalteren")
    LOENN_TIL_VERGE_FRA_STATSFORVALTEREN,

    @JsonProperty("opsjoner")
    OPSJONER,

    @JsonProperty("overtidsgodtgjoerelse")
    OVERTIDSGODTGJOERELSE,

    @JsonProperty("reiseAnnet")
    REISE_ANNET,

    @JsonProperty("reiseKost")
    REISE_KOST,

    @JsonProperty("reiseLosji")
    REISE_LOSJI,

    @JsonProperty("rentefordelLaan")
    RENTEFORDEL_LAAN,

    @JsonProperty("skattepliktigDelForsikringer")
    SKATTEPLIKTIG_DEL_FORSIKRINGER,

    @JsonProperty("sluttvederlag")
    SLUTTVEDERLAG,

    @JsonProperty("smusstillegg")
    SMUSSTILLEGG,

    @JsonProperty("stipend")
    STIPEND,

    @JsonProperty("styrehonorarOgGodtgjoerelseVerv")
    STYREHONORAR_OG_GODTGJOERELSE_VERV,

    @JsonProperty("timeloenn")
    TIMELOENN,

    @JsonProperty("tips")
    TIPS,

    @JsonProperty("skattepliktigPersonalrabatt")
    SKATTEPLIKTIG_PERSONALRABATT,

    @JsonProperty("skattepliktigGodtgjoerelseSaeravtaleUtland")
    SKATTEPLIKTIG_GODTGJOERELSE_SAERAVTALE_UTLAND,

    @JsonProperty("trekkILoennForFerie")
    TREKK_I_LOENN_FOR_FERIE,

    @JsonProperty("uregelmessigeTilleggKnyttetTilArbeidetTid")
    UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,

    @JsonProperty("uregelmessigeTilleggKnyttetTilIkkeArbeidetTid")
    UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,

    @JsonProperty("yrkebilTjenestligbehovKilometer")
    YRKEBIL_TJENESTLIGBEHOV_KILOMETER,

    @JsonProperty("yrkebilTjenestligbehovListepris")
    YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS,

    @JsonProperty("lottKunTrygdeavgift")
    LOTT_KUN_TRYGDEAVGIFT,

    @JsonProperty("vederlag")
    VEDERLAG,

    @JsonProperty("dagpengerVedArbeidsloeshet")
    DAGPENGER_VED_ARBEIDSLOESHET,

    @JsonProperty("ferietilleggDagpengerVedArbeidsloeshet")
    DAGPENGER_VED_ARBEIDSLOESHET_FERIETILLEGG,

    @JsonProperty("dagpengerTilFisker")
    DAGPENGER_TIL_FISKER,

    @JsonProperty("dagpengerTilFiskerSomBareHarHyre")
    DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,

    @JsonProperty("ferietilleggDagpengerTilFiskerSomBareHarHyre")
    DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE_FERIETILLEGG,

    @JsonProperty("foreldrepenger")
    FORELDREPENGER,

    @JsonProperty("feriepengerForeldrepenger")
    FORELDREPENGER_FERIEPENGER,

    @JsonProperty("svangerskapspenger")
    SVANGERSKAPSPENGER,

    @JsonProperty("feriepengerSvangerskapspenger")
    SVANGERSKAPSPENGER_FERIEPENGER,

    @JsonProperty("sykepenger")
    SYKEPENGER,

    @JsonProperty("feriepengerSykepenger")
    SYKEPENGER_FERIEPENGER,

    @JsonProperty("sykepengerTilFisker")
    SYKEPENGER_TIL_FISKER,

    @JsonProperty("sykepengerTilFiskerSomBareHarHyre")
    SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,

    @JsonProperty("feriepengerSykepengerTilFiskerSomBareHarHyre")
    SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE_FERIEPENGER,

    @JsonProperty("pleiepenger")
    PLEIEPENGER,

    @JsonProperty("feriepengerPleiepenger")
    PLEIEPENGER_FERIEPENGER,

    @JsonProperty("omsorgspenger")
    OMSORGSPENGER,

    @JsonProperty("feriepengerOmsorgspenger")
    OMSORGSPENGER_FERIEPENGER,

    @JsonProperty("opplaeringspenger")
    OPPLÆRINGSPENGER,

    @JsonProperty("feriepengerOpplaeringspenger")
    OPPLÆRINGSPENGER_FERIEPENGER,
}

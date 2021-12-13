package no.nav.dagpenger.inntekt.inntektskomponenten.v1

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
    val månedFom: YearMonth,
    val månedTom: YearMonth
)

data class InntektkomponentResponse(
    val arbeidsInntektMaaned: List<ArbeidsInntektMaaned>?,
    val ident: Aktoer
)

data class ArbeidsInntektMaaned(
    val aarMaaned: YearMonth,
    val avvikListe: List<Avvik>?,
    val arbeidsInntektInformasjon: ArbeidsInntektInformasjon?
)

data class ArbeidsInntektInformasjon(
    val inntektListe: List<Inntekt>?
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
    val tilleggsinformasjon: TilleggInformasjon? = null
)

data class Periode(
    val startDato: LocalDate,
    val sluttDato: LocalDate
)

data class Aktoer(
    val aktoerType: AktoerType,
    val identifikator: String
)

enum class AktoerType {
    AKTOER_ID,
    NATURLIG_IDENT,
    ORGANISASJON
}

data class Avvik(
    val ident: Aktoer,
    val opplysningspliktig: Aktoer,
    val virksomhet: Aktoer?,
    val avvikPeriode: YearMonth,
    val tekst: String
)

data class TilleggInformasjon(
    val kategori: String?,
    val tilleggsinformasjonDetaljer: TilleggInformasjonsDetaljer?
)

data class TilleggInformasjonsDetaljer(
    val detaljerType: String?,
    val spesielleInntjeningsforhold: SpesielleInntjeningsforhold?
)

enum class InntektType {
    LOENNSINNTEKT,
    NAERINGSINNTEKT,
    PENSJON_ELLER_TRYGD,
    YTELSE_FRA_OFFENTLIGE
}

enum class SpesielleInntjeningsforhold {
    @Json(name = "hyreTilMannskapPaaFiskeSmaahvalfangstOgSelfangstfartoey")
    HYRE_TIL_MANNSKAP_PAA_FISKE_SMAAHVALFANGST_OG_SELFANGSTFARTOEY,
    @Json(name = "loennVedArbeidsmarkedstiltak")
    LOENN_VED_ARBEIDSMARKEDSTILTAK,
    @Json(name = "loennOgAnnenGodtgjoerelseSomIkkeErSkattepliktig")
    LOENN_OG_ANNEN_GODTGJOERELSE_SOM_IKKE_ER_SKATTEPLIKTIG,
    @Json(name = "loennUtbetaltFraDenNorskeStatOpptjentIUtlandet")
    LOENN_UTBETALT_FRA_DEN_NORSKE_STAT_OPPTJENT_I_UTLANDET,
    @Json(name = "loennVedKonkursEllerStatsgarantiOsv")
    LOENN_VED_KONKURS_ELLER_STATSGARANTI_OSV,
    @Json(name = "skattefriArbeidsinntektBarnUnderTrettenAar")
    SKATTEFRI_ARBEIDSINNTEKT_BARN_UNDER_TRETTEN_AAR,
    @Json(name = "statsansattUtlandet")
    STATSANSATT_UTLANDET,
    @Json(name = "utenlandskeSjoefolkSomIkkeErSkattepliktig")
    UTELANDSKE_SJOEFOLK_SOM_IKKE_ER_SKATTEPLIKTIG,
    UNKNOWN
}

enum class InntektBeskrivelse {
    @Json(name = "aksjerGrunnfondsbevisTilUnderkurs")
    AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS,
    @Json(name = "annet")
    ANNET,
    @Json(name = "arbeidsoppholdKost")
    ARBEIDSOPPHOLD_KOST,
    @Json(name = "arbeidsoppholdLosji")
    ARBEIDSOPPHOLD_LOSJI,
    @Json(name = "beregnetSkatt")
    BEREGNET_SKATT,
    @Json(name = "besoeksreiserHjemmetAnnet")
    BESOEKSREISER_HJEMMET_ANNET,
    @Json(name = "besoeksreiserHjemmetKilometergodtgjoerelseBil")
    BESOEKSREISER_HJEMMET_KILOMETERGODTGJOERELSE_BIL,
    @Json(name = "betaltUtenlandskSkatt")
    BETALT_UTENLANDSK_SKATT,
    @Json(name = "bil")
    BIL,
    @Json(name = "bolig")
    BOLIG,
    @Json(name = "bonus")
    BONUS,
    @Json(name = "bonusFraForsvaret")
    BONUS_FRA_FORSVARET,
    @Json(name = "elektroniskKommunikasjon")
    ELEKTRONISK_KOMMUNIKASJON,
    @Json(name = "fastBilgodtgjoerelse")
    FAST_BILGODTGJOERELSE,
    @Json(name = "fastTillegg")
    FAST_TILLEGG,
    @Json(name = "fastloenn")
    FASTLOENN,
    @Json(name = "feriepenger")
    FERIEPENGER,
    @Json(name = "fondForIdrettsutoevere")
    FOND_FOR_IDRETTSUTOEVERE,
    @Json(name = "foreldrepenger")
    FORELDREPENGER,
    @Json(name = "helligdagstillegg")
    HELLIGDAGSTILLEGG,
    @Json(name = "honorarAkkordProsentProvisjon")
    HONORAR_AKKORD_PROSENT_PROVISJON,
    @Json(name = "hyretillegg")
    HYRETILLEGG,
    @Json(name = "innbetalingTilUtenlandskPensjonsordning")
    INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING,
    @Json(name = "kilometergodtgjoerelseBil")
    KILOMETERGODTGJOERELSE_BIL,
    @Json(name = "kommunalOmsorgsloennOgFosterhjemsgodtgjoerelse")
    KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE,
    @Json(name = "kostDager")
    KOST_DAGER,
    @Json(name = "kostDoegn")
    KOST_DOEGN,
    @Json(name = "kostbesparelseIHjemmet")
    KOSTBESPARELSE_I_HJEMMET,
    @Json(name = "losji")
    LOSJI,
    @Json(name = "ikkeSkattepliktigLoennFraUtenlandskDiplomKonsulStasjon")
    IKKE_SKATTEPLIKTIG_LOENN_FRA_UTENLANDSK_DIPLOM_KONSUL_STASJON,
    @Json(name = "loennForBarnepassIBarnetsHjem")
    LOENN_FOR_BARNEPASS_I_BARNETS_HJEM,
    @Json(name = "loennTilPrivatpersonerForArbeidIHjemmet")
    LOENN_TIL_PRIVATPERSONER_FOR_ARBEID_I_HJEMMET,
    @Json(name = "loennUtbetaltAvVeldedigEllerAllmennyttigInstitusjonEllerOrganisasjon")
    LOENN_UTBETALT_AV_VELDEDIG_ELLER_ALLMENNYTTIG_INSTITUSJON_ELLER_ORGANISASJON,
    @Json(name = "loennTilVergeFraFylkesmannen")
    LOENN_TIL_VERGE_FRA_FYLKESMANNEN,
    @Json(name = "loennTilVergeFraStatsforvalteren")
    LOENN_TIL_VERGE_FRA_STATSFORVALTEREN,
    @Json(name = "opsjoner")
    OPSJONER,
    @Json(name = "overtidsgodtgjoerelse")
    OVERTIDSGODTGJOERELSE,
    @Json(name = "reiseAnnet")
    REISE_ANNET,
    @Json(name = "reiseKost")
    REISE_KOST,
    @Json(name = "reiseLosji")
    REISE_LOSJI,
    @Json(name = "rentefordelLaan")
    RENTEFORDEL_LAAN,
    @Json(name = "skattepliktigDelForsikringer")
    SKATTEPLIKTIG_DEL_FORSIKRINGER,
    @Json(name = "sluttvederlag")
    SLUTTVEDERLAG,
    @Json(name = "smusstillegg")
    SMUSSTILLEGG,
    @Json(name = "stipend")
    STIPEND,
    @Json(name = "styrehonorarOgGodtgjoerelseVerv")
    STYREHONORAR_OG_GODTGJOERELSE_VERV,
    @Json(name = "svangerskapspenger")
    SVANGERSKAPSPENGER,
    @Json(name = "timeloenn")
    TIMELOENN,
    @Json(name = "trekkILoennForFerie")
    TREKK_I_LOENN_FOR_FERIE,
    @Json(name = "uregelmessigeTilleggKnyttetTilArbeidetTid")
    UREGELMESSIGE_TILLEGG_KNYTTET_TIL_ARBEIDET_TID,
    @Json(name = "uregelmessigeTilleggKnyttetTilIkkeArbeidetTid")
    UREGELMESSIGE_TILLEGG_KNYTTET_TIL_IKKE_ARBEIDET_TID,
    @Json(name = "yrkebilTjenestligbehovKilometer")
    YRKEBIL_TJENESTLIGBEHOV_KILOMETER,
    @Json(name = "yrkebilTjenestligbehovListepris")
    YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS,
    @Json(name = "dagpengerVedArbeidsloeshet")
    DAGPENGER_VED_ARBEIDSLOESHET,
    @Json(name = "dagpengerTilFisker")
    DAGPENGER_TIL_FISKER,
    @Json(name = "dagpengerTilFiskerSomBareHarHyre")
    DAGPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
    @Json(name = "sykepengerTilFisker")
    SYKEPENGER_TIL_FISKER,
    @Json(name = "sykepengerTilFiskerSomBareHarHyre")
    SYKEPENGER_TIL_FISKER_SOM_BARE_HAR_HYRE,
    @Json(name = "lottKunTrygdeavgift")
    LOTT_KUN_TRYGDEAVGIFT,
    @Json(name = "vederlag")
    VEDERLAG,
    @Json(name = "sykepenger")
    SYKEPENGER,
    @Json(name = "tips")
    TIPS,
    @Json(name = "skattepliktigPersonalrabatt")
    SKATTEPLIKTIG_PERSONALRABATT
}

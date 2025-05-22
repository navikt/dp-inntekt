package no.nav.dagpenger.inntekt.mapping

import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Avvik
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektBeskrivelse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Periode
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth

data class GUIInntekt(
    val inntektId: InntektId?,
    val timestamp: LocalDateTime?,
    val inntekt: GUIInntektsKomponentResponse,
    val manueltRedigert: Boolean,
    val redigertAvSaksbehandler: Boolean,
    val inntektsmottaker: Inntektsmottaker? = null,
)

data class Inntektsmottaker(
    val pnr: String?,
    val navn: String?,
)

data class GUIInntektsKomponentResponse(
    val fraDato: YearMonth?,
    val tilDato: YearMonth?,
    val arbeidsInntektMaaned: List<GUIArbeidsInntektMaaned>?,
    val ident: Aktoer,
)

data class GUIArbeidsInntektMaaned(
    val aarMaaned: YearMonth,
    val avvikListe: List<Avvik>?,
    val arbeidsInntektInformasjon: GUIArbeidsInntektInformasjon?,
)

data class GUIArbeidsInntektInformasjon(
    val inntektListe: List<InntektMedVerdikode>?,
)

data class InntektMedVerdikode(
    val beloep: BigDecimal,
    val fordel: String?,
    val beskrivelse: InntektBeskrivelse?,
    val inntektskilde: String,
    val inntektsstatus: String?,
    val inntektsperiodetype: String?,
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
    val inntektType: InntektType?,
    val tilleggsinformasjon: TilleggInformasjon? = null,
    val verdikode: String,
)

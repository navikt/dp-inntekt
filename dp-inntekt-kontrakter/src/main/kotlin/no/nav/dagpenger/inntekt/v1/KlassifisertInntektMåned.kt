package no.nav.dagpenger.inntekt.v1

import java.math.BigDecimal
import java.time.YearMonth

data class KlassifisertInntektMåned(
    val årMåned: YearMonth,
    val klassifiserteInntekter: List<KlassifisertInntekt>,
    val harAvvik: Boolean? = null,
)

fun Collection<KlassifisertInntektMåned>.sumInntekt(inntektsKlasserToSum: List<InntektKlasse>) =
    this.flatMap { klassifisertInntektMåned ->
        klassifisertInntektMåned.klassifiserteInntekter
            .filter { it.inntektKlasse in inntektsKlasserToSum }
            .map { it.beløp }
    }.fold(BigDecimal.ZERO, BigDecimal::add)

typealias InntektsPerioder = Triple<List<KlassifisertInntektMåned>, List<KlassifisertInntektMåned>, List<KlassifisertInntektMåned>>

fun InntektsPerioder.all() = this.toList().flatten()

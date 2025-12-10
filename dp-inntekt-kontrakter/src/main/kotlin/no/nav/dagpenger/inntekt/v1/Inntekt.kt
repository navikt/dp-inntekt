package no.nav.dagpenger.inntekt.v1

import java.time.LocalDateTime
import java.time.YearMonth

class Inntekt(
    val inntektsId: String,
    val inntektsListe: List<KlassifisertInntektMåned>,
    val manueltRedigert: Boolean? = false,
    val begrunnelseManueltRedigert: String? = null,
    val sisteAvsluttendeKalenderMåned: YearMonth,
    val hentetTidspunkt: LocalDateTime? = null,
) {
    fun splitIntoInntektsPerioder(): InntektsPerioder {
        val inntektMap = inntektsListe.associateBy { it.årMåned }

        fun getOrCreateInntektMåned(antallMånederTilbake: Long): KlassifisertInntektMåned {
            val måned = sisteAvsluttendeKalenderMåned.minusMonths(antallMånederTilbake)
            return inntektMap[måned] ?: KlassifisertInntektMåned(måned, emptyList())
        }

        return Triple(
            (0L..11L).map { getOrCreateInntektMåned(it) }.sortedBy { it.årMåned },
            (12L..23L).map { getOrCreateInntektMåned(it) }.sortedBy { it.årMåned },
            (24L..35L).map { getOrCreateInntektMåned(it) }.sortedBy { it.årMåned },
        )
    }

    fun filterPeriod(
        from: YearMonth,
        to: YearMonth,
    ): Inntekt {
        if (from.isAfter(to)) throw IllegalArgumentException("Argument from=$from is after argument to=$to")
        return Inntekt(
            inntektsId,
            inntektsListe.filter { it.årMåned !in from..to },
            sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned,
            hentetTidspunkt = this.hentetTidspunkt,
        )
    }
}

package no.nav.dagpenger.inntekt.mapping

import java.lang.IllegalArgumentException
import no.nav.dagpenger.inntekt.db.DetachedInntekt
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjonsDetaljer

fun mapToStoredInntekt(guiInntekt: GUIInntekt): StoredInntekt = guiInntekt.inntektId?.let {
    StoredInntekt(guiInntekt.inntektId, InntektkomponentResponse(mapToArbeidsInntektMaaneder(guiInntekt.inntekt.arbeidsInntektMaaned)
        ?: emptyList(), guiInntekt.inntekt.ident), guiInntekt.manueltRedigert)
} ?: throw IllegalArgumentException("missing innktektId")

fun mapToDetachedInntekt(guiInntekt: GUIInntekt): DetachedInntekt =
    DetachedInntekt(InntektkomponentResponse(mapToArbeidsInntektMaaneder(guiInntekt.inntekt.arbeidsInntektMaaned)
        ?: emptyList(), guiInntekt.inntekt.ident), guiInntekt.manueltRedigert)

private fun mapToArbeidsInntektMaaneder(arbeidsMaaneder: List<GUIArbeidsInntektMaaned>?): List<ArbeidsInntektMaaned>? {
    return arbeidsMaaneder?.map { GUIarbeidsInntektMaaned ->
        ArbeidsInntektMaaned(
            GUIarbeidsInntektMaaned.aarMaaned,
            GUIarbeidsInntektMaaned.avvikListe,
            ArbeidsInntektInformasjon(
                GUIarbeidsInntektMaaned.arbeidsInntektInformasjon?.inntektListe?.map { inntekt ->
                    val datagrunnlagForVerdikode: DatagrunnlagKlassifisering = dataGrunnlag(inntekt.verdikode)
                    Inntekt(
                        inntekt.beloep,
                        inntekt.fordel ?: "",
                        datagrunnlagForVerdikode.beskrivelse,
                        inntekt.inntektskilde,
                        inntekt.inntektsstatus ?: "LoependeInnrapportert",
                        inntekt.inntektsperiodetype ?: "Maaned",
                        inntekt.leveringstidspunkt,
                        inntekt.opptjeningsland,
                        inntekt.opptjeningsperiode,
                        inntekt.skattemessigBosattLand,
                        inntekt.utbetaltIMaaned,
                        inntekt.opplysningspliktig,
                        inntekt.inntektsinnsender,
                        inntekt.virksomhet,
                        inntekt.inntektsmottaker,
                        inntekt.inngaarIGrunnlagForTrekk,
                        inntekt.utloeserArbeidsgiveravgift,
                        inntekt.informasjonsstatus,
                        datagrunnlagForVerdikode.type,
                        datagrunnlagForVerdikode.forhold?.let { TilleggInformasjon(inntekt.tilleggsinformasjon?.kategori, TilleggInformasjonsDetaljer(inntekt.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.detaljerType, it)) }
                    )
                } ?: emptyList()))
    }
}

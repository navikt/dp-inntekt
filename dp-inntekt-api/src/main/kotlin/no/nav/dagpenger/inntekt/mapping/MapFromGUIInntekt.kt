package no.nav.dagpenger.inntekt.mapping

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.inntekt.db.DetachedInntekt
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.TilleggInformasjonsDetaljer
import java.time.LocalDateTime.now

fun mapToStoredInntekt(guiInntekt: GUIInntekt): StoredInntekt =
    StoredInntekt(
        guiInntekt.inntektId ?: InntektId(ULID().nextULID()),
        InntektkomponentResponse(
            mapToArbeidsInntektMaaneder(guiInntekt.inntekt.arbeidsInntektMaaned)
                ?: emptyList(),
            guiInntekt.inntekt.ident,
        ),
        guiInntekt.manueltRedigert,
    )

fun mapToStoredInntekt(
    inntekterDto: InntekterDto,
    inntektId: String,
): StoredInntekt =
    StoredInntekt(
        inntektId = InntektId(id = inntektId),
        inntekt = mapToInntektkomponentResponse(inntekterDto),
        manueltRedigert = true,
        timestamp = now(),
    )

fun mapToDetachedInntekt(guiInntekt: GUIInntekt): DetachedInntekt =
    DetachedInntekt(
        InntektkomponentResponse(
            mapToArbeidsInntektMaaneder(guiInntekt.inntekt.arbeidsInntektMaaned)
                ?: emptyList(),
            guiInntekt.inntekt.ident,
        ),
        guiInntekt.manueltRedigert,
    )

private fun mapToInntektkomponentResponse(inntekterDto: InntekterDto): InntektkomponentResponse {
    val arbeidsInntektMaaneder: MutableList<ArbeidsInntektMaaned> = mutableListOf()

    inntekterDto.virksomheter.forEach { virksomhet ->
        arbeidsInntektMaaneder.addAll(
            virksomhet.inntekter?.map {
                ArbeidsInntektMaaned(
                    it.aarMaaned,
                    virksomhet.avvikListe,
                    ArbeidsInntektInformasjon(
                        virksomhet.inntekter.map { inntektMaaned ->
                            Inntekt(
                                inntektMaaned.belop,
                                inntektMaaned.inntektskilde,
                                inntektMaaned.beskrivelse,
                                inntektMaaned.inntektskilde,
                                inntektMaaned.inntektsstatus,
                                inntektMaaned.inntektsperiodetype ?: "Maaned",
                                inntektMaaned.leveringstidspunkt,
                                inntektMaaned.opptjeningsland,
                                inntektMaaned.opptjeningsperiode,
                                inntektMaaned.skattemessigBosattLand,
                                inntektMaaned.utbetaltIMaaned,
                                // TODO: Er det mottaker som skal brukes her?
                                mapToAktoerNaturligIdent(inntekterDto.mottaker.fnr),
                                // TODO: Er det virksomhet som skal brukes her?
                                mapToAktoerOrganisasjon(virksomhet),
                                mapToAktoerOrganisasjon(virksomhet),
                                mapToAktoerNaturligIdent(inntekterDto.mottaker.fnr),
                                inntektMaaned.inngaarIGrunnlagForTrekk,
                                inntektMaaned.utloeserArbeidsgiveravgift,
                                inntektMaaned.informasjonsstatus,
                                inntektMaaned.inntektType,
                                inntektMaaned.tilleggsinformasjon,
                            )
                        },
                    ),
                )
            } ?: listOf(),
        )
    }

    return InntektkomponentResponse(
        arbeidsInntektMaaneder,
        mapToAktoerNaturligIdent(inntekterDto.mottaker.fnr),
    )
}

private fun mapToAktoerOrganisasjon(virksomhet: Virksomhet): Aktoer = Aktoer(AktoerType.ORGANISASJON, virksomhet.virksomhetsnummer)

private fun mapToAktoerNaturligIdent(fnr: String?): Aktoer =
    Aktoer(AktoerType.NATURLIG_IDENT, fnr ?: throw IllegalArgumentException("Fødselsenummer mangler"))

private fun mapToArbeidsInntektMaaneder(arbeidsMaaneder: List<GUIArbeidsInntektMaaned>?): List<ArbeidsInntektMaaned>? =
    arbeidsMaaneder?.map { guiArbeidsInntektMaaned ->
        ArbeidsInntektMaaned(
            guiArbeidsInntektMaaned.aarMaaned,
            guiArbeidsInntektMaaned.avvikListe,
            ArbeidsInntektInformasjon(
                guiArbeidsInntektMaaned.arbeidsInntektInformasjon?.inntektListe?.map { inntekt ->
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
                        datagrunnlagForVerdikode.forhold?.let {
                            TilleggInformasjon(
                                inntekt.tilleggsinformasjon?.kategori,
                                TilleggInformasjonsDetaljer(
                                    inntekt.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.detaljerType,
                                    it,
                                ),
                            )
                        },
                    )
                } ?: emptyList(),
            ),
        )
    }

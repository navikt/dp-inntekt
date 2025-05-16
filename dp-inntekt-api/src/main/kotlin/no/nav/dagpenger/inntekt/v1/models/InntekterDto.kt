package no.nav.dagpenger.inntekt.v1.models

import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Aktoer
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.AktoerType
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektInformasjon
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.Inntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.mapping.Inntektsmottaker
import no.nav.dagpenger.inntekt.mapping.Virksomhet
import java.time.LocalDateTime.now

data class InntekterDto(
    val virksomheter: List<Virksomhet>,
    val mottaker: Inntektsmottaker,
)

fun InntekterDto.mapToStoredInntekt(inntektId: String): StoredInntekt =
    StoredInntekt(
        inntektId = InntektId(id = inntektId),
        inntekt = mapToInntektkomponentResponse(this),
        manueltRedigert = true,
        timestamp = now(),
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
                                mapToAktoerNaturligIdent(inntekterDto.mottaker.pnr),
                                // TODO: Er det virksomhet som skal brukes her?
                                mapToAktoerOrganisasjon(virksomhet),
                                mapToAktoerOrganisasjon(virksomhet),
                                mapToAktoerNaturligIdent(inntekterDto.mottaker.pnr),
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
        mapToAktoerNaturligIdent(inntekterDto.mottaker.pnr),
    )
}

private fun mapToAktoerOrganisasjon(virksomhet: Virksomhet): Aktoer = Aktoer(AktoerType.ORGANISASJON, virksomhet.virksomhetsnummer)

private fun mapToAktoerNaturligIdent(fnr: String?): Aktoer =
    Aktoer(AktoerType.NATURLIG_IDENT, fnr ?: throw IllegalArgumentException("Fødselsenummer mangler"))

package no.nav.dagpenger.inntekt.mapping

import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import java.time.LocalDateTime

fun mapToGUIInntekt(
    inntekt: InntektkomponentResponse,
    opptjeningsPeriode: Opptjeningsperiode,
    inntektsmottaker: Inntektsmottaker?,
) = GUIInntekt(
    null,
    LocalDateTime.now(),
    GUIInntektsKomponentResponse(
        tilDato = opptjeningsPeriode.sisteAvsluttendeKalenderMåned,
        fraDato = opptjeningsPeriode.førsteMåned,
        arbeidsInntektMaaned = mapToArbeidsInntektMaaneder(inntekt.arbeidsInntektMaaned),
        ident = inntekt.ident,
    ),
    false,
    false,
    inntektsmottaker,
)

fun mapToGUIInntekt(
    storedInntekt: StoredInntekt,
    opptjeningsPeriode: Opptjeningsperiode,
    inntektsMottaker: Inntektsmottaker?,
) = GUIInntekt(
    storedInntekt.inntektId,
    storedInntekt.timestamp,
    GUIInntektsKomponentResponse(
        tilDato = opptjeningsPeriode.sisteAvsluttendeKalenderMåned,
        fraDato = opptjeningsPeriode.førsteMåned,
        arbeidsInntektMaaned = mapToArbeidsInntektMaaneder(storedInntekt.inntekt.arbeidsInntektMaaned),
        ident = storedInntekt.inntekt.ident,
    ),
    storedInntekt.manueltRedigert,
    storedInntekt.manueltRedigert,
    inntektsmottaker = inntektsMottaker,
)

private fun mapToArbeidsInntektMaaneder(list: List<ArbeidsInntektMaaned>?) =
    list?.map { arbeidsInntektMaaned ->
        GUIArbeidsInntektMaaned(
            arbeidsInntektMaaned.aarMaaned,
            arbeidsInntektMaaned.avvikListe,
            GUIArbeidsInntektInformasjon(
                arbeidsInntektMaaned.arbeidsInntektInformasjon?.inntektListe?.map {
                    InntektMedVerdikode(
                        beloep = it.beloep,
                        fordel = it.fordel,
                        beskrivelse = it.beskrivelse,
                        inntektskilde = it.inntektskilde,
                        inntektsstatus = it.inntektsstatus,
                        inntektsperiodetype = it.inntektsperiodetype,
                        leveringstidspunkt = it.leveringstidspunkt,
                        opptjeningsland = it.opptjeningsland,
                        opptjeningsperiode = it.opptjeningsperiode,
                        skattemessigBosattLand = it.skattemessigBosattLand,
                        utbetaltIMaaned = it.utbetaltIMaaned,
                        opplysningspliktig = it.opplysningspliktig,
                        inntektsinnsender = it.inntektsinnsender,
                        virksomhet = it.virksomhet,
                        inntektsmottaker = it.inntektsmottaker,
                        inngaarIGrunnlagForTrekk = it.inngaarIGrunnlagForTrekk,
                        utloeserArbeidsgiveravgift = it.utloeserArbeidsgiveravgift,
                        informasjonsstatus = it.informasjonsstatus,
                        inntektType = it.inntektType,
                        tilleggsinformasjon = it.tilleggsinformasjon,
                        verdikode =
                            verdiKode(
                                DatagrunnlagKlassifisering(
                                    it.inntektType,
                                    it.beskrivelse,
                                    it.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.spesielleInntjeningsforhold,
                                ),
                            ),
                    )
                } ?: emptyList(),
            ),
        )
    }

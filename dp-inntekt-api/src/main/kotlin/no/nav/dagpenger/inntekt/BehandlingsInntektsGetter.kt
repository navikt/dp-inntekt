package no.nav.dagpenger.inntekt

import mu.KotlinLogging
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.SpesifisertInntekt
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.StoreInntektCommand
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentRequest
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.klassifiserer.klassifiserOgMapInntekt
import no.nav.dagpenger.inntekt.mapping.mapToSpesifisertInntekt

private val LOGGER = KotlinLogging.logger {}

class BehandlingsInntektsGetter(
    private val inntektskomponentClient: InntektskomponentClient,
    private val inntektStore: InntektStore
) {
    suspend fun getKlassifisertInntekt(inntektparametre: Inntektparametre, callId: String? = null): Inntekt {
        return klassifiserOgMapInntekt(getSpesifisertInntekt(inntektparametre, callId))
    }

    suspend fun getSpesifisertInntekt(inntektparametre: Inntektparametre, callId: String? = null): SpesifisertInntekt {
        return mapToSpesifisertInntekt(
            getBehandlingsInntekt(inntektparametre, callId),
            inntektparametre.opptjeningsperiode.sisteAvsluttendeKalenderMåned
        )
    }

    internal suspend fun getBehandlingsInntekt(
        inntektparametre: Inntektparametre,
        callId: String? = null
    ): StoredInntekt {
        return isInntektStored(inntektparametre)?.let {
            LOGGER.info { "Henter stored inntekt: ${inntektparametre.toDebugString()}" }
            inntektStore.getInntekt(it)
        }
            ?: fetchAndStoreInntekt(inntektparametre, callId)
    }

    private suspend fun fetchAndStoreInntekt(
        inntektparametre: Inntektparametre,
        callId: String? = null
    ): StoredInntekt {
        val inntektkomponentRequest = InntektkomponentRequest(
            aktørId = inntektparametre.aktørId,
            fødselsnummer = inntektparametre.fødselsnummer,
            månedFom = inntektparametre.opptjeningsperiode.førsteMåned,
            månedTom = inntektparametre.opptjeningsperiode.sisteAvsluttendeKalenderMåned
        )
        return inntektStore.storeInntekt(
            StoreInntektCommand(
                inntektparametre = inntektparametre,
                inntekt = inntektskomponentClient.getInntekt(inntektkomponentRequest, callId = callId)
            )
        )
    }

    private fun isInntektStored(inntektparametre: Inntektparametre) = inntektStore.getInntektId(inntektparametre)
}

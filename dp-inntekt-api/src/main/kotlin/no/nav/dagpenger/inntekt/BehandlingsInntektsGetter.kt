package no.nav.dagpenger.inntekt

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.db.Inntektparametre
import no.nav.dagpenger.inntekt.db.StoreInntektCommand
import no.nav.dagpenger.inntekt.db.StoredInntekt
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentRequest
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.klassifiserer.klassifiserOgMapInntekt
import no.nav.dagpenger.inntekt.mapping.mapToSpesifisertInntekt
import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.inntekt.v1.SpesifisertInntekt

private val LOGGER = KotlinLogging.logger {}

class BehandlingsInntektsGetter(
    private val inntektskomponentClient: InntektskomponentClient,
    private val inntektStore: InntektStore,
) {
    suspend fun getKlassifisertInntekt(
        inntektparametre: Inntektparametre,
        callId: String? = null,
    ): Inntekt = klassifiserOgMapInntekt(getSpesifisertInntekt(inntektparametre, callId))

    fun getKlassifisertInntekt(inntektId: InntektId): Inntekt = klassifiserOgMapInntekt(inntektStore.getSpesifisertInntekt(inntektId))

    suspend fun getSpesifisertInntekt(
        inntektparametre: Inntektparametre,
        callId: String? = null,
    ): SpesifisertInntekt =
        mapToSpesifisertInntekt(
            getBehandlingsInntekt(inntektparametre, callId),
            inntektparametre.opptjeningsperiode.sisteAvsluttendeKalenderMåned,
        )

    internal suspend fun getBehandlingsInntekt(
        inntektparametre: Inntektparametre,
        callId: String? = null,
    ): StoredInntekt =
        isInntektStored(inntektparametre)?.let {
            LOGGER.info { "Henter stored inntekt: ${inntektparametre.toDebugString()}" }
            inntektStore.getInntekt(it)
        }
            ?: fetchAndStoreInntekt(inntektparametre, callId)

    private suspend fun fetchAndStoreInntekt(
        inntektparametre: Inntektparametre,
        callId: String? = null,
    ): StoredInntekt {
        val inntektkomponentRequest =
            InntektkomponentRequest(
                aktørId = inntektparametre.aktørId,
                fødselsnummer = inntektparametre.fødselsnummer,
                månedFom = inntektparametre.opptjeningsperiode.førsteMåned,
                månedTom = inntektparametre.opptjeningsperiode.sisteAvsluttendeKalenderMåned,
            )
        return inntektStore.storeInntekt(
            StoreInntektCommand(
                inntektparametre = inntektparametre,
                inntekt = inntektskomponentClient.getInntekt(inntektkomponentRequest, callId = callId),
            ),
        )
    }

    private fun isInntektStored(inntektparametre: Inntektparametre) = inntektStore.getInntektId(inntektparametre)
}

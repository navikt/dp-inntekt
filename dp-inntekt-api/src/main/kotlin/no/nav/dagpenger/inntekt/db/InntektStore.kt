package no.nav.dagpenger.inntekt.db

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import no.nav.dagpenger.inntekt.v1.SpesifisertInntekt
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

interface InntektStore {
    fun getInntekt(inntektId: InntektId): StoredInntekt

    fun getSpesifisertInntekt(inntektId: InntektId): SpesifisertInntekt

    fun getInntektId(inntektparametre: Inntektparametre): InntektId?

    fun getBeregningsdato(inntektId: InntektId): LocalDate

    fun getInntektPersonMapping(inntektId: String): InntektPersonMapping

    fun storeInntekt(
        command: StoreInntektCommand,
        created: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    ): StoredInntekt

    fun getManueltRedigert(inntektId: InntektId): ManueltRedigert?

    fun markerInntektBrukt(inntektId: InntektId): Int

    fun getStoredInntektMedMetadata(inntektId: InntektId): StoredInntektMedMetadata
}

data class Inntektparametre(
    val aktørId: String,
    val fødselsnummer: String,
    val beregningsdato: LocalDate,
    val regelkontekst: RegelKontekst,
) {
    val opptjeningsperiode: Opptjeningsperiode = Opptjeningsperiode(beregningsdato)

    fun toDebugString(): String = "Inntektparametre(aktørId='$aktørId', beregningsdato=$beregningsdato, regelkontekst=$regelkontekst)"
}

data class RegelKontekst(
    val id: String,
    val type: String,
)

data class StoreInntektCommand(
    val inntektparametre: Inntektparametre,
    val inntekt: InntektkomponentResponse,
    val manueltRedigert: ManueltRedigert? = null,
)

data class InntektPersonMapping(
    val inntektId: InntektId,
    val aktørId: String,
    val fnr: String? = null,
    val kontekstId: String,
    val beregningsdato: LocalDate,
    val timestamp: LocalDateTime,
    val kontekstType: String,
)

data class ManueltRedigert(
    val redigertAv: String,
) {
    companion object {
        fun from(
            bool: Boolean,
            redigertAv: String,
        ) = when (bool) {
            true -> ManueltRedigert(redigertAv)
            false -> null
        }
    }
}

data class StoredInntekt(
    val inntektId: InntektId,
    val inntekt: InntektkomponentResponse,
    val manueltRedigert: Boolean,
    val timestamp: LocalDateTime? = null,
)

data class DetachedInntekt(
    val inntekt: InntektkomponentResponse,
    val manueltRedigert: Boolean,
)

data class InntektId(
    val id: String,
) {
    init {
        try {
            ULID.parseULID(id)
        } catch (e: IllegalArgumentException) {
            throw IllegalInntektIdException("ID $id is not a valid ULID", e)
        }
    }
}

data class StoredInntektMedMetadata(
    val inntektId: InntektId,
    val inntekt: InntektkomponentResponse,
    val manueltRedigert: Boolean,
    val timestamp: LocalDateTime? = null,
    val fødselsnummer: String,
)

class InntektNotFoundException(
    override val message: String,
) : RuntimeException(message)

class StoreException(
    override val message: String,
) : RuntimeException(message)

class IllegalInntektIdException(
    override val message: String,
    override val cause: Throwable?,
) : java.lang.RuntimeException(message, cause)

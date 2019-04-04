package no.nav.dagpenger.inntekt.db

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.v1.InntektRequest

class VoidInntektStore : InntektStore {

    private val ulidGenetor = ULID()

    override fun getInntekt(inntektId: InntektId): StoredInntekt {
        TODO("Not implemented")
    }

    override fun getInntektId(request: InntektRequest): InntektId? {
        return null
    }

    override fun insertInntekt(request: InntektRequest, inntekt: InntektkomponentResponse): StoredInntekt {
        return StoredInntekt(InntektId(ulidGenetor.nextULID()), inntekt)
    }
}
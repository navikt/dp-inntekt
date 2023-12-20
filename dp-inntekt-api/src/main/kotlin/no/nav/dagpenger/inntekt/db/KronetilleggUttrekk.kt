package no.nav.dagpenger.inntekt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.klassifiserer.klassifiserOgMapInntekt
import no.nav.dagpenger.inntekt.mapping.mapToSpesifisertInntekt
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import java.time.LocalDate
import javax.sql.DataSource

internal class KronetilleggUttrekk(private val dataSource: DataSource) {
    fun utrekkFra(inntektId: InntektId): Uttrekk =
        hentInntekt(inntektId).let { kronetilleggInntekt ->
            val opptjeningsperiode = Opptjeningsperiode(kronetilleggInntekt.beregningsdato)
            val klassifisertDbInntekt =
                klassifiserOgMapInntekt(
                    mapToSpesifisertInntekt(
                        kronetilleggInntekt.inntekt,
                        opptjeningsperiode.sisteAvsluttendeKalenderMåned,
                    ),
                )
            return if (klassifisertDbInntekt.inntektsListe.any { inntekt ->
                    inntekt.klassifiserteInntekter.any {
                        it.inntektKlasse == InntektKlasse.DAGPENGER ||
                            it.inntektKlasse == InntektKlasse.DAGPENGER_FANGST_FISKE
                    }
                }
            ) {
                Uttrekk(inntektId.id, true)
            } else {
                Uttrekk(inntektId.id, false)
            }
        }

    private fun hentInntekt(inntektId: InntektId) =
        using(sessionOf(dataSource)) { session ->
            session.run(
                // language=PostgreSQL
                queryOf(
                    """
                    SELECT id, inntekt.inntekt, manuelt_redigert, beregningsdato, kontekstid
                    FROM inntekt_v1_person_mapping
                        JOIN inntekt_v1 inntekt on inntekt.id = inntekt_v1_person_mapping.inntektid
                    WHERE inntektid = :inntektId;
                    """.trimIndent(),
                    mapOf(
                        "inntektId" to inntektId.id,
                    ),
                ).map {
                    KronetilleggInntekt(
                        beregningsdato = it.localDate("beregningsdato"),
                        inntekt =
                            StoredInntekt(
                                inntektId = InntektId(inntektId.id),
                                inntekt = jacksonObjectMapper.readValue(it.string("inntekt"), InntektkomponentResponse::class.java)!!,
                                manueltRedigert = it.boolean("manuelt_redigert"),
                            ),
                        vedtakId = it.int("kontekstid"),
                    )
                }.asSingle,
            ) ?: throw InntektNotFoundException("Inntekt with id ${inntektId.id} not found.")
        }
}

private data class KronetilleggInntekt(
    val beregningsdato: LocalDate,
    val inntekt: StoredInntekt,
    val vedtakId: Int,
)

data class Uttrekk(val id: String, val svar: Boolean)

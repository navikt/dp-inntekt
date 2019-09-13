package no.nav.dagpenger.inntekt.db

import com.squareup.moshi.JsonAdapter
import de.huxhorn.sulky.ulid.ULID
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.BehandlingsKey
import no.nav.dagpenger.inntekt.HealthCheck
import no.nav.dagpenger.inntekt.HealthStatus
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse

import no.nav.dagpenger.inntekt.moshiInstance
import org.postgresql.util.PSQLException
import java.time.LocalDate
import javax.sql.DataSource

internal class PostgresInntektStore(private val dataSource: DataSource) : InntektStore, HealthCheck {
    private val LOGGER = KotlinLogging.logger {}

    override fun getManueltRedigert(inntektId: InntektId): ManueltRedigert? {
        try {
            return using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        """SELECT redigert_av
                                    FROM inntekt_V1_manuelt_redigert
                                    WHERE inntekt_id = ?
                            """.trimMargin(), inntektId.id
                    ).map { row ->
                        ManueltRedigert(row.string(1))
                    }.asSingle
                )
            }
        } catch (p: PSQLException) {
            throw StoreException(p.message!!)
        }
    }

    private val adapter: JsonAdapter<InntektkomponentResponse> =
        moshiInstance.adapter(InntektkomponentResponse::class.java)
    private val ulidGenerator = ULID()

    override fun getInntektId(request: BehandlingsKey): InntektId? {
        try {
            return using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        """SELECT inntektId
                                    FROM inntekt_V1_arena_mapping
                                    WHERE aktørId = ? AND vedtakid = ? AND beregningsdato = ?
                                    ORDER BY timestamp DESC LIMIT 1
                            """.trimMargin(), request.aktørId, request.vedtakId, request.beregningsDato
                    ).map { row ->
                        InntektId(row.string("inntektId"))
                    }.asSingle
                )
            }
        } catch (p: PSQLException) {
            throw StoreException(p.message!!)
        }
    }

    override fun getBeregningsdato(inntektId: InntektId): LocalDate {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """SELECT beregningsdato
                                FROM inntekt_V1_arena_mapping
                                WHERE inntektId = ?
                        """.trimMargin(), inntektId.id
                ).map { row ->
                    row.localDate("beregningsdato")
                }.asSingle
            ) ?: throw InntektNotFoundException("Inntekt with id $inntektId not found.")
        }
    }

    override fun getInntekt(inntektId: InntektId): StoredInntekt {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """ SELECT id, inntekt, manuelt_redigert, timestamp from inntekt_V1 where id = ?""",
                    inntektId.id
                ).map { row ->
                    StoredInntekt(
                        inntektId = InntektId(row.string("id")),
                        inntekt = adapter.fromJson(row.string("inntekt"))!!,
                        manueltRedigert = row.boolean("manuelt_redigert"),
                        timestamp = row.zonedDateTime("timestamp").toLocalDateTime()
                    )
                }
                    .asSingle)
                ?: throw InntektNotFoundException("Inntekt with id $inntektId not found.")
        }
    }

    override fun insertInntekt(request: BehandlingsKey, inntekt: InntektkomponentResponse): StoredInntekt =
        insertInntekt(request, inntekt, null)

    override fun insertInntekt(
        request: BehandlingsKey,
        inntekt: InntektkomponentResponse,
        manueltRedigert: ManueltRedigert?
    ): StoredInntekt {
        try {
            val inntektId = InntektId(ulidGenerator.nextULID())
            using(sessionOf(dataSource)) { session ->
                session.transaction { tx ->
                    tx.run(
                        queryOf(
                            "INSERT INTO inntekt_V1 (id, inntekt, manuelt_redigert) VALUES (?, (to_json(?::json)), ?)",
                            inntektId.id,
                            adapter.toJson(inntekt),
                            manueltRedigert?.let { true } ?: false
                        ).asUpdate
                    )
                    tx.run(
                        queryOf(
                            "INSERT INTO inntekt_V1_arena_mapping VALUES (?, ?, ?, ?)",
                            inntektId.id,
                            request.aktørId,
                            request.vedtakId,
                            request.beregningsDato
                        ).asUpdate
                    )

                    manueltRedigert?.let {
                        tx.run(
                            queryOf(
                                "INSERT INTO inntekt_V1_manuelt_redigert VALUES(?,?)",
                                inntektId.id,
                                it.redigertAv
                            ).asUpdate
                        )
                    }
                }
            }
            return getInntekt(inntektId)
        } catch (p: PSQLException) {
            throw StoreException(p.message!!)
        }
    }

    override fun markerInntektBrukt(inntektId: InntektId): Int {
        try {
            return using(sessionOf(dataSource)) { session ->
                session.transaction { tx ->
                    tx.run(
                        queryOf(
                            "UPDATE inntekt_V1 SET brukt = true WHERE id = :id AND NOT EXISTS (" +
                                "   SELECT 1 FROM inntekt_V1 WHERE brukt = true AND id = :id" + ");",
                            mapOf("id" to inntektId.id)
                        ).asUpdate
                    )
                }
            }
        } catch (p: PSQLException) {
            throw StoreException(p.message!!)
        }
    }

    override fun status(): HealthStatus {
        return try {
            using(sessionOf(dataSource)) { session -> session.run(queryOf(""" SELECT 1""").asExecute) }.let { HealthStatus.UP }
        } catch (p: PSQLException) {
            LOGGER.error("Failed health check", p)
            return HealthStatus.DOWN
        }
    }
}

package no.nav.dagpenger.inntekt.db

import com.fasterxml.jackson.module.kotlin.readValue
import de.huxhorn.sulky.ulid.ULID
import io.prometheus.metrics.core.metrics.Summary
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.HealthCheck
import no.nav.dagpenger.inntekt.HealthStatus
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.mapping.mapToSpesifisertInntekt
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import no.nav.dagpenger.inntekt.serder.jacksonObjectMapper
import no.nav.dagpenger.inntekt.v1.SpesifisertInntekt
import org.intellij.lang.annotations.Language
import org.postgresql.util.PGobject
import org.postgresql.util.PSQLException
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.sql.DataSource

@Suppress("ktlint:standard:max-line-length")
internal class PostgresInntektStore(
    private val dataSource: DataSource,
) : InntektStore,
    HealthCheck {
    companion object {
        private val ulidGenerator = ULID()
        private val LOGGER = KotlinLogging.logger {}
        private val markerInntektTimer =
            Summary
                .builder()
                .name("marker_inntekt_brukt")
                .help("Hvor lang tid det tar å markere en inntekt brukt (i sekunder")
                .register()
    }

    override fun getManueltRedigert(inntektId: InntektId): ManueltRedigert? {
        @Language("sql")
        val statement =
            """
            SELECT redigert_av
                FROM inntekt_V1_manuelt_redigert
            WHERE inntekt_id = ?
            """.trimMargin()
        try {
            return using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(statement, inntektId.id)
                        .map { row ->
                            ManueltRedigert(row.string(1))
                        }.asSingle,
                )
            }
        } catch (p: PSQLException) {
            throw StoreException(p.message!!)
        }
    }

    override fun getInntektId(inntektparametre: Inntektparametre): InntektId? {
        try {
            @Language("sql")
            val statement: String =
                """
                SELECT inntektId
                    FROM inntekt_V1_person_mapping
                WHERE aktørId = ? 
                AND (fnr = ? OR fnr IS NULL)
                AND kontekstId = ? 
                AND kontekstType = ?::kontekstTypeNavn
                AND beregningsdato = ? 
                ORDER BY timestamp DESC LIMIT 1
                """.trimMargin()

            return using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        statement,
                        inntektparametre.aktørId,
                        inntektparametre.fødselsnummer,
                        inntektparametre.regelkontekst.id,
                        inntektparametre.regelkontekst.type,
                        inntektparametre.beregningsdato,
                    ).map { row ->
                        InntektId(row.string("inntektId"))
                    }.asSingle,
                )
            }
        } catch (p: PSQLException) {
            throw StoreException(p.message!!)
        }
    }

    override fun getInntektPersonMapping(inntektId: String): InntektPersonMapping {
        @Language("sql")
        val statement = "SELECT * FROM inntekt_V1_person_mapping WHERE inntektId = :inntektId)".trimMargin()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    statement,
                    mapOf("inntektId" to inntektId),
                ).map { row ->
                    InntektPersonMapping(
                        inntektId = InntektId(row.string("inntektid")),
                        aktoerId = row.string("aktorid"),
                        fnr = row.string("fnr"),
                        kontekstId = row.string("kontekstid"),
                        beregningsdato = row.zonedDateTime("beregningsdato").toLocalDate(),
                        timestamp = row.zonedDateTime("timestamp").toLocalDateTime(),
                        kontekstType = row.string("konteksttype"),
                    )
                }.asSingle,
            ) ?: throw InntektNotFoundException("Inntekt with id $inntektId not found.")
        }
    }

    override fun getBeregningsdato(inntektId: InntektId): LocalDate {
        @Language("sql")
        val statement =
            """SELECT coalesce(
               (SELECT beregningsdato FROM inntekt_V1_person_mapping WHERE inntektId = :inntektId),
               (SELECT beregningsdato FROM temp_inntekt_V1_person_mapping WHERE inntektId = :inntektId)
           ) as beregningsdato
            """.trimMargin()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    statement,
                    mapOf("inntektId" to inntektId.id),
                ).map { row ->
                    row.localDateOrNull("beregningsdato")
                }.asSingle,
            ) ?: throw InntektNotFoundException("Inntekt with id $inntektId not found.")
        }
    }

    override fun getInntekt(inntektId: InntektId): StoredInntekt =
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """ SELECT id, inntekt, manuelt_redigert, timestamp from inntekt_V1 where id = ?""",
                    inntektId.id,
                ).map { row ->
                    StoredInntekt(
                        inntektId = InntektId(row.string("id")),
                        inntekt = row.binaryStream("inntekt").use { jacksonObjectMapper.readValue<InntektkomponentResponse>(it) },
                        manueltRedigert = row.boolean("manuelt_redigert"),
                        timestamp = row.zonedDateTime("timestamp").toLocalDateTime(),
                    )
                }.asSingle,
            )
                ?: throw InntektNotFoundException("Inntekt with id $inntektId not found.")
        }

    override fun getSpesifisertInntekt(inntektId: InntektId): SpesifisertInntekt {
        @Language("sql")
        val statement =
            """ 
            SELECT inntekt.id, inntekt.inntekt, inntekt.manuelt_redigert, inntekt.timestamp, mapping.beregningsdato 
            from inntekt_V1 inntekt 
            inner join inntekt_V1_person_mapping mapping on inntekt.id = mapping.inntektid 
            where inntekt.id = ?
            
            """.trimIndent()

        val stored =
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        statement,
                        inntektId.id,
                    ).map { row ->
                        StoredInntekt(
                            inntektId = InntektId(row.string("id")),
                            inntekt = row.binaryStream("inntekt").use { jacksonObjectMapper.readValue<InntektkomponentResponse>(it) },
                            manueltRedigert = row.boolean("manuelt_redigert"),
                            timestamp = row.zonedDateTime("timestamp").toLocalDateTime(),
                        ) to row.localDate("beregningsdato")
                    }.asSingle,
                )
                    ?: throw InntektNotFoundException("Inntekt with id $inntektId not found.")
            }
        return mapToSpesifisertInntekt(stored.first, Opptjeningsperiode(stored.second).sisteAvsluttendeKalenderMåned)
    }

    override fun getInntektMedPersonFnr(inntektId: InntektId): StoredInntektMedFnr {
        @Language("sql")
        val statement =
            """ 
            SELECT inntekt.id, inntekt.inntekt, inntekt.manuelt_redigert, inntekt.timestamp, mapping.fnr 
            from inntekt_V1 inntekt 
            inner join inntekt_V1_person_mapping mapping on inntekt.id = mapping.inntektid 
            where inntekt.id = ?
            
            """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(statement, inntektId.id)
                    .map {
                        StoredInntektMedFnr(
                            inntektId = InntektId(it.string("id")),
                            inntekt = it.binaryStream("inntekt").use { jacksonObjectMapper.readValue<InntektkomponentResponse>(it) },
                            manueltRedigert = it.boolean("manuelt_redigert"),
                            timestamp = it.zonedDateTime("timestamp").toLocalDateTime(),
                            fødselsnummer = it.string("fnr"),
                        )
                    }.asSingle,
            ) ?: throw InntektNotFoundException("Inntekt with id $inntektId not found.")
        }
    }

    override fun storeInntekt(
        command: StoreInntektCommand,
        created: ZonedDateTime,
    ): StoredInntekt {
        try {
            val inntektId = InntektId(ulidGenerator.nextULID())
            using(sessionOf(dataSource)) { session ->
                session.transaction { tx ->
                    tx.run(
                        queryOf(
                            "INSERT INTO inntekt_V1 (id, inntekt, manuelt_redigert, timestamp) VALUES (:id, :data, :manuelt, :created)",
                            mapOf(
                                "id" to inntektId.id,
                                "created" to created,
                                "data" to
                                    PGobject().apply {
                                        type = "jsonb"
                                        value = jacksonObjectMapper.writeValueAsString(command.inntekt)
                                    },
                                when (command.manueltRedigert) {
                                    null -> "manuelt" to false
                                    else -> "manuelt" to true
                                },
                            ),
                        ).asUpdate,
                    )
                    tx.run(
                        queryOf(
                            "INSERT INTO inntekt_V1_person_mapping(inntektId, aktørId, fnr, kontekstId, beregningsdato, kontekstType) VALUES (:inntektId, :aktorId, :fnr, :kontekstId, :beregningsdato, :kontekstType::kontekstTypeNavn)",
                            mapOf(
                                "inntektId" to inntektId.id,
                                "aktorId" to command.inntektparametre.aktørId,
                                "fnr" to command.inntektparametre.fødselsnummer,
                                "kontekstId" to command.inntektparametre.regelkontekst.id,
                                "kontekstType" to command.inntektparametre.regelkontekst.type,
                                "beregningsdato" to command.inntektparametre.beregningsdato,
                            ),
                        ).asUpdate,
                    )

                    command.manueltRedigert?.let {
                        tx.run(
                            queryOf(
                                "INSERT INTO inntekt_V1_manuelt_redigert VALUES(:id,:redigert)",
                                mapOf(
                                    "id" to inntektId.id,
                                    "redigert" to it.redigertAv,
                                ),
                            ).asUpdate,
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
        val timer = markerInntektTimer.startTimer()
        try {
            return using(sessionOf(dataSource)) { session ->
                session.transaction { tx ->
                    tx.run(
                        queryOf(
                            """
                            UPDATE inntekt_V1 SET brukt = true WHERE id = :id;
                            """.trimIndent(),
                            mapOf("id" to inntektId.id),
                        ).asUpdate,
                    )
                }
            }
        } catch (p: PSQLException) {
            throw StoreException(p.message!!)
        } finally {
            timer.observeDuration()
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

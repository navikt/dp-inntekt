package no.nav.dagpenger.inntekt.subsumsjonbrukt

import io.prometheus.client.Counter
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource

const val INNTEKT_SLETTET = "inntekt_slettet"
private val deleteCounter =
    Counter.build()
        .name(INNTEKT_SLETTET)
        .help("Antall inntektsett slettet fra databasen")
        .register()

class Vaktmester(private val dataSource: DataSource, private val lifeSpanInDays: Int = 180) {
    fun rydd() {
        val rowCount =
            using(sessionOf(dataSource)) { session ->
                session.transaction { transaction ->
                    transaction.run(
                        queryOf(
                            """
                            DELETE FROM inntekt_v1 WHERE brukt = false AND timestamp  < (now() - (make_interval(days := :days)))
                            """.trimIndent(),
                            mapOf(
                                "days" to lifeSpanInDays,
                            ),
                        ).asUpdate,
                    )
                }
            }
        deleteCounter.inc(rowCount.toDouble())
    }
}

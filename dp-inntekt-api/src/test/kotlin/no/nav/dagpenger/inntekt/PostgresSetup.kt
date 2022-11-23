package no.nav.dagpenger.inntekt

import com.zaxxer.hikari.HikariDataSource
import no.nav.dagpenger.inntekt.db.clean
import no.nav.dagpenger.inntekt.db.migrate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy

fun withCleanDb(test: () -> Unit) = DataSource.instance.also { clean(it) }.run { test() }

fun withMigratedDb(test: () -> Unit) =
    DataSource.instance.also { clean(it) }.also { migrate(it) }.run { test() }

object PostgresContainer {
    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:11.2").apply {
            this.waitingFor(HostPortWaitStrategy())
            start()
        }
    }
}

object DataSource {
    val instance: HikariDataSource by lazy {
        HikariDataSource().apply {
            username = PostgresContainer.instance.username
            password = PostgresContainer.instance.password
            jdbcUrl = PostgresContainer.instance.jdbcUrl
            connectionTimeout = 1000L
        }
    }
}

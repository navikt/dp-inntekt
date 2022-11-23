package no.nav.dagpenger.inntekt

import com.zaxxer.hikari.HikariDataSource
import no.nav.dagpenger.inntekt.db.clean
import no.nav.dagpenger.inntekt.db.migrate
import org.flywaydb.core.internal.configuration.ConfigUtils
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy

fun withMigratedDb(block: () -> Unit) {
    withCleanDb {
        migrate(DataSource.instance)
        block()
    }
}

fun withCleanDb(block: () -> Unit) {
    setup()
    clean(DataSource.instance).run {
        block()
    }.also {
        tearDown()
    }
}

object PostgresContainer {
    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:11.2").apply {
            this.waitingFor(HostPortWaitStrategy())
            start()
        }
    }
}

fun setup() {
    System.setProperty(ConfigUtils.CLEAN_DISABLED, "false")
}

fun tearDown() {
    System.clearProperty(ConfigUtils.CLEAN_DISABLED)
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

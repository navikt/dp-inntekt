package no.nav.dagpenger.inntekt.db

import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.stringType
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.Config
import no.nav.dagpenger.inntekt.Config.config
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.CleanResult
import org.flywaydb.core.internal.configuration.ConfigUtils

private val logger = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

// Understands how to create a data source from environment variables
internal object PostgresDataSourceBuilder {
    const val DB_USERNAME_KEY = "DB_USERNAME"
    const val DB_PASSWORD_KEY = "DB_PASSWORD"
    const val DB_DATABASE_KEY = "DB_DATABASE"
    const val DB_HOST_KEY = "DB_HOST"
    const val DB_PORT_KEY = "DB_PORT"

    val dataSource by lazy {
        HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", config[Key(DB_HOST_KEY, stringType)])
            addDataSourceProperty("portNumber", config[Key(DB_PORT_KEY, stringType)])
            addDataSourceProperty("databaseName", config[Key(DB_DATABASE_KEY, stringType)])
            addDataSourceProperty("user", config[Key(DB_USERNAME_KEY, stringType)])
            addDataSourceProperty("password", config[Key(DB_PASSWORD_KEY, stringType)])
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            initializationFailTimeout = 5000
            maxLifetime = 30001
        }.also {
            sikkerlogg.info { "Created datasource ${it.dataSourceClassName} for ${it.dataSourceProperties}" }
        }
    }

    private val flyWayBuilder: FluentConfiguration =
        Flyway.configure()
            .connectRetries(10)
            .validateMigrationNaming(true)
            .failOnMissingLocations(true)
            // Skru på denne for å støtte CONCURRENTLY opprettelse av indekser
            .configuration(java.util.Map.of("flyway.postgresql.transactional.lock", "false"))

    fun clean(): CleanResult =
        flyWayBuilder
            .cleanDisabled(Config.config[Key(ConfigUtils.CLEAN_DISABLED, booleanType)])
            .dataSource(dataSource)
            .load()
            .clean()

    internal fun runMigration(initSql: String? = null): Int =
        flyWayBuilder
            .dataSource(dataSource)
            .initSql(initSql)
            .locations("db/migration")
            .load()
            .migrate()
            .migrations
            .size

    internal fun runMigration(locations: List<String>): Int =
        flyWayBuilder
            .dataSource(dataSource)
            .locations(*locations.toTypedArray())
            .load()
            .migrate()
            .migrations
            .size
}

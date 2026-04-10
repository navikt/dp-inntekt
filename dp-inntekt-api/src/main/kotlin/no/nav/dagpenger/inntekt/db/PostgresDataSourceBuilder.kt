package no.nav.dagpenger.inntekt.db

import ch.qos.logback.core.util.OptionHelper.getEnv
import ch.qos.logback.core.util.OptionHelper.getSystemProperty
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.dagpenger.inntekt.Config
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.CleanResult
import org.flywaydb.core.internal.configuration.ConfigUtils
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// Understands how to create a data source from environment variables
internal object PostgresDataSourceBuilder {
    const val DB_USERNAME_KEY = "DB_USERNAME"
    const val DB_PASSWORD_KEY = "DB_PASSWORD"
    const val DB_URL_KEY = "DB_URL"
    const val DB_JDBC_URL_KEY = "DB_JDBC_URL"

    val dataSource by lazy { HikariDataSource(hikariConfig) }

    private fun getOrThrow(key: String): String = getEnv(key) ?: getSystemProperty(key)

    private fun getOrElse(
        key: String,
        secondKey: String,
    ): String = runCatching { getOrThrow(key) }.getOrElse { getOrThrow(secondKey) }

    private val hikariConfig by lazy {
        HikariConfig().apply {
            // Støtter både DB_URL og DB_JDBC_URL for å være kompatibel med både databaser laget der JDBC URL er inkludert og der det ikke er det. DB_JDBC_URL har prioritet over DB_URL
            jdbcUrl =
                getOrElse(DB_JDBC_URL_KEY, DB_URL_KEY).ensurePrefix("jdbc:postgresql://").stripCredentials()
            username = getOrThrow(DB_USERNAME_KEY)
            password = getOrThrow(DB_PASSWORD_KEY)
            // Default 10
            maximumPoolSize = 10
            // Default 30 sekund
            connectionTimeout = 10.seconds.inWholeMilliseconds
            // Default 10 minutter
            idleTimeout = 10.minutes.inWholeMilliseconds
            // Default 2 minutter
            keepaliveTime = 2.minutes.inWholeMilliseconds
            // Default 30 minutter
            maxLifetime = 30.minutes.inWholeMilliseconds
            leakDetectionThreshold = 10.seconds.inWholeMilliseconds
            metricRegistry =
                PrometheusMeterRegistry(
                    PrometheusConfig.DEFAULT,
                    PrometheusRegistry.defaultRegistry,
                    Clock.SYSTEM,
                )
        }
    }

    private val flyWayBuilder: FluentConfiguration =
        Flyway
            .configure()
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

    private fun String.stripCredentials() = this.replace(Regex("://.*:.*@"), "://")

    private fun String.ensurePrefix(prefix: String) =
        if (this.startsWith(prefix)) {
            this
        } else {
            prefix + this.substringAfter("//")
        }
}

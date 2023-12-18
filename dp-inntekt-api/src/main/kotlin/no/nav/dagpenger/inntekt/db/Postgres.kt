package no.nav.dagpenger.inntekt.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.dagpenger.inntekt.InntektApiConfig
import no.nav.dagpenger.inntekt.Profile
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.configuration.ConfigUtils

fun migrate(config: InntektApiConfig): Int {
    return when (config.application.profile) {
        Profile.LOCAL ->
            HikariDataSource(hikariConfigFrom(config)).use {
                migrate(
                    dataSource = it,
                    locations = config.database.flywayLocations,
                )
            }

        else ->
            hikariDataSourceWithVaultIntegration(config, Role.ADMIN).use {
                migrate(
                    dataSource = it,
                    initSql = "SET ROLE \"${config.database.name}-${Role.ADMIN}\"",
                    locations = config.database.flywayLocations,
                )
            }
    }
}

private fun hikariDataSourceWithVaultIntegration(
    config: InntektApiConfig,
    role: Role = Role.USER,
) = HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(
    hikariConfigFrom(config),
    config.vault.mountPath,
    "${config.database.name}-$role",
)

fun dataSourceFrom(config: InntektApiConfig): HikariDataSource =
    when (config.application.profile) {
        Profile.LOCAL -> HikariDataSource(hikariConfigFrom(config))
        else -> hikariDataSourceWithVaultIntegration(config)
    }

fun hikariConfigFrom(config: InntektApiConfig) =
    HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${config.database.host}:${config.database.port}/${config.database.name}"
        maximumPoolSize = 2
        minimumIdle = 0
        idleTimeout = 10001
        connectionTimeout = 1000
        maxLifetime = 30001
        config.database.user?.let { username = it }
        config.database.password?.let { password = it }
    }

fun migrate(
    dataSource: HikariDataSource,
    initSql: String = "",
    locations: List<String> = listOf("db/migration"),
): Int =
    Flyway.configure().validateMigrationNaming(true).locations(*locations.toTypedArray()).dataSource(dataSource)
        .initSql(initSql).load().migrate().migrations.size

fun clean(dataSource: HikariDataSource) =
    Flyway.configure().cleanDisabled(
        System.getProperty(ConfigUtils.CLEAN_DISABLED)?.toBooleanStrict() ?: true,
    ).dataSource(dataSource).load().clean()

private enum class Role {
    ADMIN,
    USER,
    ;

    override fun toString() = name.toLowerCase()
}

package no.nav.dagpenger.inntekt

import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.dagpenger.streams.Credential
import no.nav.dagpenger.streams.KafkaAivenCredentials
import java.net.InetAddress
import java.net.UnknownHostException

internal object Config {
    private val localProperties =
        ConfigurationMap(
            mapOf(
                "database.host" to "localhost",
                "database.port" to "5432",
                "database.name" to "dp-inntekt-db",
                "database.user" to "postgres",
                "database.password" to "postgres",
                "vault.mountpath" to "postgresql/dev/",
                "KAFKA_BROKERS" to "localhost:9092",
                "application.profile" to "LOCAL",
                "application.httpPort" to "8099",
                "hentinntektliste.url" to "https://localhost/inntektskomponenten-ws/rs/api/v1/hentinntektliste",
                "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret/api",
                "oidc.sts.issuerurl" to "http://localhost/",
                "srvdp.inntekt.api.username" to "postgres",
                "srvdp.inntekt.api.password" to "postgres",
                "flyway.locations" to "db/migration,db/testdata",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "pdl.url" to "http://localhost:4321",
                "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret",
            ),
        )
    private val devProperties =
        ConfigurationMap(
            mapOf(
                "database.host" to "b27dbvl013.preprod.local",
                "database.port" to "5432",
                "database.name" to "dp-inntekt-db-q0",
                "flyway.locations" to "db/migration,db/testdata",
                "vault.mountpath" to "postgresql/preprod-fss/",
                "hentinntektliste.url" to "https://app-q0.adeo.no/inntektskomponenten-ws/rs/api/v1/hentinntektliste",
                "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret/api",
                "oidc.sts.issuerurl" to "https://security-token-service.nais.preprod.local/",
                "application.profile" to "DEV",
                "application.httpPort" to "8099",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "pdl.url" to "https://pdl-api-q1.dev.intern.nav.no/graphql",
                "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret",
            ),
        )
    private val prodProperties =
        ConfigurationMap(
            mapOf(
                "database.host" to "fsspgdb.adeo.no",
                "database.port" to "5432",
                "database.name" to "dp-inntekt-db",
                "flyway.locations" to "db/migration",
                "vault.mountpath" to "postgresql/prod-fss/",
                "hentinntektliste.url" to "https://app.adeo.no/inntektskomponenten-ws/rs/api/v1/hentinntektliste",
                "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret/api",
                "oidc.sts.issuerurl" to "https://security-token-service.nais.adeo.no/",
                "application.profile" to "PROD",
                "application.httpPort" to "8099",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "pdl.url" to "http://pdl-api.pdl.svc.nais.local/graphql",
                "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret",
            ),
        )
    val config by lazy {
        when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
            "dev-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding devProperties
            "prod-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prodProperties
            else -> {
                ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding localProperties
            }
        }
    }
    private val Configuration.database
        get() =
            InntektApiConfig.Database(
                host = this[Key("database.host", stringType)],
                port = this[Key("database.port", stringType)],
                name = this[Key("database.name", stringType)],
                user = this.getOrNull(Key("database.user", stringType)),
                password = this.getOrNull(Key("database.password", stringType)),
                flywayLocations =
                    this.getOrNull(Key("flyway.locations", stringType))?.split(",")
                        ?: listOf("db/migration"),
            )
    private val Configuration.vault
        get() =
            InntektApiConfig.Vault(
                mountPath = this[Key("vault.mountpath", stringType)],
            )
    private val Configuration.pdl
        get() =
            InntektApiConfig.Pdl(
                url = this[Key("pdl.url", stringType)],
            )
    private val Configuration.enhetsregister
        get() =
            InntektApiConfig.Enhetsregister(
                url = this[Key("enhetsregisteret.url", stringType)],
            )
    private val Configuration.profile get() = this[Key("application.profile", stringType)].let { Profile.valueOf(it) }
    private val Configuration.application
        get() =
            InntektApiConfig.Application(
                id = this.getOrElse(Key("application.id", stringType), "dp-inntekt-api-consumer"),
                brokers = this[Key("KAFKA_BROKERS", stringType)],
                profile = this.profile,
                credential = if (this.profile == Profile.LOCAL) null else KafkaAivenCredentials(),
                httpPort = this[Key("application.httpPort", intType)],
                username = this[Key("srvdp.inntekt.api.username", stringType)],
                password = this[Key("srvdp.inntekt.api.password", stringType)],
                hentinntektListeUrl = this[Key("hentinntektliste.url", stringType)],
                enhetsregisteretUrl = this[Key("enhetsregisteret.url", stringType)],
                oicdStsUrl = this[Key("oidc.sts.issuerurl", stringType)],
                name = "dp-inntekt-api",
            )
    val Configuration.inntektApiConfig
        get() =
            InntektApiConfig(
                database = this.database,
                vault = this.vault,
                application = this.application,
                pdl = this.pdl,
                enhetsregisteretUrl = this.enhetsregister,
                inntektBruktDataTopic = this[Key("kafka.inntekt.brukt.topic", stringType)],
            )
}

data class InntektApiConfig(
    val database: Database,
    val vault: Vault,
    val application: Application,
    val pdl: Pdl,
    val enhetsregisteretUrl: Enhetsregister,
    val inntektBruktDataTopic: String,
) {
    data class Database(
        val host: String,
        val port: String,
        val name: String,
        val user: String?,
        val password: String?,
        val flywayLocations: List<String>,
    )

    data class Vault(
        val mountPath: String,
    )

    data class Application(
        val id: String,
        val brokers: String,
        val profile: Profile,
        val credential: Credential?,
        val httpPort: Int,
        val username: String,
        val password: String,
        val hentinntektListeUrl: String,
        val enhetsregisteretUrl: String,
        val oicdStsUrl: String,
        val name: String,
    )

    data class Pdl(
        val url: String,
    )

    data class Enhetsregister(
        val url: String,
    )
}

private fun getHostname(): String {
    return try {
        val addr: InetAddress = InetAddress.getLocalHost()
        addr.hostName
    } catch (e: UnknownHostException) {
        "unknown"
    }
}

enum class Profile {
    LOCAL,
    DEV,
    PROD,
}

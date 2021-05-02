package no.nav.dagpenger.inntekt

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.finn.unleash.util.UnleashConfig
import no.nav.dagpenger.streams.KafkaAivenCredentials
import java.net.InetAddress
import java.net.UnknownHostException

private val localProperties = ConfigurationMap(
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
        "jwks.url" to "https://localhost",
        "jwks.issuer" to "https://localhost",
        "srvdp.inntekt.api.username" to "postgres",
        "srvdp.inntekt.api.password" to "postgres",
        "flyway.locations" to "db/migration,db/testdata",
        "api.secret" to "secret",
        "api.keys" to "dp-datalaster-inntekt",
        "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
        "unleash.url" to "http://localhost/api/",
        "pdl.url" to "http://localhost:4321",
        "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret"
    )
)
private val devProperties = ConfigurationMap(
    mapOf(
        "database.host" to "b27dbvl007.preprod.local",
        "database.port" to "5432",
        "database.name" to "dp-inntekt-db-preprod",
        "flyway.locations" to "db/migration,db/testdata",
        "vault.mountpath" to "postgresql/preprod-fss/",
        "hentinntektliste.url" to "https://app-q1.adeo.no/inntektskomponenten-ws/rs/api/v1/hentinntektliste",
        "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret/api",
        "oidc.sts.issuerurl" to "https://security-token-service.nais.preprod.local/",
        "jwks.url" to "https://isso-q.adeo.no:443/isso/oauth2/connect/jwk_uri",
        "jwks.issuer" to "https://isso-q.adeo.no:443/isso/oauth2",
        "application.profile" to "DEV",
        "application.httpPort" to "8099",
        "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
        "unleash.url" to "https://unleash.nais.io/api/",
        "pdl.url" to "http://pdl-api.default.svc.nais.local/graphql",
        "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret"
    )
)
private val prodProperties = ConfigurationMap(
    mapOf(
        "database.host" to "fsspgdb.adeo.no",
        "database.port" to "5432",
        "database.name" to "dp-inntekt-db",
        "flyway.locations" to "db/migration",
        "vault.mountpath" to "postgresql/prod-fss/",
        "hentinntektliste.url" to "https://app.adeo.no/inntektskomponenten-ws/rs/api/v1/hentinntektliste",
        "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret/api",
        "oidc.sts.issuerurl" to "https://security-token-service.nais.adeo.no/",
        "jwks.url" to "https://isso.adeo.no:443/isso/oauth2/connect/jwk_uri",
        "jwks.issuer" to "https://isso.adeo.no:443/isso/oauth2",
        "application.profile" to "PROD",
        "application.httpPort" to "8099",
        "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
        "unleash.url" to "https://unleash.nais.io/api/",
        "pdl.url" to "http://pdl-api.default.svc.nais.local/graphql",
        "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret"
    )
)

data class Configuration(
    val database: Database = Database(),
    val vault: Vault = Vault(),
    val application: Application = Application(),
    val pdl: Pdl = Pdl(),
    val enhetsregisteretUrl: Enhetsregister = Enhetsregister(),
    val inntektBruktDataTopic: String = config()[Key("kafka.inntekt.brukt.topic", stringType)]
) {

    data class Database(
        val host: String = config()[Key("database.host", stringType)],
        val port: String = config()[Key("database.port", stringType)],
        val name: String = config()[Key("database.name", stringType)],
        val user: String? = config().getOrNull(Key("database.user", stringType)),
        val password: String? = config().getOrNull(Key("database.password", stringType)),
        val flywayLocations: List<String> = config().getOrNull(Key("flyway.locations", stringType))?.split(",")
            ?: listOf("db/migration")

    )

    data class Vault(
        val mountPath: String = config()[Key("vault.mountpath", stringType)]
    )

    data class Application(
        val id: String = config().getOrElse(Key("application.id", stringType), "dp-inntekt-api-consumer"),
        val brokers: String = config()[Key("KAFKA_BROKERS", stringType)],
        val profile: Profile = config()[Key("application.profile", stringType)].let { Profile.valueOf(it) },
        val credential: KafkaAivenCredentials? = if (profile == Profile.LOCAL) null else KafkaAivenCredentials(),
        val httpPort: Int = config()[Key("application.httpPort", intType)],
        val username: String = config()[Key("srvdp.inntekt.api.username", stringType)],
        val password: String = config()[Key("srvdp.inntekt.api.password", stringType)],
        val hentinntektListeUrl: String = config()[Key("hentinntektliste.url", stringType)],
        val enhetsregisteretUrl: String = config()[Key("enhetsregisteret.url", stringType)],
        val oicdStsUrl: String = config()[Key("oidc.sts.issuerurl", stringType)],
        val jwksUrl: String = config()[Key("jwks.url", stringType)],
        val jwksIssuer: String = config()[Key("jwks.issuer", stringType)],
        val name: String = "dp-inntekt-api",
        val apiSecret: String = config()[Key("api.secret", stringType)],
        val allowedApiKeys: List<String> = config()[Key("api.keys", stringType)].split(",").toList(),
        val unleashConfig: UnleashConfig = UnleashConfig.builder()
            .appName(config().getOrElse(Key("app.name", stringType), "dp-inntekt-api"))
            .instanceId(getHostname())
            .unleashAPI(config()[Key("unleash.url", stringType)])
            .build()
    )

    data class Pdl(
        val url: String = config()[Key("pdl.url", stringType)]
    )

    data class Enhetsregister(
        val url: String = config()[Key("enhetsregisteret.url", stringType)]
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
    LOCAL, DEV, PROD
}

fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
    "dev-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding devProperties
    "prod-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prodProperties
    else -> {
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding localProperties
    }
}

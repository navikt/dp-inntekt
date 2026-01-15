package no.nav.dagpenger.inntekt

import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.oauth2.CachedOauth2Client
import no.nav.dagpenger.oauth2.OAuth2Config
import org.apache.kafka.common.security.auth.SecurityProtocol

object Config {
    private val localProperties =
        ConfigurationMap(
            mapOf(
                "KAFKA_BROKERS" to "localhost:9092",
                "application.httpPort" to "8099",
                "application.profile" to "LOCAL",
                "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret",
                "flyway.locations" to "db/migration,db/testdata",
                "hentinntektliste.url" to "https://inntektskomponenten-ws/rs/api/v1/hentinntektliste",
                "inntektskomponenten.api.scope" to "api://dev-fss.team-inntekt.ikomp-q1/.default",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "pdl.api.scope" to "api://dev-fss.pdl.pdl-api/.default",
                "pdl.url" to "https://pdl-api.dev-fss-pub.nais.io/graphql",
                "dp-behandling.api.scope" to "api://dev-fss.teamdagpenger.dp-behandling/.default",
                "dp-behandling.url" to "http://dp-behandling",
            ),
        )
    private val devProperties =
        ConfigurationMap(
            mapOf(
                "application.httpPort" to "8099",
                "application.profile" to "DEV",
                "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret",
                @Suppress("ktlint:standard:max-line-length")
                "hentinntektliste.url"
                    to "https://ikomp-q1.dev-fss-pub.nais.io/rs/api/v1/hentinntektliste",
                "inntektskomponenten.api.scope" to "api://dev-fss.team-inntekt.ikomp-q1/.default",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "pdl.api.scope" to "api://dev-fss.pdl.pdl-api-q1/.default",
                "pdl.url" to "https://pdl-api-q1.dev-fss-pub.nais.io/graphql",
                "dp-behandling.api.scope" to "api://dev-gcp.teamdagpenger.dp-behandling/.default",
                "dp-behandling.url" to "http://dp-behandling",
            ),
        )
    private val prodProperties =
        ConfigurationMap(
            mapOf(
                "application.httpPort" to "8099",
                "application.profile" to "PROD",
                "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret",
                @Suppress("ktlint:standard:max-line-length")
                "hentinntektliste.url"
                    to "https://ikomp.prod-fss-pub.nais.io/rs/api/v1/hentinntektliste",
                "inntektskomponenten.api.scope" to "api://prod-fss.team-inntekt.ikomp/.default",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "pdl.api.scope" to "api://prod-fss.pdl.pdl-api/.default",
                "pdl.url" to "https://pdl-api.prod-fss-pub.nais.io/graphql",
                "dp-behandling.api.scope" to "api://prod-gcp.teamdagpenger.dp-behandling/.default",
                "dp-behandling.url" to "http://dp-behandling",
            ),
        )
    val config by lazy {
        when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
            "dev-gcp" -> {
                ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding devProperties
            }

            "prod-gcp" -> {
                ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prodProperties
            }

            else -> {
                ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding localProperties
            }
        }
    }
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
    private val Configuration.dpBehandling
        get() =
            InntektApiConfig.DpBehandling(
                url = this[Key("dp-behandling.url", stringType)],
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
                hentinntektListeUrl = this[Key("hentinntektliste.url", stringType)],
                enhetsregisteretUrl = this[Key("enhetsregisteret.url", stringType)],
                name = "dp-inntekt-api",
            )
    val Configuration.inntektApiConfig
        get() =
            InntektApiConfig(
                application = this.application,
                pdl = this.pdl,
                enhetsregisteretUrl = this.enhetsregister,
                inntektBruktDataTopic = this[Key("kafka.inntekt.brukt.topic", stringType)],
                dpBehandling = this.dpBehandling,
            )

    val pdlTokenProvider by lazy {
        azureAdTokenSupplier(config[Key("pdl.api.scope", stringType)])
    }

    data class KafkaAivenCredentials(
        val securityProtocolConfig: String = SecurityProtocol.SSL.name,
        val sslEndpointIdentificationAlgorithmConfig: String = "",
        val sslTruststoreTypeConfig: String = "jks",
        val sslKeystoreTypeConfig: String = "PKCS12",
        val sslTruststoreLocationConfig: String = "/var/run/secrets/nais.io/kafka/client.truststore.jks",
        val sslTruststorePasswordConfig: String = config[Key("KAFKA_CREDSTORE_PASSWORD", stringType)],
        val sslKeystoreLocationConfig: String = "/var/run/secrets/nais.io/kafka/client.keystore.p12",
        val sslKeystorePasswordConfig: String = sslTruststorePasswordConfig,
    )

    val inntektsKomponentTokenProvider by lazy {
        azureAdTokenSupplier(config[Key("inntektskomponenten.api.scope", stringType)])
    }

    val dpBehandlingTokenProvider by lazy {
        azureAdOBOTokenSupplier(
            config[Key("dp-behandling.api.scope", stringType)],
        )
    }

    private val azureAdClient: CachedOauth2Client by lazy {
        val azureAdConfig = OAuth2Config.AzureAd(config)
        CachedOauth2Client(
            tokenEndpointUrl = azureAdConfig.tokenEndpointUrl,
            authType = azureAdConfig.clientSecret(),
        )
    }

    private fun azureAdTokenSupplier(scope: String): () -> String =
        {
            runBlocking {
                azureAdClient.clientCredentials(scope).access_token ?: throw RuntimeException("Failed to get token")
            }
        }

    private fun azureAdOBOTokenSupplier(scope: String): (token: String) -> String =
        { token ->
            runBlocking {
                azureAdClient.onBehalfOf(token, scope).access_token ?: throw RuntimeException("Failed to get token")
            }
        }
}

data class InntektApiConfig(
    val application: Application,
    val pdl: Pdl,
    val enhetsregisteretUrl: Enhetsregister,
    val inntektBruktDataTopic: String,
    val dpBehandling: DpBehandling,
) {
    data class Application(
        val id: String,
        val brokers: String,
        val profile: Profile,
        val credential: Config.KafkaAivenCredentials?,
        val httpPort: Int,
        val hentinntektListeUrl: String,
        val enhetsregisteretUrl: String,
        val name: String,
    )

    data class Pdl(
        val url: String,
    )

    data class Enhetsregister(
        val url: String,
    )

    data class DpBehandling(
        val url: String,
    )
}

enum class Profile {
    LOCAL,
    DEV,
    PROD,
}

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
import no.nav.dagpenger.streams.Credential
import no.nav.dagpenger.streams.KafkaAivenCredentials

internal object Config {
    private val localProperties =
        ConfigurationMap(
            mapOf(
                "KAFKA_BROKERS" to "localhost:9092",
                "application.httpPort" to "8099",
                "application.profile" to "LOCAL",
                "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret",
                "flyway.locations" to "db/migration,db/testdata",
                "hentinntektliste.url" to "https://localhost/proxy/inntektskomponenten-ws/rs/api/v1/hentinntektliste",
                "inntektskomponenten.api.scope" to "api://dev-fss.team-inntekt.team-inntekt-proxy/.default",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "pdl.api.scope" to "api://dev-fss.pdl.pdl-api/.default",
                "pdl.url" to "https://pdl-api.dev-fss-pub.nais.io/graphql",
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
                    to "https://team-inntekt-proxy.dev-fss-pub.nais.io/proxy/inntektskomponenten-q0/rs/api/v1/hentinntektliste",
                "inntektskomponenten.api.scope" to "api://dev-fss.team-inntekt.inntektskomponenten/.default",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "pdl.api.scope" to "api://dev-fss.pdl.pdl-api-q1/.default",
                "pdl.url" to "https://pdl-api-q1.dev-fss-pub.nais.io/graphql",
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
                    to "https://team-inntekt-proxy.prod-fss-pub.nais.io/proxy/inntektskomponenten/rs/api/v1/hentinntektliste",
                "inntektskomponenten.api.scope" to "api://prod-fss.team-inntekt.inntektskomponenten/.default",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "pdl.api.scope" to "api://prod-fss.pdl.pdl-api/.default",
                "pdl.url" to "https://pdl-api.prod-fss-pub.nais.io/graphql",
            ),
        )
    val config by lazy {
        when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
            "dev-gcp" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding devProperties
            "prod-gcp" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prodProperties
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
            )

    val pdlTokenProvider by lazy {
        azureAdTokenSupplier(config[Key("pdl.api.scope", stringType)])
    }

    val inntektsKomponentTokenProvider by lazy {
        azureAdTokenSupplier(config[Key("inntektskomponenten.api.scope", stringType)])
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
}

data class InntektApiConfig(
    val application: Application,
    val pdl: Pdl,
    val enhetsregisteretUrl: Enhetsregister,
    val inntektBruktDataTopic: String,
) {
    data class Application(
        val id: String,
        val brokers: String,
        val profile: Profile,
        val credential: Credential?,
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
}

enum class Profile {
    LOCAL,
    DEV,
    PROD,
}

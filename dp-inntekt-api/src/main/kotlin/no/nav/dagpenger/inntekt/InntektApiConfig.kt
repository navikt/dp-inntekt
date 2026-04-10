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

object Config {
    private val localProperties =
        ConfigurationMap(
            mapOf(
                "KAFKA_BROKERS" to "localhost:9092",
                "application.httpPort" to "8099",
                "enhetsregisteret.api.url" to "https://data.brreg.no/enhetsregisteret",
                "flyway.locations" to "db/migration,db/testdata",
                "inntektskomponenten.api.url" to "https://inntektskomponenten-ws/rs/api/v1/hentinntektliste",
                "inntektskomponenten.api.scope" to "api://dev-fss.team-inntekt.ikomp-q2/.default",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "pdl.api.scope" to "api://dev-fss.pdl.pdl-api/.default",
                "pdl.api.url" to "https://pdl-api.dev-fss-pub.nais.io/graphql",
                "dp-behandling.api.scope" to "api://dev-gcp.teamdagpenger.dp-behandling/.default",
                "dp-behandling.api.url" to "http://dp-behandling",
            ),
        )
    private val devProperties =
        ConfigurationMap(
            mapOf(
                "application.httpPort" to "8099",
                "enhetsregisteret.api.url" to "https://data.brreg.no/enhetsregisteret",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "dp-behandling.api.scope" to "api://dev-gcp.teamdagpenger.dp-behandling/.default",
                "dp-behandling.api.url" to "http://dp-behandling",
            ),
        )
    private val prodProperties =
        ConfigurationMap(
            mapOf(
                "application.httpPort" to "8099",
                "enhetsregisteret.api.url" to "https://data.brreg.no/enhetsregisteret",
                "kafka.inntekt.brukt.topic" to "teamdagpenger.inntektbrukt.v1",
                "dp-behandling.api.scope" to "api://prod-gcp.teamdagpenger.dp-behandling/.default",
                "dp-behandling.api.url" to "http://dp-behandling",
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
                url = this[Key("pdl.api.url", stringType)],
            )
    private val Configuration.enhetsregister
        get() =
            InntektApiConfig.Enhetsregister(
                url = this[Key("enhetsregisteret.api.url", stringType)],
            )
    private val Configuration.dpBehandling
        get() =
            InntektApiConfig.DpBehandling(
                url = this[Key("dp-behandling.api.url", stringType)],
            )

    private val Configuration.application
        get() =
            InntektApiConfig.Application(
                consumerGroup = this.getOrElse(Key("CONSUMER_GROUP", stringType), "dp-inntekt-api-consumer"),
                httpPort = this[Key("application.httpPort", intType)],
                hentinntektListeUrl = this[Key("inntektskomponenten.api.url", stringType)],
                enhetsregisteretUrl = this[Key("enhetsregisteret.api.url", stringType)],
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
        val consumerGroup: String,
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

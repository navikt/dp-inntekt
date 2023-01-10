package no.nav.dagpenger.inntekt

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.natpryce.konfig.Configuration
import com.natpryce.konfig.Key
import com.natpryce.konfig.stringType
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import mu.KotlinLogging
import java.net.URL
import java.util.concurrent.TimeUnit

private val LOGGER = KotlinLogging.logger {}

internal fun JWTAuthenticationProvider.Config.azureAdJWT(config: Configuration) {
    val jwksUri = config[Key("AZURE_OPENID_CONFIG_JWKS_URI", stringType)]
    val issuer = config[Key("AZURE_OPENID_CONFIG_ISSUER", stringType)]
    val clientId = config[Key("AZURE_APP_CLIENT_ID", stringType)]
    this.realm = "dp-inntekt-api"

    this.verifier(jwkProvider(jwksUri), issuer)
    validate { credentials: JWTCredential ->
        try {
            requireNotNull(credentials.payload.audience) {
                "Auth: Missing audience in token"
            }
            require(credentials.payload.audience.contains(clientId)) {
                "Auth: Valid audience not found in claims"
            }
            JWTPrincipal(credentials.payload)
        } catch (e: Throwable) {
            LOGGER.error("Unauthorized", e)
            null
        }
    }
}

private fun jwkProvider(url: String): JwkProvider {
    return JwkProviderBuilder(URL(url))
        .cached(10, 24, TimeUnit.HOURS) // cache up to 10 JWKs for 24 hours
        .rateLimited(
            10,
            1,
            TimeUnit.MINUTES
        ) // if not cached, only allow max 10 different keys per minute to be fetched from external provider
        .build()
}

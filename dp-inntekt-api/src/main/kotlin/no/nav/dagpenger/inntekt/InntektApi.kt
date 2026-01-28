package no.nav.dagpenger.inntekt

import com.natpryce.konfig.Configuration
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.dagpenger.inntekt.api.v1.enhetsregisteret
import no.nav.dagpenger.inntekt.api.v1.inntekt
import no.nav.dagpenger.inntekt.api.v1.opptjeningsperiodeApi
import no.nav.dagpenger.inntekt.api.v1.uklassifisertInntekt
import no.nav.dagpenger.inntekt.api.v3.inntektV3
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.dpbehandling.DpBehandlingKlient
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.oppslag.PersonOppslag
import no.nav.dagpenger.inntekt.oppslag.enhetsregister.EnhetsregisterClient

internal fun Application.inntektApi(
    config: Configuration = Config.config,
    inntektskomponentHttpClient: InntektskomponentClient,
    inntektStore: InntektStore,
    behandlingsInntektsGetter: BehandlingsInntektsGetter,
    personOppslag: PersonOppslag,
    enhetsregisterClient: EnhetsregisterClient,
    dpBehandlingKlient: DpBehandlingKlient,
) {
    install(DefaultHeaders)
    install(Authentication) {
        jwt("azure") {
            azureAdJWT(config)
        }
    }

    routing {
        route("/v1") {
            route("/inntekt") {
                uklassifisertInntekt(
                    inntektskomponentHttpClient,
                    inntektStore,
                    personOppslag,
                    enhetsregisterClient,
                    dpBehandlingKlient,
                )
            }
            opptjeningsperiodeApi(inntektStore)
            enhetsregisteret(enhetsregisterClient)
        }
        route("v2") {
            route("/inntekt") {
                authenticate("azure") {
                    inntekt(behandlingsInntektsGetter, personOppslag)
                }
            }
        }
        authenticate("azure") {
            route("/v3/inntekt") {
                inntektV3(behandlingsInntektsGetter, personOppslag, inntektStore, inntektskomponentHttpClient)
            }
        }
    }
}

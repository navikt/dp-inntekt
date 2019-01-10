package no.nav.dagpenger.inntekt

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

fun main() {
    val application = embeddedServer(Netty, port = 8080) {
        inntektApi()
    }
    application.start(wait = false)
    Runtime.getRuntime().addShutdownHook(Thread {
        application.stop(5, 60, TimeUnit.SECONDS)
    })
}

data class Inntekt(
    val number: BigDecimal
) {
    companion object {
        val
            sampleInntekt = Inntekt(BigDecimal(111))
    }
}

fun Application.inntektApi() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
        }
    }

    routing {

        route("inntekt") {
            get("/{id}") {

                val aktorId = call.parameters["id"]
                call.respond(
                    Inntekt.sampleInntekt
                )
            }
        }
    }
}

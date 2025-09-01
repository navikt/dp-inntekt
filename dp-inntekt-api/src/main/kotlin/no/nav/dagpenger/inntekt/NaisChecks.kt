package no.nav.dagpenger.inntekt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

private val LOGGER = KotlinLogging.logger {}

fun Routing.naischecks(
    healthChecks: List<HealthCheck>,
    meterRegistry: PrometheusMeterRegistry,
) {
    get("/isAlive") {
        val failingHealthChecks = healthChecks.filter { it.status() == HealthStatus.DOWN }

        when {
            failingHealthChecks.isEmpty() -> call.respondText("ALIVE", ContentType.Text.Plain)
            else -> {
                LOGGER.error("The health check(s) is failing ${failingHealthChecks.joinToString(", ")}")
                call.response.status(HttpStatusCode.ServiceUnavailable)
            }
        }
    }
    get("/isReady") {
        call.respondText("READY", ContentType.Text.Plain)
    }
    get("/metrics") {
        val names =
            call.request.queryParameters
                .getAll("name[]")
                ?.toSet() ?: setOf()
        call.respondText(meterRegistry.scrape(), ContentType.parse("text/plain; version=0.0.4; charset=utf-8"))
    }
}

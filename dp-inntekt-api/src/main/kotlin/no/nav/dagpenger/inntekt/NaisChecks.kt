package no.nav.dagpenger.inntekt

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}
private val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry

fun Routing.naischecks(healthChecks: List<HealthCheck>) {
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
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }
}

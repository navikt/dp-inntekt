package no.nav.dagpenger.inntekt

interface HealthCheck {
    fun status(): HealthStatus
}

enum class HealthStatus {
    UP,
    DOWN,
}

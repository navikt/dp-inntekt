package no.nav.dagpenger.inntekt

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.inntekt.db.PostgresInntektStore
import org.junit.jupiter.api.Test
import java.sql.SQLTransientConnectionException
import javax.sql.DataSource

internal class HealthCheckTest {
    @Test
    fun `skal la appen være i live når databasen er utilgjengelig`() {
        val dataSource =
            mockk<DataSource> {
                every { connection } throws SQLTransientConnectionException("No connection available")
            }
        val kafkaHelsesjekk =
            mockk<HealthCheck> {
                every { status() } returns HealthStatus.UP
            }

        val isAlive = aliveCheck(kafkaHelsesjekk)

        isAlive() shouldBe true
        PostgresInntektStore(dataSource).status() shouldBe HealthStatus.DOWN
    }

    @Test
    fun `skal ikke være i live når Kafka er nede`() {
        val kafkaHelsesjekk =
            mockk<HealthCheck> {
                every { status() } returns HealthStatus.DOWN
            }

        val isAlive = aliveCheck(kafkaHelsesjekk)

        isAlive() shouldBe false
    }
}

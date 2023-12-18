package no.nav.dagpenger.inntekt

import no.nav.dagpenger.inntekt.Config.inntektApiConfig
import org.junit.jupiter.api.Test

class ConfigurationTest {
    @Test
    fun `Default configuration is LOCAL `() {
        with(Config.config.inntektApiConfig) {
            kotlin.test.assertEquals(Profile.LOCAL, this.application.profile)
        }
    }

    @Test
    fun `System properties overrides hard coded properties`() {
        withProps(mapOf("database.host" to "SYSTEM_DB")) {
            with(Config.config.inntektApiConfig) {
                kotlin.test.assertEquals("SYSTEM_DB", this.database.host)
            }
        }
    }
}

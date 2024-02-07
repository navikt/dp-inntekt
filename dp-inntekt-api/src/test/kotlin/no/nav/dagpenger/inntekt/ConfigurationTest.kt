package no.nav.dagpenger.inntekt

import no.nav.dagpenger.inntekt.Config.inntektApiConfig
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ConfigurationTest {
    @Test
    fun `Default configuration is LOCAL `() {
        with(Config.config.inntektApiConfig) {
            assertEquals(Profile.LOCAL, this.application.profile)
        }
    }
}

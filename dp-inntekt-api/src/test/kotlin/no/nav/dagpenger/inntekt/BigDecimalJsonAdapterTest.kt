package no.nav.dagpenger.inntekt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class BigDecimalJsonAdapterTest {
    private val bigDecimalJsonAdapter = BigDecimalJsonAdapter()

    @Test
    fun toJson() {
        assertEquals("5.29999999999999982236431605997495353221893310546875", bigDecimalJsonAdapter.toJson(BigDecimal(5.3)))
    }

    @Test
    fun fromJson() {
        assertEquals(BigDecimal(5.3), bigDecimalJsonAdapter.fromJson("5.29999999999999982236431605997495353221893310546875"))
    }
}

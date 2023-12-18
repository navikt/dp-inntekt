package no.nav.dagpenger.inntekt

import org.junit.jupiter.api.Test
import java.time.YearMonth
import kotlin.test.assertEquals

internal class YearMonthJsonAdapterTest {
    private val yearMonthJsonAdapter = YearMonthJsonAdapter()

    @Test
    fun toJson() {
        assertEquals("2019-01", yearMonthJsonAdapter.toJson(YearMonth.of(2019, 1)))
    }

    @Test
    fun fromJson() {
        assertEquals(YearMonth.of(2019, 1), yearMonthJsonAdapter.fromJson("2019-01"))
    }
}

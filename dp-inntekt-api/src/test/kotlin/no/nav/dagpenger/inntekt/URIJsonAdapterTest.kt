package no.nav.dagpenger.inntekt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

internal class URIJsonAdapterTest {
    private val uriJsonAdapter = URIJsonAdapter()

    @Test
    fun toJson() {
        assertEquals("about:blank", uriJsonAdapter.toJson(URI("about:blank")))
    }

    @Test
    fun fromJson() {
        assertEquals(URI("about:blank"), uriJsonAdapter.fromJson("about:blank"))
    }
}

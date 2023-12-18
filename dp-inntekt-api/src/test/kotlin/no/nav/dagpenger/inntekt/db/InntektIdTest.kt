package no.nav.dagpenger.inntekt.db

import de.huxhorn.sulky.ulid.ULID
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.UUID
import kotlin.test.assertTrue

internal class InntektIdTest {
    @Test
    fun `Inntekt id should be in ULID format`() {
        val id = ULID().nextULID()
        val inntektId = InntektId(id)

        assertEquals(id, inntektId.id)
    }

    @Test
    fun `Inntekt id not in ULID format should fail`() {
        val id = UUID.randomUUID().toString()
        val result = kotlin.runCatching { InntektId(id) }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalInntektIdException)
    }
}

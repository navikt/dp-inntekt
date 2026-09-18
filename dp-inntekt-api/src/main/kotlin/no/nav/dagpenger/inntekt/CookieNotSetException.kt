package no.nav.dagpenger.inntekt

import java.lang.RuntimeException

class CookieNotSetException(
    override val message: String?,
) : RuntimeException(message)

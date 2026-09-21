package no.nav.dagpenger.inntekt

class UgyldigAuthorizationHeaderException(
    message: String,
) : RuntimeException(message)

typealias VedTilgangTilPerson = suspend (ident: String, token: String, block: suspend () -> Unit) -> Unit

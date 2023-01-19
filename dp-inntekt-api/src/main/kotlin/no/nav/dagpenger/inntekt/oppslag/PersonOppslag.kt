package no.nav.dagpenger.inntekt.oppslag

interface PersonOppslag {
    suspend fun hentPerson(ident: String): Person
}
class PersonNotFoundException(val ident: String?, msg: String = "Fant ikke person") : RuntimeException(msg)

data class Person(
    val fødselsnummer: String,
    val aktørId: String,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
) {
    fun sammensattNavn(): String = "$etternavn, $fornavn" + (mellomnavn?.let { " $it" } ?: "")
}

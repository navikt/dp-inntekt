package no.nav.dagpenger.inntekt.api.v1.models

data class FullVirksomhetsInformasjon(
    val organisasjonsnummer: String,
    val navn: String,
    val postadresse: Adresse,
    val forretningsadresse: Adresse,
)

data class Adresse(
    val land: String,
    val landkode: String,
    val postnummer: String,
    val poststed: String,
    val adresse: List<String>,
    val kommune: String,
    val kommunenummer: String,
)

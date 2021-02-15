package no.nav.dagpenger.inntekt

private val mockedConfigs = listOf(
    "srvdp.inntekt.api.username",
    "srvdp.inntekt.api.password",
    "hentinntektliste.url",
    "enhetsregisteret.url",
    "oppslag.url",
    "oidc.sts.issuerurl",
    "api.secret",
    "api.keys",
    "jwks.url",
    "jwks.issuer"
)

val dummyConfigs = mockedConfigs.associateWith { "test" }

fun withProps(props: Map<String, String>, test: () -> Unit) {
    for ((k, v) in props) {
        System.getProperties()[k] = v
    }
    test()
    for ((k, _) in props) {
        System.getProperties().remove(k)
    }
}

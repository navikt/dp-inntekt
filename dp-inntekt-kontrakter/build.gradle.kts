plugins {
    id("common")
    `java-library`
    id("maven-publish")
}

dependencies {
    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val githubUser: String? by project
val githubPassword: String? by project

publishing {

    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/dp-inntekt")
            credentials {
                username = githubUser
                password = githubPassword
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar.get())

            pom {
                name.set("kontrakter")
                description.set(
                    "Holder definisjonen av dagpenger inntekt",
                )
                url.set("https://github.com/navikt/dp-inntekt")
                withXml {
                    asNode().appendNode("packaging", "jar")
                }
                licenses {
                    license {
                        name.set("MIT License")
                        name.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        organization.set("NAV (Arbeids- og velferdsdirektoratet) - The Norwegian Labour and Welfare Administration")
                        organizationUrl.set("https://www.nav.no")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/navikt/dp-inntekt.git")
                    developerConnection.set("scm:git:git://github.com/navikt/dp-inntekt.git")
                    url.set("https://github.com/navikt/dp-inntekt")
                }
            }
        }
    }
}

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version Kotlin.version
    id(Spotless.spotless) version Spotless.version
    `java-library`
    `maven-publish`
}

val grpcVersion = "1.27.2"

allprojects {
    group = "no.nav.dagpenger"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = Spotless.spotless)

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        // ulid
        implementation(Ulid.ulid)

        testImplementation(kotlin("test"))
        testImplementation(Junit5.api)
        testRuntimeOnly(Junit5.engine)
        testImplementation(KoTest.assertions)
        testImplementation(KoTest.runner)
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<Wrapper> {
        gradleVersion = "6.5.1"
    }

    spotless {
        kotlin {
            targetExclude("**/generated/**") // ignore generated gRpc stuff
            ktlint(Ktlint.version)
        }
        kotlinGradle {
            target("*.gradle.kts", "buildSrc/**/*.kt*")
            ktlint(Ktlint.version)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            showStandardStreams = true
        }
    }

    tasks.named("compileKotlin") {
        dependsOn("spotlessApply", "spotlessKotlinCheck")
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        testImplementation(kotlin("test"))
        testImplementation(Junit5.api)
        testRuntimeOnly(Junit5.engine)
        testImplementation(Mockk.mockk)
    }

    val artifactDescription = "dp-inntekt-grpc - gRPC client for fetching inntekt in Dagpenger context"
    val repoUrl = "https://github.com/navikt/dp-inntekt.git"
    val scmUrl = "scm:git:https://github.com/navikt/dp-inntekt.git"

    val sourcesJar by tasks.registering(Jar::class) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    artifacts {
        add("archives", sourcesJar)
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifact(sourcesJar.get())

                pom {
                    description.set(artifactDescription)
                    name.set(project.name)
                    url.set(repoUrl)
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
                        connection.set(scmUrl)
                        developerConnection.set(scmUrl)
                        url.set(repoUrl)
                    }
                }
            }
        }
    }
}

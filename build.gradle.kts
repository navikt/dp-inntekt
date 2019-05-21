import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    kotlin("jvm") version "1.3.21"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

buildscript {
    repositories {
        mavenCentral()
    }
}

apply {
    plugin("com.diffplug.gradle.spotless")
}

repositories {
    jcenter()
    maven("https://jitpack.io")
}

application {
    applicationName = "dp-inntekt-api"
    mainClassName = "no.nav.dagpenger.inntekt.InntektApiKt"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val ktorVersion = "1.2.0"
val fuelVersion = "2.0.1"
val kotlinLoggingVersion = "1.6.22"
val jupiterVersion = "5.3.2"
val log4j2Version = "2.11.1"
val prometheusVersion = "0.6.0"
val moshiVersion = "1.8.0"
val ktorMoshiVersion = "1.0.1"
val mockkVersion = "1.9.1"
val flywayVersion = "6.0.0-beta"
val hikariVersion = "3.3.1"
val postgresVersion = "42.2.5"
val vaultJdbcVersion = "1.3.1"
val kotliqueryVersion = "1.3.0"
val vaultJavaDriverVersion = "3.1.0"
val konfigVersion = "1.6.10.0"
val testcontainers_version = "1.10.6"
val dpBibliotekerVersion = "2019.05.21-09.57.669ffe8e266f"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.ktor:ktor-server:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")

    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.ryanharter.ktor:ktor-moshi:$ktorMoshiVersion")

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-moshi:$fuelVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    implementation("com.vlkan.log4j2:log4j2-logstash-layout-fatjar:0.15")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.2.0")

    implementation("com.github.navikt.dp-biblioteker:sts-klient:$dpBibliotekerVersion")
    implementation("com.github.navikt.dp-biblioteker:ktor-utils:$dpBibliotekerVersion")

    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
    implementation("com.natpryce:konfig:$konfigVersion")
    implementation("no.nav:vault-jdbc:$vaultJdbcVersion") {
        exclude(module = "slf4j-simple")
        exclude(module = "slf4j-api")
    }

    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_log4j2:$prometheusVersion")

    implementation("no.bekk.bekkopen:nocommons:0.8.2")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:$jupiterVersion")
    testImplementation("com.github.tomakehurst:wiremock-standalone:2.21.0")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
    testImplementation("org.testcontainers:postgresql:$testcontainers_version")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

spotless {
    kotlin {
        ktlint("0.31.0")
    }
    kotlinGradle {
        target("*.gradle.kts", "additionalScripts/*.gradle.kts")
        ktlint("0.31.0")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "5.0"
}
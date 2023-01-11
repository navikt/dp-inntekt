import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    application
    id(Shadow.shadow) version Shadow.version
    id(Graphql.graphql) version Graphql.version
    id("de.undercouch.download") version "5.3.0"
}

buildscript {
    repositories {
        mavenCentral()
    }
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

application {
    applicationName = "dp-inntekt-api"
    mainClass.set("no.nav.dagpenger.inntekt.ApplicationKt")
}

val grpcVersion = "1.38.1"

dependencies {
    implementation(project(":dp-inntekt-grpc"))

    implementation(Dagpenger.Events)

    implementation(Ktor2.Server.library("netty"))
    implementation(Ktor2.Server.library("auth"))
    implementation(Ktor2.Server.library("auth-jwt"))
    implementation(Ktor2.Server.library("status-pages"))
    implementation(Ktor2.Server.library("call-id"))
    implementation(Ktor2.Server.library("call-logging"))
    implementation(Ktor2.Server.library("default-headers"))
    implementation(Ktor2.Server.library("content-negotiation"))
    implementation(Ktor2.Server.library("metrics-micrometer"))
    implementation(Ktor2.Server.library("metrics-micrometer"))

    implementation(Micrometer.prometheusRegistry)

    implementation(Graphql.client)
    implementation(Graphql.library("ktor-client"))
    implementation(Graphql.library("client-jackson"))
    implementation(Ktor2.Client.library("logging-jvm"))
    implementation(Ktor2.Client.library("apache"))

    // unleash
    implementation("no.finn.unleash:unleash-client-java:3.3.1")

    implementation(Moshi.moshi)
    implementation(Moshi.moshiAdapters)
    implementation(Moshi.moshiKotlin)
    implementation("com.github.cs125-illinois:ktor-moshi:2b13e43520")

    implementation(Dagpenger.Streams)
    implementation(Kafka.clients)

    implementation(Kotlin.Logging.kotlinLogging)

    implementation(Fuel.fuel)
    implementation(Fuel.fuelMoshi)
    implementation(Fuel.library("coroutines"))

    implementation(Log4j2.api)
    implementation(Log4j2.core)
    implementation(Log4j2.slf4j)
    implementation(Log4j2.library("jul")) // The Apache Log4j implementation of java.util.logging. java.util.logging used by gprc
    implementation(Log4j2.library("layout-template-json"))

    implementation(Ulid.ulid)

    implementation(Dagpenger.Biblioteker.stsKlient)

    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Postgres)
    implementation(Database.Kotlinquery)
    implementation(Konfig.konfig)
    implementation(Database.VaultJdbc) {
        exclude(module = "slf4j-simple")
        exclude(module = "slf4j-api")
    }

    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.log4j2)
    implementation(Bekk.nocommons)

    implementation(Kotlinx.bimap)

    // grpc
    testImplementation("io.grpc:grpc-testing:$grpcVersion")
    runtimeOnly("io.grpc:grpc-netty-shaded:$grpcVersion")

    testImplementation(kotlin("test"))
    testImplementation(Ktor2.Server.library("test-host")) {
        // https://youtrack.jetbrains.com/issue/KT-46090
        exclude("org.jetbrains.kotlin", "kotlin-test-junit")
    }

    testImplementation("no.nav.security:mock-oauth2-server:0.5.7")
    testImplementation(Ktor2.Client.library("mock"))
    testImplementation(Junit5.api)
    testImplementation(Junit5.params)
    testRuntimeOnly(Junit5.engine)
    testImplementation(Wiremock.standalone)
    testImplementation(KoTest.assertions)
    testImplementation(KoTest.runner)
    testImplementation(KoTest.property)
    testImplementation(TestContainers.postgresql)
    testImplementation(TestContainers.kafka)
    testImplementation(Mockk.mockk)
    testImplementation(JsonAssert.jsonassert)
}

tasks.named("shadowJar") {
    dependsOn("test")
}

tasks.named("compileKotlin") {
    dependsOn("spotlessCheck")
    dependsOn("graphqlGenerateClient")
}

tasks.named("test") {
    dependsOn("copySchemaToResources")
}

tasks.named("graphqlGenerateClient") {
    dependsOn("downloadPdlSDL")
}

val schema = "schema.graphql"

val graphqlGenerateClient by tasks.getting(com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask::class) {
    packageName.set("no.nav.pdl")
    schemaFile.set(File("$buildDir/$schema"))
    queryFileDirectory.set(File("$projectDir/src/main/resources"))
}

val downloadPdlSDL by tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadPdlSDL") {
    src("https://navikt.github.io/pdl/pdl-api-sdl.graphqls")
    dest(File(buildDir, schema))
}

tasks.register<Copy>("copySchemaToResources") {
    dependsOn(downloadPdlSDL)

    from(buildDir) {
        include(schema)
    }
    into("$projectDir/src/main/resources")
}

// To get intellij to make sense of generated sources from graphql client
java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    val graphqlDir = "$buildDir/generated/source/graphql/main"
    mainJavaSourceSet.srcDirs(graphqlDir)
}

tasks.withType<ShadowJar> {
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}

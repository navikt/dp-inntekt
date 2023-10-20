import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer


plugins {
    application
    id(Shadow.shadow) version Shadow.version
    id("com.expediagroup.graphql") version "6.4.0"
    id("de.undercouch.download") version "5.5.0"
}

buildscript {
    repositories {
        mavenCentral()
    }
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

application {
    applicationName = "dp-inntekt-api"
    mainClass.set("no.nav.dagpenger.inntekt.ApplicationKt")
}

val grpcVersion = "1.58.0"
val expediaGraphqlVersion = "6.4.0"

dependencies {
    implementation(project(":dp-inntekt-grpc"))

    implementation("com.github.navikt:dagpenger-events:2023081713361692272216.01ab7c590338")
    implementation("com.github.navikt:dagpenger-streams:20230831.f3d785")

    implementation(Ktor2.Server.library("netty"))
    implementation(Ktor2.Server.library("auth"))
    implementation(Ktor2.Server.library("auth-jwt"))
    implementation(Ktor2.Server.library("status-pages"))
    implementation(Ktor2.Server.library("call-id"))
    implementation(Ktor2.Server.library("call-logging"))
    implementation(Ktor2.Server.library("default-headers"))
    implementation(Ktor2.Server.library("content-negotiation"))
    implementation("io.ktor:ktor-serialization-jackson:${Ktor2.version}")
    implementation(Ktor2.Server.library("metrics-micrometer"))

    implementation(Micrometer.prometheusRegistry)

    implementation("com.expediagroup:graphql-kotlin-client:$expediaGraphqlVersion")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$expediaGraphqlVersion")
    implementation("com.expediagroup:graphql-kotlin-client-jackson:$expediaGraphqlVersion")

    implementation(Ktor2.Client.library("logging-jvm"))
    implementation(Ktor2.Client.library("apache"))

    implementation(Jackson.core)
    implementation(Jackson.kotlin)
    implementation(Jackson.jsr310)

    // unleash
    implementation("io.getunleash:unleash-client-java:8.0.0")

    implementation(Moshi.moshi)
    implementation(Moshi.moshiAdapters)
    implementation(Moshi.moshiKotlin)

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
    implementation("org.slf4j:slf4j-api:2.0.7")
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
    testImplementation(KoTest.library("assertions-json"))
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

tasks.named("processResources") {
    dependsOn("copySchemaToResources")
}

tasks.named("graphqlGenerateClient") {
    dependsOn("copySchemaToResources")
    dependsOn("downloadPdlSDL")
}

val Project.buildDir_: File get() = layout.buildDirectory.get().asFile
val schema = "schema.graphql"

val graphqlGenerateClient by tasks.getting(com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask::class) {
    packageName.set("no.nav.pdl")
    schemaFile.set(File("$buildDir_/$schema"))
    queryFileDirectory.set(File("$projectDir/src/main/resources"))
}

val downloadPdlSDL by tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadPdlSDL") {
    src("https://navikt.github.io/pdl/pdl-api-sdl.graphqls")
    dest(File(buildDir_, schema))
}

tasks.register<Copy>("copySchemaToResources") {
    dependsOn(downloadPdlSDL)

    from(buildDir_) {
        include(schema)
    }
    into("$projectDir/src/main/resources")
}

// To get intellij to make sense of generated sources from graphql client
java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    val graphqlDir = "$buildDir_/generated/source/graphql/main"
    mainJavaSourceSet.srcDirs(graphqlDir)
}

tasks.withType<ShadowJar> {
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}

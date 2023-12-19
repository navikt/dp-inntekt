import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer


plugins {
    id("common")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.expediagroup.graphql") version "6.4.0"
    id("de.undercouch.download") version "5.5.0"
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

val expediaGraphqlVersion = "7.0.2"
val moshiVersion = "1.14.0"
val log4j2Version = "2.20.0"

dependencies {

    implementation("com.github.navikt:dagpenger-events:20231204.ee1cc3")
    implementation("com.github.navikt:dagpenger-streams:20230831.f3d785")

    implementation(libs.bundles.ktor.server)
    implementation("io.ktor:ktor-server-netty:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-default-headers:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-call-logging:${libs.versions.ktor.get()}")
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.metrics.micrometer)

    implementation("io.micrometer:micrometer-registry-prometheus:1.10.1")

    implementation("com.expediagroup:graphql-kotlin-client:$expediaGraphqlVersion")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$expediaGraphqlVersion")
    implementation("com.expediagroup:graphql-kotlin-client-jackson:$expediaGraphqlVersion")

    implementation(libs.bundles.ktor.client)
    implementation("io.ktor:ktor-client-apache:${libs.versions.ktor.get()}")

    implementation(libs.bundles.jackson)

    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")

    implementation("org.apache.kafka:kafka-clients:3.6.1")

    implementation(libs.kotlin.logging)

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-layout-template-json:$log4j2Version")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")

    implementation("no.nav.dagpenger:sts-klient:2023.12.13-22.05.4909146325c9")

    implementation(libs.bundles.postgres)
    implementation(libs.konfig)
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("no.nav:vault-jdbc:1.3.10") {
        exclude(module = "slf4j-simple")
        exclude(module = "slf4j-api")
    }

    implementation("io.prometheus:simpleclient_common:0.16.0")
    implementation("io.prometheus:simpleclient_hotspot:0.16.0")
    implementation("io.prometheus:simpleclient_log4j2:0.16.0")
    implementation("no.bekk.bekkopen:nocommons:0.9.0")

    implementation("com.uchuhimo:kotlinx-bimap:1.2")

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)

    testImplementation("no.nav.security:mock-oauth2-server:2.1.0")
    testImplementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")

    testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")

    testImplementation(libs.bundles.kotest.assertions)
    testImplementation("io.kotest:kotest-property:${libs.versions.kotest.get()}")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${libs.versions.kotest.get()}")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testImplementation(libs.testcontainer.postgresql)
    testImplementation("org.testcontainers:kafka:${libs.versions.testcontainer.get()}")
    testImplementation(libs.mockk)
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
}

tasks.named("shadowJar") {
    dependsOn("test")
}

tasks.named("compileKotlin") {
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
    mergeServiceFiles()
}

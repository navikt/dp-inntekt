import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer


plugins {
    id("common")
    application
    alias(libs.plugins.shadow.jar)
    id("com.expediagroup.graphql") version "6.4.0"
    id("de.undercouch.download") version "5.6.0"
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

val expediaGraphqlVersion = "7.1.0"
val moshiVersion = "1.14.0"
val log4j2Version = "2.24.1"

dependencies {

    implementation(project(":dp-inntekt-kontrakter"))

    implementation("com.github.navikt:dagpenger-streams:20230831.f3d785")

    implementation(libs.bundles.ktor.server)
    implementation("io.ktor:ktor-server-netty:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-default-headers:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-call-logging:${libs.versions.ktor.get()}")
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.metrics.micrometer)

    implementation("io.micrometer:micrometer-registry-prometheus:1.12.5")

    implementation("com.expediagroup:graphql-kotlin-client:$expediaGraphqlVersion")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$expediaGraphqlVersion")
    implementation("com.expediagroup:graphql-kotlin-client-jackson:$expediaGraphqlVersion")

    implementation(libs.bundles.ktor.client)

    implementation(libs.bundles.jackson)
    implementation("org.apache.kafka:kafka-clients:7.5.3-ce")

    implementation(libs.kotlin.logging)

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-layout-template-json:$log4j2Version")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")

    implementation(libs.bundles.postgres)
    implementation(libs.konfig)
    implementation("org.slf4j:slf4j-api:2.0.12")

    implementation("io.prometheus:simpleclient_common:0.16.0")
    implementation("io.prometheus:simpleclient_hotspot:0.16.0")
    implementation("io.prometheus:simpleclient_log4j2:0.16.0")
    implementation("no.bekk.bekkopen:nocommons:0.16.0")

    implementation("com.uchuhimo:kotlinx-bimap:1.2")
    implementation(libs.dp.biblioteker.oauth2.klient)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)

    testImplementation("no.nav.security:mock-oauth2-server:2.1.4")
    testImplementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")

    testImplementation("org.wiremock:wiremock-standalone:3.9.2")

    testImplementation(libs.bundles.kotest.assertions)
    testImplementation("io.kotest:kotest-property:${libs.versions.kotest.get()}")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${libs.versions.kotest.get()}")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testImplementation(libs.testcontainer.postgresql)
    testImplementation("org.testcontainers:kafka:${libs.versions.testcontainer.get()}")
    testImplementation(libs.mockk)
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
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

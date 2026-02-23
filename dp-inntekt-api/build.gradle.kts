plugins {
    id("common")
    application
    id("com.expediagroup.graphql") version "8.8.1"
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

val expediaGraphqlVersion = "8.8.1"
val moshiVersion = "1.14.0"

dependencies {

    implementation(project(":dp-inntekt-kontrakter"))

    // ktor server
    implementation("com.github.navikt.tbd-libs:naisful-app:2025.11.04-10.54-c831038e")
    implementation("io.ktor:ktor-server-default-headers:${libs.versions.ktor.get()}")
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)

    implementation("io.micrometer:micrometer-registry-prometheus:1.16.2")

    implementation("io.prometheus:client_java:1.5.0")
    implementation("io.prometheus:prometheus-metrics-core:1.5.0")
    implementation("io.prometheus:prometheus-metrics-instrumentation-jvm:1.5.0")

    implementation("com.expediagroup:graphql-kotlin-client:$expediaGraphqlVersion")
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$expediaGraphqlVersion")
    implementation("com.expediagroup:graphql-kotlin-client-jackson:$expediaGraphqlVersion")

    implementation(libs.bundles.ktor.client)

    implementation(libs.bundles.jackson)

    // kafka
    implementation("com.github.navikt.tbd-libs:kafka:2025.11.04-10.54-c831038e")
    // kafka testing
    testImplementation("com.github.navikt.tbd-libs:kafka-test:2025.11.04-10.54-c831038e")

    implementation(libs.kotlin.logging)

    implementation("ch.qos.logback:logback-classic:1.5.26")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")

    implementation(libs.bundles.postgres)
    implementation(libs.konfig)
    implementation("org.slf4j:slf4j-api:2.0.17")

    implementation("no.bekk.bekkopen:nocommons:0.16.0")

    implementation("com.uchuhimo:kotlinx-bimap:1.2")
    implementation("no.nav.dagpenger:oauth2-klient:2025.11.27-14.20.4aa9aa3d3d14")

    testImplementation(kotlin("test"))
    testImplementation("com.github.navikt.tbd-libs:naisful-test-app:2025.11.04-10.54-c831038e")

    testImplementation("no.nav.security:mock-oauth2-server:3.0.1")
    testImplementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")

    testImplementation("org.wiremock:wiremock-standalone:3.13.2")

    testImplementation(libs.bundles.kotest.assertions)
    testImplementation("io.kotest:kotest-property:${libs.versions.kotest.get()}")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${libs.versions.kotest.get()}")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testImplementation(libs.testcontainer.postgresql)
    testImplementation("org.testcontainers:kafka:${libs.versions.testcontainer.get()}")
    testImplementation(libs.mockk)
    testImplementation("org.skyscreamer:jsonassert:1.5.3")
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

ktlint {
    filter {
        exclude { element -> element.file.path.contains("generated/") }
    }
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

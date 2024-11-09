plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user guide at https://docs.gradle.org/5.0/userguide/multi_project_builds.html
 */

rootProject.name = "dp-inntekt"
include("dp-inntekt-api", "dp-inntekt-kontrakter")

dependencyResolutionManagement {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    versionCatalogs {
        create("libs") {
            from("no.nav.dagpenger:dp-version-catalog:20241109.99.17eb9e")
        }
    }
}

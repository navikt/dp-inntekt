buildscript { repositories { mavenCentral() } }

//
//allprojects {
//    group = "no.nav.dagpenger"
//
//    apply(plugin = "org.jetbrains.kotlin.jvm")
//    apply(plugin = Spotless.spotless)
//
//    dependencies {
//        implementation(kotlin("stdlib-jdk8"))
//
//        // ulid
//        implementation(Ulid.ulid)
//
//        testImplementation(kotlin("test"))
//        testImplementation(Junit5.api)
//        testRuntimeOnly(Junit5.engine)
//        testImplementation(KoTest.assertions)
//        testImplementation(KoTest.runner)
//    }
//
//    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
//    }
//
//    java {
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//    }
//
//    tasks.withType<Wrapper> {
//        gradleVersion = "7.5.1"
//    }
//
//    spotless {
//        kotlin {
//            ktlint(Ktlint.version)
//        }
//        kotlinGradle {
//            target("*.gradle.kts", "buildSrc/**/*.kt*")
//            ktlint(Ktlint.version)
//        }
//    }
//
//    tasks.withType<Test> {
//        useJUnitPlatform()
//        testLogging {
//            showExceptions = true
//            showStackTraces = true
//            exceptionFormat = TestExceptionFormat.FULL
//            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
//            showStandardStreams = true
//        }
//    }
//
//    tasks.named("compileKotlin") {
//        dependsOn("spotlessApply", "spotlessKotlinCheck")
//    }
//
//    repositories {
//        mavenCentral()
//        maven("https://jitpack.io")
//    }
//}
//
//subprojects {
//    apply(plugin = "org.jetbrains.kotlin.jvm")
//    apply(plugin = "maven-publish")
//
//    dependencies {
//        implementation(kotlin("stdlib-jdk8"))
//
//        testImplementation(kotlin("test"))
//        testImplementation(Junit5.api)
//        testRuntimeOnly(Junit5.engine)
//        testImplementation(Mockk.mockk)
//    }
//}

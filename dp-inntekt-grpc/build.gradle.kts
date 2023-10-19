import com.google.protobuf.gradle.* // ktlint-disable no-wildcard-imports

val grpcVersion = "1.51.1"
val grpcKotlinVersion = "1.4.0"
val protbufVersion = "3.24.4"
val protobufGradleVersion = "0.9.4"

plugins {
    kotlin("jvm")
    id("com.google.protobuf") version "0.9.2"
}

apply(plugin = "com.google.protobuf")

group = "com.github.navikt"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Grpc and Protobuf
    protobuf(files("src/proto/"))
    implementation("com.google.protobuf:protobuf-gradle-plugin:$protobufGradleVersion")
    api("io.grpc:grpc-api:$grpcVersion")
    api("io.grpc:grpc-protobuf:$grpcVersion")
    api("io.grpc:grpc-stub:$grpcVersion")
    api("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-okhttp:$grpcVersion")

    if (JavaVersion.current().isJava9Compatible) {
        // Workaround for @javax.annotation.Generated
        // see: https://github.com/grpc/grpc-java/issues/3633
        compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    }

    //
    implementation(Kotlin.Coroutines.module("core"))

    // events
    implementation(Dagpenger.Events)
    implementation(Moshi.moshi)

    // api key verifier
    implementation(Dagpenger.Biblioteker.ktorUtils)

    // test
    testImplementation("io.grpc:grpc-testing:$grpcVersion")
    testImplementation(Junit5.api)
    testRuntimeOnly(Junit5.engine)
    testRuntimeOnly(Junit5.vintageEngine)
    testImplementation(KoTest.assertions)
}

// To get intellij to make sense of generated sources
java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    val protoSrcDir = "$buildDir/generated/source/proto/main"
    mainJavaSourceSet.srcDirs("$protoSrcDir/java", "$protoSrcDir/grpc", "$protoSrcDir/grpckotlin")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protbufVersion"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckotlin") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("grpckotlin")
            }
        }
    }
}

sourceSets {
    create("proto") {
        proto {
            srcDir("src/main/proto")
        }
    }
}

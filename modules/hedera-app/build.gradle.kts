plugins {
    id("com.hedera.hashgraph.hedera-conventions")
    id("de.jjohannes.extra-java-module-info") version "0.15"
}

description = "Hedera Application"

dependencies {
    implementation(project(":modules:hedera-app-api"))
    implementation(project(":modules:hedera-proto-api"))
    implementation(project(":modules:hedera-file-service"))
    implementation(project(":modules:hedera-file-service-api"))
    implementation(project(":modules:hedera-token-service"))
    implementation(project(":modules:hedera-token-service-api"))
    implementation(libs.jsr305.annotation)
    implementation(libs.helidon.grpc.server)
    implementation(libs.io.grpc)
    implementation(libs.proto.parse)
    implementation(libs.swirlds.common)
    implementation(libs.swirlds.merkle)
    testImplementation(libs.bundles.bouncycastle)
}

/** Patch libraries that are not Java 9 modules */
extraJavaModuleInfo {
    automaticModule("org.eclipse.microprofile.health:microprofile-health-api", "microprofile.health.api")

    automaticModule("com.google.j2objc:j2objc-annotations", "j2objc.annotations")
    automaticModule("io.perfmark:perfmark-api", "perfmark.api")

    automaticModule("com.google.guava:failureaccess", "failureaccess")
    automaticModule("com.google.guava:listenablefuture", "listenablefuture")

    automaticModule("io.grpc:grpc-api", "io.grpc") {
        mergeJar("io.grpc:grpc-context")
        mergeJar("io.grpc:grpc-core")
    }
    automaticModule("io.grpc:grpc-netty", "grpc.netty")
    automaticModule("io.grpc:grpc-services", "grpc.services")
    automaticModule("io.grpc:grpc-protobuf", "grpc.protobuf")
    automaticModule("io.grpc:grpc-stub", "grpc.stub")
    automaticModule("io.grpc:grpc-protobuf-lite", "grpc.protobuf.lite")

    automaticModule("com.google.code.findbugs:jsr305", "jsr305")
    automaticModule("com.offbynull.portmapper:portmapper", "portmapper")
    automaticModule("com.goterl:lazysodium-java", "lazysodium.java")
    automaticModule("org.openjfx:javafx-base", "javafx.base")

    failOnMissingModuleInfo.set(false)
}

spotbugs {
    setReportLevel("high")
}

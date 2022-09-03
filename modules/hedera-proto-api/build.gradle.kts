plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera Protobuf API"

dependencies {
    implementation(libs.proto.parse)
    testImplementation("com.hedera.hashgraph:hedera-protobuf-java-api:0.29.1")
}


// ===== PROTOBUF GENERATION ===========================================================================================

var generatedSrcDir = buildDir.resolve("generated/sources/proto")
java.sourceSets["main"].java {srcDir(generatedSrcDir.resolve("main/java"))}
java.sourceSets["test"].java {srcDir(generatedSrcDir.resolve("test/java"))}

tasks.register<com.hedera.hashgraph.protoparser.ProtoGeneratorPlugin>("protoGenerator") {
    protoSrcDir.set(projectDir.resolve("src/main/proto"))
    generatedFileDir.set(generatedSrcDir)
}
tasks.named("build") { dependsOn("protoGenerator") }
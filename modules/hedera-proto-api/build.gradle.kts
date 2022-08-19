plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera Protobuf API"

dependencies {
    implementation(libs.proto.parse)
}


// ===== PROTOBUF GENERATION ===========================================================================================

var generatedSrcDir = buildDir.resolve("generated/sources/proto-java")
java.sourceSets["main"].java {
    srcDir(generatedSrcDir)
}
tasks.register<com.hedera.hashgraph.protoparser.ProtoGeneratorPlugin>("protoGenerator") {
    protoSrcDir.set(projectDir.resolve("src/main/proto"))
    generatedFileDir.set(generatedSrcDir)
}
tasks.named("build") { dependsOn("protoGenerator") }
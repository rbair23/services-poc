plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera Application API"

dependencies {
    implementation(libs.proto.parse)
}

tasks.register<com.hedera.hashgraph.protoparser.ProtoGeneratorPlugin>("protoGenerator") {
    protoSrcDir.set(projectDir.resolve("src/main/proto"))
    generatedFileDir.set(projectDir.resolve("src/main/generated-java"))
}
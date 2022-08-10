plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera File Service"

dependencies {
    implementation(project(":modules:hedera-app-api"))
    implementation(project(":modules:hedera-file-service-api"))
    implementation(libs.swirlds.common)
    implementation(libs.swirlds.merkle)
    implementation(libs.proto.parse)
}

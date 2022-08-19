plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera Token Service"

dependencies {
    implementation(project(":modules:hedera-app-api"))
    implementation(project(":modules:hedera-proto-api"))
    implementation(project(":modules:hedera-token-service-api"))
    implementation(libs.swirlds.common)
    implementation(libs.swirlds.merkle)
    implementation(libs.proto.parse)
}

plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera Token Service API"

dependencies {
    implementation(project(":modules:hedera-app-api"))
    implementation(project(":modules:hedera-proto-api"))
}

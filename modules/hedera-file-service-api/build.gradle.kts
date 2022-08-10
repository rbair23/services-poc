plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera File Service API"

dependencies {
    implementation(project(":modules:hedera-app-api"))
}

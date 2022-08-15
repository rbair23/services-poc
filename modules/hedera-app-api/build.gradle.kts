plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera Application API"

dependencies {
    implementation(libs.proto.parse)
    implementation(project(":modules:hedera-proto-api"))
}
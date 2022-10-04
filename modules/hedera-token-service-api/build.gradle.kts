plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera Token Service API"

dependencies {
    implementation(project(":modules:hedera-app-api"))
    implementation(project(":modules:hedera-proto-api"))
    implementation("org.jetbrains:annotations:20.1.0")
    compileOnly(libs.spotbugs)
}

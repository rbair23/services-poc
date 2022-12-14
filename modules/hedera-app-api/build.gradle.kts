plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera Application API"

dependencies {
    implementation(project(":modules:hedera-proto-api"))
    implementation(libs.proto.parse)
    implementation(libs.swirlds.common)
    implementation(libs.swirlds.merkle)
    compileOnly(libs.spotbugs)
}

spotbugs {
    setReportLevel("high")
}

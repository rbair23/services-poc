plugins {
    id("com.hedera.hashgraph.hedera-conventions")
}

description = "Hedera Application"

dependencies {
    implementation(project(":modules:hedera-app-api"))
    implementation(project(":modules:hedera-file-service"))
    implementation(project(":modules:hedera-file-service-api"))
//    implementation(libs.grpc.api) // TODO Do I really need this?
//    implementation(libs.grpc.netty)
//    implementation(libs.netty.handler)
    implementation(libs.helidon.grpc.server)
}

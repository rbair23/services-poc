/*-
 * ‌
 * Hedera Build Sources
 * ​
 * Copyright (C) 2018 - 2022 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

plugins {
    antlr
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main'
    // that automatically become available as plugins in the main build.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/comhederahashgraph-1500")
    }
}

dependencies {
    antlr("org.antlr:antlr4:4.10.1")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")
    implementation("me.champeau.jmh:jmh-gradle-plugin:0.6.6")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.8.0")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.0.12")
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-package", "com.hedera.hashgraph.protoparser.grammar")
}
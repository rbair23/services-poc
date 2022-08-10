import org.sonarqube.gradle.SonarQubeTask

plugins {
    `java-platform`
    id("org.sonarqube")
}

// Configure the Sonarqube extension for SonarCloud reporting. These properties should not be changed so no need to
// have them in the gradle.properties defintions.
sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "hashgraph")
        property("sonar.projectKey", "com.hedera.hashgraph:hedera-services")
        property("sonar.projectName", "Hedera Services")
        property("sonar.projectVersion", project.version)
        property("sonar.projectDescription", "Hedera Services (crypto, file, contract, consensus) on the Platform")
        property("sonar.links.homepage", "https://github.com/hashgraph/hedera-services")
        property("sonar.links.ci", "https://github.com/hashgraph/hedera-services/actions")
        property("sonar.links.issue", "https://github.com/hashgraph/hedera-services/issues")
        property("sonar.links.scm", "https://github.com/hashgraph/hedera-services.git")

        property("sonar.coverage.exclusions", "**/test-clients/**,**/hedera-node/src/jmh/**")

        // Ignored to match pom.xml setup
        property("sonar.issue.ignore.multicriteria", "e1,e2")
        property("sonar.issue.ignore.multicriteria.e1.resourceKey", "**/*.java")
        property("sonar.issue.ignore.multicriteria.e1.ruleKey", "java:S125")
        property("sonar.issue.ignore.multicriteria.e2.resourceKey", "**/*.java")
        property("sonar.issue.ignore.multicriteria.e2.ruleKey", "java:S1874")
    }
}


plugins {
    id("java-test-fixtures")
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("tools.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.core:jackson-annotations:2.20")
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("io.projectreactor.kotlin:reactor-kotlin-extensions")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    api("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    api("org.springframework.boot:spring-boot-starter-data-redis")

    implementation("org.springframework.security:spring-security-crypto")
    implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage:3.80.1")
    implementation("com.oracle.oci.sdk:oci-java-sdk-emaildataplane:3.80.1")
    implementation("com.oracle.oci.sdk:oci-java-sdk-common-httpclient-jersey3:3.80.1")
    implementation("com.wafflestudio.spring:spring-boot-starter-waffle-oci-vault:2.1.0")
    implementation("com.wafflestudio.spring.truffle:spring-boot-starter-truffle:2.1.0")
    implementation("com.google.firebase:firebase-admin:9.7.0")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")

    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("io.projectreactor:reactor-test")
    testFixturesApi("io.mockk:mockk:1.14.7")
    testFixturesApi("io.mockk:mockk-agent-jvm:1.14.7")
    testFixturesApi("com.ninja-squad:springmockk:5.0.1")

    testFixturesApi("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testFixturesApi("io.kotest:kotest-runner-junit5:5.9.1")
    testFixturesApi("io.kotest:kotest-assertions-core:5.9.1")

    testFixturesImplementation("org.testcontainers:mongodb:1.21.3")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    testFixturesImplementation("org.springframework.boot:spring-boot-testcontainers")
}

plugins {
    id("java-test-fixtures")
}

dependencies {
    implementation("org.springframework.security:spring-security-crypto")
    implementation("software.amazon.awssdk:s3:2.32.14")
    implementation("software.amazon.awssdk:ses:2.32.14")
    implementation("com.wafflestudio.spring:spring-boot-starter-waffle:1.0.4")
    implementation("com.google.firebase:firebase-admin:9.5.0")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    testFixturesImplementation("org.testcontainers:mongodb:1.21.3")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    testFixturesImplementation("org.springframework.boot:spring-boot-testcontainers")
}

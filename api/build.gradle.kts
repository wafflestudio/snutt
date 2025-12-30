plugins {
    id("org.springframework.boot")
    id("org.unbroken-dome.test-sets") version "4.1.0"
}

dependencies {
    implementation(project(":core"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
    runtimeOnly("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")

    testImplementation(testFixtures(project(":core")))
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mongodb:1.21.3")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
}

tasks.bootJar {
    archiveFileName.set("snutt-api.jar")
}

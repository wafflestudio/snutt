plugins {
    id("org.springframework.boot")
    id("org.unbroken-dome.test-sets") version "4.1.0"
}

dependencies {
    implementation(project(":core"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:3.0.0")
    runtimeOnly("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")

    testImplementation(testFixtures(project(":core")))
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mongodb:1.21.3")
    testImplementation("org.springframework.boot:spring-boot-webtestclient")
}

tasks.bootJar {
    archiveFileName.set("snutt-api.jar")
}

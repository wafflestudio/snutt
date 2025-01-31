plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":core"))

    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("com.h2database:h2")

    implementation("io.github.resilience4j:resilience4j-reactor:2.1.0")
    implementation("io.github.resilience4j:resilience4j-kotlin:2.1.0")
    implementation("io.github.resilience4j:resilience4j-ratelimiter:2.1.0")

    implementation("org.jsoup:jsoup:1.16.1")

    // excel
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    testImplementation("org.springframework.batch:spring-batch-test")
}

tasks.bootJar {
    archiveFileName.set("snutt-batch.jar")
}

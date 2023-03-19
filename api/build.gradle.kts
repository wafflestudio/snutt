plugins {
    id("org.springframework.boot")
    id("org.unbroken-dome.test-sets") version "4.0.0"
}

testSets {
    register("migrationTest")
}

dependencies {
    implementation(project(":core"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("io.jsonwebtoken:jjwt:0.9.1")
    runtimeOnly("javax.xml.bind:jaxb-api:2.1")

    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
}

tasks.bootJar {
    archiveFileName.set("snu4t-api.jar")
}

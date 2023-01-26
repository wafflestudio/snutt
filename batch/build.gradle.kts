plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":core"))

    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("com.h2database:h2")
    runtimeOnly("mysql:mysql-connector-java")

    // excel
    implementation("org.apache.poi:poi-ooxml:5.2.3")


    testImplementation("org.springframework.batch:spring-batch-test")
}

tasks.bootJar {
    archiveFileName.set("snu4t-batch.jar")
}

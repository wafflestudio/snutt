plugins {
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("org.springframework.boot")
}

val snippetsDir by extra { file("build/generated-snippets") }
val asciidoctorExtensions: Configuration by configurations.creating
dependencies {
    implementation(project(":core"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")

    asciidoctorExtensions("org.springframework.restdocs:spring-restdocs-asciidoctor")
}

tasks.asciidoctor {
    inputs.dir(snippetsDir)
    setBaseDir(file("src/docs/asciidoc"))
    setOutputDir(file("src/main/resources/static/docs"))
    dependsOn(tasks.test)
    configurations(asciidoctorExtensions.name)
    doFirst {
        delete {
            file("src/main/resources/static/docs")
        }
    }
}

tasks.bootJar {
    archiveFileName.set("api.jar")
    dependsOn(tasks.asciidoctor)
    from("src/main/resources/static/docs") {
        into("static/docs")
    }
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    id("org.springframework.boot") version "3.0.2" apply false
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
}

group = "com.wafflestudio"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

allprojects {
    repositories {
        mavenCentral()
        mavenCodeArtifact()
        mavenLocal()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("io.spring.dependency-management")
        plugin("kotlin-spring")
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.0.2")
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
        implementation("org.springframework.boot:spring-boot-starter-log4j2")
        implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

        implementation("com.wafflestudio.truffle.sdk:truffle-spring-boot-starter:1.0.1")

        // test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.projectreactor:reactor-test")
        testImplementation("io.mockk:mockk:1.12.4")
        testImplementation("io.mockk:mockk-agent-jvm:1.12.4")
        testImplementation("com.ninja-squad:springmockk:4.0.0")
        testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
        testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
        testImplementation("io.kotest:kotest-assertions-core:5.5.4")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    configurations {
        all {
            exclude(module = "spring-boot-starter-logging")
            exclude(module = "logback-core")
            exclude(module = "logback-classic")
        }
    }
}

fun RepositoryHandler.mavenCodeArtifact() {
    maven {
        val authToken = properties["codeArtifactAuthToken"] as String? ?: ByteArrayOutputStream().use {
            runCatching {
                exec {
                    commandLine = (
                        "aws codeartifact get-authorization-token " +
                            "--domain wafflestudio --domain-owner 405906814034 " +
                            "--query authorizationToken --region ap-northeast-1 --output text"
                        ).split(" ")
                    standardOutput = it
                }
            }
            it.toString()
        }
        url = uri("https://wafflestudio-405906814034.d.codeartifact.ap-northeast-1.amazonaws.com/maven/truffle-kotlin/")
        credentials {
            username = "aws"
            password = authToken
        }
    }
}

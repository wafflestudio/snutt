import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    id("org.springframework.boot") version "3.5.4" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
}

group = "com.wafflestudio"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

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

    dependencies {
        api(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
        implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

        implementation("org.springframework.boot:spring-boot-starter-data-redis")

        implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.14")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.projectreactor:reactor-test")
        testImplementation("io.mockk:mockk:1.14.5")
        testImplementation("io.mockk:mockk-agent-jvm:1.14.5")
        testImplementation("com.ninja-squad:springmockk:4.0.2")
        testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
        testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
        testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

interface InjectedExecOps {
    @get:Inject val execOps: ExecOperations
}

fun RepositoryHandler.mavenCodeArtifact() {
    val injected = project.objects.newInstance<InjectedExecOps>()
    maven {
        val authToken =
            properties["codeArtifactAuthToken"] as String? ?: ByteArrayOutputStream().use {
                runCatching {
                    injected.execOps.exec {
                        commandLine =
                            listOf(
                                "aws",
                                "codeartifact",
                                "get-authorization-token",
                                "--domain",
                                "wafflestudio",
                                "--domain-owner",
                                "405906814034",
                                "--query",
                                "authorizationToken",
                                "--region",
                                "ap-northeast-1",
                                "--output",
                                "text",
                            )
                        standardOutput = it
                    }
                }
                it.toString()
            }
        url = uri("https://wafflestudio-405906814034.d.codeartifact.ap-northeast-1.amazonaws.com/maven/spring-waffle/")
        credentials {
            username = "aws"
            password = authToken
        }
    }
}

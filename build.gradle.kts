import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    id("org.graalvm.buildtools.native") version "0.11.3" apply false
    id("org.springframework.boot") version "4.0.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.2.0"
}

group = "com.wafflestudio"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_25
java.targetCompatibility = JavaVersion.VERSION_25

allprojects {
    repositories {
        mavenCentral()
        mavenCodeArtifact()
        mavenLocal()
    }
}

subprojects {
    apply {
        plugin("org.graalvm.buildtools.native")
        plugin("kotlin")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("io.spring.dependency-management")
        plugin("kotlin-spring")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(JvmTarget.JVM_25)
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

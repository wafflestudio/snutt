import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.diffplug.spotless") version "8.4.0"
    id("org.graalvm.buildtools.native") version "0.11.3" apply false
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.2.0"
}

group = "com.wafflestudio"
version = "0.0.1-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/wafflestudio/spring-waffle")
            credentials {
                username = "wafflestudio"
                password = findProperty("gpr.key") as String?
                    ?: System.getenv("GITHUB_TOKEN")
                    ?: runCatching {
                        ProcessBuilder("gh", "auth", "token")
                            .start()
                            .inputStream
                            .bufferedReader()
                            .readText()
                            .trim()
                    }.getOrDefault("")
            }
        }
        mavenLocal()
    }
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.8.0")
    }
}

subprojects {
    apply {
        plugin("com.diffplug.spotless")
        plugin("org.graalvm.buildtools.native")
        plugin("kotlin")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("io.spring.dependency-management")
        plugin("kotlin-spring")
    }

    configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            ktlint("1.8.0")
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint("1.8.0")
        }
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.1")
        }
        dependencies {
            // Downgrade MongoDB driver to avoid ByteBuf leak (JAVA-6038)
            dependency("org.mongodb:mongodb-driver-core:5.5.2")
            dependency("org.mongodb:mongodb-driver-sync:5.5.2")
            dependency("org.mongodb:mongodb-driver-reactivestreams:5.5.2")
            dependency("org.mongodb:bson:5.5.2")
            dependency("org.mongodb:bson-record-codec:5.5.2")
            dependency("org.mongodb:bson-kotlin:5.5.2")
        }
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
  kotlin("jvm") version "2.0.0"
  id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
  application
}

repositories {
  mavenCentral()
}

val ktorClientVersion = "2.3.12"

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("io.ktor:ktor-client-core:$ktorClientVersion")
  implementation("io.ktor:ktor-client-cio:$ktorClientVersion")

  testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
  testImplementation("io.mockk:mockk:1.13.12")
  testImplementation("org.assertj:assertj-core:3.26.3")
  testImplementation("org.wiremock:wiremock:3.9.1")
  testImplementation("org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:1.0-alpha-13")
  testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.0"))
  testImplementation("org.testcontainers:testcontainers")
  testImplementation("org.testcontainers:junit-jupiter")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  testLogging {
    events(PASSED, SKIPPED, FAILED)
    exceptionFormat = FULL
    showExceptions = true
    showCauses = true
    showStackTraces = true
  }
}

application {
  mainClass.set("com.rogervinas.wiremock.AppKt")
}

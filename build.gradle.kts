import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
  kotlin("jvm") version "2.1.10"
  id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
  application
}

repositories {
  mavenCentral()
}

val ktorClientVersion = "3.1.0"

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("io.ktor:ktor-client-core:$ktorClientVersion")
  implementation("io.ktor:ktor-client-cio:$ktorClientVersion")

  testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
  testImplementation("io.mockk:mockk:1.13.16")
  testImplementation("org.assertj:assertj-core:3.27.3")
  testImplementation("org.wiremock:wiremock:3.12.0")
  testImplementation("org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:1.0-alpha-14")
  testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
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

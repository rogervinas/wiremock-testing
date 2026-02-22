import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
  kotlin("jvm") version "2.3.10"
  id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
  application
}

repositories {
  mavenCentral()
}

val ktorClientVersion = "3.4.0"

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("io.ktor:ktor-client-core:$ktorClientVersion")
  implementation("io.ktor:ktor-client-cio:$ktorClientVersion")

  testImplementation(platform("org.junit:junit-bom:6.0.3"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  testImplementation("io.mockk:mockk:1.14.9")
  testImplementation("org.assertj:assertj-core:3.27.7")
  testImplementation("org.wiremock:wiremock:3.13.2")
  testImplementation("org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:1.0-alpha-15")

  testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.3"))
  testImplementation("org.testcontainers:testcontainers")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter")
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

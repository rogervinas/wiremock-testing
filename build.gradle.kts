import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  kotlin("jvm") version "1.9.20"
  application
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("io.ktor:ktor-client-core:2.3.6")
  implementation("io.ktor:ktor-client-cio:2.3.6")

  testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
  testImplementation("io.mockk:mockk:1.12.2")
  testImplementation("org.assertj:assertj-core:3.24.2")
  testImplementation("org.wiremock:wiremock:3.3.1")
  testImplementation("org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:1.0-alpha-13")
  testImplementation("org.testcontainers:testcontainers:1.19.1")
  testImplementation("org.testcontainers:junit-jupiter:1.19.1")
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
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

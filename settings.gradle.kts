rootProject.name = "wiremock-testing"

plugins {
  id("com.gradle.enterprise") version ("3.15.1")
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

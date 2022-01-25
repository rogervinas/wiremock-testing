package com.rogervinas.wiremock

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File

@Testcontainers
@TestInstance(PER_CLASS)
class AppShouldWithWireMockDocker {

  private val name = "Ivy"

  private val fooServiceName = "foo-api"
  private val fooServicePort = 8080
  private val barServiceName = "bar-api"
  private val barServicePort = 8080

  @Container
  val container = DockerComposeContainer<Nothing>(File("docker-compose.yml"))
    .apply {
      withLocalCompose(true)
      withExposedService(fooServiceName, fooServicePort, Wait.forListeningPort())
      withExposedService(barServiceName, barServicePort, Wait.forListeningPort())
    }

  @Test
  fun `call foo and bar`() {
    val fooApiHost = container.getServiceHost(fooServiceName, fooServicePort)
    val fooApiPort = container.getServicePort(fooServiceName, fooServicePort)
    val barApiHost = container.getServiceHost(barServiceName, barServicePort)
    val barApiPort = container.getServicePort(barServiceName, barServicePort)

    val fooApiUrl = "http://${fooApiHost}:${fooApiPort}"
    val barApiUrl = "http://${barApiHost}:${barApiPort}"

    val app = App(name, fooApiUrl, barApiUrl)

    assertThat(app.execute()).isEqualTo(
      """
        Hi! I am $name
        I called Foo and its response is Hello $name I am Foo!
        I called Bar and its response is Hello $name I am Bar!
        Bye!
      """.trimIndent()
    )
  }
}

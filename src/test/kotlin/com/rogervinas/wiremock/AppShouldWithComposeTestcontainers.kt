package com.rogervinas.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.wait.strategy.Wait.forListeningPort
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File

@Testcontainers
@TestInstance(PER_CLASS)
class AppShouldWithComposeTestcontainers {
  companion object {
    private const val NAME = "Ivy"

    private const val FOO_SERVICE_NAME = "foo-api"
    private const val FOO_SERVICE_PORT = 8080
    private const val BAR_SERVICE_NAME = "bar-api"
    private const val BAR_SERVICE_PORT = 8080

    private lateinit var fooApiHost: String
    private var fooApiPort: Int = 0
    private lateinit var barApiHost: String
    private var barApiPort: Int = 0

    @Container
    @JvmStatic
    val container =
      ComposeContainer(File("docker-compose.yml"))
        .withExposedService(FOO_SERVICE_NAME, FOO_SERVICE_PORT, forListeningPort())
        .withExposedService(BAR_SERVICE_NAME, BAR_SERVICE_PORT, forListeningPort())

    @BeforeAll
    @JvmStatic
    fun beforeAll() {
      fooApiHost = container.getServiceHost(FOO_SERVICE_NAME, FOO_SERVICE_PORT)
      fooApiPort = container.getServicePort(FOO_SERVICE_NAME, FOO_SERVICE_PORT)
      barApiHost = container.getServiceHost(BAR_SERVICE_NAME, BAR_SERVICE_PORT)
      barApiPort = container.getServicePort(BAR_SERVICE_NAME, BAR_SERVICE_PORT)
    }
  }

  @Test
  fun `call foo and bar`() {
    val fooApiUrl = "http://$fooApiHost:$fooApiPort"
    val barApiUrl = "http://$barApiHost:$barApiPort"

    val app = App(NAME, fooApiUrl, barApiUrl)

    assertThat(app.execute()).isEqualTo(
      """
      Hi! I am $NAME
      I called Foo and its response is Hello $NAME I am Foo!
      I called Bar and its response is Hello $NAME I am Bar!
      Bye!
      """.trimIndent(),
    )
  }

  @Test
  fun `call foo an bar with dynamic stubs`() {
    val fooApiUrl = "http://$fooApiHost:$fooApiPort/dynamic"
    val barApiUrl = "http://$barApiHost:$barApiPort/dynamic"

    WireMock(fooApiHost, fooApiPort)
      .register(
        get(urlPathEqualTo("/dynamic/foo"))
          .withQueryParam("name", equalTo(NAME))
          .willReturn(ok().withBody("Hi $NAME I am Foo, how are you?")),
      )
    WireMock(barApiHost, barApiPort)
      .register(
        get(urlPathMatching("/dynamic/bar/$NAME"))
          .willReturn(ok().withBody("Hi $NAME I am Bar, nice to meet you!")),
      )

    val app = App(NAME, fooApiUrl, barApiUrl)

    assertThat(app.execute()).isEqualTo(
      """
      Hi! I am $NAME
      I called Foo and its response is Hi $NAME I am Foo, how are you?
      I called Bar and its response is Hi $NAME I am Bar, nice to meet you!
      Bye!
      """.trimIndent(),
    )
  }
}

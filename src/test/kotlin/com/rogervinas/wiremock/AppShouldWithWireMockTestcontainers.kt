package com.rogervinas.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.wiremock.integrations.testcontainers.WireMockContainer
import java.io.File

@Testcontainers
@TestInstance(PER_CLASS)
class AppShouldWithWireMockTestcontainers {

  companion object {
    private const val name = "Ivy"

    @Container
    @JvmStatic
    val containerFoo = WireMockContainer("wiremock/wiremock:3.2.0")
      .withMappingFromJSON(File("wiremock/foo-api/mappings/foo-get.json").readText())
      .withCliArg("--global-response-templating")

    @Container
    @JvmStatic
    val containerBar = WireMockContainer("wiremock/wiremock:3.2.0")
      .withMappingFromJSON(File("wiremock/bar-api/mappings/bar-get.json").readText())
      .withCliArg("--global-response-templating")
  }

  @Test
  fun `call foo and bar`() {
    val fooApiUrl = "http://${containerFoo.host}:${containerFoo.port}"
    val barApiUrl = "http://${containerBar.host}:${containerBar.port}"

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

  @Test
  fun `call foo an bar with dynamic stubs`() {
    val fooApiUrl = "http://${containerFoo.host}:${containerFoo.port}/dynamic"
    val barApiUrl = "http://${containerBar.host}:${containerBar.port}/dynamic"

    WireMock(containerFoo.host, containerFoo.port)
      .register(
        get(urlPathEqualTo("/dynamic/foo"))
          .withQueryParam("name", WireMock.equalTo(name))
          .willReturn(ok().withBody("Hi $name I am Foo, how are you?"))
      )
    WireMock(containerBar.host, containerBar.port)
      .register(
        get(urlPathMatching("/dynamic/bar/$name"))
          .willReturn(ok().withBody("Hi $name I am Bar, nice to meet you!"))
      )

    val app = App(name, fooApiUrl, barApiUrl)

    assertThat(app.execute()).isEqualTo(
      """
        Hi! I am $name
        I called Foo and its response is Hi $name I am Foo, how are you?
        I called Bar and its response is Hi $name I am Bar, nice to meet you!
        Bye!
      """.trimIndent()
    )
  }
}

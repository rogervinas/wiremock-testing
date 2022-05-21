package com.rogervinas.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.junit5.WireMockExtension.newInstance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.RegisterExtension

@TestInstance(PER_CLASS)
class AppShouldWithTwoWireMockExtensions {

  private val name = "Leo"

  @RegisterExtension
  val wireMockFoo: WireMockExtension = newInstance().build()

  @RegisterExtension
  val wireMockBar: WireMockExtension = newInstance().build()

  @Test
  fun `call foo and bar`() {
    wireMockFoo.stubFor(
      get(urlPathEqualTo("/foo"))
        .withQueryParam("name", equalTo(name))
        .willReturn(ok().withBody("Hello $name I am Foo!"))
    )
    wireMockBar.stubFor(
      get(urlPathMatching("/bar/$name"))
        .willReturn(ok().withBody("Hello $name I am Bar!"))
    )

    val app = App(name, wireMockFoo.baseUrl(), wireMockBar.baseUrl())

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

package com.rogervinas.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.RegisterExtension

@TestInstance(PER_CLASS)
class AppShouldWithTwoWireMockExtensions {

  @RegisterExtension
  val wireMockFoo: WireMockExtension = WireMockExtension.newInstance().build()

  @RegisterExtension
  val wireMockBar: WireMockExtension = WireMockExtension.newInstance().build()

  @Test
  fun `call foo and bar`() {
    val name = "Helen"
    wireMockFoo.stubFor(
      get(WireMock.urlPathEqualTo("/foo"))
        .withQueryParam("name", equalTo(name))
        .willReturn(ok().withBody("Hello $name I am Foo!"))
    )
    wireMockBar.stubFor(
      get(WireMock.urlPathMatching("/bar/$name"))
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

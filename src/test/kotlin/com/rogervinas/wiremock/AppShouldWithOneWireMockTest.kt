package com.rogervinas.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WireMockTest
class AppShouldWithOneWireMockTest {

  private val name = "Ada"

  @Test
  fun `call foo and bar`(wm: WireMockRuntimeInfo) {
    stubFor(
      get(urlPathEqualTo("/foo"))
        .withQueryParam("name", equalTo(name))
        .willReturn(ok().withBody("Hello $name I am Foo!"))
    )
    stubFor(
      get(urlPathMatching("/bar/$name"))
        .willReturn(ok().withBody("Hello $name I am Bar!"))
    )

    val app = App(name, wm.httpBaseUrl, wm.httpBaseUrl)

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

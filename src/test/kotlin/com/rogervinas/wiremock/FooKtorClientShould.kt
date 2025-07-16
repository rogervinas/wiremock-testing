package com.rogervinas.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.RegisterExtension

@TestInstance(PER_CLASS)
class FooKtorClientShould {
  private val name = "Joe"

  @RegisterExtension
  val wm: WireMockExtension =
    WireMockExtension
      .newInstance()
      .options(options().globalTemplating(true))
      .configureStaticDsl(true)
      .build()

  @Test
  fun `call foo api`() {
    stubFor(
      get(urlPathEqualTo("/foo"))
        .withQueryParam("name", matching(".+"))
        .willReturn(ok().withBody("Hello {{request.query.name}} I am Foo!")),
    )

    assertThat(FooKtorClient(wm.baseUrl()).call(name))
      .isEqualTo("Hello $name I am Foo!")
  }

  @Test
  fun `handle foo api server error`() {
    stubFor(
      get(urlPathEqualTo("/foo"))
        .willReturn(WireMock.serverError()),
    )

    assertThat(FooKtorClient(wm.baseUrl()).call(name))
      .startsWith("Foo api error: Server error")
  }
}

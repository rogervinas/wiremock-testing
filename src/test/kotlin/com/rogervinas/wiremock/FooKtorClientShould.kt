package com.rogervinas.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
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
  val wireMock: WireMockExtension = WireMockExtension.newInstance()
    .options(wireMockConfig().extensions(ResponseTemplateTransformer(true)))
    .build()

  @Test
  fun `call foo api`() {
    wireMock.stubFor(
      get(urlPathEqualTo("/foo"))
        .withQueryParam("name", matching(".+"))
        .willReturn(ok().withBody("Hello {{request.query.name}} I am Foo!"))
    )

    assertThat(FooKtorClient(wireMock.baseUrl()).call(name))
      .isEqualTo("Hello $name I am Foo!")
  }

  @Test
  fun `handle foo api server error`() {
    wireMock.stubFor(
      get(urlPathEqualTo("/foo"))
        .willReturn(WireMock.serverError())
    )

    assertThat(FooKtorClient(wireMock.baseUrl()).call(name))
      .startsWith("Foo api error: Server error")
  }
}

package com.rogervinas.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WireMockTest
class BarKtorClientShould {

  private val name = "Sue"

  @Test
  fun `call bar api`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    stubFor(
      get(urlPathMatching("/bar/$name"))
        .willReturn(ok().withBody("Hello $name I am Bar!"))
    )

    assertThat(BarKtorClient(wireMockRuntimeInfo.httpBaseUrl).call("Sue"))
      .isEqualTo("Hello Sue I am Bar!")
  }

  @Test
  fun `handle bar api server error`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    stubFor(
      get(urlPathMatching("/bar/.+"))
        .willReturn(serverError())
    )

    assertThat(BarKtorClient(wireMockRuntimeInfo.httpBaseUrl).call(name))
      .startsWith("Bar api error: Server error")
  }
}

package com.rogervinas.wiremock

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AppShould {

  @Test
  fun `say hello`() {
    assertThat(App().execute()).isEqualTo("Hello World!")
  }
}

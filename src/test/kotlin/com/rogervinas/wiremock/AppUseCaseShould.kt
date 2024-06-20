package com.rogervinas.wiremock

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AppUseCaseShould {
  private val name = "Tim"
  private val fooResponse = "Hi I am Foo"
  private val fooError = "Sorry Foo failed"
  private val barResponse = "Hi I am Bar"
  private val barError = "Sorry Bar failed"

  @MockK
  lateinit var fooClient: FooClient

  @MockK
  lateinit var barClient: BarClient

  @Test
  fun `call foo and bar`() {
    every { fooClient.call(name) } returns fooResponse
    every { barClient.call(name) } returns barResponse
    assertThat(AppUseCase().execute(name, fooClient, barClient)).isEqualTo(
      """
      Hi! I am $name
      I called Foo and its response is $fooResponse
      I called Bar and its response is $barResponse
      Bye!
      """.trimIndent(),
    )
  }

  @Test
  fun `call foo and bar even if foo fails`() {
    every { fooClient.call(name) } throws Exception(fooError)
    every { barClient.call(name) } returns barResponse
    assertThat(AppUseCase().execute(name, fooClient, barClient)).isEqualTo(
      """
      Hi! I am $name
      I called Foo and it failed with $fooError
      I called Bar and its response is $barResponse
      Bye!
      """.trimIndent(),
    )
  }

  @Test
  fun `call foo and bar even if bar fails`() {
    every { fooClient.call(name) } returns fooResponse
    every { barClient.call(name) } throws Exception(barError)
    assertThat(AppUseCase().execute(name, fooClient, barClient)).isEqualTo(
      """
      Hi! I am $name
      I called Foo and its response is $fooResponse
      I called Bar and it failed with $barError
      Bye!
      """.trimIndent(),
    )
  }

  @Test
  fun `call foo and bar even if both fail`() {
    every { fooClient.call(name) } throws Exception(fooError)
    every { barClient.call(name) } throws Exception(barError)
    assertThat(AppUseCase().execute(name, fooClient, barClient)).isEqualTo(
      """
      Hi! I am $name
      I called Foo and it failed with $fooError
      I called Bar and it failed with $barError
      Bye!
      """.trimIndent(),
    )
  }
}

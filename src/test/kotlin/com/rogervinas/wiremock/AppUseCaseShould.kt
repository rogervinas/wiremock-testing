package com.rogervinas.wiremock

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val NAME = "Joe"
private const val FOO_RESPONSE = "Hi I am Foo"
private const val FOO_ERROR = "Sorry Foo failed"
private const val BAR_RESPONSE = "Hi I am Bar"
private const val BAR_ERROR = "Sorry Bar failed"

@ExtendWith(MockKExtension::class)
class AppUseCaseShould {

  @MockK
  lateinit var fooClient: FooClient

  @MockK
  lateinit var barClient: BarClient

  @Test
  fun `call foo and bar`() {
    every { fooClient.call(NAME) } returns FOO_RESPONSE
    every { barClient.call(NAME) } returns BAR_RESPONSE
    assertThat(AppUseCase().execute(NAME, fooClient, barClient)).isEqualTo(
      """
        Hi! I am $NAME
        I called Foo and its response is $FOO_RESPONSE
        I called Bar and its response is $BAR_RESPONSE
        Bye!
      """.trimIndent()
    )
  }

  @Test
  fun `call foo and bar even if foo fails`() {
    every { fooClient.call(NAME) } throws Exception(FOO_ERROR)
    every { barClient.call(NAME) } returns BAR_RESPONSE
    assertThat(AppUseCase().execute(NAME, fooClient, barClient)).isEqualTo(
      """
        Hi! I am $NAME
        I called Foo and it failed with $FOO_ERROR
        I called Bar and its response is $BAR_RESPONSE
        Bye!
      """.trimIndent()
    )
  }

  @Test
  fun `call foo and bar even if bar fails`() {
    every { fooClient.call(NAME) } returns FOO_RESPONSE
    every { barClient.call(NAME) } throws Exception(BAR_ERROR)
    assertThat(AppUseCase().execute(NAME, fooClient, barClient)).isEqualTo(
      """
        Hi! I am $NAME
        I called Foo and its response is $FOO_RESPONSE
        I called Bar and it failed with $BAR_ERROR
        Bye!
      """.trimIndent()
    )
  }

  @Test
  fun `call foo and bar even if both fail`() {
    every { fooClient.call(NAME) } throws Exception(FOO_ERROR)
    every { barClient.call(NAME) } throws Exception(BAR_ERROR)
    assertThat(AppUseCase().execute(NAME, fooClient, barClient)).isEqualTo(
      """
        Hi! I am $NAME
        I called Foo and it failed with $FOO_ERROR
        I called Bar and it failed with $BAR_ERROR
        Bye!
      """.trimIndent()
    )
  }
}

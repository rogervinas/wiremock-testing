package com.rogervinas.wiremock

class AppUseCase {
  fun execute(
    name: String,
    fooClient: FooClient,
    barClient: BarClient,
  ): String =
    """
    Hi! I am $name
    ${tryCallFoo(fooClient, name)}
    ${tryCallBar(barClient, name)}
    Bye!
    """.trimIndent()

  private fun tryCallFoo(
    fooClient: FooClient,
    name: String,
  ) = "I called Foo and " +
    try {
      "its response is ${fooClient.call(name)}"
    } catch (e: Exception) {
      "it failed with ${e.message}"
    }

  private fun tryCallBar(
    barClient: BarClient,
    name: String,
  ) = "I called Bar and " +
    try {
      "its response is ${barClient.call(name)}"
    } catch (e: Exception) {
      "it failed with ${e.message}"
    }
}

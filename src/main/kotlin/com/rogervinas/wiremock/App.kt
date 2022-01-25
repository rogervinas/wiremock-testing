package com.rogervinas.wiremock

class App(
  private val name: String,
  private val fooApiUrl: String,
  private val barApiUrl: String
) {

  fun execute() = AppUseCase().execute(name, FooKtorClient(fooApiUrl), BarKtorClient(barApiUrl))
}

fun main() {
  val app = App("Bitelchus", "http://localhost:8081", "http://localhost:8082")
  println(app.execute())
}

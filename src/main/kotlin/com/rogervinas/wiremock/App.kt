package com.rogervinas.wiremock

class App(
  private val name: String,
  private val fooUrl: String,
  private val barUrl: String
) {

  fun execute() = AppUseCase().execute(name, FooKtorClient(fooUrl), BarKtorClient(barUrl))
}

fun main() {
  val app = App("Bitelchus", "http://localhost:8081", "http://localhost:8082")
  println(app.execute())
}

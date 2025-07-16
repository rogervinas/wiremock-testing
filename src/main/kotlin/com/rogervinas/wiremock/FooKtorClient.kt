package com.rogervinas.wiremock

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.runBlocking

class FooKtorClient(
  private val url: String,
) : FooClient {
  private val client =
    HttpClient(CIO) {
      expectSuccess = true
    }

  override fun call(name: String): String =
    runBlocking {
      try {
        client
          .get("$url/foo") {
            parameter("name", name)
          }.body<String>()
      } catch (e: Exception) {
        "Foo api error: ${e.message}"
      }
    }
}

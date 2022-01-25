package com.rogervinas.wiremock

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking

class BarKtorClient(private val url: String) : BarClient {

  private val client = HttpClient(CIO)

  override fun call(name: String): String {
    return runBlocking {
      try {
        client.get("$url/bar/$name")
      } catch (e: Exception) {
        "Bar api error: ${e.message}"
      }
    }
  }
}

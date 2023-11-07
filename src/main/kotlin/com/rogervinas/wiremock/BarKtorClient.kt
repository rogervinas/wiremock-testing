package com.rogervinas.wiremock

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking

class BarKtorClient(private val url: String) : BarClient {

  private val client = HttpClient(CIO) {
    expectSuccess = true
  }

  override fun call(name: String): String = runBlocking {
    try {
      client.get("$url/bar/$name").body<String>()
    } catch (e: Exception) {
      "Bar api error: ${e.message}"
    }
  }
}

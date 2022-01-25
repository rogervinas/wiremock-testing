package com.rogervinas.wiremock

interface BarClient {

  fun call(name: String): String
}

[![CI](https://github.com/rogervinas/wiremock-testing/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/rogervinas/wiremock-testing/actions/workflows/gradle.yml)

# WireMock Testing

[WireMock](http://wiremock.org/) is a great library to mock APIs in your tests and supports [Junit5](http://wiremock.org/docs/junit-jupiter/) with two modes:

- Declarative with **@WireMockTest**
- Programmatic with **WireMockExtension**

But "talk is cheap, show me the code [...](https://www.goodreads.com/quotes/437173-talk-is-cheap-show-me-the-code#:~:text=Quote%20by%20Linus%20Torvalds%3A%20%E2%80%9CTalk,Show%20me%20the%20code.%E2%80%9D)" 😮

Ok so let's implement this scenario:

![WireMockTest](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/mi4p3w5oalczqpm7duwr.png)

* [BarClient](#barclient)
  * [BarClient interface](#barclient-interface)
  * [BarKtorClient test](#barktorclient-test)
  * [BarKtorClient implementation](#barktorclient-implementation)
* [FooClient](#fooclient)
  * [FooClient interface](#fooclient-interface)
  * [FooKtorClient test](#fooktorclient-test)
  * [FooKtorClient implementation](#fooktorclient-implementation)
* [AppUseCase](#appusecase)
* [App](#app)
  * [App implementation](#app-implementation)
  * [App test with @WireMockTest](#app-test-with-wiremocktest)
  * [App test with WireMockExtension](#app-test-with-wiremockextension)
* [Test this demo](#test-this-demo)
* [Run this demo](#run-this-demo)

## BarClient

### BarClient interface

```kotlin
interface BarClient {

  fun call(name: String): String
}
```

### BarKtorClient test

I will use a [Ktor client](https://ktor.io/docs/client.html) for no other reason that I need an Http client and this seems interesting, as we are using **Kotlin**.

So a simple **@WireMockTest** for the **BarKtorClient** looks like:

```kotlin
@WireMockTest
class BarKtorClientShould {

 private val name = "Sue"
 
 @Test
 fun `call bar api`(wm: WireMockRuntimeInfo) {
  stubFor(
   get(urlPathMatching("/bar/$name"))
   .willReturn(ok().withBody("Hello $name I am Bar!"))
  )

  assertThat(
    BarKtorClient(wm.httpBaseUrl).call(name)
  ).isEqualTo("Hello $name I am Bar!")
 }

 @Test
 fun `handle bar api server error`(wm: WireMockRuntimeInfo) {
  stubFor(
   get(urlPathMatching("/bar/.+"))
   .willReturn(serverError())
  )

  assertThat(BarKtorClient(wm.httpBaseUrl).call(name))
   .startsWith("Bar api error: Server error")
 }
}
```

### BarKtorClient implementation

In order to make the test pass 🟩 we can implement the **BarKtorClient** this way:

```kotlin
class BarKtorClient(private val url: String) : BarClient {

 private val client = HttpClient(CIO)

 override fun call(name: String): String = runBlocking {
  try {
   client.get("$url/bar/$name")
  } catch (e: Exception) {
   "Bar api error: ${e.message}"
  }
 }
}
```

## FooClient

### FooClient interface

```kotlin
interface FooClient {
  
  fun call(name: String): String
}
```

### FooKtorClient test

For this test I want to use [WireMock's response templating](https://wiremock.org/docs/response-templating/) feature, so I will register a **WireMockExtension** instead of using **@WireMockTest**:

```kotlin
@TestInstance(PER_CLASS)
class FooKtorClientShould {
  
 private val name = "Joe"

 @RegisterExtension
 val wm: WireMockExtension = WireMockExtension.newInstance()
  .options(wireMockConfig()
    .extensions(ResponseTemplateTransformer(true))
  )
  .configureStaticDsl(true)
  .build()

 @Test
 fun `call foo api`() {
  stubFor(
   get(urlPathEqualTo("/foo"))
   .withQueryParam("name", matching(".+"))
   .willReturn(ok().withBody("Hello {{request.query.name}} I am Foo!"))
  )

  assertThat(FooKtorClient(wm.baseUrl()).call(name))
   .isEqualTo("Hello $name I am Foo!")
 }

 @Test
 fun `handle foo api server error`() {
  stubFor(
   get(urlPathEqualTo("/foo"))
   .willReturn(WireMock.serverError())
  )

  assertThat(FooKtorClient(wm.baseUrl()).call(name))
   .startsWith("Foo api error: Server error")
 }
}
```

Note that:
* Instead of having a fixed response, with [WireMock's response templating](https://wiremock.org/docs/response-templating/) we can insert in the response values from the request. In this case the query parameter `name`.
* `@TestInstance(PER_CLASS)` makes **JUnit5** create a single instance of **FooKtorClientShould** to be used by both tests so the **WireMockExtension** is registered only once. By default **JUnit5** would create one instance for each test (see [Test Instance Lifecycle](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-instance-lifecycle)).
* `configureStaticDsl(true)` makes it possible to use the static DSL, that is using `stubFor(...)` staticly instead of `wm.stubFor(...)`.

### FooKtorClient implementation

Same as before in order to make the test pass 🟩 we can implement the **FooKtorClient** this way:

```kotlin
class FooKtorClient(private val url: String) : FooClient {
  
 private val client = HttpClient(CIO)

 override fun call(name: String): String = runBlocking {
  try {
   client.get("$url/foo") {
    parameter("name", name)
   }
  } catch (e: Exception) {
   "Foo api error: ${e.message}"
  }
 }
}
```

## AppUseCase

Now we have to implement **AppUseCase**, which will use a **FooClient** to call the **Foo API** and then a **BarClient** to call the **Bar API**. 

As it is not **WireMock** related because we can test first the implementation just using [MockK JUnit5 extension](https://mockk.io/#junit5) we can skip the details and you can review the source code of [AppUseCaseShould](src/test/kotlin/com/rogervinas/wiremock/AppUseCaseShould.kt) and [AppUseCase](src/main/kotlin/com/rogervinas/wiremock/AppUseCase.kt).

## App

### App implementation

Let me introduce first the **App** implementation, as I will present later two different types of **WireMock** tests:

```kotlin
class App(
 private val name: String,
 private val fooApiUrl: String,
 private val barApiUrl: String
) {

 fun execute() = AppUseCase().execute(
  name,
  FooKtorClient(fooApiUrl),
  BarKtorClient(barApiUrl)
 )
}
```

### App test with @WireMockTest

Since in this example **Foo API** and **Bar API** <u>do not have conflicting endpoints</u>, we can use one **@WireMockTest** to mock both APIs:

```kotlin
@WireMockTest
class AppShouldWithOneWireMockTest {

 private val name = "Ada"

 @Test
 fun `call foo and bar`(wm: WireMockRuntimeInfo) {
  stubFor(
   get(urlPathEqualTo("/foo"))
    .withQueryParam("name", equalTo(name))
    .willReturn(ok().withBody("Hello $name I am Foo!"))
  )
  stubFor(
   get(urlPathMatching("/bar/$name"))
    .willReturn(ok().withBody("Hello $name I am Bar!"))
  )

  val app = App(name, wm.httpBaseUrl, wm.httpBaseUrl)

  assertThat(app.execute()).isEqualTo(
   """
    Hi! I am $name
    I called Foo and its response is Hello $name I am Foo!
    I called Bar and its response is Hello $name I am Bar!
    Bye!
   """.trimIndent()
  )
 }
}
```

### App test with WireMockExtension

But imagine a real scenario where **Foo API** and **Bar API** <u>do have conflicting endpoints</u>, or you just want to <u>mock them separatedly for any reason</u>. In this case you can register two **WireMockExtensions** instead of using **@WireMockTest**:

```kotlin
@TestInstance(PER_CLASS)
class AppShouldWithTwoWireMockExtensions {

 private val name = "Leo"

 @RegisterExtension
 val wireMockFoo: WireMockExtension = newInstance().build()

 @RegisterExtension
 val wireMockBar: WireMockExtension = newInstance().build()

 @Test
 fun `call foo and bar`() {
  wireMockFoo.stubFor(
   get(WireMock.urlPathEqualTo("/foo"))
    .withQueryParam("name", equalTo(name))
    .willReturn(ok().withBody("Hello $name I am Foo!"))
  )
  wireMockBar.stubFor(
   get(WireMock.urlPathMatching("/bar/$name"))
    .willReturn(ok().withBody("Hello $name I am Bar!"))
  )

  val app = App(name, wireMockFoo.baseUrl(), wireMockBar.baseUrl())

  assertThat(app.execute()).isEqualTo(
   """
    Hi! I am $name
    I called Foo and its response is Hello $name I am Foo!
    I called Bar and its response is Hello $name I am Bar!
    Bye!
   """.trimIndent()
  )
 }
}
```

That was a good one! Happy coding! 💙

## Test this demo

```shell
./gradlew test
```

## Run this demo

```
docker compose up -d
./gradlew run
docker compose down
```

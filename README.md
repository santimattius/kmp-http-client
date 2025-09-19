
# KMP HTTP Client

A lightweight Kotlin Multiplatform HTTP client built on top of Ktor, offering a clean builder-style API, typed configuration, interceptors (logging, auth, error handling), and a unified response model. Targets Android and iOS.

- Core API: `shared/src/commonMain/kotlin/com/santimattius/http/`
- Entry point: `HttpClient` (default singleton and custom clients)
- Request builder: `HttpRequest`
- Response: `HttpResponse`
- Configuration: `HttpClientConfig`
- Interceptors: `com.santimattius.http.interceptor.*`

## Introduction

- Brief description:
  KMP HTTP Client simplifies HTTP usage in Kotlin Multiplatform projects. It wraps Ktor with a small, consistent API, cutting down boilerplate and centralizing configuration and observability.

- Problem it solves:
  Unifies HTTP usage across Android and iOS while keeping a minimal surface area. It avoids duplicating Ktor setup per platform, standardizes request building, and enables cross-cutting concerns through interceptors.

- Usage context:
  Designed for Kotlin Multiplatform with Android (Ktor OkHttp) and iOS (Ktor Darwin). Integrates in Android apps (`androidApp/`), iOS apps (`iosApp/`), and common modules (`shared/`).

## Installation

- Prerequisites:
  - Kotlin Multiplatform set up.
  - Android: add `android.permission.INTERNET` to `AndroidManifest.xml`; respect `minSdk`/`compileSdk` as configured in `shared/build.gradle.kts`.
  - iOS: Xcode (iOS 14+ recommended), integrate the generated `Shared` framework.
  - Ktor and Kotlinx Serialization are already managed by the `shared` module.

### Android (Gradle)

Option A — Use the local `shared` module:
```kotlin
// settings.gradle.kts
include(":shared")
```
```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":shared"))
}
```

Option B — From Maven (if you publish the library):
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    // Replace with actual coordinates when you publish
    implementation("com.santimattius:http-client:<version>")
}
```

Notes:
- Android engine: Ktor OkHttp (`ktor-client-okhttp`).
- Initialize once (e.g., `Application#onCreate`).

### iOS

The generated iOS framework is named `Shared` (`shared/build.gradle.kts` sets `baseName = "Shared"`). Common integration paths:

- Xcode + KMP plugin: include `shared` and let Gradle produce `Shared.framework`.
- Prebuilt XCFramework: archive `Shared.xcframework` and add it to your iOS project.
- Swift Package Manager wrapper: provide a `Package.swift` referencing a hosted `Shared.xcframework` (binary target).

Example SwiftPM wrapper (binary target):
```swift
// Package.swift (example)
import PackageDescription

let package = Package(
    name: "KMPHttpClient",
    platforms: [ .iOS(.v14) ],
    products: [ .library(name: "KMPHttpClient", targets: ["Shared"]) ],
    targets: [
        .binaryTarget(
            name: "Shared",
            url: "https://github.com/your-org/kmp-http-client/releases/download/1.0.0/Shared.xcframework.zip",
            checksum: "<swiftpm-checksum>"
        )
    ]
)
```

In Swift:
```swift
import Shared
```

Skie is enabled to improve Swift interop (`skie { swiftBundling { enabled = true } }`).

## Basic Usage

Initialize once and reuse the default client.

Kotlin (common/Android):
```kotlin
import com.santimattius.http.HttpClient
import com.santimattius.http.HttpRequest
import com.santimattius.http.config.HttpClientConfig

// e.g., in Application.onCreate
HttpClient.initialize(
    HttpClientConfig(baseUrl = "https://api.example.com")
        .connectTimeout(30_000)
        .socketTimeout(30_000)
        .enableLogging(true)
)

val client = HttpClient.defaultClient()

suspend fun fetchUsers(): Result<String> = runCatching {
    val request = HttpRequest
        .get("https://api.example.com")
        .path("/users")
        .queryParam("page", "1")
        .header("Accept", "application/json")
        .build()

    val response = client.execute(request)
    if (!response.isSuccessful) error("HTTP ${'$'}{response.status}")
    response.body ?: ""
}
```

Main parameters in `HttpClientConfig`:
- `baseUrl`: API base URL.
- `connectTimeout`: connection timeout (ms).
- `socketTimeout`: read/write timeout (ms).
- `enableLogging`: toggle logging.
- `logLevel`: `NONE`, `BASIC`, `HEADERS`, `BODY`.
- `cache`: `CacheConfig` for HTTP caching.

## Advanced Use Cases

POST with JSON body:
```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

suspend fun login(email: String, password: String): Boolean {
    val client = HttpClient.defaultClient()
    val request = HttpRequest
        .post("https://api.example.com")
        .path("/auth/login")
        .header("Content-Type", "application/json")
        .body(LoginRequest(email, password))
        .build()

    val response = client.execute(request)
    return response.isSuccessful
}
```

Custom client with interceptors:
```kotlin
import com.santimattius.http.config.HttpClientConfig
import com.santimattius.http.config.LogLevel
import com.santimattius.http.interceptor.LoggingInterceptor

val customClient = com.santimattius.http.HttpClient.create(
    HttpClientConfig(baseUrl = "https://api.example.com")
        .enableLogging(true)
        .logLevel(LogLevel.BODY)
).addInterceptors(LoggingInterceptor())
```

Swift interop with JSON decoding helper:
The Swift extension `HttpResponse.getBodyAs(_:)` lives in `shared/src/commonMain/swift/HttpResponseExtension.swift`.
```swift
import Foundation
import Shared

struct User: Decodable { let id: Int; let name: String }

@MainActor
func loadUsers() async throws -> [User] {
    // Ensure HttpClient.initialize(...) is called at app startup (Kotlin side)
    let request = HttpRequest.Companion().get("https://api.example.com")
        .path("/users")
        .header(name: "Accept", value: "application/json")
        .build()

    // Exposure of HttpClient may vary with your interop settings
    let client = HttpClient().defaultClient()
    let response = try await client.execute(request: request)
    return try response.getBodyAs([User].self)
}
```

## Best Practices

- Initialize once with `HttpClient.initialize(...)` and reuse `HttpClient.defaultClient()`.
- Prefer a single `baseUrl` and compose routes with `Builder.path("/segment")` and `queryParam()`.
- Tune timeouts via `connectTimeout(...)` and `socketTimeout(...)` according to your use cases.
- Use `enableLogging(true)` and `LogLevel.BODY` only in development to avoid leaking sensitive data.
- Implement interceptors for auth, retries, and error mapping; see `com.santimattius.http.interceptor.*`.
- Always check `HttpResponse.isSuccessful` and handle error bodies.
- Android: keep networking off the main thread; use coroutines and proper dispatchers.
- iOS: use async/await and consider small Kotlin facades to simplify suspend calls from Swift.

Common pitfalls and how to avoid them:
- Forgetting `HttpClient.initialize(...)` before `defaultClient()`: initialize during app startup.
- Blank URL or malformed `path`: build with `get(baseUrl).path("/segment")` and validate.
- Missing `Content-Type` for JSON: set `header("Content-Type", "application/json")`.
- Excessive logging in production: limit to `BASIC` or `NONE`.
- Suspend bridging issues on iOS: verify interop config (Skie) or add a Kotlin facade.

## Migration

From raw Ktor:
- `HttpClient(OkHttp)` / `HttpClient(Darwin)` → `HttpClient.initialize(HttpClientConfig(...))` + `HttpClient.defaultClient()`.
- `client.get/post/put(...)` → Build with `HttpRequest.get/post/put(...).header(...).queryParam(...).body(...).build()` and call `client.execute(request)`.
- Ktor features/plugins → use custom interceptors (`com.santimattius.http.interceptor.*`).

From earlier versions of this wrapper:
- Centralize configuration in `HttpClientConfig` (timeouts, logging, cache).
- Initialize once with `HttpClient.initialize(...)` and remove duplicated setups.

## References

- Source paths:
  - Core HTTP: `shared/src/commonMain/kotlin/com/santimattius/http/`
  - Swift helpers: `shared/src/commonMain/swift/`
  - Android sample: `androidApp/`
  - iOS sample: `iosApp/`

- Official docs:
  - Kotlin Multiplatform: https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
  - Ktor Client: https://ktor.io/docs/getting-started-ktor-client.html

## Environment Check

It is recommended to install and run [kdoctor](https://github.com/Kotlin/kdoctor) to verify that your development environment is correctly set up for Kotlin Multiplatform development. `kdoctor` helps diagnose and fix common configuration issues.

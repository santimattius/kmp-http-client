# KMP HTTP Client

A lightweight HTTP client for Kotlin Multiplatform based on Ktor. It provides a simple builder-style API, typed configuration, interceptors (logging, authentication, error handling), and a unified response model. Compatible with Android and iOS.

- Core API: `shared/src/commonMain/kotlin/com/santimattius/http/`
- Entry point: `HttpClient` (default singleton and custom clients)
- Request builder: `HttpRequest`
- Response: `HttpResponse`
- Configuration: `HttpClientConfig`
- Interceptors: `com.santimattius.http.interceptor.*`

## Introduction

**Brief description:**  
KMP HTTP Client simplifies the use of HTTP in Android, iOS, and Kotlin Multiplatform projects. It wraps Ktor with a small, consistent API, reducing boilerplate and centralizing configuration and observability.

**Problem it solves:**  
It unifies HTTP usage across Android and iOS while keeping a minimal surface. It avoids duplicating Ktor setup per platform, standardizes request building, and enables handling cross-cutting concerns.

## Installation

**Prerequisites:**
- Kotlin Multiplatform properly configured.
- Android: add `android.permission.INTERNET` in `AndroidManifest.xml`; respect `minSdk` and `compileSdk` defined in `shared/build.gradle.kts`.
- iOS: Xcode (iOS 14+ recommended) and integration of the generated `Shared` framework.
- Ktor and Kotlinx Serialization are already included in the `shared` module.

### Android (Gradle)

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    // Replace with actual coordinates when publishing
    implementation("com.santimattius:http-client:<version>")
}
```

Notes:
- Android engine: Ktor OkHttp (`ktor-client-okhttp`).
- Initialize once (e.g., in `Application#onCreate` or using AndroidX Startup).

### iOS

The generated framework is called `HttpClient` (defined in `shared/build.gradle.kts` with `baseName = "HttpClient"`). Integration options:

- Xcode + KMP plugin: include `shared` and let Gradle generate `HttpClient.framework`.
- Prebuilt XCFramework: create `HttpClient.xcframework` and add it to your iOS project.
- Swift Package Manager wrapper: define a `Package.swift` pointing to the published `HttpClient.xcframework` (binary target).

Example with SwiftPM (binary target):
```swift
// Package.swift (example)
import PackageDescription

let package = Package(
    name: "HttpClient",
    platforms: [ .iOS(.v14) ],
    products: [ .library(name: "HttpClient", targets: ["Shared"]) ],
    targets: [
        .binaryTarget(
            name: "HttpClient",
            url: "https://github.com/your-org/kmp-http-client/releases/download/1.0.0/HttpClient.xcframework.zip",
            checksum: "<swiftpm-checksum>"
        )
    ]
)
```

In Swift:
```swift
import HttpClient
```

Skie is enabled to improve Swift interoperability:
```kotlin
skie {
    swiftBundling {
         enabled = true 
    }
}
```  
## Basic Usage

Initialize the client once and reuse the default instance.

Kotlin (Android):
```kotlin
import com.santimattius.http.HttpClient
import com.santimattius.http.HttpRequest
import com.santimattius.http.config.HttpClientConfig

// Example in Application.onCreate
HttpClient.initialize(
    HttpClientConfig(baseUrl = "https://api.example.com")
        .connectTimeout(30_000)
        .socketTimeout(30_000)
        .enableLogging(true)
)

val client = HttpClient.defaultClient()

suspend fun fetchUsers(): Result<String> = runCatching {
    val request = HttpRequest
        .get("/users")
        .queryParam("page", "1")
        .header("Accept", "application/json")
        .build()

    val response = client.execute(request)
    if (!response.isSuccessful) error("HTTP ${response.status}")
    response.body ?: ""
}
```

**Main parameters in `HttpClientConfig`:**
- `baseUrl`: API base URL.
- `connectTimeout`: connection timeout (ms).
- `socketTimeout`: read/write timeout (ms).
- `enableLogging`: enable or disable logs.
- `logLevel`: `NONE`, `BASIC`, `HEADERS`, `BODY`.
- `cache`: cache configuration (`CacheConfig`).

---

## Advanced Use Cases

**POST with JSON body:**
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

**Custom client with interceptors:**  
The library already provides some interceptors such as:
- **AuthInterceptor**: for authorization handling.
- **TokenRefreshInterceptor**: for token management.
- **LoggingInterceptor**: for customizing log output.
- **ErrorHandlingInterceptor**: for throwing exceptions based on HTTP error types.

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
You can also implement your own interceptors using the `Interceptor` interface/protocol:

```swift
import Shared

class OkHttpInterceptor: Interceptor {
     
    func __intercept(chain: any InterceptorChain) async throws -> HttpResponse {
        print("Hello from OkHttpInterceptor")
        return try await chain.proceed(request: chain.request)
    }
    
}
```

**Swift interop with JSON decoding:**
```swift
import Foundation
import Shared

struct User: Decodable { let id: Int; let name: String }

@MainActor
func loadUsers() async throws -> [User] {
    let request = HttpRequest.Companion().get("https://api.example.com")
        .path("/users")
        .header(name: "Accept", value: "application/json")
        .build()

    let client = HttpClient().defaultClient()
    let response = try await client.execute(request: request)
    return try response.getBodyAs([User].self)
}
```

## Best Practices

- Initialize once with `HttpClient.initialize(...)` and reuse `HttpClient.defaultClient()`.
- Prefer a single `baseUrl` and build routes with `get("/segment")` and `queryParam()`.
- Adjust `connectTimeout(...)` and `socketTimeout(...)` according to your use case.
- Use `enableLogging(true)` and `LogLevel.BODY` only in development to avoid leaking sensitive data.
- Implement interceptors for auth, retries, and error handling (`com.santimattius.http.interceptor.*`).
- Always check `HttpResponse.isSuccessful` and handle errors properly.
- **Android:** keep networking off the main thread (use coroutines and proper dispatchers).
- **iOS:** use `async/await` and, if necessary, small Kotlin facades to simplify suspend calls from Swift.

**Common pitfalls and how to avoid them:**
- Forgetting `HttpClient.initialize(...)` before `defaultClient()`: always initialize during app startup.
- Malformed URLs: use `get(baseUrl).path("/segment")` and validate.
- Missing `Content-Type` for JSON: always set `header("Content-Type", "application/json")`.
- Excessive logging in production: limit to `BASIC` or `NONE`.
- Suspend bridging issues in iOS: check interop configuration (Skie) or create Kotlin facades.

## References

- **Source code:**
  - Core HTTP: `shared/src/commonMain/kotlin/com/santimattius/http/`
  - Swift extensions: `shared/src/commonMain/swift/`
  - Android sample: `androidApp/`
  - iOS sample: `iosApp/`

- **Official documentation:**
  - Kotlin Multiplatform: https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
  - Ktor Client: https://ktor.io/docs/getting-started-ktor-client.html

## Environment Check

It is recommended to install and run [kdoctor](https://github.com/Kotlin/kdoctor) to verify that your development environment is correctly set up for Kotlin Multiplatform.  
`kdoctor` helps diagnose and fix common configuration issues.  

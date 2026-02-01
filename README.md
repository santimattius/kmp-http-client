[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin_Multiplatform-2.3.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![Android](https://img.shields.io/badge/Android-AGP%209.0-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![iOS](https://img.shields.io/badge/iOS-Supported-8E8E93?logo=apple&logoColor=white)](https://developer.apple.com)
[![Gradle](https://img.shields.io/badge/Gradle-9.1-02303A?logo=gradle&logoColor=white)](https://gradle.org)
[![Android SDK](https://img.shields.io/badge/Android_SDK-36%20%7C%20min%2024-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
# KMP HTTP Client

**A lightweight Kotlin Multiplatform HTTP client built on Ktor.**

KMP HTTP Client provides a minimal, builder-style API for HTTP on Android and iOS. It offers typed configuration, pluggable interceptors (logging, auth, error handling), and a unified response model—reducing boilerplate and keeping a single, consistent API across platforms.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Best Practices](#best-practices)
- [Tech Support](#tech-support)
- [References](#references)

---

## Overview

| Item | Description |
|------|-------------|
| **Purpose** | Simplify HTTP in Kotlin Multiplatform (Android & iOS) with one API surface. |
| **Problem** | Avoid duplicated Ktor setup per platform, standardize request/response handling, and centralize cross-cutting concerns (auth, logging, errors). |
| **Solution** | A thin wrapper over Ktor with a builder API, shared config, and interceptors. |

**Core API (Kotlin):**

- **Entry point:** `HttpClient` (default singleton + custom instances)
- **Request:** `HttpRequest` (builder)
- **Response:** `HttpResponse`
- **Configuration:** `HttpClientConfig`
- **Interceptors:** `com.santimattius.http.interceptor.*`

---

## Features

- Builder-style `HttpRequest` with path, query, headers, and body
- Typed `HttpClientConfig` (timeouts, logging, cache)
- Built-in interceptors: logging, auth, token refresh, error handling
- Custom interceptors via `Interceptor` (Kotlin) / Swift protocol
- Unified `HttpResponse` with success/error and optional typed body
- Android: Ktor OkHttp engine; iOS: Ktor Darwin
- Swift interoperability via Skie for cleaner iOS usage

---

## Requirements

| Platform | Requirement |
|----------|-------------|
| **Kotlin** | Kotlin Multiplatform project (Kotlin 2.x) |
| **Android** | `minSdk` / `compileSdk` as in `shared/build.gradle.kts`; `INTERNET` permission |
| **iOS** | Xcode, iOS 14+; integration via framework or Swift Package Manager |
| **Dependencies** | Ktor and Kotlinx Serialization (included in `shared` module) |

---

## Installation

### Android (Gradle)

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.santimattius.kmp:http-client:<version>")
}
```

- Engine: Ktor OkHttp (`ktor-client-okhttp`).
- Initialize once (e.g. `Application#onCreate` or AndroidX Startup).

### iOS

The shared module produces the **KMPHttpClient** framework (`baseName` in `shared/build.gradle.kts`). You can:

1. **Xcode + KMP:** Include the `shared` module and use the Gradle-generated framework.
2. **Prebuilt XCFramework:** Build `KMPHttpClient.xcframework` and add it to the iOS app.
3. **Swift Package Manager (binary):** Add a binary target pointing to the published XCFramework.

**Example — SwiftPM binary target:**

```swift
// Package.swift
import PackageDescription

let package = Package(
    name: "KMPHttpClient",
    platforms: [.iOS(.v14)],
    products: [.library(name: "KMPHttpClient", targets: ["KMPHttpClient"])],
    targets: [
        .binaryTarget(
            name: "KMPHttpClient",
            url: "https://github.com/your-org/kmp-http-client/releases/download/1.0.0/KMPHttpClient.xcframework.zip",
            checksum: "<swiftpm-checksum>"
        )
    ]
)
```

In Swift:

```swift
import KMPHttpClient
```

Skie is enabled in the shared module for better Swift interop:

```kotlin
skie {
    swiftBundling { enabled = true }
}
```

---

## Quick Start

1. Initialize the client once at app startup.
2. Reuse the default client or create custom instances with `HttpClient.create(...)`.

**Kotlin (Android):**

```kotlin
import com.santimattius.http.HttpClient
import com.santimattius.http.HttpRequest
import com.santimattius.http.config.HttpClientConfig

// e.g. in Application.onCreate
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

**Main `HttpClientConfig` options:**

| Parameter | Description |
|-----------|-------------|
| `baseUrl` | Base URL for requests |
| `connectTimeout` | Connection timeout (ms) |
| `socketTimeout` | Read/write timeout (ms) |
| `enableLogging` | Enable/disable HTTP logging |
| `logLevel` | `NONE`, `BASIC`, `HEADERS`, `BODY` |
| `cache` | Optional cache config (`CacheConfig`) |

---

## Usage

### POST with JSON body

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

### Custom client and interceptors

Built-in interceptors:

- **AuthInterceptor** — Authorization headers
- **TokenRefreshInterceptor** — Token refresh flow
- **LoggingInterceptor** — Configurable logging
- **ErrorHandlingInterceptor** — Map HTTP errors to exceptions

```kotlin
import com.santimattius.http.config.HttpClientConfig
import com.santimattius.http.config.LogLevel
import com.santimattius.http.interceptor.LoggingInterceptor

val customClient = HttpClient.create(
    HttpClientConfig(baseUrl = "https://api.example.com")
        .enableLogging(true)
        .logLevel(LogLevel.BODY)
).addInterceptors(LoggingInterceptor())
```

Custom interceptor (Swift):

```swift
import KMPHttpClient

class OkHttpInterceptor: Interceptor {
    func __intercept(chain: any InterceptorChain) async throws -> HttpResponse {
        print("Hello from OkHttpInterceptor")
        return try await chain.proceed(request: chain.request)
    }
}
```

### Swift: JSON decoding

```swift
import Foundation
import KMPHttpClient

struct User: Decodable { let id: Int; let name: String }

@MainActor
func loadUsers() async throws -> [User] {
    let request = HttpRequest.companion.get("https://api.example.com")
        .path("/users")
        .header(name: "Accept", value: "application/json")
        .build()

    let client = HttpClient.shared.defaultClient()
    let response = try await client.execute(request: request)
    return try response.getBodyAs([User].self)
}
```

---

## Project Structure

| Path | Description |
|------|-------------|
| `shared/src/commonMain/kotlin/com/santimattius/http/` | Core HTTP API, config, interceptors |
| `shared/src/commonMain/swift/` | Swift extensions and helpers |
| `androidApp/` | Android sample app |
| `iosApp/` | iOS sample app |

---

## Best Practices

- **Initialization:** Call `HttpClient.initialize(...)` once at startup; reuse `HttpClient.defaultClient()`.
- **URLs:** Prefer a single `baseUrl` and build paths with `get("/segment")` and `queryParam()`.
- **Timeouts:** Set `connectTimeout` and `socketTimeout` to match your backend and network.
- **Logging:** Use `enableLogging(true)` and `LogLevel.BODY` only in development to avoid leaking sensitive data.
- **Cross-cutting logic:** Use interceptors for auth, retries, and error handling.
- **Responses:** Always check `HttpResponse.isSuccessful` and handle errors explicitly.
- **Android:** Keep I/O off the main thread (coroutines + appropriate dispatchers).
- **iOS:** Use `async/await`; consider Kotlin facades for suspend functions called from Swift.

**Common pitfalls:**

| Pitfall | Mitigation |
|---------|------------|
| Using `defaultClient()` before `initialize(...)` | Initialize in app startup (e.g. `Application#onCreate`). |
| Malformed URLs | Use `path("/segment")` and validate base URL. |
| Missing JSON `Content-Type` | Set `header("Content-Type", "application/json")` for JSON bodies. |
| Verbose logging in production | Use `LogLevel.BASIC` or `NONE`. |
| Swift/suspend bridging issues | Verify Skie/interop setup or add Kotlin facades. |

---

## Tech Support

This project is maintained on a **best-effort** basis by the core team and community. The following guidelines set expectations for support and maintenance.

### Support scope

| Type | In scope | Out of scope |
|------|----------|--------------|
| **Library usage** | Setup, API usage, migration from Ktor | App-level architecture, non-HTTP bugs |
| **Bug reports** | Reproducible bugs in this repo | Third-party libs, OS/IDE issues |
| **Documentation** | README, code samples, migration notes | Custom tutorials, external blogs |

### Support channels

- **GitHub Issues:** Bug reports and feature requests. Use the issue templates when available.
- **GitHub Discussions:** Questions, ideas, and community help (no guaranteed response time).

### How to get help

1. **Search** existing Issues and Discussions.
2. **Read** this README and the referenced docs.
3. **Open an Issue** for bugs (with minimal repro) or **start a Discussion** for questions.
4. **Be specific:** environment, versions, code snippet, and what you already tried.

---

## References

- **Repository:** [github.com/santimattius/kmp-http-client](https://github.com/santimattius/kmp-http-client)
- **Kotlin Multiplatform:** [Get started with Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- **Ktor Client:** [Ktor client documentation](https://ktor.io/docs/getting-started-ktor-client.html)
- **Environment:** Run [kdoctor](https://github.com/Kotlin/kdoctor) to verify your KMP development setup.

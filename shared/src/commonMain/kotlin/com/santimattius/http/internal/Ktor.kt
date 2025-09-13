package com.santimattius.http.internal

import com.santimattius.http.config.HttpClientConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

typealias KtorHttpClient = HttpClient

val jsonConfig = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}
internal fun createKtorClient(config: HttpClientConfig): HttpClient {
    return HttpClient {
        // Configure timeouts
        install(HttpTimeout) {
            requestTimeoutMillis = config.connectTimeout
            connectTimeoutMillis = config.connectTimeout
            socketTimeoutMillis = config.socketTimeout
        }

        // Configure JSON serialization
        install(ContentNegotiation) {
            json(jsonConfig)
        }

        // Configure logging if enabled
        if (config.enableLogging) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[Ktor] $message")
                    }
                }
                level = when (config.logLevel) {
                    HttpClientConfig.LogLevel.NONE -> LogLevel.NONE
                    HttpClientConfig.LogLevel.BASIC -> LogLevel.INFO
                    HttpClientConfig.LogLevel.HEADERS -> LogLevel.HEADERS
                    HttpClientConfig.LogLevel.BODY -> LogLevel.BODY
                }
            }
        }

        // Default headers
        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.CacheControl, "no-cache")
        }

        // Follow redirects
        expectSuccess = false
        followRedirects = true
    }
}
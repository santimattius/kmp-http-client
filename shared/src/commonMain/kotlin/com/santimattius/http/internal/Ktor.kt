package com.santimattius.http.internal

import com.santimattius.http.config.HttpClientConfig
import com.santimattius.http.config.LogLevel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel as KtorLogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

typealias KtorHttpClient = HttpClient

internal val jsonConfig = Json {
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
                    LogLevel.NONE -> KtorLogLevel.NONE
                    LogLevel.BASIC -> KtorLogLevel.INFO
                    LogLevel.HEADERS -> KtorLogLevel.HEADERS
                    LogLevel.BODY -> KtorLogLevel.BODY
                }
            }
        }

        // Default headers
        defaultRequest {
            url(config.baseUrl)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            //TODO: remove this header when implement cache
            header(HttpHeaders.CacheControl, "no-cache")
        }

        // Follow redirects
        expectSuccess = false
        followRedirects = true
    }
}
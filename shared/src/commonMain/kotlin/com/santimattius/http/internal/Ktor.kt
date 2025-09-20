package com.santimattius.http.internal

import com.santimattius.http.config.HttpClientConfig
import com.santimattius.http.config.LogLevel
import com.santimattius.http.internal.cache.configureCache
import com.santimattius.http.internal.cache.disableCaching
import com.santimattius.http.internal.cache.getCacheDirectoryProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.LogLevel as KtorLogLevel

/**
 * Internal package containing Ktor HTTP client implementation details.
 *
 * This package provides the underlying HTTP client implementation using Ktor's multiplatform
 * HTTP client. It handles the low-level details of HTTP communication, including:
 * - Connection management
 * - Request/response serialization
 * - Timeout configuration
 * - Logging
 * - Default headers
 *
 * @see com.santimattius.http.HttpClient For the public API that uses this implementation
 */

/**
 * Type alias for the Ktor HTTP client.
 *
 * This is used internally to abstract the underlying HTTP client implementation
 * and make it easier to swap out if needed in the future.
 */
typealias KtorHttpClient = HttpClient

/**
 * Default JSON configuration used for request/response serialization.
 *
 * This configuration:
 * - Pretty-prints JSON for better readability in logs
 * - Is lenient with JSON parsing
 * - Ignores unknown JSON keys during deserialization
 */
internal val jsonConfig = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

/**
 * Creates and configures a new Ktor HTTP client instance.
 *
 * This function sets up the HTTP client with the following features:
 * - Configurable timeouts
 * - JSON serialization/deserialization
 * - Logging (if enabled)
 * - Default headers (Content-Type, Accept, etc.)
 * - Redirect following
 *
 * @param config The configuration to apply to the HTTP client
 * @return A configured [KtorHttpClient] instance
 *
 * @see HttpClientConfig For available configuration options
 * @see KtorHttpClient For the underlying HTTP client implementation
 */
internal fun createKtorClient(
    config: HttpClientConfig,
): KtorHttpClient {
    return HttpClient {
        // Configure timeouts for different phases of the request
        install(HttpTimeout) {
            // Maximum time to wait for the entire request (including body) to complete
            requestTimeoutMillis = config.connectTimeout
            // Maximum time to wait for the initial connection to be established
            connectTimeoutMillis = config.connectTimeout
            // Maximum time between data packets when reading the response
            socketTimeoutMillis = config.socketTimeout
        }

        // Configure JSON serialization/deserialization
        install(ContentNegotiation) {
            // Use the shared JSON configuration for consistent serialization
            json(jsonConfig)
        }

        // Configure logging if enabled in the config
        if (config.enableLogging) {
            install(Logging) {
                // Custom logger that prefixes all messages with [HTTP CLIENT]
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[HTTP CLIENT] $message")
                    }
                }
                // Map our LogLevel to Ktor's LogLevel
                level = when (config.logLevel) {
                    LogLevel.NONE -> KtorLogLevel.NONE
                    LogLevel.BASIC -> KtorLogLevel.INFO
                    LogLevel.HEADERS -> KtorLogLevel.HEADERS
                    LogLevel.BODY -> KtorLogLevel.BODY
                }
            }
        }

        if (config.cache.enabled) {
            configureCache(config.cache, getCacheDirectoryProvider())
        } else {
            disableCaching()
        }
        // Configure default request settings
        defaultRequest {
            // Set the base URL for all requests
            url(config.baseUrl)
            // Set default headers for all requests
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
}
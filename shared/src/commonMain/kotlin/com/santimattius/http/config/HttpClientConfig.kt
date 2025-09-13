package com.santimattius.http.config

import kotlin.time.Duration.Companion.seconds

/**
 * Configuration class for the HTTP client.
 *
 * @property baseUrl The base URL for all requests
 * @property connectTimeout Connection timeout duration (default: 30 seconds)
 * @property socketTimeout Socket timeout duration (default: 30 seconds)
 * @property enableLogging Whether to enable request/response logging (default: false)
 * @property logLevel The level of logging detail (default: BASIC)
 */
data class HttpClientConfig(
    val baseUrl: String,
    val connectTimeout: Long = 30.seconds.inWholeMilliseconds,
    val socketTimeout: Long = 30.seconds.inWholeMilliseconds,
    val enableLogging: Boolean = false,
    val logLevel: LogLevel = LogLevel.BASIC,
) {
    /**
     * Logging levels for the HTTP client.
     * Similar to OkHttp's logging levels but adapted for Ktor.
     */
    enum class LogLevel {
        /** No logs */
        NONE,

        /** Logs request and response lines */
        BASIC,

        /** Logs request and response lines and their respective headers */
        HEADERS,

        /** Logs request and response lines and their respective headers and bodies (if present) */
        BODY
    }
}

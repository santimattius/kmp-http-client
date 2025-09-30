package com.santimattius.http.configuration

import kotlin.time.Duration.Companion.seconds

/**
 * Configuration for HTTP caching.
 *
 * @property enabled Whether caching is enabled (default: false)
 * @property cacheDirectory Custom cache directory name (default: "http_cache")
 * @property maxCacheSize Maximum cache size in bytes (default: 10MB)
 * @property cacheTtl Time-to-live for cache entries in milliseconds (default: 1 hour)
 */
data class CacheConfig(
    val enabled: Boolean = false,
    val cacheDirectory: String = "http_cache",
    val maxCacheSize: Long = 10L * 1024 * 1024, // 10 MB
    val cacheTtl: Long = 60 * 60 * 1000, // 1 hour
    val isShared:Boolean = true
) {
    constructor(enable: Boolean, cacheDirectory: String) : this(
        enabled = enable,
        cacheDirectory = cacheDirectory
    )
}

/**
 * Configuration for the HTTP client.
 *
 * @property baseUrl The base URL for all HTTP requests (e.g., "https://api.example.com")
 * @property connectTimeout Connection timeout in milliseconds (default: 30 seconds)
 * @property socketTimeout Read/write timeout in milliseconds (default: 30 seconds)
 * @property enableLogging Whether to enable request/response logging (default: false)
 * @property logLevel The verbosity level for logging (default: [LogLevel.BASIC])
 * @property cache Configuration for HTTP caching (default: disabled)
 */
data class HttpClientConfig(
    val baseUrl: String,
    val connectTimeout: Long,
    val socketTimeout: Long,
    val enableLogging: Boolean = false,
    val logLevel: LogLevel = LogLevel.BASIC,
    val cache: CacheConfig = CacheConfig()
) {
    /**
     * Creates a new HTTP client configuration with default values.
     *
     * Default values:
     * - connectTimeout: 30 seconds
     * - socketTimeout: 30 seconds
     * - enableLogging: false
     * - logLevel: LogLevel.BASIC
     *
     * @param baseUrl The base URL for all HTTP requests
     */
    /**
     * Creates a new HTTP client configuration with default values.
     *
     * Default values:
     * - connectTimeout: 30 seconds
     * - socketTimeout: 30 seconds
     * - enableLogging: false
     * - logLevel: LogLevel.BASIC
     * - cache: Disabled by default
     *
     * @param baseUrl The base URL for all HTTP requests
     */
    constructor(
        baseUrl: String,
    ) : this(
        baseUrl = baseUrl,
        connectTimeout = 10.seconds.inWholeMilliseconds,
        socketTimeout = 10.seconds.inWholeMilliseconds,
        enableLogging = false,
        logLevel = LogLevel.BASIC
    )

    /**
     * Sets the connection timeout.
     *
     * @param timeout The timeout in milliseconds
     * @return A new [HttpClientConfig] with the updated timeout
     */
    fun connectTimeout(timeout: Long) = copy(connectTimeout = timeout)

    /**
     * Sets the socket (read/write) timeout.
     *
     * @param timeout The timeout in milliseconds
     * @return A new [HttpClientConfig] with the updated timeout
     */
    fun socketTimeout(timeout: Long) = copy(socketTimeout = timeout)

    /**
     * Enables or disables request/response logging.
     *
     * @param enable Whether to enable logging
     * @return A new [HttpClientConfig] with logging enabled or disabled
     */
    fun enableLogging(enable: Boolean) = copy(enableLogging = enable)

    /**
     * Sets the logging level.
     *
     * @param level The desired logging level
     * @return A new [HttpClientConfig] with the updated logging level
     * @see LogLevel For available logging levels
     */
    fun logLevel(level: LogLevel) = copy(logLevel = level)

    /**
     * Sets the cache configuration.
     *
     * @param cacheConfig The cache configuration
     * @return A new [HttpClientConfig] with the updated cache configuration
     */
    fun cache(cacheConfig: CacheConfig) = copy(cache = cacheConfig)
}

/**
 * Defines the verbosity of HTTP request/response logging.
 *
 * This enum is similar to OkHttp's logging levels but adapted for Ktor.
 * The logging levels are hierarchical - each level includes all the information
 * from the previous levels plus additional details.
 *
 * @see HttpClientConfig For configuring the logging level
 */
enum class LogLevel {
    /**
     * No logging.
     *
     * Disables all logging output.
     */
    NONE,

    /**
     * Logs request and response lines.
     *
     * Includes:
     * - Request method and URL
     * - Response status code and message
     * - Request duration
     */
    BASIC,

    /**
     * Logs request and response lines and their respective headers.
     *
     * Includes everything from [BASIC], plus:
     * - Request headers
     * - Response headers
     */
    HEADERS,

    /**
     * Logs request and response lines, headers, and bodies.
     *
     * Includes everything from [HEADERS], plus:
     * - Request body (if present)
     * - Response body (if present)
     *
     * Note: Be cautious when using this level in production as it may log
     * sensitive information such as authentication tokens or personal data.
     */
    BODY
}
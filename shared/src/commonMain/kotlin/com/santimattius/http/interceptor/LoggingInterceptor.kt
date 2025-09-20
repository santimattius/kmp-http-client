package com.santimattius.http.interceptor

import com.santimattius.http.HttpRequest
import com.santimattius.http.HttpResponse
import com.santimattius.http.config.LogLevel
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmOverloads
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Interceptor that logs HTTP request and response information to help with debugging.
 *
 * This interceptor can be configured with different logging levels to control the amount
 * of detail included in the logs. It's particularly useful for:
 * - Debugging API calls during development
 * - Monitoring network traffic
 * - Logging request/response details for troubleshooting
 * - Performance monitoring
 *
 * ## Log Levels
 * - `NONE`: No logging
 * - `BASIC`: Logs request/response lines
 * - `HEADERS`: Logs request/response lines and their headers
 * - `BODY`: Logs request/response lines, headers, and body (if present)
 *
 * ## Example Usage
 * ```kotlin
 * // Create a client with logging
 * val client = HttpClient.create(
 *     HttpClientConfig(
 *         baseUrl = "https://api.example.com"
 *     )
 * ).addInterceptors(
 *     listOf(loggingInterceptor(LogLevel.BODY))
 * )
 * ```
 *
 * @property level The verbosity level for logging (default: [LogLevel.BASIC])
 * @property logger The logging function to use (default: prints to standard output)
 *
 * @see LogLevel For available logging levels
 * @see loggingInterceptor For a convenient factory function
 */
class LoggingInterceptor @JvmOverloads constructor(
    private val level: LogLevel = LogLevel.BASIC,
    private val logger: (String) -> Unit = { println(it) }
) : Interceptor {

    /**
     * JSON formatter used for pretty-printing JSON request/response bodies.
     */
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Intercepts the HTTP request and response to log information according to the configured log level.
     *
     * This method:
     * 1. Logs the outgoing request (if logging is enabled)
     * 2. Proceeds with the request and measures the duration
     * 3. Logs the incoming response (if logging is enabled)
     * 4. Returns the response to the caller
     *
     * @param chain The interceptor chain
     * @return The HTTP response from the server
     *
     */
    @OptIn(ExperimentalTime::class)
    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
        val request = chain.request

        // Log request if logging is enabled
        if (level != LogLevel.NONE) {
            logRequest(request)
        }

        // Execute the request and measure duration
        val startTime = Clock.System.now()
        val response = chain.proceed(request)
        val endTime = Clock.System.now()
        val duration = endTime - startTime

        // Log response if logging is enabled
        if (level != LogLevel.NONE) {
            logResponse(response, duration)
        }

        return response
    }

    /**
     * Logs the details of an HTTP request according to the current log level.
     *
     * The logged information includes:
     * - Request method and URL (all levels)
     * - Request headers (HEADERS and BODY levels)
     * - Request body (BODY level only, if present)
     *
     * @param request The HTTP request to log
     */
    private fun logRequest(request: HttpRequest) {
        // Always log the request line
        logger("--> ${request.method} ${request.url}")

        if (level >= LogLevel.HEADERS) {
            // Log all request headers
            request.headers.forEach { (name, value) ->
                logger("$name: $value")
            }

            // Log request body if present and log level is BODY or higher
            if (level >= LogLevel.BODY && request.body != null) {
                logger("")
                logger(formatBody(request.body))
            }
        }
        logger("--> END ${request.method}")
    }

    /**
     * Logs the details of an HTTP response according to the current log level.
     *
     * The logged information includes:
     * - Response status code, URL, and duration (all levels)
     * - Response headers (HEADERS and BODY levels)
     * - Response body (BODY level only, if present)
     *
     * @param response The HTTP response to log
     * @param duration The duration of the request-response cycle
     */
    private fun logResponse(response: HttpResponse, duration: kotlin.time.Duration) {
        // Always log the response status line with duration
        logger("<-- ${response.status} ${response.url} (${duration.inWholeMilliseconds}ms)")

        if (level >= LogLevel.HEADERS) {
            // Log all response headers
            response.headers.forEach { (name, value) ->
                logger("$name: $value")
            }

            // Log response body if present and log level is BODY or higher
            if (level >= LogLevel.BODY && response.body != null) {
                logger("")
                logger(formatBody(response.body))
            }
        }
        logger("<-- END HTTP")
    }

    /**
     * Formats the request/response body for logging.
     *
     * Handles different types of body content:
     * - Strings: returned as-is
     * - Byte arrays: shows size in bytes
     * - Streams: indicates streaming content
     * - Other objects: attempts JSON serialization, falls back to toString()
     *
     * @param body The body content to format
     * @return A string representation of the body suitable for logging
     */
    private fun formatBody(body: Any?): String {
        return when (body) {
            is String -> body
            is ByteArray -> "[${body.size} bytes]"
            is ByteReadChannel -> "[streaming content]"
            null -> "[null]"
            else -> try {
                // Try to pretty-print JSON if it's a JSON-serializable object
                json.encodeToString(AnyJsonSerializer, body)
            } catch (e: Exception) {
                // Fall back to toString() for non-JSON objects
                body.toString()
            }
        }
    }

    /**
     * A serializer that can handle any JSON-serializable object.
     * Used for pretty-printing request/response bodies.
     */
    private object AnyJsonSerializer :
        JsonTransformingSerializer<Any>(Json.Default.serializersModule.serializer<Any>()) {
        override fun transformDeserialize(element: JsonElement): JsonElement = element
        override fun transformSerialize(element: JsonElement): JsonElement = element
    }
}

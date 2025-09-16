package com.santimattius.http.internal.requests

import com.santimattius.http.HttpRequest
import com.santimattius.http.internal.jsonConfig
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestData
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.takeFrom
import io.ktor.util.appendAll
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.Json

/**
 * Internal utilities for converting between the library's HTTP request model
 * and Ktor's HTTP request model.
 *
 * This package handles the low-level details of request construction and
 * serialization, ensuring compatibility with the Ktor HTTP client.
 */

/**
 * Mapper for converting between the library's [HttpRequest] and Ktor's [HttpRequestData].
 *
 * This object provides functionality to convert the library's high-level HTTP request
 * representation into Ktor's low-level request format that can be processed by the
 * underlying HTTP client.
 *
 * ## Supported Features
 * - URL construction with query parameters
 * - Header management
 * - Request body serialization for different content types:
 *   - String content
 *   - Byte array content
 *   - JSON-serializable objects
 *
 * @see HttpRequest The source request type
 * @see HttpRequestData The target Ktor request type
 */
internal object HttpRequestMapper {

    /**
     * Converts an [HttpRequest] to Ktor's [HttpRequestBuilder].
     *
     * This method performs the following transformations:
     * 1. Builds the complete URL with query parameters
     * 2. Processes request headers
     * 3. Serializes the request body according to content type
     * 4. Constructs a Ktor request with all components
     *
     * @param request The [HttpRequest] to convert
     * @return A [Result] containing either the configured [HttpRequestBuilder] or an exception
     *         if the conversion fails
     * @throws IllegalArgumentException if the HTTP method is invalid
     * @throws Exception if there's an error during request construction
     *
     */
    @OptIn(InternalAPI::class)
    fun toKtorRequest(request: HttpRequest): Result<HttpRequestBuilder> = runCatching {
        // Build URL with query parameters
        val url = buildUrl(request.url, request.queryParameters)
        
        // Process headers
        val headers = buildHeaders(request.headers)
        
        // Serialize request body if present
        val body = request.body?.let { buildBody(it, headers) }
        
        // Construct and return the Ktor request
        HttpRequestBuilder().apply {
            this.url.takeFrom(url)
            this.method = HttpMethod.parse(request.method.name.uppercase())
            this.headers.appendAll(headers)
            this.body = body ?: EmptyContent
        }
    }

    /**
     * Builds a URL with query parameters.
     *
     * @param baseUrl The base URL without query parameters
     * @param queryParams Map of query parameters to append to the URL
     * @return A properly encoded [Url] with query parameters
     */
    private fun buildUrl(baseUrl: String, queryParams: Map<String, String>): Url {
        return URLBuilder(baseUrl).apply {
            if (queryParams.isNotEmpty()) {
                parameters.appendAll(
                    ParametersBuilder().apply {
                        queryParams.forEach { (key, value) ->
                            append(key, value)
                        }
                    }
                )
            }
        }.build()
    }

    /**
     * Converts a map of headers into Ktor's [Headers] object.
     *
     * @param headers Map of header names to header values
     * @return A [Headers] instance containing all provided headers
     */
    private fun buildHeaders(headers: Map<String, String>): Headers {
        return HeadersBuilder().apply {
            headers.forEach { (key, value) ->
                append(key, value)
            }
        }.build()
    }

    /**
     * Creates an [OutgoingContent] instance from the request body.
     *
     * This method handles different types of request bodies:
     * - [String]: Sent as plain text or content type specified in headers
     * - [ByteArray]: Sent as binary data with octet-stream content type by default
     * - Other objects: Serialized as JSON with application/json content type by default
     *
     * @param body The request body to send
     * @param headers The request headers (used to determine content type)
     * @return An [OutgoingContent] instance representing the request body
     * @throws Exception if body serialization fails
     */
    private fun buildBody(body: Any, headers: Headers): OutgoingContent {
        return when (body) {
            is String -> {
                TextContent(
                    text = body,
                    contentType = headers[HttpHeaders.ContentType]?.let { ContentType.parse(it) }
                        ?: ContentType.Text.Plain
                )
            }

            is ByteArray -> {
                ByteArrayContent(
                    bytes = body,
                    contentType = headers[HttpHeaders.ContentType]?.let { ContentType.parse(it) }
                        ?: ContentType.Application.OctetStream
                )
            }

            else -> {
                // Default to JSON serialization for other types
                TextContent(
                    text = jsonConfig.encodeToString(body),
                    contentType = headers[HttpHeaders.ContentType]?.let { ContentType.parse(it) }
                        ?: ContentType.Application.Json
                )
            }
        }
    }

    /**
     * Implementation of [OutgoingContent.ByteArrayContent] for sending binary data.
     *
     * @property bytes The binary data to send
     * @property contentType The content type of the binary data
     */
    private class ByteArrayContent(
        private val bytes: ByteArray,
        override val contentType: ContentType
    ) : OutgoingContent.ByteArrayContent() {
        /**
         * Returns the binary data to be sent in the request body.
         *
         * @return The byte array containing the request body
         */
        override fun bytes(): ByteArray = bytes
    }
}


/**
 * Extension function to convert an [HttpRequest] to Ktor's [HttpRequestBuilder].
 *
 * This is a convenience method that delegates to [HttpRequestMapper.toKtorRequest].
 *
 * @return A [Result] containing either the configured [HttpRequestBuilder] or an exception
 *         if the conversion fails.
 * @see HttpRequestMapper.toKtorRequest For the underlying implementation.
 */
internal fun HttpRequest.toKtorRequest(): Result<HttpRequestBuilder> =
    HttpRequestMapper.toKtorRequest(this)

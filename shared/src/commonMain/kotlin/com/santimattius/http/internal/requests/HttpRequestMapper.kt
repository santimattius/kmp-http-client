package com.santimattius.http.internal.requests

import com.santimattius.http.HttpRequest
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
 * Maps [HttpRequest] to Ktor's [HttpRequestData].
 */
object HttpRequestMapper {

    /**
     * Converts an [HttpRequest] to Ktor's [HttpRequestData].
     *
     * @param request The [HttpRequest] to convert
     * @return A [Result] containing either the [HttpRequestData] or an exception
     */
    @OptIn(InternalAPI::class)
    fun toKtorRequest(request: HttpRequest): Result<HttpRequestBuilder> = runCatching {
        val url = buildUrl(request.url, request.queryParameters)
        val headers = buildHeaders(request.headers)
        val body = request.body?.let { buildBody(it, headers) }
        HttpRequestBuilder().apply {
            this.url.takeFrom(url)
            this.method = HttpMethod.parse(request.method)
            this.headers.appendAll(headers)
            this.body = body ?: EmptyContent
        }
    }

    private fun buildUrl(baseUrl: String, queryParams: Map<String, String>): Url {
        return URLBuilder(baseUrl).apply {
            parameters.appendAll(
                ParametersBuilder().apply {
                    queryParams.forEach { (key, value) ->
                        append(key, value)
                    }
                }
            )
        }.build()
    }

    private fun buildHeaders(headers: Map<String, String>): Headers {
        return HeadersBuilder().apply {
            headers.forEach { (key, value) ->
                append(key, value)
            }
        }.build()
    }

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
                // Try to serialize as JSON if no content type is specified
                val json = Json { ignoreUnknownKeys = true }
                TextContent(
                    text = json.encodeToString(body),
                    contentType = headers[HttpHeaders.ContentType]?.let { ContentType.parse(it) }
                        ?: ContentType.Application.Json
                )
            }
        }
    }

    private class ByteArrayContent(
        private val bytes: ByteArray,
        override val contentType: ContentType
    ) : OutgoingContent.ByteArrayContent() {
        override fun bytes(): ByteArray = bytes
    }
}

/**
 * Extension function to convert [HttpRequest] to [HttpRequestData].
 *
 * @return A [Result] containing either the [HttpRequestData] or an exception
 */
fun HttpRequest.toKtorRequest(): Result<HttpRequestBuilder> =
    HttpRequestMapper.toKtorRequest(this)

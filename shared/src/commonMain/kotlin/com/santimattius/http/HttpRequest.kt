package com.santimattius.http

/**
 * Represents an HTTP request with common properties and methods.
 * Use the companion object's builder methods to create instances.
 */
data class HttpRequest(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val queryParameters: Map<String, String>,
    val body: Any?
) {
    /**
     * Builder class for creating [HttpRequest] instances.
     */
    /**
     * Base builder class with common request properties.
     */
    open class Builder internal constructor(internal val method: String) {
        internal var url: String = ""
        internal val headers = mutableMapOf<String, String>()
        internal val queryParameters = mutableMapOf<String, String>()
        internal var body: Any? = null

        /**
         * Sets the URL for the request.
         */
        fun url(url: String) = apply { this.url = url }

        /**
         * Adds a single header to the request.
         */
        fun header(name: String, value: String) = apply {
            headers[name] = value
        }

        /**
         * Adds multiple headers to the request.
         */
        fun headers(headers: Map<String, String>) = apply {
            this.headers.putAll(headers)
        }

        /**
         * Adds a query parameter to the URL.
         */
        fun queryParam(name: String, value: String) = apply {
            queryParameters[name] = value
        }

        /**
         * Adds multiple query parameters to the URL.
         */
        fun queryParams(params: Map<String, String>) = apply {
            queryParameters.putAll(params)
        }

        /**
         * Builds the [HttpRequest] instance.
         * @throws IllegalArgumentException if URL is not provided
         */
        fun build(): HttpRequest {
            require(url.isNotBlank()) { "URL must not be blank" }
            return HttpRequest(method, url, headers, queryParameters, body)
        }
    }

    /**
     * Builder for HTTP methods that support request bodies (POST, PUT, PATCH).
     */
    class BodyAwareBuilder internal constructor(method: String) : Builder(method) {
        /**
         * Sets the request body.
         */
        fun body(body: Any?) = apply {
            this.body = body
        }
    }

    companion object {
        /**
         * Creates a new GET request builder.
         */
        fun get() = Builder("GET")

        /**
         * Creates a new POST request builder with body support.
         */
        fun post() = BodyAwareBuilder("POST")

        /**
         * Creates a new PUT request builder with body support.
         */
        fun put() = BodyAwareBuilder("PUT")

        /**
         * Creates a new DELETE request builder.
         */
        fun delete() = Builder("DELETE")

        /**
         * Creates a new PATCH request builder with body support.
         */
        fun patch() = BodyAwareBuilder("PATCH")

        /**
         * Creates a new HEAD request builder.
         */
        fun head() = Builder("HEAD")

        /**
         * Creates a new OPTIONS request builder.
         */
        fun options() = Builder("OPTIONS")
    }

    override fun toString(): String {
        return "HttpRequest(method='$method', url='$url', headers=$headers, queryParameters=$queryParameters, body=$body)"
    }
}

/**
 * Represents an HTTP response.
 */
class HttpResponse(
    val url: String = "",
    val status: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null
) {
    val isSuccessful: Boolean
        get() = status in 200..299

    override fun toString(): String {
        return "HttpResponse(status=$status, headers=$headers, body=$body)"
    }
}


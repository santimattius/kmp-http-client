package com.santimattius.http

/**
 * Represents an HTTP request with common properties and methods.
 * Use the companion object's builder methods to create instances.
 */
data class HttpRequest(
    val method: HttpMethod,
    val url: String,
    val headers: Map<String, String>,
    val queryParameters: Map<String, String>,
    val body: Any?
) {

    fun newBuilder(): Builder {
        return when (method) {
            HttpMethod.Get -> get(url)
            HttpMethod.Post -> post(url).body(body)
            HttpMethod.Put -> put(url).body(body)
            HttpMethod.Delete -> delete(url)
            HttpMethod.Options -> options(url)
            HttpMethod.Head -> head(url)
            HttpMethod.Patch -> patch(url).body(body)
        }.headers(headers)
            .queryParams(queryParameters)
    }
    /**
     * Builder class for creating [HttpRequest] instances.
     */
    /**
     * Base builder class with common request properties.
     */
    open class Builder internal constructor(
        internal val method: HttpMethod,
        internal var url: String
    ) {

        internal val headers = mutableMapOf<String, String>()
        internal val queryParameters = mutableMapOf<String, String>()
        internal var body: Any? = null

        /**
         * Adds a single header to the request.
         */
        open fun header(name: String, value: String) = apply {
            headers[name] = value
        }

        /**
         * Adds multiple headers to the request.
         */
        open fun headers(headers: Map<String, String>) = apply {
            this.headers.putAll(headers)
        }

        /**
         * Adds a query parameter to the URL.
         */
        open fun queryParam(name: String, value: String) = apply {
            queryParameters[name] = value
        }

        /**
         * Adds multiple query parameters to the URL.
         */
        open fun queryParams(params: Map<String, String>) = apply {
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
    class BodyAwareBuilder internal constructor(method: HttpMethod, url: String) :
        Builder(method, url) {
        /**
         * Sets the request body.
         */
        fun body(body: Any?) = apply {
            this.body = body
        }

        override fun queryParam(name: String, value: String): BodyAwareBuilder = apply {
            super.queryParam(name, value)
        }

        override fun queryParams(params: Map<String, String>): BodyAwareBuilder = apply {
            super.queryParams(params)
        }

        override fun header(name: String, value: String): BodyAwareBuilder = apply {
            super.header(name, value)
        }

        override fun headers(headers: Map<String, String>): BodyAwareBuilder = apply {
            super.headers(headers)
        }

    }

    companion object {
        //TODO: add url into method
        /**
         * Creates a new GET request builder.
         */
        fun get(url: String) = Builder(HttpMethod.Get, url)

        /**
         * Creates a new POST request builder with body support.
         */
        fun post(url: String) = BodyAwareBuilder(HttpMethod.Post, url)

        /**
         * Creates a new PUT request builder with body support.
         */
        fun put(url: String) = BodyAwareBuilder(HttpMethod.Put, url)

        /**
         * Creates a new DELETE request builder.
         */
        fun delete(url: String) = Builder(HttpMethod.Delete, url)

        /**
         * Creates a new PATCH request builder with body support.
         */
        fun patch(url: String) = BodyAwareBuilder(HttpMethod.Patch, url)

        /**
         * Creates a new HEAD request builder.
         */
        fun head(url: String) = Builder(HttpMethod.Head, url)

        /**
         * Creates a new OPTIONS request builder.
         */
        fun options(url: String) = Builder(HttpMethod.Options, url)
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


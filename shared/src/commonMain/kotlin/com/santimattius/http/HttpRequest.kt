package com.santimattius.http

/**
 * Represents an HTTP request with common properties and methods.
 *
 * This class encapsulates all the necessary components of an HTTP request including:
 * - HTTP method (GET, POST, PUT, etc.)
 * - Target URL
 * - Request headers
 * - Query parameters
 * - Request body (optional)
 *
 * Instances of this class are immutable. To modify a request, use the [newBuilder] method
 * to create a modified copy.
 *
 * @property method The HTTP method for this request
 * @property url The target URL for this request
 * @property headers Map of HTTP headers to include in the request
 * @property queryParameters Map of query parameters to append to the URL
 * @property body The request body, or null if not applicable for the request method
 */
data class HttpRequest(
    val method: HttpMethod,
    val url: String,
    val headers: Map<String, String>,
    val queryParameters: Map<String, String>,
    val body: Any?
) {

    /**
     * Creates a new [Builder] instance initialized with this request's properties.
     * This allows for easy modification of an existing request by creating a modified copy.
     *
     * @return A new [Builder] instance with all properties copied from this request
     */
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
     * Builder class for creating and configuring [HttpRequest] instances.
     *
     * This builder provides a fluent API for constructing HTTP requests with various
     * configurations. It supports setting headers, query parameters, and request bodies
     * in a type-safe and builder-pattern style.
     *
     * Example usage:
     * ```kotlin
     * val request = HttpRequest.get("https://api.example.com/data")
     *     .header("Accept", "application/json")
     *     .queryParam("page", "1")
     *     .build()
     * ```
     */
    open class Builder internal constructor(
        internal val method: HttpMethod,
        internal var url: String
    ) {

        internal var path: String = ""

        /** Internal storage for request headers */
        internal val headers = mutableMapOf<String, String>()

        /** Internal storage for URL query parameters */
        internal val queryParameters = mutableMapOf<String, String>()

        /** Internal storage for the request body */
        internal var body: Any? = null


        open fun path(path: String) = apply {
            this.path = path
        }
        /**
         * Adds a single header to the request.
         *
         * @param name The name of the header (case-insensitive)
         * @param value The value of the header
         * @return This builder instance for method chaining
         */
        open fun header(name: String, value: String) = apply {
            headers[name] = value
        }

        /**
         * Adds multiple headers to the request.
         *
         * @param headers A map of header names to values to add
         * @return This builder instance for method chaining
         */
        open fun headers(headers: Map<String, String>) = apply {
            this.headers.putAll(headers)
        }

        /**
         * Adds a single query parameter to the request URL.
         *
         * @param name The name of the query parameter
         * @param value The value of the query parameter (will be URL-encoded)
         * @return This builder instance for method chaining
         */
        open fun queryParam(name: String, value: String) = apply {
            queryParameters[name] = value
        }

        /**
         * Adds multiple query parameters to the request URL.
         *
         * @param params A map of parameter names to values (values will be URL-encoded)
         * @return This builder instance for method chaining
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
            if (path.isNotBlank()) {
                url = "$url$path"
            }
            return HttpRequest(method, url, headers, queryParameters, body)
        }
    }

    /**
     * Builder for HTTP methods that support request bodies (POST, PUT, PATCH).
     *
     * This specialized builder extends the base [Builder] to add support for request bodies,
     * which are required for certain HTTP methods like POST and PUT.
     *
     * @property method The HTTP method (POST, PUT, or PATCH)
     * @property url The target URL for the request
     */
    class BodyAwareBuilder internal constructor(method: HttpMethod, url: String) :
        Builder(method, url) {

        override fun path(path: String) = apply {
            super.path(path)
        }
        /**
         * Sets the request body.
         *
         * The body can be of any type, but typically it's one of:
         * - String: Sent as plain text
         * - ByteArray: Sent as raw bytes
         * - Any other type: Will be serialized to JSON if a JSON serializer is configured
         *
         * @param body The request body content
         * @return This builder instance for method chaining
         */
        fun body(body: Any?) = apply {
            this.body = body
        }

        /**
         * Adds a query parameter to the URL.
         *
         * @param name The name of the query parameter
         * @param value The value of the query parameter (will be URL-encoded)
         * @return This builder instance for method chaining
         */
        override fun queryParam(name: String, value: String): BodyAwareBuilder = apply {
            super.queryParam(name, value)
        }

        /**
         * Adds multiple query parameters to the URL.
         *
         * @param params A map of parameter names to values (values will be URL-encoded)
         * @return This builder instance for method chaining
         */
        override fun queryParams(params: Map<String, String>): BodyAwareBuilder = apply {
            super.queryParams(params)
        }

        /**
         * Adds a single header to the request.
         *
         * @param name The name of the header (case-insensitive)
         * @param value The value of the header
         * @return This builder instance for method chaining
         */
        override fun header(name: String, value: String): BodyAwareBuilder = apply {
            super.header(name, value)
        }

        /**
         * Adds multiple headers to the request.
         *
         * @param headers A map of header names to values to add
         * @return This builder instance for method chaining
         */
        override fun headers(headers: Map<String, String>): BodyAwareBuilder = apply {
            super.headers(headers)
        }

    }

    companion object {
        /**
         * Creates a new GET request builder.
         *
         * GET requests are used to retrieve data from a server.
         * They should not have a request body.
         *
         * @param url The target URL for the request
         * @return A new [Builder] instance configured for a GET request
         */
        fun get(url: String) = Builder(HttpMethod.Get, url)

        /**
         * Creates a new POST request builder with body support.
         *
         * POST requests are used to submit data to a server, typically causing a change in state.
         *
         * @param url The target URL for the request
         * @return A new [BodyAwareBuilder] instance configured for a POST request
         */
        fun post(url: String) = BodyAwareBuilder(HttpMethod.Post, url)

        /**
         * Creates a new PUT request builder with body support.
         *
         * PUT requests are used to update an existing resource or create it if it doesn't exist.
         *
         * @param url The target URL for the request
         * @return A new [BodyAwareBuilder] instance configured for a PUT request
         */
        fun put(url: String) = BodyAwareBuilder(HttpMethod.Put, url)

        /**
         * Creates a new DELETE request builder.
         *
         * DELETE requests are used to remove a resource from the server.
         *
         * @param url The target URL for the request
         * @return A new [Builder] instance configured for a DELETE request
         */
        fun delete(url: String) = Builder(HttpMethod.Delete, url)

        /**
         * Creates a new PATCH request builder with body support.
         *
         * PATCH requests are used to apply partial modifications to a resource.
         *
         * @param url The target URL for the request
         * @return A new [BodyAwareBuilder] instance configured for a PATCH request
         */
        fun patch(url: String) = BodyAwareBuilder(HttpMethod.Patch, url)

        /**
         * Creates a new HEAD request builder.
         *
         * HEAD requests are identical to GET requests but without the response body.
         *
         * @param url The target URL for the request
         * @return A new [Builder] instance configured for a HEAD request
         */
        fun head(url: String) = Builder(HttpMethod.Head, url)

        /**
         * Creates a new OPTIONS request builder.
         *
         * OPTIONS requests are used to describe the communication options for the target resource.
         *
         * @param url The target URL for the request
         * @return A new [Builder] instance configured for an OPTIONS request
         */
        fun options(url: String) = Builder(HttpMethod.Options, url)
    }

    override fun toString(): String {
        return "HttpRequest(method='$method', url='$url', headers=$headers, queryParameters=$queryParameters, body=$body)"
    }
}


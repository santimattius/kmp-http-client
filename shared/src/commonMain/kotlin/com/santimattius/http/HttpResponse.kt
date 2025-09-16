package com.santimattius.http

/**
 * Represents an HTTP response received from a server.
 *
 * This class encapsulates all aspects of an HTTP response including:
 * - The HTTP status code
 * - Response headers
 * - Response body (if any)
 * - The original request URL
 *
 * @property url The URL that was requested to generate this response
 * @property status The HTTP status code (e.g., 200, 404, 500)
 * @property headers Map of response headers (header name to value)
 * @property body The response body as a String, or null if there was no body
 */
class HttpResponse(
    val url: String = "",
    val status: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
) {
    /**
     * Indicates whether the request was successful.
     *
     * A response is considered successful if its status code is in the range [200..299].
     *
     * @return `true` if the status code is in the 200-299 range, `false` otherwise
     */
    val isSuccessful: Boolean
        get() = status in 200..299

    /**
     * Returns a string representation of the HTTP response.
     *
     * The string includes the status code, headers, and a preview of the body (if available).
     * For large response bodies, only the first 100 characters are shown.
     *
     * @return A string representation of the response
     */
    override fun toString(): String {
        val bodyPreview = body?.let {
            if (it.length > 100) "${it.take(100)}... [${it.length - 100} more characters]" else it
        } ?: "null"
        return "HttpResponse(url='$url', status=$status, headers=$headers, body=$bodyPreview)"
    }
}
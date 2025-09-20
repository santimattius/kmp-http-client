package com.santimattius.http.interceptor

import com.santimattius.http.HttpResponse
import com.santimattius.http.exception.statusCodeToException

/**
 * Interceptor that automatically handles HTTP error responses by throwing appropriate exceptions.
 *
 * This interceptor checks the HTTP status code of each response and throws a corresponding
 * exception for error status codes (400-599). This allows for consistent error handling
 * throughout the application.
 *
 * ## Features
 * - Converts HTTP error responses to appropriate exceptions
 * - Includes the error response body in the exception when available
 * - Works with any HTTP client that implements the [Interceptor] interface
 *
 * ## Example Usage
 * ```kotlin
 * // Create a client with error handling
 * val client = HttpClient.create(
 *     HttpClientConfig(
 *         baseUrl = "https://api.example.com"
 *     )
 * ).addInterceptors(
 *     listOf(ErrorHandlingInterceptor())
 * )
 *
 * // Later in your code:
 * try {
 *     val response = client.execute(someRequest)
 *     // Handle successful response
 * } catch (e: HttpException) {
 *     // Handle specific HTTP errors (e.g., 404, 500, etc.)
 *     when (e) {
 *         is NotFoundException -> handleNotFound()
 *         is UnauthorizedException -> handleUnauthorized()
 *         // ... handle other error types
 *     }
 * }
 * ```
 *
 * @see com.santimattius.http.exception For the hierarchy of HTTP exceptions
 * @see statusCodeToException For how status codes are mapped to exceptions
 */
class ErrorHandlingInterceptor : Interceptor {

    /**
     * Intercepts the HTTP request/response chain to handle error status codes.
     *
     * This method:
     * 1. Proceeds with the request to get the response
     * 2. Checks if the response has an error status code (400-599)
     * 3. If it's an error, throws an appropriate exception
     * 4. Otherwise, returns the response to the next interceptor
     *
     * @param chain The interceptor chain
     * @return The HTTP response if it's successful (status code < 400)
     * @throws com.santimattius.http.exception.HttpException If the response has an error status code
     *
     */
    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
        // Proceed with the request to get the response
        val response = chain.proceed(chain.request)

        // Check for error status codes (400-599)
        if (response.status in 400..599) {
            val errorMessage =
                "HTTP ${response.status}: ${response.body?.takeIf { it.isNotBlank() } ?: "No error details"}"
            throw statusCodeToException(
                code = response.status,
                errorBody = response.body,
                message = errorMessage
            )
        }

        return response
    }
}
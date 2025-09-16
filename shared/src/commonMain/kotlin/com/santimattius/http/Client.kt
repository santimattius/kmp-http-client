package com.santimattius.http

import com.santimattius.http.interceptor.Interceptor

/**
 * Interface defining the contract for an HTTP client.
 *
 * This interface represents a client capable of executing HTTP requests and receiving responses.
 * It supports request/response interception and follows the [AutoCloseable] pattern for resource cleanup.
 *
 * Implementations of this interface should be thread-safe and support concurrent requests.
 *
 * Example usage:
 * ```kotlin
 * val client: Client = // obtain client instance
 * val request = HttpRequest.get("https://api.example.com/data").build()
 * val response = client.execute(request)
 * if (response.isSuccessful) {
 *     // Handle successful response
 * } else {
 *     // Handle error response
 * }
 * ```
 *
 * @see HttpClient For creating and managing client instances
 * @see HttpRequest For building requests
 * @see HttpResponse For handling responses
 */
interface Client : AutoCloseable {

    /**
     * Executes an HTTP request and returns the response.
     *
     * This is a suspending function that should be called from a coroutine or another suspending function.
     * The function will suspend until the request is complete and a response is received.
     *
     * @param request The HTTP request to execute. Must be a valid request with a non-empty URL.
     * @return The HTTP response containing status code, headers, and body
     * @throws com.santimattius.http.exception.HttpException if the request fails due to network issues,
     *         timeouts, or other I/O errors
     * @throws IllegalArgumentException if the request is invalid (e.g., malformed URL)
     */
    suspend fun execute(request: HttpRequest): HttpResponse

    /**
     * Creates a new client with additional interceptors.
     *
     * This method returns a new [Client] instance that includes all existing interceptors
     * plus any new ones specified. The original client remains unchanged.
     *
     * @param interceptors The list of interceptors to add to the new client instance
     * @return A new [Client] instance with the combined interceptors
     *
     * @see Interceptor For more information about interceptors and their capabilities
     *
     * Example:
     * ```kotlin
     * val clientWithLogging = client.addInterceptors(listOf(LoggingInterceptor()))
     * ```
     */
    fun addInterceptors(interceptors: List<Interceptor>): Client

}
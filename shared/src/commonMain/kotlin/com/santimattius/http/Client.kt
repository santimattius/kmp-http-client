package com.santimattius.http

import com.santimattius.http.exception.BadRequestException
import com.santimattius.http.exception.ClientException
import com.santimattius.http.exception.ForbiddenException
import com.santimattius.http.exception.HttpErrorException
import com.santimattius.http.exception.HttpException
import com.santimattius.http.exception.InternalServerErrorException
import com.santimattius.http.exception.NetworkException
import com.santimattius.http.exception.NotFoundException
import com.santimattius.http.exception.ParseException
import com.santimattius.http.exception.ServiceUnavailableException
import com.santimattius.http.exception.TimeoutException
import com.santimattius.http.exception.UnauthorizedException
import com.santimattius.http.interceptor.Interceptor
import kotlinx.coroutines.CancellationException

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
     * @throws IllegalArgumentException if the request is invalid (e.g., malformed URL)
     * @throws NetworkException if network connectivity fails
     * @throws TimeoutException if the request times out
     * @throws ParseException if response parsing fails
     * @throws BadRequestException for HTTP 400 errors
     * @throws UnauthorizedException for HTTP 401 errors
     * @throws ForbiddenException for HTTP 403 errors
     * @throws NotFoundException for HTTP 404 errors
     * @throws InternalServerErrorException for HTTP 500 errors
     * @throws ServiceUnavailableException for HTTP 503 errors
     * @throws HttpErrorException for other HTTP error codes (4xx, 5xx)
     * @throws HttpException for other HTTP-related errors
     * @throws ClientException for other client-side errors
     * @throws CancellationException if the coroutine is cancelled
     */
    @Throws(
        IllegalArgumentException::class,
        // Network & Timeout
        NetworkException::class,
        TimeoutException::class,
        // Parsing
        ParseException::class,
        // HTTP 4xx Errors
        BadRequestException::class,
        UnauthorizedException::class,
        ForbiddenException::class,
        NotFoundException::class,
        // HTTP 5xx Errors
        InternalServerErrorException::class,
        ServiceUnavailableException::class,
        // Generic HTTP Error
        HttpErrorException::class,
        // Base exceptions
        HttpException::class,
        ClientException::class,
        // Coroutines
        CancellationException::class
    )
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
    fun addInterceptors(interceptor: Interceptor): Client {
        return addInterceptors(listOf(interceptor))
    }

}
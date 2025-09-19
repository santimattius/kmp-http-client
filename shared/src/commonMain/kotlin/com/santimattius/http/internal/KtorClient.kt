package com.santimattius.http.internal

import com.santimattius.http.Client
import com.santimattius.http.HttpRequest
import com.santimattius.http.HttpResponse
import com.santimattius.http.config.HttpClientConfig
import com.santimattius.http.interceptor.Interceptor
import com.santimattius.http.internal.requests.toKtorRequest
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.util.toMap

/**
 * Internal implementation of the [Client] interface using Ktor's HTTP client.
 *
 * This class is responsible for:
 * - Managing the underlying Ktor HTTP client instance
 * - Executing HTTP requests through the interceptor chain
 * - Converting between the library's request/response models and Ktor's models
 * - Handling request lifecycle and resource cleanup
 *
 * @see Client For the public interface that this class implements
 * @see KtorHttpClient For the underlying Ktor HTTP client implementation
 */

/**
 * Ktor-based implementation of the [Client] interface.
 *
 * This class serves as the main entry point for executing HTTP requests.
 * It manages the lifecycle of the underlying Ktor HTTP client and coordinates
 * the interceptor chain for processing requests and responses.
 *
 * @property config The configuration for the HTTP client
 * @property interceptors List of interceptors to apply to requests/responses
 *
 * @constructor Creates a new instance with the specified configuration and interceptors
 */
internal class KtorClient private constructor(
    private val config: HttpClientConfig,
    private val interceptors: List<Interceptor> = emptyList()
) : Client {

    /**
     * Lazily initialized Ktor HTTP client.
     *
     * The client is created on first use using the provided configuration.
     * This ensures that resources are only allocated when actually needed.
     */
    private val client: KtorHttpClient by lazy { createKtorClient(config) }

    /**
     * Executes an HTTP request through the interceptor chain.
     *
     * This method:
     * 1. Creates an interceptor chain with the provided request
     * 2. Processes the request through all interceptors
     * 3. Returns the final response
     *
     * @param request The HTTP request to execute
     * @return The HTTP response
     * @throws Exception if the request fails or if any interceptor throws an exception
     */
    override suspend fun execute(request: HttpRequest): HttpResponse {
        val chain = RealInterceptorChain(
            request = request,
            interceptors = interceptors,
            index = 0,
            call = { req -> executeKtorRequest(req) }
        )
        return chain.proceed(request)
    }

    /**
     * Creates a new client instance with additional interceptors.
     *
     * This method returns a new [KtorClient] instance that shares the same configuration
     * but includes the additional interceptors. The original client remains unchanged.
     *
     * @param interceptors The interceptors to add
     * @return A new client instance with the combined interceptors
     */
    override fun addInterceptors(interceptors: List<Interceptor>): Client {
        return KtorClient(
            config = config,
            interceptors = this.interceptors + interceptors
        )
    }

    /**
     * Executes a request using the underlying Ktor client.
     *
     * This method converts the library's [HttpRequest] to a Ktor request,
     * executes it using the Ktor client, and converts the response back
     * to the library's [HttpResponse] format.
     *
     * @param request The request to execute
     * @return The HTTP response
     * @throws Exception if the request fails or if the response cannot be processed
     */
    private suspend fun executeKtorRequest(request: HttpRequest): HttpResponse {
        val requestBuilder = request.newBuilder()
        if (request.url.startsWith("/")) {
            requestBuilder.url = config.baseUrl + request.url
        }
        val httpRequestBuilder = requestBuilder.build().toKtorRequest().getOrThrow()
        return client.request(builder = httpRequestBuilder).toHttpResponse()
    }


    /**
     * Converts a Ktor HTTP response to the library's [HttpResponse] format.
     *
     * This extension function extracts the relevant information from a Ktor response
     * and creates a simplified response object with the status code, headers, and body.
     *
     * @return An [HttpResponse] containing the response data
     */
    private suspend fun io.ktor.client.statement.HttpResponse.toHttpResponse(): HttpResponse {
        return HttpResponse(
            url = request.url.toString(),
            status = status.value,
            headers = headers.toMap().mapValues { it.value.joinToString(",") },
            body = try {
                bodyAsText()
            } catch (e: Exception) {
                null
            }
        )
    }


    /**
     * Closes the underlying HTTP client and releases all associated resources.
     *
     * After calling this method, the client should not be used anymore.
     */
    override fun close() {
        client.close()
    }

    companion object {
        /**
         * Creates a new instance of [KtorClient] with the specified configuration and interceptors.
         *
         * @param config The HTTP client configuration
         * @param interceptors Optional list of interceptors to apply to requests/responses
         * @return A new [Client] instance
         */
        fun create(
            config: HttpClientConfig,
            interceptors: List<Interceptor> = emptyList()
        ): Client {
            return KtorClient(config, interceptors)
        }
    }
}

/**
 * Implementation of the interceptor chain used to process HTTP requests and responses.
 *
 * This class manages the execution of interceptors in sequence, allowing each interceptor
 * to modify the request and/or response as needed.
 *
 * @property request The current HTTP request
 * @property interceptors The list of interceptors to process
 * @property index The current position in the interceptor chain
 * @property call The terminal call that executes the final request
 */
private class RealInterceptorChain(
    override val request: HttpRequest,
    private val interceptors: List<Interceptor>,
    private val index: Int,
    private val call: suspend (HttpRequest) -> HttpResponse
) : Interceptor.Chain {

    /**
     * Proceeds with the request by either:
     * 1. Passing it to the next interceptor in the chain, or
     * 2. Executing the final request if there are no more interceptors
     *
     * @param request The request to process
     * @return The HTTP response
     */
    override suspend fun proceed(request: HttpRequest): HttpResponse {
        // If we've reached the end of the interceptor chain, execute the request
        if (index >= interceptors.size) return call(request)

        // Create the next chain in the sequence
        val next = RealInterceptorChain(
            request = request,
            interceptors = interceptors,
            index = index + 1,
            call = call
        )

        // Get the current interceptor and process the request
        val interceptor = interceptors[index]
        return interceptor.intercept(next)
    }
}
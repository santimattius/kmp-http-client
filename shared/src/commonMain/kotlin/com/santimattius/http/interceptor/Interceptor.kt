package com.santimattius.http.interceptor

import com.santimattius.http.HttpRequest
import com.santimattius.http.HttpResponse

/**
 * Interface for intercepting and potentially transforming HTTP requests and responses.
 *
 * Interceptors can be used to:
 * - Add common headers to requests
 * - Log request/response data
 * - Handle authentication and token refresh
 * - Implement retry logic
 * - Modify requests/responses on the fly
 * - Measure network performance
 *
 * ## Implementation Guidelines
 * - Interceptors are called in the order they are added to the client
 * - Each interceptor can modify the request before it's sent and the response before it's returned
 * - Interceptors should be stateless and thread-safe
 * - Consider using [Chain.proceed] to continue to the next interceptor in the chain
 *
 * @see com.santimattius.http.Client.addInterceptors For adding interceptors to a client
 * @see LoggingInterceptor For a simple logging interceptor implementation
 * @see ErrorHandlingInterceptor For error handling patterns
 */
interface Interceptor {

    /**
     * Intercepts an HTTP request and returns a response.
     *
     * Implementations of this method can:
     * 1. Inspect and modify the request via [Chain.request]
     * 2. Optionally short-circuit the chain by returning a response directly
     * 3. Continue the chain by calling [Chain.proceed]
     * 4. Inspect and modify the response before returning it
     *
     * @param chain The interceptor chain that holds the current request and allows proceeding to the next interceptor
     * @return The HTTP response, which may have been modified by this or subsequent interceptors
     *
     * @sample com.santimattius.http.interceptor.samples.interceptorSample
     *
     * @throws Exception if an error occurs during request processing
     */
    suspend fun intercept(chain: Chain): HttpResponse

    /**
     * Chain of interceptors that processes a single HTTP request and response.
     *
     * Each interceptor in the chain must call [proceed] exactly once, unless it decides
     * to short-circuit the chain by returning a response directly.
     */
    interface Chain {
        /**
         * The current request being processed.
         *
         * This may have been modified by previous interceptors in the chain.
         */
        val request: HttpRequest

        /**
         * Proceeds with the request, calling the next interceptor in the chain.
         *
         * This method allows interceptors to:
         * 1. Modify the request before sending it
         * 2. Inspect and modify the response after it's received
         * 3. Handle any exceptions that occur during the request
         *
         * @param request The request to proceed with (can be modified by interceptors)
         * @return The HTTP response, which may have been modified by subsequent interceptors
         * @throws Exception if the request fails or a subsequent interceptor throws an exception
         *
         */
        suspend fun proceed(request: HttpRequest): HttpResponse
    }
}
package com.santimattius.http.interceptor

import com.santimattius.http.HttpRequest
import com.santimattius.http.HttpResponse

/**
 * Interface for intercepting HTTP requests and responses.
 * Similar to OkHttp's Interceptor but designed for Ktor.
 */
interface Interceptor {

    /**
     * Intercepts an HTTP request and optionally returns a response.
     *
     * @param chain The interceptor chain
     * @return The HTTP response
     */
    suspend fun intercept(chain: Chain): HttpResponse

    /**
     * Chain of interceptors and the final HTTP call.
     */
    interface Chain {
        /**
         * The request being processed.
         */
        val request: HttpRequest

        /**
         * Proceeds with the request, calling the next interceptor in the chain.
         *
         * @param request The request to proceed with (can be modified by interceptors)
         * @return The HTTP response
         */
        suspend fun proceed(request: HttpRequest): HttpResponse
    }
}
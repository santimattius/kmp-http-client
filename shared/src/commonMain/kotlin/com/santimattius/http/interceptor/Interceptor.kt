package com.santimattius.http.interceptor

import com.santimattius.http.HttpRequest
import com.santimattius.http.HttpResponse
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

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
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
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

/**
 * Typealias for a function that can be used as an interceptor.
 */
typealias InterceptorFunction = suspend (Interceptor.Chain) -> HttpResponse

/**
 * Creates an interceptor from a function.
 */
fun interceptor(block: InterceptorFunction): Interceptor = object : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse = block(chain)
}

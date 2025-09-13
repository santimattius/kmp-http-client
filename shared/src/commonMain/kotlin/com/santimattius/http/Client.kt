package com.santimattius.http

import com.santimattius.http.interceptor.Interceptor

/**
 * Interface defining the contract for an HTTP client.
 */
interface Client : AutoCloseable {

    /**
     * Executes an HTTP request and returns the response.
     *
     * @param request The HTTP request to execute
     * @return The HTTP response
     */
    suspend fun execute(request: HttpRequest): HttpResponse


    /**
     * Creates a new client with additional interceptors.
     *
     * @param interceptors The interceptors to add
     * @return A new [Client] instance with the added interceptors
     */
    fun addInterceptors(interceptors: List<Interceptor>): Client

}
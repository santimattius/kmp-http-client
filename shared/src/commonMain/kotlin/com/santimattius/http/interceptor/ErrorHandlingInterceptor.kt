package com.santimattius.http.interceptor

import com.santimattius.http.HttpResponse
import com.santimattius.http.exception.*
import com.santimattius.http.exception.statusCodeToException

/**
 * Interceptor that handles HTTP errors by throwing appropriate exceptions.
 */
class ErrorHandlingInterceptor : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
        val response = chain.proceed(chain.request)
        
        // Check for error status codes (400-599)
        if (response.status in 400..599) {
            val errorMessage = "HTTP ${response.status}: ${response.body?.takeIf { it is String } as? String ?: ""}"
            throw statusCodeToException(
                code = response.status,
                errorBody = response.body as? String,
                message = errorMessage
            )
        }
        
        return response
    }
}

/**
 * Creates an error handling interceptor.
 */
fun errorHandlingInterceptor(): Interceptor = ErrorHandlingInterceptor()

package com.santimattius.http.interceptor

import com.santimattius.http.HttpResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface HttpInterceptor : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
        return suspendCancellableCoroutine { continuation ->
            intercept(chain) { response ->
                continuation.resume(response)
            }
        }
    }

    fun intercept(chain: Interceptor.Chain, response: (HttpResponse) -> Unit)

}
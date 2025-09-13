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

internal class KtorClient private constructor(
    private val config: HttpClientConfig,
    private val interceptors: List<Interceptor> = emptyList()
) : Client {

    private val client: KtorHttpClient by lazy { createKtorClient(config) }

    override suspend fun execute(request: HttpRequest): HttpResponse {
        val chain = RealInterceptorChain(
            request = request,
            interceptors = interceptors,
            index = 0,
            call = { req -> executeKtorRequest(req) }
        )
        return chain.proceed(request)
    }

    override fun addInterceptors(interceptors: List<Interceptor>): Client {
        return KtorClient(
            config = config,
            interceptors = this.interceptors + interceptors
        )
    }

    private suspend fun executeKtorRequest(request: HttpRequest): HttpResponse {
        val httpRequestBuilder = request.toKtorRequest().getOrThrow()
        return client.request(httpRequestBuilder).toHttpResponse()
    }


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


    override fun close() {
        client.close()
    }

    companion object {
        fun create(
            config: HttpClientConfig,
            interceptors: List<Interceptor> = emptyList()
        ): Client {
            return KtorClient(config, interceptors)
        }
    }
}

private class RealInterceptorChain(
    override val request: HttpRequest,
    private val interceptors: List<Interceptor>,
    private val index: Int,
    private val call: suspend (HttpRequest) -> HttpResponse
) : Interceptor.Chain {

    override suspend fun proceed(request: HttpRequest): HttpResponse {
        if (index >= interceptors.size) return call(request)

        val next = RealInterceptorChain(
            request = request,
            interceptors = interceptors,
            index = index + 1,
            call = call
        )

        val interceptor = interceptors[index]
        return interceptor.intercept(next)
    }
}
package com.santimattius.http.interceptor

import com.santimattius.http.HttpRequest
import com.santimattius.http.HttpResponse
import com.santimattius.http.config.HttpClientConfig
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Interceptor that logs HTTP request and response information.
 *
 * @property level The logging level to use
 * @property logger The logger to use for output
 */
class LoggingInterceptor(
    private val level: HttpClientConfig.LogLevel = HttpClientConfig.LogLevel.BASIC,
    private val logger: (String) -> Unit = { println(it) }
) : Interceptor {

    private val json = Json { prettyPrint = true }

    @OptIn(ExperimentalTime::class)
    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
        val request = chain.request
        
        // Log request
        if (level != HttpClientConfig.LogLevel.NONE) {
            logRequest(request)
        }

        val startTime = Clock.System.now()
        val response = chain.proceed(request)
        val endTime = Clock.System.now()
        val duration = endTime - startTime

        // Log response
        if (level != HttpClientConfig.LogLevel.NONE) {
            logResponse(response, duration)
        }

        return response
    }

    private fun logRequest(request: HttpRequest) {
        logger("--> ${request.method} ${request.url}")
        
        if (level >= HttpClientConfig.LogLevel.HEADERS) {
            // Log request headers
            request.headers.forEach { (name, value) ->
                logger("$name: $value")
            }
            
            // Log request body if present and level is BODY
            if (level >= HttpClientConfig.LogLevel.BODY && request.body != null) {
                logger("")
                logger(formatBody(request.body))
            }
        }
        logger("--> END ${request.method}")
    }

    private fun logResponse(response: HttpResponse, duration: kotlin.time.Duration) {
        logger("<-- ${response.status} ${response.url} (${duration.inWholeMilliseconds}ms)")
        
        if (level >= HttpClientConfig.LogLevel.HEADERS) {
            // Log response headers
            response.headers.forEach { (name, value) ->
                logger("$name: $value")
            }
            
            // Log response body if present and level is BODY
            if (level >= HttpClientConfig.LogLevel.BODY && response.body != null) {
                logger("")
                logger(formatBody(response.body))
            }
        }
        logger("<-- END HTTP")
    }

    private fun formatBody(body: Any?): String {
        return when (body) {
            is String -> body
            is ByteArray -> "[${body.size} bytes]"
            is ByteReadChannel -> "[streaming content]"
            else -> try {
                json.encodeToString(body)
            } catch (e: Exception) {
                body.toString()
            }
        }
    }
}

/**
 * Creates a logging interceptor with the given log level.
 */
fun loggingInterceptor(
    level: HttpClientConfig.LogLevel = HttpClientConfig.LogLevel.BASIC,
    logger: (String) -> Unit = { println(it) }
): Interceptor = LoggingInterceptor(level, logger)

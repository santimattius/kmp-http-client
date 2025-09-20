package com.santimattius.http.extension

import com.santimattius.http.Client
import com.santimattius.http.HttpRequest
import com.santimattius.http.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * Executes an HTTP request and returns the result as a [Result] object.
 *
 * @param request The HTTP request to execute.
 * @return A [Result] object containing the HTTP response if the request was successful,
 * or an exception if the request failed.
 * @throws CancellationException if the request was cancelled.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Client.executeAsResult(request: HttpRequest): Result<HttpResponse> {
    try {
        val response = execute(request)
        return Result.success(response)
    } catch (ex: CancellationException) {
        throw ex
    } catch (ex: Exception) {
        return Result.failure(ex)
    }
}
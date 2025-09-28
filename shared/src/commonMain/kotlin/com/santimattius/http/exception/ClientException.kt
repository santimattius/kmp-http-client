package com.santimattius.http.exception

open class ClientException(
    message: String? = null,
    cause: Throwable? = null
) : Throwable(message, cause)
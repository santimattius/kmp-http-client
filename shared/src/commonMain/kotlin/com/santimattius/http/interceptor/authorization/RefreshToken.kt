package com.santimattius.http.interceptor.authorization

interface RefreshToken {

    suspend fun isRefreshed(): Boolean
}

interface UnauthorizedCallback{

    suspend fun onUnauthorized()
}
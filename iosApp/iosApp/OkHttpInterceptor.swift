//
//  OkHttpInterceptors.swift
//  iosApp
//
//  Created by Santiago Mattiauda on 18/9/25.
//
import KMPHttpClient

class OkHttpInterceptor: Interceptor {
     
    func __intercept(chain: any InterceptorChain) async throws -> HttpResponse {
        print("Hello from OkHttpInterceptor")
        return try await chain.proceed(request: chain.request)
    }
    
}

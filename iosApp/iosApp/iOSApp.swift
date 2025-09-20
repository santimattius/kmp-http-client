import SwiftUI
import KMPHttpClient

@main
struct iOSApp: App {
    
    init() {
        initHttpClient()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}



func initHttpClient(){
    //TODO: default initialization
    let timeout:Int64 = 10000
    let baseUrl = "https://api-picture.onrender.com"
    
    let config = HttpClientConfig(
        baseUrl: baseUrl,
    ).logLevel(level: .basic)
        .connectTimeout(timeout: timeout)
        .enableLogging(enable: true)
    
    var interceptors: [Interceptor] = []
    interceptors.append(NetworkInterceptor())
    let authInterceptor = AuthInterceptor(
        tokenProvider: IOSTokenProvider() ,
        headerName: "Authorization",
        tokenPrefix: "Bearer"
    )
    interceptors.append(authInterceptor)
    let refreshToken = TokenRefreshInterceptor(
        refreshToken: IOSRefreshToken(),
        onUnauthorized: IOSUnauthorized()
    )
    interceptors.append(refreshToken)
    interceptors.append(LoggingInterceptor(level: .basic, logger: { (message) in
        print(message)
    }))
    

    HttpClient.shared.initialize(config: config, interceptors: interceptors)
}


class NetworkInterceptor: Interceptor {
     
    func __intercept(chain: any InterceptorChain) async throws -> HttpResponse {
        return try await chain.proceed(request: chain.request)
    }

    
}

class IOSTokenProvider:TokenProvider {
    
    func __getToken() async throws -> String? {
        return UUID().uuidString
    }
    
}

class IOSRefreshToken: RefreshToken {
    
    func __isRefreshed() async throws -> KotlinBoolean {
        return true
    }
    
}

class IOSUnauthorized: UnauthorizedCallback{
     
    func __onUnauthorized() async throws {
        print("Hello, World!")
    }

}





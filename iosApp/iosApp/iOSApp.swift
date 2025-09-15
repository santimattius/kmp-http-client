import SwiftUI
import Shared

@main
struct iOSApp: App {
    
    init(){
    
        //TODO: default initialization
        let timeout:Int64 = 10000
        let baseUrl = "http://127.0.0.1:8080"
        
        let config = HttpClientConfig(
            baseUrl: baseUrl,
            connectTimeout: timeout,
            socketTimeout: timeout,
            enableLogging: true,
            logLevel: .basic
        )
        
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
        
        //TODO: create HttpClient
        let client = HttpClient.shared.create(config: config)
            .addInterceptors(interceptors: interceptors)
        
        Task {
            let response:HttpResponse =  try! await client.execute(
                request: HttpRequest
                    .companion
                    .get(url: "/")
                    .queryParam(name: "key", value: "value")
                    .build()
            )
            
        }
        
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
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


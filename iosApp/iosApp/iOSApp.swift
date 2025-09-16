import SwiftUI
import Shared

class OkHttpInterceptor: Interceptor {
     
    func __intercept(chain: any InterceptorChain) async throws -> HttpResponse {
        return try await chain.proceed(request: chain.request)
    }
    
}

struct Picture : Decodable {
    let id : String?
    let author : String?
    let width : Int?
    let height : Int?
    let url : String?
    let download_url : String?
}

@main
struct iOSApp: App {
    
    init(){
        let baseUrl = "https://api-picture.onrender.com"
        let timeout:Int64 = 10000
        initHttpClient()
        var interceptors: [Interceptor] = []
        interceptors.append(OkHttpInterceptor())
        
        //TODO: create HttpClient
        let config = HttpClientConfig(
            baseUrl: baseUrl,
        ).logLevel(level: .basic)
            .connectTimeout(timeout: timeout)
            .enableLogging(enable: true)
        
        let client = HttpClient.shared.create(config: config)
            .addInterceptors(interceptors: interceptors)
        
        Task {
            do{
                let response =  try! await client.execute(
                    request: HttpRequest
                        .companion
                        .get(url: "\(baseUrl)/random")
                        //.queryParam(name: "key", value: "value")
                        .build()
                )
                let picture = try await response.getBodyAs(Picture.self)
                print("Hello Picture Author: \(String(describing: picture.author))")
            }catch let e{
                print(e)
            }
            
        }
        
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





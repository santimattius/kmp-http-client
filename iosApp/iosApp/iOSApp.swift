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

class NetworkInterceptor:HttpInterceptor {
    func intercept(chain: any InterceptorChain, response: @escaping (HttpResponse) -> Void) {
        //TODO: bad implementation
        Task{
            let request = chain.request
                .doNewBuilder()
                .queryParam(name: "hello", value: "ios")
                .build()
            response(try! await chain.proceed(request: request))
        }
    }
    
}

class IOSTokenProvider:TokenProvider{
    func fetchToken(onTokenFetched: @escaping (String?) -> Void) {
        onTokenFetched(UUID().uuidString)
    }
}

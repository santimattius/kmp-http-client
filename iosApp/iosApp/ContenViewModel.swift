//
//  ContenViewModel.swift
//  iosApp
//
//  Created by Santiago Mattiauda on 18/9/25.
//
import SwiftUI
import KMPHttpClient


@Observable
class ContenViewModel{
      
    let client: Client
    
    
    init () {
        client = HttpClient.shared.create(config: HttpClientConfig(
            baseUrl: "https://www.freetogame.com/api",
        ).logLevel(level: .basic)
            .connectTimeout(timeout: 10000)
            .enableLogging(enable: true)
            .cache(cacheConfig: CacheConfig(
                enable: true, cacheDirectory: "ios-http-cache")
            )
        ).addInterceptors(interceptor: OkHttpInterceptor())
    }
    
    func call(){
        Task {
            do{
                /*let response =  try! await client.execute(
                    request: HttpRequest
                        .companion
                        .get(url: "/game")
                        .queryParam(name: "id", value: "475")
                        .build()
                )
                 
                 let game = try await response.getBodyAs(Game.self)
                 print("Hello Game: \(game)")
                 */
                let response =  await client.executeAsResult(
                    request: HttpRequest
                        .companion
                        .get(url: "/game")
                        .queryParam(name: "id", value: "475")
                        .build()
                )
                switch response {
                case .success(let httpResponse):
                    print("Hello Response: \(httpResponse)")
                    let game = try await httpResponse.getBodyAs(Game.self)
                    print("Hello Game: \(game)")
                case .failure(let error):
                    print(error)
                }

            }catch let e{
                print(e)
            }
            
        }
    }
}


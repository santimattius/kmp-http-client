//
// Created by Santiago Mattiauda on 20/9/25.
//

import Foundation
import KMPHttpClient

public extension Client {

    func executeAsResult(request: HttpRequest) async -> Result<HttpResponse, Error> {
        do {
            return .success(try await execute(request: request))
        } catch {
            return .failure(error)
        }
    }
}
//
// Created by Santiago Mattiauda on 15/9/25.
//

import Foundation
import Shared

public extension HttpResponse {
    func getBodyAs<T: Decodable>(_ type: T.Type) async throws -> T {
        guard let bodyString = self.body as? String else {
            throw NSError(
                domain: "HttpResponse",
                code: -1,
                userInfo: [NSLocalizedDescriptionKey: "Response body is nil"]
            )
        }

        guard let data = bodyString.data(using: .utf8) else {
            throw NSError(
                domain: "HttpResponse",
                code: -2,
                userInfo: [NSLocalizedDescriptionKey: "Failed to convert String to Data"]
            )
        }

        return try JSONDecoder().decode(type, from: data)
    }
}
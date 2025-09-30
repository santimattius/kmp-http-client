# Swift Error Handling Guide

## Overview

This guide explains how to properly handle exceptions from the KMP HTTP Client in Swift/iOS applications.

## Exception Hierarchy

All HTTP-related exceptions inherit from `HttpException`:

```
HttpException (base)
├── NetworkException          // Network connectivity issues
├── TimeoutException          // Request timeouts
├── ParseException            // Response parsing errors
├── UnknownHttpException      // Unknown errors
└── HttpErrorException        // HTTP error responses (4xx, 5xx)
    ├── BadRequestException           (400)
    ├── UnauthorizedException         (401)
    ├── ForbiddenException            (403)
    ├── NotFoundException             (404)
    ├── InternalServerErrorException  (500)
    └── ServiceUnavailableException   (503)

ClientException (base)
└── CacheStorageException     // Cache-related errors
```

## Common Error Handling Patterns

### Pattern 1: Specific Error Handling

Handle specific HTTP errors differently:

```swift
import KMPHttpClient

func fetchData() async throws -> Data {
    let client = HttpClient.shared.defaultClient()
    let request = HttpRequest.companion
        .get("https://api.example.com")
        .path("/data")
        .build()
    
    do {
        let response = try await client.execute(request: request)
        
        guard let bodyString = response.body as? String,
              let data = bodyString.data(using: .utf8) else {
            throw NSError(domain: "App", code: -1, 
                         userInfo: [NSLocalizedDescriptionKey: "Invalid response"])
        }
        
        return data
        
    } catch let error as UnauthorizedException {
        // User needs to log in again
        print("⚠️ Session expired: \(error.message ?? "")")
        // Navigate to login screen
        throw error
        
    } catch let error as ForbiddenException {
        // User doesn't have permission
        print("⚠️ Access denied: \(error.message ?? "")")
        // Show permission error
        throw error
        
    } catch let error as NotFoundException {
        // Resource not found
        print("⚠️ Resource not found: \(error.message ?? "")")
        throw error
        
    } catch let error as NetworkException {
        // Network connectivity issue
        print("⚠️ Network error: \(error.message ?? "")")
        // Show offline message
        throw error
        
    } catch let error as TimeoutException {
        // Request timed out
        print("⚠️ Request timeout: \(error.message ?? "")")
        // Show timeout message
        throw error
        
    } catch let error as HttpErrorException {
        // Other HTTP errors (e.g., 500, 503)
        print("⚠️ HTTP \(error.code): \(error.message ?? "")")
        if let errorBody = error.errorBody {
            print("Error details: \(errorBody)")
        }
        throw error
        
    } catch {
        // Unknown error
        print("⚠️ Unknown error: \(error)")
        throw error
    }
}
```

### Pattern 2: Generic Error Handling

Handle all errors generically:

```swift
func fetchData() async -> Result<Data, Error> {
    let client = HttpClient.shared.defaultClient()
    let request = HttpRequest.companion
        .get("https://api.example.com")
        .path("/data")
        .build()
    
    do {
        let response = try await client.execute(request: request)
        
        guard let bodyString = response.body as? String,
              let data = bodyString.data(using: .utf8) else {
            return .failure(NSError(domain: "App", code: -1))
        }
        
        return .success(data)
        
    } catch {
        return .failure(error)
    }
}

// Usage
let result = await fetchData()
switch result {
case .success(let data):
    print("✅ Success: \(data)")
case .failure(let error):
    print("❌ Error: \(error)")
}
```

### Pattern 3: SwiftUI Integration

Handle errors in SwiftUI views:

```swift
import SwiftUI
import KMPHttpClient

@MainActor
class DataViewModel: ObservableObject {
    @Published var data: [Item] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let client: Client
    
    init() {
        let config = HttpClientConfig(baseUrl: "https://api.example.com")
        try? HttpClient.shared.initialize(config: config)
        self.client = try! HttpClient.shared.defaultClient()
    }
    
    func loadData() async {
        isLoading = true
        errorMessage = nil
        
        let request = HttpRequest.companion
            .get("https://api.example.com")
            .path("/items")
            .build()
        
        do {
            let response = try await client.execute(request: request)
            
            guard let bodyString = response.body as? String,
                  let data = bodyString.data(using: .utf8) else {
                throw NSError(domain: "App", code: -1)
            }
            
            self.data = try JSONDecoder().decode([Item].self, from: data)
            
        } catch let error as UnauthorizedException {
            errorMessage = "Your session has expired. Please log in again."
            
        } catch let error as NetworkException {
            errorMessage = "Network connection failed. Please check your internet."
            
        } catch let error as TimeoutException {
            errorMessage = "Request timed out. Please try again."
            
        } catch let error as HttpErrorException {
            errorMessage = "Server error (\(error.code)). Please try again later."
            
        } catch {
            errorMessage = "An unexpected error occurred: \(error.localizedDescription)"
        }
        
        isLoading = false
    }
}

struct ContentView: View {
    @StateObject private var viewModel = DataViewModel()
    
    var body: some View {
        VStack {
            if viewModel.isLoading {
                ProgressView("Loading...")
            } else if let error = viewModel.errorMessage {
                VStack {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundColor(.red)
                    Text(error)
                        .multilineTextAlignment(.center)
                        .padding()
                    Button("Retry") {
                        Task {
                            await viewModel.loadData()
                        }
                    }
                }
            } else {
                List(viewModel.data) { item in
                    Text(item.name)
                }
            }
        }
        .task {
            await viewModel.loadData()
        }
    }
}
```

### Pattern 4: Retry Logic

Implement retry logic for transient errors:

```swift
func fetchDataWithRetry(maxRetries: Int = 3) async throws -> Data {
    var lastError: Error?
    
    for attempt in 1...maxRetries {
        do {
            return try await fetchData()
            
        } catch let error as NetworkException {
            // Retry on network errors
            lastError = error
            print("⚠️ Network error on attempt \(attempt)/\(maxRetries)")
            try await Task.sleep(nanoseconds: UInt64(attempt) * 1_000_000_000) // Exponential backoff
            
        } catch let error as TimeoutException {
            // Retry on timeout
            lastError = error
            print("⚠️ Timeout on attempt \(attempt)/\(maxRetries)")
            try await Task.sleep(nanoseconds: UInt64(attempt) * 1_000_000_000)
            
        } catch let error as ServiceUnavailableException {
            // Retry on 503 errors
            lastError = error
            print("⚠️ Service unavailable on attempt \(attempt)/\(maxRetries)")
            try await Task.sleep(nanoseconds: UInt64(attempt) * 1_000_000_000)
            
        } catch {
            // Don't retry on other errors
            throw error
        }
    }
    
    throw lastError ?? NSError(domain: "App", code: -1)
}
```

## Best Practices

### 1. Always Handle Specific Exceptions

Don't just catch generic `Error`. Handle specific exceptions to provide better UX:

```swift
// ❌ Bad
do {
    let response = try await client.execute(request: request)
} catch {
    print("Error: \(error)")
}

// ✅ Good
do {
    let response = try await client.execute(request: request)
} catch let error as UnauthorizedException {
    // Handle auth error
} catch let error as NetworkException {
    // Handle network error
} catch {
    // Handle unknown error
}
```

### 2. Extract Error Messages

HTTP error exceptions contain useful information:

```swift
catch let error as HttpErrorException {
    print("HTTP \(error.code)")
    if let message = error.message {
        print("Message: \(message)")
    }
    if let errorBody = error.errorBody {
        print("Details: \(errorBody)")
    }
}
```

### 3. Use Result Type for Non-Throwing APIs

If you prefer not to use `throws`, wrap in `Result`:

```swift
func fetchData() async -> Result<Data, Error> {
    do {
        let response = try await client.execute(request: request)
        // Process response...
        return .success(data)
    } catch {
        return .failure(error)
    }
}
```

### 4. Log Errors Appropriately

Use different log levels for different errors:

```swift
import OSLog

let logger = Logger(subsystem: "com.example.app", category: "network")

do {
    let response = try await client.execute(request: request)
} catch let error as NetworkException {
    logger.warning("Network error: \(error.message ?? "")")
} catch let error as HttpErrorException {
    logger.error("HTTP \(error.code): \(error.message ?? "")")
} catch {
    logger.fault("Unexpected error: \(error)")
}
```

### 5. Provide User-Friendly Messages

Don't show raw error messages to users:

```swift
func userFriendlyMessage(for error: Error) -> String {
    switch error {
    case is UnauthorizedException:
        return "Your session has expired. Please log in again."
    case is ForbiddenException:
        return "You don't have permission to access this resource."
    case is NotFoundException:
        return "The requested resource was not found."
    case is NetworkException:
        return "Please check your internet connection and try again."
    case is TimeoutException:
        return "The request took too long. Please try again."
    case let httpError as HttpErrorException where httpError.code >= 500:
        return "Our servers are experiencing issues. Please try again later."
    default:
        return "An unexpected error occurred. Please try again."
    }
}
```

## Testing Error Handling

### Unit Test Example

```swift
import XCTest
@testable import YourApp
import KMPHttpClient

class NetworkServiceTests: XCTestCase {
    
    func testUnauthorizedHandling() async throws {
        // This test assumes you have a way to mock responses
        // or a test server that returns 401
        
        let service = NetworkService()
        
        do {
            _ = try await service.fetchProtectedData()
            XCTFail("Should have thrown UnauthorizedException")
        } catch is UnauthorizedException {
            // ✅ Correct exception caught
            XCTAssert(true)
        } catch {
            XCTFail("Wrong exception type: \(error)")
        }
    }
    
    func testNetworkErrorHandling() async throws {
        // Test network error handling
        let service = NetworkService()
        
        // Simulate offline mode
        // ...
        
        do {
            _ = try await service.fetchData()
            XCTFail("Should have thrown NetworkException")
        } catch is NetworkException {
            XCTAssert(true)
        } catch {
            XCTFail("Wrong exception type: \(error)")
        }
    }
}
```

## Common Pitfalls

### 1. Not Catching CancellationException

When using Task cancellation, make sure to handle it properly:

```swift
// ✅ Good
Task {
    do {
        let response = try await client.execute(request: request)
        // Process response
    } catch is CancellationError {
        // Task was cancelled, don't show error to user
        print("Task cancelled")
    } catch {
        // Handle other errors
        showError(error)
    }
}
```

### 2. Ignoring Error Details

HTTP errors often contain useful information in the error body:

```swift
// ❌ Bad
catch let error as HttpErrorException {
    print("HTTP error")
}

// ✅ Good
catch let error as HttpErrorException {
    print("HTTP \(error.code): \(error.message ?? "")")
    if let errorBody = error.errorBody {
        // Parse error body for more details
        if let data = errorBody.data(using: .utf8),
           let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            print("Error details: \(json)")
        }
    }
}
```

### 3. Not Handling All Exception Types

Make sure to handle all relevant exception types:

```swift
// ✅ Comprehensive error handling
do {
    let response = try await client.execute(request: request)
} catch is UnauthorizedException {
    // Handle auth
} catch is ForbiddenException {
    // Handle permissions
} catch is NotFoundException {
    // Handle not found
} catch is NetworkException {
    // Handle network
} catch is TimeoutException {
    // Handle timeout
} catch is HttpErrorException {
    // Handle other HTTP errors
} catch {
    // Handle unknown
}
```

## Additional Resources

- [Kotlin/Native Error Handling](https://kotlinlang.org/docs/native-objc-interop.html#errors-and-exceptions)
- [Swift Error Handling](https://docs.swift.org/swift-book/LanguageGuide/ErrorHandling.html)
- [KMP HTTP Client README](./README.md)

## Support

If you encounter any issues with error handling, please:
1. Check that all exceptions are properly declared with `@Throws` in Kotlin
2. Verify that you're handling all relevant exception types in Swift
3. Check the error message and error body for details
4. File an issue on GitHub with a minimal reproduction case

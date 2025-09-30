// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "KMPHttpClient",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(
            name: "KMPHttpClient",
            targets: ["KMPHttpClient"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "KMPHttpClient",
            url: "https://github.com/santimattius/kmp-http-client/releases/download/v1.0.0/KMPHttpClient-1.0.0.xcframework.zip",
            checksum: "14d876ae46da61285c56df892130d92c7c2ec4177bb1cc5a584ea82f0372f57d"
        )
    ]
)

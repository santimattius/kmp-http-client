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
            url: "https://github.com/santimattius/kmp-http-client/releases/download/1.1.0-ALPHA01/KMPHttpClient-1.1.0-ALPHA01.xcframework.zip",
            checksum: "fd1f1163cd91b56e4cf1c300ab4a21d298fb3a65a56101a7975926f51a1ab4ae"
        )
    ]
)

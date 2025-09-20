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
            url: "https://github.com/santimattius/kmp-http-client/releases/download/1.0.0-ALPHA01/KMPHttpClient-1.0.0-ALPHA01.xcframework.zip",
            checksum: "0477978f9fc406cba261dfc27fc612fba10be08c7b7859dd60f18b33b49e1107"
        )
    ]
)

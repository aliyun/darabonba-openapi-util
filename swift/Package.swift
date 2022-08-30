// swift-tools-version:5.6
// The swift-tools-version declares the minimum version of Swift required to build this package.
import PackageDescription

let package = Package(
        name: "AlibabaCloudOpenApiUtil",
        platforms: [.macOS(.v10_15),
                    .iOS(.v13),
                    .tvOS(.v13),
                    .watchOS(.v6)],
        products: [
            .library(
                    name: "AlibabaCloudOpenApiUtil",
                    targets: ["AlibabaCloudOpenApiUtil"])
        ],
        dependencies: [
            // Dependencies declare other packages that this package depends on.
            .package(url: "https://github.com/aliyun/tea-swift.git", from: "1.0.0"),
            .package(url: "https://github.com/krzyzanowskim/CryptoSwift.git", from: "1.5.1"),
        ],
        targets: [
            .target(
                    name: "AlibabaCloudOpenApiUtil",
                    dependencies: [
                        .product(name: "Tea", package: "tea-swift"),
                        .product(name: "CryptoSwift", package: "CryptoSwift"),
                    ]),
            .testTarget(
                    name: "AlibabaCloudOpenApiUtilTests",
                    dependencies: [
                        "AlibabaCloudOpenApiUtil",
                        .product(name: "Tea", package: "tea-swift")
                    ]),
        ],
        swiftLanguageVersions: [.v5]
)

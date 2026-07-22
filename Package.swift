// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "VectoraSearch",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: "VectoraSearch",
            targets: ["VectoraSearch"])
    ],
    targets: [
        .binaryTarget(
            name: "VectoraSearch",
            url: "https://github.com/shadadman/Vectora/releases/download/v1.0.0/VectoraSearch.xcframework.zip",
            checksum: "PLACEHOLDER_CHECKSUM")
    ]
)

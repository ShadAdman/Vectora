# Vectora Publishing and Consumption

This document describes how to publish Vectora and how to consume it in various environments.

## Release Process

1. **Update version**: Update `VERSION_NAME` in `gradle.properties` (optional, CI will use the tag version).
2. **Commit changes**: Ensure all changes are committed.
3. **Create git tag**:
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0"
   ```
4. **Push tag**:
   ```bash
   git push origin v1.0.0
   ```
5. **GitHub Actions**:
   - `KMP Publish`: Publishes all modules to Maven Central.
   - `Android AAR`: Builds and archives the Android AAR.
   - `iOS XCFramework`: Builds XCFramework, creates a GitHub Release, and updates `Package.swift`.

---

## Consumer Setup

### 1. Kotlin Multiplatform + Maven Central

Add the dependency to your `commonMain` source set:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.shadadman:vectora-search:1.0.0")
        }
    }
}
```

### 2. Regular Android + Maven Central

In your Android app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.shadadman:vectora-search:1.0.0")
}
```

### 3. Regular Android using a local AAR

1. Download the `vectora-search-release.aar` from GitHub Actions artifacts or Releases.
2. Place it in your app's `libs` directory.
3. Add the dependency:

```kotlin
dependencies {
    implementation(files("libs/vectora-search-release.aar"))
    // Note: You may need to manually add transitive dependencies 
    // such as kotlinx-coroutines-core and kflite if they are not bundled.
}
```

### 4. Native iOS + Swift Package Manager

1. In Xcode, go to **File > Add Packages...**.
2. Enter the repository URL: `https://github.com/shadadman/Vectora`.
3. Select the version you want to use.
4. Xcode will download the XCFramework and link it to your project.

---

## CI/CD Infrastructure

The project uses GitHub Actions for automated publishing:

- **Secrets Required**:
  - `SONATYPE_USERNAME`: Your Sonatype account username.
  - `SONATYPE_PASSWORD`: Your Sonatype account password.
  - `GPG_PRIVATE_KEY`: Your GPG private key (exported as ASCII armor).
  - `GPG_PASSPHRASE`: The passphrase for your GPG key.
  - `GITHUB_TOKEN`: Provided automatically by GitHub Actions (used for Releases).

The workflows are defined in `.github/workflows/`.

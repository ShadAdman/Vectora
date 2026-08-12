import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
       namespace = "org.shad.adman.vectora.search"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
       compilerOptions {
           jvmTarget = JvmTarget.JVM_17
       }
    }
    
    val xcf = XCFramework("VectoraSearch")
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "VectoraSearch"
            isStatic = false // Dynamic framework is better for XCFramework distribution
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":vectora-core"))
            implementation(project(":vectora-engine"))
            implementation(project(":vectora-query"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    if (project.findProperty("RELEASE_SIGNING_ENABLED") != "false") signAllPublications()
}

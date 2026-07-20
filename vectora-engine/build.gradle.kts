import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    android {
       namespace = "org.shad.adman.vectora.engine"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "VectoraEngine"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":vectora-core"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

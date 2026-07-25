import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
       compilerOptions {
           jvmTarget = JvmTarget.JVM_17
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
            implementation(libs.kflite)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.startup)
            }
        }
    }
}

android {
    namespace = "org.shad.adman.vectora.engine"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].apply {
        res.srcDirs("src/androidMain/res", "src/commonMain/resources/res")
    }
}

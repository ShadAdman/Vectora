import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    android {
       namespace = "org.shad.adman.vectora.engine.skainet"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
       compilerOptions {
           jvmTarget = JvmTarget.JVM_17
       }
    }

    jvm {
        compilerOptions {
            // SKaiNET jvm artifacts ship Java 21 bytecode
            jvmTarget = JvmTarget.JVM_21
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":vectora-core"))
            implementation(project.dependencies.platform(libs.skainet.bom))
            implementation(project.dependencies.platform(libs.skainet.transformers.bom))
            implementation(libs.skainet.transformers.bert)
            implementation(libs.skainet.frontend.core)
            implementation(libs.skainet.frontend.text)
            implementation(libs.skainet.lang.core)
            implementation(libs.skainet.io.core)
            implementation(libs.skainet.io.safetensors)
            implementation(libs.skainet.io.gguf)
            implementation(libs.skainet.backend.cpu)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            // NEON JNI kernels (priority 100, runtime armv8-a / armv8.2 tier selection)
            runtimeOnly(libs.skainet.backend.jni.cpu)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    if (project.findProperty("RELEASE_SIGNING_ENABLED") != "false") signAllPublications()
}

// SKaiNET's JVM CPU backend uses the Panama Vector API for SIMD kernels; the
// incubator module must be enabled at JVM start or it falls back to scalar code.
tasks.withType<Test>().configureEach {
    jvmArgs("--add-modules", "jdk.incubator.vector")
}

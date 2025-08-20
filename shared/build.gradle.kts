import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("co.touchlab.skie") version "0.10.5"
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "2.1.21"


}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    val iosX64Target = iosX64()                 // אופציונלי (למק אינטל)
    val iosArm64Target = iosArm64()
    val iosSimArm64Target = iosSimulatorArm64()

    // יצירת XCFramework בשם Shared
    val xcf = XCFramework("Shared")

    listOf(
        iosX64Target,
        iosArm64Target,
        iosSimArm64Target
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcf.add(this)
        }
    }


    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.ktor.client.okhttp) // Android
            implementation(project.dependencies.platform(libs.firebase.bom))
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation("dev.gitlive:firebase-firestore:2.1.0")
            implementation("dev.gitlive:firebase-common:2.1.0")
            implementation("dev.gitlive:firebase-auth:2.1.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin) // iOz


        }
    }
}

android {
    namespace = "org.example.project.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
dependencies {
    implementation(libs.firebase.crashlytics.buildtools)
    implementation("dev.gitlive:firebase-firestore:2.1.0")
    implementation("dev.gitlive:firebase-common:2.1.0")
    implementation("dev.gitlive:firebase-auth:2.1.0")

}

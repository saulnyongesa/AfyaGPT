/*
 * app/build.gradle.kts — AfyaGPT Module Build Configuration
 *
 * This file defines the Android build settings and all project dependencies
 * for the AfyaGPT application module. Dependencies are managed via the
 * version catalog (gradle/libs.versions.toml) for consistent versioning.
 */

plugins {
    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP: Kotlin Symbol Processing — required by Room and Hilt for code generation
    alias(libs.plugins.ksp)
    // Hilt: Dependency injection framework for Android
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.afyagpt"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.afyagpt"
        minSdk = 26          // Android 8.0 — ensures modern API support
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Room schema export directory — placed at android{} level as required by KSP DSL
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    buildTypes {
        release {
            isMinifyEnabled = true   // Enable R8 code shrinking in release
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // With AGP 9.0+, kotlinOptions is no longer required for simple JVM target setting.
    // It defaults to the value set in compileOptions.targetCompatibility.

    buildFeatures {
        compose = true     // Enable Jetpack Compose
        buildConfig = true // Enable BuildConfig generation
    }
}

dependencies {
    // ─── AndroidX Core ───────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    // Splash screen API for animated launch screen
    implementation(libs.androidx.splashscreen)

    // ─── Material Components ─────────────────────────────────────────────────
    // Required for Theme.Material3 XML parents used in themes.xml
    implementation(libs.google.material)

    // ─── Lifecycle ───────────────────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // ViewModel integration with Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Collect StateFlow as Compose State safely
    implementation(libs.androidx.lifecycle.runtime.compose)

    // ─── Activity ────────────────────────────────────────────────────────────
    implementation(libs.androidx.activity.compose)

    // ─── Jetpack Compose ─────────────────────────────────────────────────────
    // BOM ensures all Compose libraries use consistent versions
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    // Material 3 design system components
    implementation(libs.androidx.compose.material3)
    // Extended icon set (stethoscope, local_pharmacy, etc.)
    implementation(libs.androidx.compose.material.icons.extended)

    // ─── Navigation ──────────────────────────────────────────────────────────
    // Compose Navigation for screen-to-screen routing
    implementation(libs.androidx.navigation.compose)

    // ─── Room (SQLite ORM) ───────────────────────────────────────────────────
    // Core Room runtime library
    implementation(libs.androidx.room.runtime)
    // Kotlin coroutines support for Room (Flow + suspend functions)
    implementation(libs.androidx.room.ktx)
    // KSP annotation processor for generating Room code at compile time
    ksp(libs.androidx.room.compiler)

    // ─── Hilt (Dependency Injection) ─────────────────────────────────────────
    // Hilt runtime library
    implementation(libs.hilt.android)
    // Hilt code generator (processes @HiltAndroidApp, @Inject, etc.)
    ksp(libs.hilt.android.compiler)
    // Hilt integration for Jetpack Navigation Compose (hiltViewModel())
    implementation(libs.hilt.navigation.compose)

    // ─── DataStore ───────────────────────────────────────────────────────────
    // Typed key-value persistence (replaces SharedPreferences)
    // Used for: active session, theme preference, last sync time
    implementation(libs.androidx.datastore.preferences)

    // ─── Coroutines ──────────────────────────────────────────────────────────
    // Kotlin coroutines for async operations (DB queries, background tasks)
    implementation(libs.kotlinx.coroutines.android)

    // ─── Security ────────────────────────────────────────────────────────────
    // Android Keystore-backed encrypted preferences
    implementation(libs.androidx.security.crypto)

    // ─── Google Fonts ────────────────────────────────────────────────────────
    // Load Inter font from Google Fonts CDN (cached offline after first load)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // ─── QR Code ─────────────────────────────────────────────────────────────
    // ZXing core library for generating QR code bitmaps
    implementation(libs.zxing.core)
    // ZXing Android wrapper (scanning + generation utilities)
    implementation(libs.zxing.android.embedded)

    // ─── Testing ─────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

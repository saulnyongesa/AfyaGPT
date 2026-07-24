/*
 * build.gradle.kts (Project Root)
 *
 * Root-level Gradle build file. Plugin declarations are made here
 * so they are available to all submodules without being applied directly.
 */
plugins {
    // Android Application plugin — applied in :app module
    alias(libs.plugins.android.application) apply false
    // Kotlin Android plugin — REQUIRED for Android projects
//    alias(libs.plugins.kotlin.android) apply false
    // Kotlin Compose compiler plugin — enables Compose UI compilation
    alias(libs.plugins.kotlin.compose) apply false
    // KSP — Kotlin Symbol Processor for Room and Hilt annotation processing
    alias(libs.plugins.ksp) apply false
    // Hilt Android plugin — enables @HiltAndroidApp and component generation
    alias(libs.plugins.hilt.android) apply false
}

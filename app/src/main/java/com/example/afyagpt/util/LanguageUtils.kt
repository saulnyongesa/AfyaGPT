package com.example.afyagpt.util

/**
 * LanguageUtils.kt — Legacy alias file delegating to Localization.kt
 * Supports English, Swahili (Kiswahili), and French (Français).
 */
typealias LegacyAppLanguage = AppLanguage

object LanguageDictionary {
    fun getString(key: String, language: AppLanguage): String {
        return AppStrings.get(key, language)
    }
}

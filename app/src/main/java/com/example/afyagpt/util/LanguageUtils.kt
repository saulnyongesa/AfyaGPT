package com.example.afyagpt.util

/**
 * LanguageUtils.kt — Multi-Language Support (English & Kiswahili)
 * Designed for Community Health Promoters (CHPs) in Kenya.
 */
enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    KISWAHILI("Kiswahili", "sw")
}

object LanguageDictionary {

    private val swahiliMap = mapOf(
        "Health Intelligence for Every Community" to "Akili ya Afya kwa Kila Jamii",
        "Start Triage Assessment" to "Anza Tathmini ya Afya",
        "New Patient" to "Mgonjwa Mpya",
        "Patient Records" to "Kumbukumbu za Wagonjwa",
        "Search registered patients..." to "Tafuta wagonjwa waliosajiliwa...",
        "Select Health Facility *" to "Chagua Kituo cha Afya *",
        "Create Account" to "Fungua Akaunti",
        "Sign In" to "Ingia",
        "Phone Number or Email" to "Namba ya Simu au Barua Pepe",
        "Enter PIN" to "Weka PIN",
        "Create 6-Digit PIN" to "Weka PIN ya Tarakimu 6",
        "Confirm 6-Digit PIN" to "Thibitisha PIN ya Tarakimu 6",
        "CRITICAL" to "HATARI KUBWA",
        "HIGH" to "HATARI YA KATI",
        "MEDIUM" to "HATARI YA KATI",
        "LOW" to "HATARI NDOGO",
        "Respiratory" to "Mfumo wa Kupumua",
        "Diarrhea" to "Kuhara",
        "Fever" to "Homa",
        "Ear Problem" to "Tatizo la Sikio",
        "Nutrition" to "Lishe",
        "Danger Signs" to "Dalili za Hatari",
        "Sync Data" to "Patanisha Data"
    )

    fun getString(key: String, language: AppLanguage): String {
        return if (language == AppLanguage.KISWAHILI) {
            swahiliMap[key] ?: key
        } else {
            key
        }
    }
}

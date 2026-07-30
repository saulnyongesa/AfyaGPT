package com.example.afyagpt.util

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * AppLanguage — Enum representing supported languages in AfyaGPT.
 */
enum class AppLanguage(val code: String, val displayName: String, val flagEmoji: String) {
    ENGLISH("en", "English", "🇬🇧"),
    SWAHILI("sw", "Kiswahili", "🇰🇪"),
    FRENCH("fr", "Français", "🇫🇷");

    companion object {
        fun fromCode(code: String): AppLanguage {
            val clean = code.trim().lowercase()
            return when {
                clean.contains("swahili") || clean == "sw" || clean == "kiswahili" -> SWAHILI
                clean.contains("french") || clean == "fr" || clean.contains("français") || clean.contains("francais") -> FRENCH
                else -> ENGLISH
            }
        }
    }
}

/**
 * AppStrings — Centralized translation dictionary providing localized strings for English, Swahili, and French.
 */
object AppStrings {

    fun get(key: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> englishMap[key] ?: key
            AppLanguage.SWAHILI -> swahiliMap[key] ?: englishMap[key] ?: key
            AppLanguage.FRENCH -> frenchMap[key] ?: englishMap[key] ?: key
        }
    }

    private val englishMap = mapOf(
        // Onboarding Steps
        "welcome_title" to "Welcome to AfyaGPT",
        "welcome_subtitle" to "Child Health AI & Community Intelligence",
        "step_language" to "Choose Language",
        "step_user_type" to "Select User Role",
        "step_offline_download" to "Offline Data Setup",
        "step_permissions" to "System Permissions",
        "get_started" to "Complete Setup & Continue",
        "continue" to "Next Step",

        // Auth Screens
        "welcome_back" to "Welcome Back",
        "login_subtitle" to "Login to continue managing your patients.",
        "phone_or_email" to "Phone Number or Email",
        "pin_6_digit" to "6-Digit PIN",
        "forgot_pin" to "Forgot PIN?",
        "login_btn" to "Login",
        "no_account_signup" to "Don't have an account? Sign up",
        "create_account" to "Create Account",
        "signup_subtitle" to "Register to join the AfyaGPT network.",
        "personal_details" to "Personal Details",
        "full_name" to "Full Name (as per ID)",
        "phone_number" to "Phone Number (e.g., 0712345678)",
        "email_optional" to "Email Address (Optional)",
        "professional_details" to "Professional Details",
        "profession" to "Profession",
        "reg_number" to "Registration/License Number (Optional)",
        "security" to "Security",
        "create_pin" to "Create 6-Digit PIN",
        "confirm_pin" to "Confirm 6-Digit PIN",
        "already_have_account" to "Already have an account? Login",
        "profile" to "Profile",
        "locked_with_timer" to "Locked ({}s)",
        "dont_have_account" to "Don't have an account?",
        "sign_up" to " Sign Up",


        // User Roles
        "role_chw" to "Community Health Worker",
        "role_nurse" to "Nurse",
        "role_clinical_officer" to "Clinical Officer",
        "role_caregiver" to "Caregiver / Parent",
        "role_doctor" to "Doctor / Medical Officer",

        // Offline Data Packs
        "pack_who_imci" to "WHO IMCI Protocol & Rules Engine",
        "pack_national_guidelines" to "National Kenya Clinical Guidelines",
        "pack_ai_kb" to "Offline Medical AI Reasoning Engine",
        "pack_voice_packs" to "Multi-Lingual Voice & Audio Packs",

        // System Permissions
        "perm_microphone" to "Microphone Access (Voice AI Consultation)",
        "perm_gps" to "GPS Location Access (Climate & Emergency Mapping)",
        "perm_storage" to "Offline Storage Access (Data & Local Patient Cache)",

        // Climate & Risk Engine
        "climate_dashboard" to "Climate Risk Engine",
        "today_climate_risk" to "Today's Climate Risk Context",
        "temperature" to "Temperature",
        "humidity" to "Humidity",
        "flood_alert" to "Flood Risk Alert",
        "air_quality" to "Air Quality Index (AQI)",
        "malaria_risk" to "Malaria Vector Risk",
        "heatstroke_risk" to "Heatstroke Risk",
        "asthma_risk" to "Asthma / Respiratory Risk",
        "diarrhea_risk" to "Diarrhea Surge Risk",

        // Emergency Mode
        "emergency_mode" to "Emergency Mode",
        "emergency_protocol" to "🔴 EMERGENCY TRIAGE PROTOCOL",
        "danger_signs_alert" to "Check for Immediate General Danger Signs",
        "refer_immediately" to "REFER IMMEDIATELY TO HOSPITAL",

        // Voice AI Consultation
        "voice_consultation" to "Voice AI Consultation",
        "tap_mic_to_speak" to "Tap Microphone to Speak",
        "listening" to "Listening to caregiver...",
        "ai_reasoning" to "AI Medical Reasoning",
        "voice_explanation" to "Voice Explanation (Audio TTS)",

        // Home Dashboard & Headers
        "daily_overview" to "Daily Clinical Overview",
        "facility" to "Facility",
        "county" to "County",
        "logged_in_as" to "Logged in as",
        "quick_actions" to "Quick Actions",
        "recent_patients" to "Recent Patients",
        "view_all" to "View All",
        "recorded_patients" to "Recorded Patients",
        "pending_followups" to "Pending Follow-ups",

        // Online Directives / Ministry Announcements
        "online_directives" to "Ministry & Facility Directives",
        "directive_malaria_title" to "Malaria Outbreak Alert (High Priority)",
        "directive_malaria_msg" to "High incidence reported in endemic sub-counties. Perform blood RDT for all pediatric fever presentations.",
        "directive_kepi_title" to "KEPI Vaccine Supply Update",
        "directive_kepi_msg" to "Rotavirus vaccine batch #9420 and BCG stock re-supplied at Sub-County Central Depot.",

        // Preferences & Settings
        "appearance" to "Appearance & Preferences",
        "change_theme" to "App Theme",
        "change_language" to "App Language",
        "logout" to "Logout",

        // General Nav
        "nav_home" to "Home",
        "nav_triage" to "Assess Child",
        "nav_patients" to "Patient History",
        "nav_library" to "WHO Guidelines",
        "nav_settings" to "Settings",
        "cancel" to "Cancel",
        "save" to "Save Record",
        "ok" to "OK"
    )

    private val swahiliMap = mapOf(
        // Onboarding Steps
        "welcome_title" to "Karibu AfyaGPT",
        "welcome_subtitle" to "Akili Bandia ya Afya ya Watoto na Jamii",
        "step_language" to "Chagua Lugha",
        "step_user_type" to "Chagua Wajibu Wako",
        "step_offline_download" to "Pakua Data ya Nje ya Mtandao",
        "step_permissions" to "Ruhusa za Mfumo",
        "get_started" to "Kamilisha Usanidi na Uendelee",
        "continue" to "Hatua Inayofuata",

        // Auth Screens
        "welcome_back" to "Karibu Tena",
        "login_subtitle" to "Ingia ili kuendelea kudhibiti wagonjwa wako.",
        "phone_or_email" to "Nambari ya Simu au Barua pepe",
        "pin_6_digit" to "PIN ya tarakimu 6",
        "forgot_pin" to "Umesahau PIN?",
        "login_btn" to "Ingia",
        "no_account_signup" to "Huna akaunti? Jisajili",
        "create_account" to "Fungua Akaunti",
        "signup_subtitle" to "Jisajili ili kujiunga na mtandao wa AfyaGPT.",
        "personal_details" to "Maelezo Binafsi",
        "full_name" to "Jina Kamili (kama kwenye Kitambulisho)",
        "phone_number" to "Nambari ya Simu",
        "email_optional" to "Barua pepe (Si lazima)",
        "professional_details" to "Maelezo ya Kitaaluma",
        "profession" to "Taaluma",
        "reg_number" to "Nambari ya Usajili/Leseni (Si lazima)",
        "security" to "Usalama",
        "create_pin" to "Unda PIN ya tarakimu 6",
        "confirm_pin" to "Thibitisha PIN ya tarakimu 6",
        "already_have_account" to "Tayari una akaunti? Ingia",
        "profile" to "Wasifu",
        "locked_with_timer" to "Imefungwa ({}s)",
        "dont_have_account" to "Huna akaunti?",
        "sign_up" to " Jisajili",


        // User Roles
        "role_chw" to "Mhudumu wa Afya ya Jamii (CHP)",
        "role_nurse" to "Muuguzi (Nurse)",
        "role_clinical_officer" to "Afisa wa Kliniki",
        "role_caregiver" to "Mlezi / Mzazi",
        "role_doctor" to "Daktari (Doctor)",

        // Offline Data Packs
        "pack_who_imci" to "Mwongozo wa WHO IMCI na Sheria za Tiba",
        "pack_national_guidelines" to "Miongozo ya Kitaifa ya Afya ya Kenya",
        "pack_ai_kb" to "Injini ya Akili Bandia ya Tiba Nje ya Mtandao",
        "pack_voice_packs" to "Vifurushi vya Sauti za Lugha Mbalimbali",

        // System Permissions
        "perm_microphone" to "Ruhusa ya Maikrofoni (Ushauri wa Sauti wa AI)",
        "perm_gps" to "Ruhusa ya Eneo la GPS (Ramani ya Hali ya Hewa na Dharura)",
        "perm_storage" to "Ruhusa ya Hifadhi ya Nje ya Mtandao",

        // Climate & Risk Engine
        "climate_dashboard" to "Injini ya Hatari za Tabianchi",
        "today_climate_risk" to "Muktadha wa Hatari ya Tabianchi Leo",
        "temperature" to "Joto",
        "humidity" to "Unyevu",
        "flood_alert" to "Tahadhari ya Mafuriko",
        "air_quality" to "Kiwango cha Ubora wa Hewa (AQI)",
        "malaria_risk" to "Hatari ya Malaria",
        "heatstroke_risk" to "Hatari ya Kupatwa na Joto",
        "asthma_risk" to "Hatari ya Pumu na Kupumua",
        "diarrhea_risk" to "Hatari ya Ongezeko la Kuhara",

        // Emergency Mode
        "emergency_mode" to "Hali ya Dharura",
        "emergency_protocol" to "🔴 PROTOKALI YA DALILI ZA DHARURA",
        "danger_signs_alert" to "Kagua Dalili za Hatari za Haraka",
        "refer_immediately" to "PELEKA HOSPITALI MARA MOJA",

        // Voice AI Consultation
        "voice_consultation" to "Ushauri wa Sauti wa AI",
        "tap_mic_to_speak" to "Gusa Maikrofoni Kuzungumza",
        "listening" to "Inasikiliza mzazi...",
        "ai_reasoning" to "Sababu za Kitiba za AI",
        "voice_explanation" to "Maelezo ya Sauti (TTS)",

        // Home Dashboard & Headers
        "daily_overview" to "Muhtasari wa Kila Siku wa Tiba",
        "facility" to "Kituo cha Afya",
        "county" to "Kaunti",
        "logged_in_as" to "Umeingia kama",
        "quick_actions" to "Vituo vya Haraka",
        "recent_patients" to "Wagonjwa wa Hivi Karibuni",
        "view_all" to "Tazama Zote",
        "recorded_patients" to "Wagonjwa Waliosajiliwa",
        "pending_followups" to "Ufuatiliaji Unaosubiri",

        // Online Directives / Ministry Announcements
        "online_directives" to "Miongozo ya Wizara na Kituo",
        "directive_malaria_title" to "Tahadhari ya Mlipuko wa Malaria (Kipao Mbele)",
        "directive_malaria_msg" to "Kiwango cha juu cha malaria kimeripotiwa. Fanya kipimo cha RDT cha damu kwa watoto wote wenye homa.",
        "directive_kepi_title" to "Taarifa ya Ugavi wa Chanjo za KEPI",
        "directive_kepi_msg" to "Shehena ya chanjo ya Rotavirus #9420 na BCG imewasili katika Bohari Kuu ya Wilaya.",

        // Preferences & Settings
        "appearance" to "Mwonekano na Mipangilio",
        "change_theme" to "Mandhari ya Programu",
        "change_language" to "Lugha ya Programu",
        "logout" to "Ondoka (Logout)",

        // General Nav
        "nav_home" to "Nyumbani",
        "nav_triage" to "Kagua Mtoto",
        "nav_patients" to "Kumbukumbu za Wagonjwa",
        "nav_library" to "Miongozo ya WHO",
        "nav_settings" to "Mipangilio",
        "cancel" to "Ghairi",
        "save" to "Hifadhi Kumbukumbu",
        "ok" to "Sawa"
    )

    private val frenchMap = mapOf(
        // Onboarding Steps
        "welcome_title" to "Bienvenue sur AfyaGPT",
        "welcome_subtitle" to "IA de Santé Infantile & Intelligence Communautaire",
        "step_language" to "Choisir la Langue",
        "step_user_type" to "Sélectionner le Rôle",
        "step_offline_download" to "Téléchargement Hors Ligne",
        "step_permissions" to "Autorisations Système",
        "get_started" to "Terminer la Configuration et Continuer",
        "continue" to "Étape Suivante",

        // Auth Screens
        "welcome_back" to "Bon retour",
        "login_subtitle" to "Connectez-vous pour continuer à gérer vos patients.",
        "phone_or_email" to "Numéro de téléphone ou Email",
        "pin_6_digit" to "Code PIN à 6 chiffres",
        "forgot_pin" to "Code PIN oublié ?",
        "login_btn" to "Se connecter",
        "no_account_signup" to "Vous n'avez pas de compte ? S'inscrire",
        "create_account" to "Créer un compte",
        "signup_subtitle" to "Inscrivez-vous pour rejoindre le réseau AfyaGPT.",
        "personal_details" to "Détails Personnels",
        "full_name" to "Nom complet",
        "phone_number" to "Numéro de téléphone",
        "email_optional" to "Adresse Email (Optionnel)",
        "professional_details" to "Détails Professionnels",
        "profession" to "Profession",
        "reg_number" to "Numéro d'inscription/licence (Optionnel)",
        "security" to "Sécurité",
        "create_pin" to "Créer un code PIN à 6 chiffres",
        "confirm_pin" to "Confirmer le code PIN à 6 chiffres",
        "already_have_account" to "Vous avez déjà un compte ? Se connecter",
        "profile" to "Profil",
        "locked_with_timer" to "Verrouillé ({}s)",
        "dont_have_account" to "Vous n'avez pas de compte ?",
        "sign_up" to " S'inscrire",


        // User Roles
        "role_chw" to "Agent de Santé Communautaire",
        "role_nurse" to "Infirmier / Infirmière",
        "role_clinical_officer" to "Officier Clinique",
        "role_caregiver" to "Parent / Tuteur",
        "role_doctor" to "Médecin",

        // Offline Data Packs
        "pack_who_imci" to "Protocole WHO IMCI & Moteur de Règles",
        "pack_national_guidelines" to "Directives Cliniques Nationales du Kenya",
        "pack_ai_kb" to "Moteur IA Médical Hors Ligne",
        "pack_voice_packs" to "Packs Vocaux Multilingues",

        // System Permissions
        "perm_microphone" to "Accès Microphone (Consultation Vocale IA)",
        "perm_gps" to "Accès Localisation GPS (Cartographie Climat & Urgence)",
        "perm_storage" to "Accès Stockage Hors Ligne",

        // Climate & Risk Engine
        "climate_dashboard" to "Moteur de Risques Climatiques",
        "today_climate_risk" to "Contexte de Risque Climatique d'Aujourd'hui",
        "temperature" to "Température",
        "humidity" to "Humidité",
        "flood_alert" to "Alerte Inondation",
        "air_quality" to "Qualité de l'Air (AQI)",
        "malaria_risk" to "Risque de Paludisme",
        "heatstroke_risk" to "Risque de Coup de Chaleur",
        "asthma_risk" to "Risque d'Asthme / Respiratoire",
        "diarrhea_risk" to "Risque de Diarrhée",

        // Emergency Mode
        "emergency_mode" to "Mode Urgence",
        "emergency_protocol" to "🔴 PROTOCOLE DE TRIAGE D'URGENCE",
        "danger_signs_alert" to "Vérification des Signes de Danger Immédiats",
        "refer_immediately" to "RÉFÉRER IMMÉDIATEMENT À L'HÔPITAL",

        // Voice AI Consultation
        "voice_consultation" to "Consultation Vocale IA",
        "tap_mic_to_speak" to "Appuyez sur le micro pour parler",
        "listening" to "Écoute du parent...",
        "ai_reasoning" to "Raisonnement Médical IA",
        "voice_explanation" to "Explication Vocale (Audio)",

        // Home Dashboard & Headers
        "daily_overview" to "Aperçu Clinique Quotidien",
        "facility" to "Établissement",
        "county" to "Comté",
        "logged_in_as" to "Connecté en tant que",
        "quick_actions" to "Actions Rapides",
        "recent_patients" to "Patients Récents",
        "view_all" to "Voir Tout",
        "recorded_patients" to "Patients Enregistrés",
        "pending_followups" to "Suivis en Attente",

        // Online Directives / Ministry Announcements
        "online_directives" to "Directives du Ministère et de l'Établissement",
        "directive_malaria_title" to "Alerte Épidémie de Paludisme (Haute Priorité)",
        "directive_malaria_msg" to "Forte incidence signalée. Effectuer un TDR sanguin pour tous les enfants fiévreux.",
        "directive_kepi_title" to "Mise à jour de l'Approvisionnement en Vaccins KEPI",
        "directive_kepi_msg" to "Le lot de vaccins Rotavirus #9420 et le BCG ont été réapprovisionnés au dépôt central.",

        // Preferences & Settings
        "appearance" to "Apparence et Préférences",
        "change_theme" to "Thème de l'Application",
        "change_language" to "Langue de l'Application",
        "logout" to "Déconnexion",

        // General Nav
        "nav_home" to "Accueil",
        "nav_triage" to "Évaluer l'Enfant",
        "nav_patients" to "Historique Patients",
        "nav_library" to "Directives OMS",
        "nav_settings" to "Paramètres",
        "cancel" to "Annuler",
        "save" to "Enregistrer",
        "ok" to "D'accord"
    )
}

/**
 * CompositionLocal for passing current AppLanguage down the Compose tree.
 */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

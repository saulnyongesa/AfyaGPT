package com.example.afyagpt.ui.navigation

/*
 * AppRoute.kt — Navigation Routes
 *
 * Sealed class defining every navigation destination in AfyaGPT.
 * All routes are declared here to prevent magic string duplication.
 */

sealed class AppRoute(val route: String) {
    // ── Auth & Onboarding ───────────────────────────────────────────────────
    object Splash       : AppRoute("splash")
    object InitialSetup : AppRoute("onboarding/setup")
    object Login        : AppRoute("auth/login")
    object SignUp       : AppRoute("auth/signup")
    object ForgotPin    : AppRoute("auth/forgot-pin")

    // ── Main Tabs ───────────────────────────────────────────────────────────
    object Home        : AppRoute("home")
    object Triage      : AppRoute("triage")
    object Library     : AppRoute("library")
    object Records     : AppRoute("records")

    // ── Secondary & AI Feature Screens ──────────────────────────────────────
    object Settings           : AppRoute("settings")
    object Profile            : AppRoute("profile")
    object Search             : AppRoute("search")
    object ClimateDashboard   : AppRoute("climate/dashboard")
    object EmergencyMode      : AppRoute("emergency/mode")
    object VoiceConsultation  : AppRoute("voice/consultation")

    // ── Triage Flow (internal step-by-step screens) ─────────────────────────
    object TriageStart        : AppRoute("triage/start")
    object DangerSigns        : AppRoute("triage/danger-signs")
    object Vitals             : AppRoute("triage/vitals")
    object RespRateTimer      : AppRoute("triage/resp-timer")
    object CoughAssessment    : AppRoute("triage/cough")
    object DiarrheaAssessment : AppRoute("triage/diarrhea")
    object FeverAssessment    : AppRoute("triage/fever")
    object EarAssessment      : AppRoute("triage/ear")
    object NutritionAssessment: AppRoute("triage/nutrition")
    object TriageResult       : AppRoute("triage/result")

    // ── Patient Records ─────────────────────────────────────────────────────
    object RegisterPatient : AppRoute("patients/register")
    object PatientList     : AppRoute("patients/list")

    /** Patient detail — route arg is patientId. */
    object PatientDetail : AppRoute("patients/detail/{patientId}") {
        fun createRoute(patientId: Int) = "patients/detail/$patientId"
        const val ARG = "patientId"
    }

    // ── Immunization ────────────────────────────────────────────────────────
    /** Immunization schedule for a patient — route arg is patientId. */
    object Immunization : AppRoute("immunization/{patientId}") {
        fun createRoute(patientId: Int) = "immunization/$patientId"
        const val ARG = "patientId"
    }
}

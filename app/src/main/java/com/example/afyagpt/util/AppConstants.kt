package com.example.afyagpt.util

/**
 * AppConstants.kt
 * System-wide constants for AfyaGPT Android application.
 */
object AppConstants {
    /** Live Heroku backend server URL for Django REST Framework API synchronization */
    const val BACKEND_BASE_URL = "https://afyagpt-backend-493c86fa66a5.herokuapp.com/api/"
    
    /** Health check status URL */
    const val BACKEND_HEALTHCHECK_URL = "https://afyagpt-backend-493c86fa66a5.herokuapp.com/health/"
}

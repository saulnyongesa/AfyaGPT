package com.example.afyagpt.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.ui.screens.auth.AuthViewModel
import com.example.afyagpt.ui.screens.auth.ForgotPinScreen
import com.example.afyagpt.ui.screens.auth.LoginScreen
import com.example.afyagpt.ui.screens.auth.SignUpScreen
import com.example.afyagpt.ui.screens.auth.SplashScreen
import com.example.afyagpt.ui.screens.home.HomeScreen
import com.example.afyagpt.ui.screens.home.HomeViewModel
import com.example.afyagpt.ui.screens.library.LibraryScreen
import com.example.afyagpt.ui.screens.profile.ProfileScreen
import com.example.afyagpt.ui.screens.profile.ProfileViewModel
import com.example.afyagpt.ui.screens.search.GlobalSearchScreen
import com.example.afyagpt.ui.screens.search.SearchViewModel
import com.example.afyagpt.ui.screens.immunization.ImmunizationScreen
import com.example.afyagpt.ui.screens.immunization.ImmunizationViewModel
import com.example.afyagpt.ui.screens.patients.PatientDetailScreen
import com.example.afyagpt.ui.screens.patients.PatientListScreen
import com.example.afyagpt.ui.screens.patients.PatientViewModel
import com.example.afyagpt.ui.screens.patients.RegisterPatientScreen
import com.example.afyagpt.ui.screens.settings.SettingsScreen
import com.example.afyagpt.ui.screens.settings.SettingsViewModel
import com.example.afyagpt.ui.screens.triage.CoughAssessmentScreen
import com.example.afyagpt.ui.screens.triage.DangerSignsScreen
import com.example.afyagpt.ui.screens.triage.DiarrheaAssessmentScreen
import com.example.afyagpt.ui.screens.triage.EarAssessmentScreen
import com.example.afyagpt.ui.screens.triage.FeverAssessmentScreen
import com.example.afyagpt.ui.screens.triage.NutritionAssessmentScreen
import com.example.afyagpt.ui.screens.triage.RespRateTimerScreen
import com.example.afyagpt.ui.screens.triage.TriageResultScreen
import com.example.afyagpt.ui.screens.triage.TriageStartScreen
import com.example.afyagpt.ui.screens.triage.TriageViewModel
import com.example.afyagpt.ui.screens.triage.VitalsScreen

/*
 * NavGraph.kt — Root Application Navigation Graph
 *
 * Registers every screen destination and its arguments. The triage flow uses a
 * shared [TriageViewModel] scoped to the "triage" back-stack entry so that all
 * triage step screens share the same assessment state.
 *
 * Auth screens (Splash, Login, SignUp) never show a TopBar or BottomNav.
 * Main app screens always show BottomNav; inner screens (triage steps, detail
 * screens) show a TopBar with a back arrow.
 */

@Composable
fun AfyaNavGraph(
    navController: NavHostController = rememberNavController(),
    userPreferences: UserPreferences
) {
    /*
     * Observe login state from DataStore.
     * null = not yet loaded (show splash loading state)
     * false = not logged in → show login
     * true  = logged in → skip auth
     */
    val isLoggedIn by userPreferences.isLoggedIn().collectAsState(initial = null)
    val isSetupCompleted by userPreferences.isSetupCompleted().collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route
    ) {

        // ── Splash ──────────────────────────────────────────────────────────
        composable(AppRoute.Splash.route) {
            SplashScreen(
                isSetupCompleted = isSetupCompleted,
                isLoggedIn = isLoggedIn,
                onNavigateToSetup = {
                    navController.navigate(AppRoute.InitialSetup.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Initial Setup (Language & Theme Onboarding) ─────────────────────
        composable(AppRoute.InitialSetup.route) {
            val onboardingViewModel: com.example.afyagpt.ui.screens.onboarding.OnboardingViewModel = hiltViewModel()
            com.example.afyagpt.ui.screens.onboarding.OnboardingSetupScreen(
                viewModel = onboardingViewModel,
                onCompleteSetup = {
                    if (isLoggedIn == true) {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.InitialSetup.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(AppRoute.InitialSetup.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ── Auth: Login ──────────────────────────────────────────────────────
        composable(AppRoute.Login.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(AppRoute.SignUp.route) },
                onNavigateToForgotPin = { navController.navigate(AppRoute.ForgotPin.route) }
            )
        }

        // ── Auth: Sign Up ────────────────────────────────────────────────────
        composable(AppRoute.SignUp.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Auth: Forgot PIN ─────────────────────────────────────────────────
        composable(AppRoute.ForgotPin.route) {
            ForgotPinScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ── Home Dashboard ───────────────────────────────────────────────────
        composable(AppRoute.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigate = { route ->
                    if (route == AppRoute.Home.route) return@HomeScreen
                    navController.navigate(route) {
                        if (bottomNavItems.any { it.route == route }) {
                            popUpTo(AppRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onLogout = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Settings ─────────────────────────────────────────────────────────
        composable(AppRoute.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Climate Risk Engine Dashboard ────────────────────────────────────
        composable(AppRoute.ClimateDashboard.route) {
            com.example.afyagpt.ui.screens.climate.ClimateDashboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Emergency Mode Triage ───────────────────────────────────────────
        composable(AppRoute.EmergencyMode.route) {
            com.example.afyagpt.ui.screens.emergency.EmergencyModeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Voice AI Consultation ───────────────────────────────────────────
        composable(AppRoute.VoiceConsultation.route) {
            val voiceViewModel: com.example.afyagpt.ui.screens.voice.VoiceConsultationViewModel = hiltViewModel()
            com.example.afyagpt.ui.screens.voice.VoiceConsultationScreen(
                viewModel = voiceViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ════════════════════════════════════════════════════════════════════
        // PHASE 2 — IMCI TRIAGE FLOW
        // All triage screens share one TriageViewModel scoped to the triage
        // back-stack entry (AppRoute.TriageStart.route).
        // ════════════════════════════════════════════════════════════════════

        // Triage entry — patient selection
        composable(AppRoute.TriageStart.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            TriageStartScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDangerSigns = {
                    navController.navigate(AppRoute.DangerSigns.route)
                },
                onNavigateToRegisterPatient = {
                    navController.navigate(AppRoute.RegisterPatient.route)
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(AppRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // Step 1: Danger Signs
        composable(AppRoute.DangerSigns.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            DangerSignsScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVitals = { navController.navigate(AppRoute.Vitals.route) }
            )
        }

        // Step 2: Vital Signs
        composable(AppRoute.Vitals.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            VitalsScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRespTimer = { navController.navigate(AppRoute.RespRateTimer.route) },
                onNavigateToAssessment = { navController.navigate(AppRoute.CoughAssessment.route) }
            )
        }

        // Step 2b: Respiratory Rate Timer
        composable(AppRoute.RespRateTimer.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            val state by triageViewModel.state.collectAsState()
            RespRateTimerScreen(
                viewModel = triageViewModel,
                ageMonths = state.selectedPatientAgeMonths,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Step 3A: Cough / Respiratory Assessment
        composable(AppRoute.CoughAssessment.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            CoughAssessmentScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDiarrhea = { navController.navigate(AppRoute.DiarrheaAssessment.route) }
            )
        }

        // Step 3B: Diarrhea Assessment
        composable(AppRoute.DiarrheaAssessment.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            DiarrheaAssessmentScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFever = { navController.navigate(AppRoute.FeverAssessment.route) }
            )
        }

        // Step 3C: Fever Assessment
        composable(AppRoute.FeverAssessment.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            FeverAssessmentScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEar = { navController.navigate(AppRoute.EarAssessment.route) }
            )
        }

        // Step 3D: Ear Assessment
        composable(AppRoute.EarAssessment.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            EarAssessmentScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNutrition = { navController.navigate(AppRoute.NutritionAssessment.route) }
            )
        }

        // Step 4: Nutrition Assessment (last step before result)
        composable(AppRoute.NutritionAssessment.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            NutritionAssessmentScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { navController.navigate(AppRoute.TriageResult.route) }
            )
        }

        // Step 5: Triage Result
        composable(AppRoute.TriageResult.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            TriageResultScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Triage.route) { inclusive = true }
                    }
                },
                onCreateReferral = { /* Phase 6 */ },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(AppRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // Triage tab entry (Triage tab in bottom nav → goes to start screen)
        composable(AppRoute.Triage.route) {
            val triageViewModel: TriageViewModel = hiltViewModel()
            TriageStartScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDangerSigns = {
                    navController.navigate(AppRoute.DangerSigns.route)
                },
                onNavigateToRegisterPatient = {
                    navController.navigate(AppRoute.RegisterPatient.route)
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(AppRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ════════════════════════════════════════════════════════════════════
        // PHASE 3 — PATIENT RECORDS
        // ════════════════════════════════════════════════════════════════════

        // Records tab entry (bottom nav) → Patient list
        composable(AppRoute.Records.route) {
            val patientViewModel: PatientViewModel = hiltViewModel()
            PatientListScreen(
                viewModel = patientViewModel,
                onNavigateToDetail = { patientId ->
                    navController.navigate(AppRoute.PatientDetail.createRoute(patientId))
                },
                onNavigateToRegister = {
                    navController.navigate(AppRoute.RegisterPatient.route)
                },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // Register new patient
        composable(AppRoute.RegisterPatient.route) {
            val patientViewModel: PatientViewModel = hiltViewModel()
            RegisterPatientScreen(
                viewModel = patientViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // Patient detail
        composable(
            route = AppRoute.PatientDetail.route,
            arguments = listOf(navArgument(AppRoute.PatientDetail.ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt(AppRoute.PatientDetail.ARG) ?: return@composable
            val patientViewModel: PatientViewModel = hiltViewModel()
            PatientDetailScreen(
                patientId = patientId,
                viewModel = patientViewModel,
                onNavigateBack = { navController.popBackStack() },
                onStartTriage = { id ->
                    // Pre-select patient in TriageViewModel via TriageStartScreen
                    navController.navigate(AppRoute.TriageStart.route)
                },
                onNavigateToImmunization = { id ->
                    navController.navigate(AppRoute.Immunization.createRoute(id))
                },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // ════════════════════════════════════════════════════════════════════
        // PHASE 4 — IMMUNIZATION TRACKER
        // ════════════════════════════════════════════════════════════════════

        composable(
            route = AppRoute.Immunization.route,
            arguments = listOf(navArgument(AppRoute.Immunization.ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt(AppRoute.Immunization.ARG) ?: return@composable
            val immunizationViewModel: ImmunizationViewModel = hiltViewModel()
            ImmunizationScreen(
                patientId = patientId,
                patientName = "", // loaded internally by screen
                viewModel = immunizationViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(AppRoute.Library.route) { 
            LibraryScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(AppRoute.Profile.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(AppRoute.Search.route) {
            val searchViewModel: SearchViewModel = hiltViewModel()
            GlobalSearchScreen(
                viewModel = searchViewModel,
                onNavigateToPatientDetail = { patientId ->
                    navController.navigate(AppRoute.PatientDetail.createRoute(patientId))
                },
                onNavigateToLibrary = {
                    navController.navigate(AppRoute.Library.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

package com.example.afyagpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.domain.model.AppTheme
import com.example.afyagpt.ui.navigation.AfyaNavGraph
import com.example.afyagpt.ui.theme.AfyaGPTTheme
import com.example.afyagpt.util.AppLanguage
import com.example.afyagpt.util.LocalAppLanguage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity.kt — Single-Activity Entry Point
 *
 * Hosts the entire Compose navigation graph with dynamic language & theme re-composition support.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge drawing so content renders cleanly with WindowInsets padding
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val activeThemeStr by userPreferences.getActiveTheme()
                .collectAsState(initial = AppTheme.BLUE_YELLOW.name)
            val activeLangStr by userPreferences.getActiveLanguage()
                .collectAsState(initial = AppLanguage.ENGLISH.name)

            val appTheme = try {
                AppTheme.valueOf(activeThemeStr)
            } catch (e: Exception) {
                AppTheme.BLUE_YELLOW
            }
            val appLanguage = AppLanguage.fromCode(activeLangStr)

            CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                AfyaGPTTheme(appTheme = appTheme) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AfyaNavGraph(userPreferences = userPreferences)
                    }
                }
            }
        }
    }
}
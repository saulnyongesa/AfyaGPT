/**
 * AfyaGPTApp.kt
 *
 * Application entry point for the AfyaGPT Android application.
 *
 * This class must be declared in AndroidManifest.xml under the `android:name`
 * attribute of the `<application>` tag:
 *
 *   <application
 *       android:name=".AfyaGPTApp"
 *       ... />
 *
 * Package: com.example.afyagpt
 */
package com.example.afyagpt

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom [Application] class annotated with [@HiltAndroidApp].
 *
 * Why [@HiltAndroidApp] is required:
 * Hilt's code generation works by creating a base class at compile time that
 * contains all of the application-level dependency injection boilerplate. The
 * [@HiltAndroidApp] annotation tells the Hilt Gradle plugin to:
 *
 * 1. Generate a `Hilt_AfyaGPTApp` base class that initialises the
 *    [dagger.hilt.components.SingletonComponent] — the top of Hilt's component
 *    hierarchy.
 * 2. Wire the generated component to the Android application lifecycle so that
 *    singletons provided in [@InstallIn(SingletonComponent::class)] modules are
 *    created when the application process starts and destroyed when it ends.
 * 3. Enable member injection for any class annotated with [@AndroidEntryPoint],
 *    such as Activities, Fragments, Services, and ViewModels.
 *
 * Without this annotation, none of the @Inject constructors or @Provides methods
 * in the DI modules would function at runtime.
 */
@HiltAndroidApp
class AfyaGPTApp : Application() {

    /**
     * Called when the application is first created.
     *
     * The Hilt component is initialised automatically by the generated base class
     * before this method is called, so all singleton bindings are available by the
     * time any Activity or Service starts.
     *
     * Add any one-time application initialisation here (e.g., logging frameworks,
     * crash reporters, WorkManager configuration).
     */
    override fun onCreate() {
        super.onCreate()
        // Hilt's generated component initialisation happens in the super call above.
        // Additional SDK initialisation (e.g., Timber, Firebase) can be added here.
    }
}

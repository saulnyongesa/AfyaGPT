package com.example.afyagpt.domain.suggestion

import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.data.local.entity.TriageSessionEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ClinicalSuggestionCoordinator.kt
 *
 * Single entry point the rest of the app calls. Decides between a remote AI
 * provider (if one is injected and reachable) and the local rules provider.
 * Falls back silently to local rules on any AI failure, so the clinician is
 * never blocked by a network or AI outage.
 */
@Singleton
class ClinicalSuggestionCoordinator @Inject constructor(
    private val localProvider: LocalRuleSuggestionProvider,
    private val connectivityChecker: ConnectivityChecker
) {
    private var remoteProvider: RemoteAiSuggestionProvider? = null

    suspend fun getSuggestion(
        patient: PatientEntity,
        session: TriageSessionEntity,
        history: List<TriageSessionEntity> = emptyList()
    ): ClinicalSuggestion {
        val aiUsable = remoteProvider != null && connectivityChecker.isOnline()
        return if (aiUsable) {
            try {
                remoteProvider!!.suggest(patient, session, history)
            } catch (e: Exception) {
                localProvider.suggest(patient, session, history)
            }
        } else {
            localProvider.suggest(patient, session, history)
        }
    }
}

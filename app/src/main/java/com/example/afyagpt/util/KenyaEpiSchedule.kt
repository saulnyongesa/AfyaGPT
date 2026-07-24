package com.example.afyagpt.util

import com.example.afyagpt.data.local.entity.VaccinationEntity
import java.time.LocalDate

object KenyaEpiSchedule {
    /**
     * Kenya EPI schedule: maps each vaccine to the age in weeks when it is due.
     * Generates VaccinationEntity list safely from patient DOB.
     */
    private val schedule = listOf(
        // name, weeksFromBirth, doseNumber
        Triple("BCG", 0, 1),
        Triple("OPV-0", 0, 1),
        Triple("OPV-1", 6, 1),
        Triple("Penta-1", 6, 1),
        Triple("PCV-1", 6, 1),
        Triple("Rota-1", 6, 1),
        Triple("OPV-2", 10, 2),
        Triple("Penta-2", 10, 2),
        Triple("PCV-2", 10, 2),
        Triple("Rota-2", 10, 2),
        Triple("OPV-3", 14, 3),
        Triple("Penta-3", 14, 3),
        Triple("PCV-3", 14, 3),
        Triple("Vitamin A (1st)", 26, 1),  // 6 months
        Triple("MR-1", 39, 1),             // 9 months
        Triple("Yellow Fever", 39, 1),
        Triple("MR-2", 52, 2),             // 12 months
        Triple("MR-3 (Booster)", 78, 3)   // 18 months
    )

    fun generateFor(patientId: Int, dateOfBirth: String, registeredBy: Int): List<VaccinationEntity> {
        val dob = try {
            LocalDate.parse(dateOfBirth.trim())
        } catch (e: Exception) {
            LocalDate.now()
        }
        val today = LocalDate.now()
        val nowTime = DateTimeUtils.now()

        return schedule.map { (name, weeks, dose) ->
            val scheduledDate = dob.plusWeeks(weeks.toLong())
            val status = if (scheduledDate.isBefore(today)) "OVERDUE" else "PENDING"
            VaccinationEntity(
                patientId = patientId,
                vaccineName = name,
                doseNumber = dose,
                scheduledDate = scheduledDate.toString(),
                status = status,
                createdAt = nowTime,
                isSynced = false
            )
        }
    }
}

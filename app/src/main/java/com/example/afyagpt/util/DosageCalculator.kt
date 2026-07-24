package com.example.afyagpt.util

/**
 * Data class representing a treatment plan recommendation.
 */
data class Treatment(
    val medicine: String,
    val dose: String,
    val frequency: String,
    val days: Int,
    val instructions: String
)

/**
 * Utility object for calculating dosages according to IMCI protocols.
 */
object DosageCalculator {

    /**
     * Calculates the dosage for Amoxicillin based on weight.
     * @param weightKg Weight of the child in kg.
     * @return [Treatment] recommendation.
     */
    fun amoxicillin(weightKg: Float): Treatment {
        return when {
            weightKg < 6f -> Treatment("Amoxicillin", "125mg", "BD (twice daily)", 5, "Give 125mg twice daily for 5 days")
            weightKg < 10f -> Treatment("Amoxicillin", "250mg", "BD (twice daily)", 5, "Give 250mg twice daily for 5 days")
            weightKg < 19f -> Treatment("Amoxicillin", "500mg", "BD (twice daily)", 5, "Give 500mg twice daily for 5 days")
            else -> Treatment("Amoxicillin", "500mg", "TDS (three times daily)", 5, "Give 500mg three times daily for 5 days")
        }
    }

    /**
     * Calculates the dosage for Artemether-Lumefantrine (ACT) based on weight.
     * @param weightKg Weight of the child in kg.
     * @return [Treatment] recommendation.
     */
    fun artemetherLumefantrine(weightKg: Float): Treatment {
        return when {
            weightKg < 15f -> Treatment("Artemether-Lumefantrine", "1 tab/dose", "BD", 3, "Give 1 tablet per dose, 6 doses over 3 days (0h, 8h, 24h, 36h, 48h, 60h)")
            weightKg < 25f -> Treatment("Artemether-Lumefantrine", "2 tabs/dose", "BD", 3, "Give 2 tablets per dose, 6 doses over 3 days")
            weightKg < 35f -> Treatment("Artemether-Lumefantrine", "3 tabs/dose", "BD", 3, "Give 3 tablets per dose, 6 doses over 3 days")
            else -> Treatment("Artemether-Lumefantrine", "4 tabs/dose", "BD", 3, "Give 4 tablets per dose, 6 doses over 3 days")
        }
    }

    /**
     * Recommends ORS treatment based on the plan type.
     * @param planType The dehydration plan ("Plan A", "Plan B", "Plan C").
     * @return [Treatment] recommendation.
     */
    fun ors(planType: String): Treatment {
        return when (planType) {
            "Plan A" -> Treatment("ORS", "50-100ml", "After each loose stool", 1, "Give 50-100ml after each loose stool")
            "Plan B" -> Treatment("ORS", "75ml/kg", "Over 4 hours", 1, "Give 75ml/kg over 4 hours in clinic")
            else -> Treatment("IV Fluids", "Immediate", "Stat", 1, "Refer immediately for IV fluids")
        }
    }

    /**
     * Calculates the dosage for Zinc based on age.
     * @param ageMonths Age of the child in months.
     * @return [Treatment] recommendation.
     */
    fun zinc(ageMonths: Int): Treatment {
        return if (ageMonths < 6) {
            Treatment("Zinc", "10mg", "OD (once daily)", 14, "Give 10mg once daily for 14 days")
        } else {
            Treatment("Zinc", "20mg", "OD (once daily)", 14, "Give 20mg once daily for 14 days")
        }
    }

    /**
     * Calculates the dosage for Paracetamol based on weight.
     * @param weightKg Weight of the child in kg.
     * @return [Treatment] recommendation.
     */
    fun paracetamol(weightKg: Float): Treatment {
        val dose = (weightKg * 12.5f).toInt()
        return Treatment("Paracetamol", "${dose}mg", "Every 6 hours", 3, "Give ${dose}mg every 6 hours (max 4 doses/day)")
    }
}

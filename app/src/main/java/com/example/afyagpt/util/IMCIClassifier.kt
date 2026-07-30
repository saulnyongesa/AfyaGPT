package com.example.afyagpt.util

/**
 * IMCI Classifier object containing enums and methods for standard IMCI protocols.
 */
object IMCIClassifier {

    /**
     * Represents various risk levels across classifications.
     */
    enum class RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

    /**
     * Specific classifications under IMCI guidelines mapped to risk levels.
     */
    enum class IMCIClassification(val label: String, val riskLevel: RiskLevel) {
        // Respiratory
        SEVERE_PNEUMONIA("Severe Pneumonia", RiskLevel.CRITICAL),
        PNEUMONIA("Pneumonia", RiskLevel.HIGH),
        NO_PNEUMONIA("No Pneumonia", RiskLevel.LOW),
        
        // Diarrhea
        SEVERE_DEHYDRATION("Severe Dehydration", RiskLevel.CRITICAL),
        SOME_DEHYDRATION("Some Dehydration", RiskLevel.HIGH),
        NO_DEHYDRATION("No Dehydration", RiskLevel.LOW),
        PERSISTENT_DIARRHEA("Persistent Diarrhea", RiskLevel.HIGH),
        DYSENTERY("Dysentery", RiskLevel.HIGH),
        
        // Fever
        SEVERE_FEBRILE_DISEASE("Severe Febrile Disease", RiskLevel.CRITICAL),
        MALARIA("Malaria", RiskLevel.HIGH),
        FEVER_NO_MALARIA("Fever - No Malaria", RiskLevel.MEDIUM),
        
        // Ear
        MASTOIDITIS("Mastoiditis", RiskLevel.CRITICAL),
        ACUTE_EAR_INFECTION("Acute Ear Infection", RiskLevel.HIGH),
        CHRONIC_EAR_INFECTION("Chronic Ear Infection", RiskLevel.MEDIUM),
        NO_EAR_PROBLEM("No Ear Problem", RiskLevel.LOW),
        
        // Nutrition
        SAM("Severe Acute Malnutrition", RiskLevel.CRITICAL),
        MAM("Moderate Acute Malnutrition", RiskLevel.HIGH),
        NOT_MALNOURISHED("Not Malnourished", RiskLevel.LOW),
        SEVERE_ANEMIA("Severe Anemia", RiskLevel.CRITICAL),
        SOME_ANEMIA("Some Anemia", RiskLevel.HIGH),
        NO_ANEMIA("No Anemia", RiskLevel.LOW)
    }

    /**
     * Classifies respiratory condition based on symptoms.
     * @param ageMonths the age of the child in months.
     * @param respRate the respiratory rate (breaths per min).
     * @param chestIndrawing true if chest indrawing is present.
     * @param stridor true if stridor is present in a calm child.
     * @return the [IMCIClassification] for respiratory status.
     */
    fun classifyRespiratory(ageMonths: Int, respRate: Int?, chestIndrawing: Boolean, stridor: Boolean): IMCIClassification {
        if (stridor || chestIndrawing) return IMCIClassification.SEVERE_PNEUMONIA
        if (respRate != null) {
            val threshold = when {
                ageMonths < 2 -> 60
                ageMonths in 2..11 -> 50
                else -> 40 // 1-5y is 12-60 months
            }
            if (respRate >= threshold) return IMCIClassification.PNEUMONIA
        }
        return IMCIClassification.NO_PNEUMONIA
    }

    /**
     * Classifies diarrhea condition based on symptoms.
     * @param hasDiarrhea whether the child has diarrhea.
     * @param daysWithDiarrhea duration of diarrhea in days.
     * @param bloodInStool true if there is blood in stool.
     * @param sunkenEyes true if the eyes are sunken.
     * @param skinPinch the result of the skin pinch test ("VERY_SLOWLY", "SLOWLY", or normal).
     * @param drinkingAbility the ability to drink ("CANNOT_DRINK", "DRINKS_POORLY", or normal).
     * @return the [IMCIClassification] for diarrhea or null if no diarrhea.
     */
    fun classifyDiarrhea(
        hasDiarrhea: Boolean, 
        daysWithDiarrhea: Int, 
        bloodInStool: Boolean, 
        sunkenEyes: Boolean, 
        skinPinch: String, 
        drinkingAbility: String
    ): IMCIClassification? {
        if (!hasDiarrhea) return null
        if (skinPinch == "VERY_SLOWLY" || drinkingAbility == "CANNOT_DRINK") return IMCIClassification.SEVERE_DEHYDRATION
        if (sunkenEyes || skinPinch == "SLOWLY" || drinkingAbility == "DRINKS_POORLY") return IMCIClassification.SOME_DEHYDRATION
        if (bloodInStool) return IMCIClassification.DYSENTERY
        if (daysWithDiarrhea >= 14) return IMCIClassification.PERSISTENT_DIARRHEA
        return IMCIClassification.NO_DEHYDRATION
    }

    /**
     * Classifies fever condition based on symptoms.
     * @param hasFever whether the child has fever.
     * @param temperatureC the measured body temperature in Celsius.
     * @param rdtResult the Malaria RDT result ("POSITIVE", "NEGATIVE", etc).
     * @param stiffNeck true if the child has a stiff neck.
     * @param measlesRash true if there is a generalized rash indicating measles.
     * @param daysWithFever duration of fever in days.
     * @param malariaRiskZone the malaria risk zone the child lives in.
     * @return the [IMCIClassification] for fever or null if no fever.
     */
    fun classifyFever(
        hasFever: Boolean,
        temperatureC: Float?,
        rdtResult: String,
        stiffNeck: Boolean,
        measlesRash: Boolean,
        daysWithFever: Int,
        malariaRiskZone: String
    ): IMCIClassification? {
        val isFeverPresent = hasFever || (temperatureC != null && temperatureC >= 37.5f)
        val isHypothermia = (temperatureC != null && temperatureC < 35.5f)

        if (!isFeverPresent && !isHypothermia) return null
        if (stiffNeck || isHypothermia || (measlesRash && daysWithFever > 3)) return IMCIClassification.SEVERE_FEBRILE_DISEASE
        if (rdtResult == "POSITIVE") return IMCIClassification.MALARIA
        return IMCIClassification.FEVER_NO_MALARIA
    }

    /**
     * Classifies ear condition based on symptoms.
     * @param earPain true if the child has ear pain.
     * @param earDischarge true if there is pus discharging from the ear.
     * @param dischargeDays number of days with ear discharge.
     * @param mastoidTenderness true if there is tender swelling behind the ear.
     * @return the [IMCIClassification] for ear problems.
     */
    fun classifyEar(earPain: Boolean, earDischarge: Boolean, dischargeDays: Int, mastoidTenderness: Boolean): IMCIClassification {
        if (mastoidTenderness) return IMCIClassification.MASTOIDITIS
        if (earPain || (earDischarge && dischargeDays < 14)) return IMCIClassification.ACUTE_EAR_INFECTION
        if (earDischarge && dischargeDays >= 14) return IMCIClassification.CHRONIC_EAR_INFECTION
        return IMCIClassification.NO_EAR_PROBLEM
    }

    /**
     * Classifies nutrition condition based on symptoms.
     * @param muacMm Mid-Upper Arm Circumference in mm.
     * @param visibleWasting true if there is severe visible wasting.
     * @param bilateralOedema true if there is oedema of both feet.
     * @param palmPallor the status of palmar pallor ("SEVERE", "MILD", "NONE").
     * @return A Pair containing the nutrition [IMCIClassification] and anemia [IMCIClassification].
     */
    fun classifyNutrition(muacMm: Int?, visibleWasting: Boolean, bilateralOedema: Boolean, palmPallor: String): Pair<IMCIClassification, IMCIClassification> {
        val nutritionClass = if (visibleWasting || bilateralOedema || (muacMm != null && muacMm < 115)) {
            IMCIClassification.SAM
        } else if (muacMm != null && muacMm in 115..124) {
            IMCIClassification.MAM
        } else {
            IMCIClassification.NOT_MALNOURISHED
        }

        val anemiaClass = when (palmPallor) {
            "SEVERE" -> IMCIClassification.SEVERE_ANEMIA
            "MILD" -> IMCIClassification.SOME_ANEMIA
            else -> IMCIClassification.NO_ANEMIA
        }

        return Pair(nutritionClass, anemiaClass)
    }

    /**
     * Determines the highest overall risk level among all the child's classifications.
     * @param classifications a vararg of [IMCIClassification] instances (which may be null).
     * @return the highest [RiskLevel] detected.
     */
    fun overallRisk(vararg classifications: IMCIClassification?): RiskLevel {
        return classifications
            .filterNotNull()
            .maxByOrNull { it.riskLevel }
            ?.riskLevel ?: RiskLevel.LOW
    }

    /**
     * Generates a comprehensive treatment plan based on classifications.
     * @param classifications a list of the child's [IMCIClassification]s.
     * @param weightKg the weight of the child in kg.
     * @param ageMonths the age of the child in months.
     * @return a list of [Treatment] recommendations.
     */
    fun generateTreatmentPlan(classifications: List<IMCIClassification?>, weightKg: Float, ageMonths: Int): List<Treatment> {
        val treatments = mutableListOf<Treatment>()
        
        classifications.filterNotNull().forEach { classification ->
            when (classification) {
                IMCIClassification.PNEUMONIA -> {
                    treatments.add(DosageCalculator.amoxicillin(weightKg))
                }
                IMCIClassification.MALARIA -> {
                    treatments.add(DosageCalculator.artemetherLumefantrine(weightKg))
                    treatments.add(DosageCalculator.paracetamol(weightKg))
                }
                IMCIClassification.SOME_DEHYDRATION -> {
                    treatments.add(DosageCalculator.ors("Plan B"))
                    treatments.add(DosageCalculator.zinc(ageMonths))
                }
                IMCIClassification.SEVERE_DEHYDRATION -> {
                    treatments.add(DosageCalculator.ors("Plan C"))
                }
                IMCIClassification.ACUTE_EAR_INFECTION -> {
                    treatments.add(DosageCalculator.amoxicillin(weightKg))
                    treatments.add(DosageCalculator.paracetamol(weightKg))
                }
                IMCIClassification.DYSENTERY -> {
                    // Usually ciprofloxacin, but adding basic ORS for now
                    treatments.add(DosageCalculator.ors("Plan A"))
                    treatments.add(DosageCalculator.zinc(ageMonths))
                }
                else -> {
                    // Other conditions may require refer, or no specific drug here.
                }
            }
        }
        
        return treatments.distinctBy { it.medicine }
    }

    /**
     * Generates a list of counseling messages to give the caregiver based on the condition.
     * @param classifications a list of the child's [IMCIClassification]s.
     * @return a list of strings containing counseling messages.
     */
    fun generateCounselingMessages(classifications: List<IMCIClassification?>): List<String> {
        val messages = mutableListOf<String>()
        
        classifications.filterNotNull().forEach { classification ->
            when (classification) {
                IMCIClassification.PNEUMONIA -> {
                    messages.add("Give amoxicillin as prescribed. Seek care immediately if breathing worsens.")
                }
                IMCIClassification.SOME_DEHYDRATION -> {
                    messages.add("Give ORS sips every 5 minutes. Show caregiver how to mix ORS.")
                }
                IMCIClassification.MALARIA -> {
                    messages.add("Give full course of ACT. Do not stop early even if child improves.")
                }
                IMCIClassification.NO_PNEUMONIA -> {
                    messages.add("Soothe the throat and relieve the cough with a safe remedy.")
                }
                IMCIClassification.MAM -> {
                    messages.add("Assess the child's feeding and counsel the mother on feeding according to the Food Box.")
                }
                else -> {
                    // Add generic or specific messages based on other classifications.
                }
            }
        }
        
        return messages.distinct()
    }
}

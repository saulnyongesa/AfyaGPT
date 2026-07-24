package com.example.afyagpt.data.repository

import com.example.afyagpt.data.local.dao.VaccinationDao
import com.example.afyagpt.data.local.entity.VaccinationEntity
import com.example.afyagpt.util.KenyaEpiSchedule
import javax.inject.Inject

class VaccinationRepository @Inject constructor(private val vaccinationDao: VaccinationDao) {
    fun getVaccinationsForPatient(patientId: Int) = vaccinationDao.getVaccinationsForPatient(patientId)
    suspend fun insertVaccination(v: VaccinationEntity): Long = vaccinationDao.insertVaccination(v)
    suspend fun markVaccineGiven(id: Int, date: String, administeredBy: Int) = vaccinationDao.markAsGiven(id, date, administeredBy)
    fun getOverdueVaccinations() = vaccinationDao.getOverdueVaccinations()

    /** Generates and inserts the full Kenya EPI schedule for a new patient. */
    suspend fun generateEpiSchedule(patientId: Int, dateOfBirth: String, registeredBy: Int) {
        val schedule = KenyaEpiSchedule.generateFor(patientId, dateOfBirth, registeredBy)
        schedule.forEach { vaccinationDao.insertVaccination(it) }
    }
}

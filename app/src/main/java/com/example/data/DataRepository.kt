package com.example.data

import kotlinx.coroutines.flow.Flow

class DataRepository(private val dao: AppDao) {

    val patient: Flow<PatientEntity?> = dao.getPatient()
    val guardian: Flow<GuardianEntity?> = dao.getGuardian()
    val medications: Flow<List<MedicationEntity>> = dao.getAllMedications()
    val familyMembers: Flow<List<FamilyMemberEntity>> = dao.getAllFamilyMembers()
    val safeZone: Flow<SafeZoneEntity?> = dao.getSafeZone()
    val emergencyLogs: Flow<List<EmergencyLogEntity>> = dao.getAllEmergencyLogs()
    val memoryPhotos: Flow<List<MemoryPhotoEntity>> = dao.getMemoryPhotos()
    val cognitiveScores: Flow<List<CognitiveScoreEntity>> = dao.getCognitiveScores()

    suspend fun insertPatient(patient: PatientEntity) = dao.insertPatient(patient)
    suspend fun insertGuardian(guardian: GuardianEntity) = dao.insertGuardian(guardian)
    suspend fun insertMedication(medication: MedicationEntity) = dao.insertMedication(medication)
    suspend fun deleteMedication(medication: MedicationEntity) = dao.deleteMedication(medication)
    suspend fun updateMedicationTaken(id: Int, taken: Boolean) = dao.updateMedicationTaken(id, taken)
    suspend fun insertFamilyMember(member: FamilyMemberEntity) = dao.insertFamilyMember(member)
    suspend fun deleteFamilyMember(member: FamilyMemberEntity) = dao.deleteFamilyMember(member)
    suspend fun insertSafeZone(safeZone: SafeZoneEntity) = dao.insertSafeZone(safeZone)
    suspend fun insertEmergencyLog(log: EmergencyLogEntity) = dao.insertEmergencyLog(log)
    suspend fun clearEmergencyLogs() = dao.clearEmergencyLogs()
    suspend fun insertMemoryPhoto(photo: MemoryPhotoEntity) = dao.insertMemoryPhoto(photo)
    suspend fun deleteMemoryPhoto(id: Int) = dao.deleteMemoryPhoto(id)
    suspend fun insertCognitiveScore(score: CognitiveScoreEntity) = dao.insertCognitiveScore(score)
}

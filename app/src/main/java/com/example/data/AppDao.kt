package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM patient WHERE id = 1 LIMIT 1")
    fun getPatient(): Flow<PatientEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity)

    @Query("SELECT * FROM guardian WHERE id = 1 LIMIT 1")
    fun getGuardian(): Flow<GuardianEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuardian(guardian: GuardianEntity)

    @Query("SELECT * FROM medications ORDER BY time ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET takenStatus = :takenStatus WHERE id = :medicationId")
    suspend fun updateMedicationTaken(medicationId: Int, takenStatus: Boolean)

    @Query("SELECT * FROM family_members ORDER BY id DESC")
    fun getAllFamilyMembers(): Flow<List<FamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMemberEntity)

    @Delete
    suspend fun deleteFamilyMember(member: FamilyMemberEntity)

    @Query("SELECT * FROM safe_zones WHERE id = 1 LIMIT 1")
    fun getSafeZone(): Flow<SafeZoneEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafeZone(safeZone: SafeZoneEntity)

    @Query("SELECT * FROM emergency_logs ORDER BY time DESC")
    fun getAllEmergencyLogs(): Flow<List<EmergencyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyLog(log: EmergencyLogEntity)

    @Query("DELETE FROM emergency_logs")
    suspend fun clearEmergencyLogs()

    @Query("SELECT * FROM memory_photos ORDER BY id DESC")
    fun getMemoryPhotos(): Flow<List<MemoryPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoryPhoto(photo: MemoryPhotoEntity)

    @Query("DELETE FROM memory_photos WHERE id = :id")
    suspend fun deleteMemoryPhoto(id: Int)

    @Query("SELECT * FROM cognitive_scores ORDER BY id DESC")
    fun getCognitiveScores(): Flow<List<CognitiveScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCognitiveScore(score: CognitiveScoreEntity)
}

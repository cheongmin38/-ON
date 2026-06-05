package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient")
data class PatientEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val birthDate: String,
    val homeAddress: String,
    val emergencyContact: String,
    val guardianId: Int = 1
)

@Entity(tableName = "guardian")
data class GuardianEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val phone: String,
    val relationship: String,
    val linkedPatientId: Int = 1
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val time: String, // format e.g., "09:00" or "오전 09:00"
    val takenStatus: Boolean = false, // true = 먹었어요, false = 아직 미복용
    val memo: String = ""
)

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val relationship: String,
    val phone: String,
    val photoUrl: String, // resource drawable, URI or placeholder
    val description: String
)

@Entity(tableName = "safe_zones")
data class SafeZoneEntity(
    @PrimaryKey val id: Int = 1, // Let's keep a single active safe zone for simplicity or standard safe zone list
    val name: String = "집",
    val address: String,
    val radius: Int, // 300, 500, 1000 meters
    val latitude: Double = 37.1234, // Simulated default
    val longitude: Double = 127.1234
)

@Entity(tableName = "emergency_logs")
data class EmergencyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Int = 1,
    val time: Long = System.currentTimeMillis(),
    val location: String,
    val status: String // e.g. "긴급 호출", "안외 구역 이탈"
)

// Past photos for AI recollection therapy
@Entity(tableName = "memory_photos")
data class MemoryPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val photoUrl: String,
    val description: String,
    val aiResponse: String = ""
)

// Cognitive test results
@Entity(tableName = "cognitive_scores")
data class CognitiveScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val date: String, // date "2026-06-05"
    val feedback: String
)

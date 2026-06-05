package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = DataRepository(db.appDao)

    // Mode state: "SPLASH", "LOGIN", "USER_TYPE_SELECTION", "ONBOARDING_GUARDIAN", "ONBOARDING_PATIENT", "REGISTER_GUARDIAN", "REGISTER_PATIENT", "PATIENT", "GUARDIAN"
    private val _currentMode = MutableStateFlow("SPLASH")
    val currentMode: StateFlow<String> = _currentMode.asStateFlow()

    // Screen state inside specific modes
    private val _currentScreen = MutableStateFlow("MAIN") // e.g. "MAIN", "SAFE_ZONE", "MEDICINES", "FAMILY", "THERAPY", "GAME"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Simulated states
    private val _simulatedLocation = MutableStateFlow("서울특별시 마포구 공덕동 영진아파트 근처")
    val simulatedLocation: StateFlow<String> = _simulatedLocation.asStateFlow()

    private val _lastLocationUpdate = MutableStateFlow("3분 전 업데이트")
    val lastLocationUpdate: StateFlow<String> = _lastLocationUpdate.asStateFlow()

    private val _isOutsideSafeZone = MutableStateFlow(false)
    val isOutsideSafeZone: StateFlow<Boolean> = _isOutsideSafeZone.asStateFlow()

    // Alarm states
    private val _activeMedicationAlarm = MutableStateFlow<MedicationEntity?>(null)
    val activeMedicationAlarm: StateFlow<MedicationEntity?> = _activeMedicationAlarm.asStateFlow()

    private val _activeSosLog = MutableStateFlow<EmergencyLogEntity?>(null)
    val activeSosLog: StateFlow<EmergencyLogEntity?> = _activeSosLog.asStateFlow()

    // Local UI loading/interactive states
    private val _aiSummaryText = MutableStateFlow("")
    val aiSummaryText: StateFlow<String> = _aiSummaryText.asStateFlow()

    private val _aiTherapyResult = MutableStateFlow("")
    val aiTherapyResult: StateFlow<String> = _aiTherapyResult.asStateFlow()

    private val _isLoadingAi = MutableStateFlow(false)
    val isLoadingAi: StateFlow<Boolean> = _isLoadingAi.asStateFlow()

    // Observe Room DB flows
    val patient: StateFlow<PatientEntity?> = repository.patient.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val guardian: StateFlow<GuardianEntity?> = repository.guardian.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val medications: StateFlow<List<MedicationEntity>> = repository.medications.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val familyMembers: StateFlow<List<FamilyMemberEntity>> = repository.familyMembers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val safeZone: StateFlow<SafeZoneEntity?> = repository.safeZone.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val emergencyLogs: StateFlow<List<EmergencyLogEntity>> = repository.emergencyLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val memoryPhotos: StateFlow<List<MemoryPhotoEntity>> = repository.memoryPhotos.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val cognitiveScores: StateFlow<List<CognitiveScoreEntity>> = repository.cognitiveScores.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Pre-populate core MVP database models on first launch
        viewModelScope.launch {
            // Check if patient exists
            repository.patient.first().let { currentPatient ->
                if (currentPatient == null) {
                    populateDefaultData()
                }
            }
        }
    }

    fun setMode(mode: String) {
        _currentMode.value = mode
        _currentScreen.value = "MAIN"
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    private suspend fun populateDefaultData() {
        // 1. Patient Profile
        repository.insertPatient(
            PatientEntity(
                id = 1,
                name = "김만옥",
                birthDate = "1952년 11월 12일",
                homeAddress = "서울특별시 마포구 공덕동 102",
                emergencyContact = "010-1234-5678" // son's number
            )
        )

        // 2. Guardian Profile
        repository.insertGuardian(
            GuardianEntity(
                id = 1,
                name = "김민수",
                phone = "010-1234-5678",
                relationship = "아들 (장남)"
            )
        )

        // 3. Companion Medications
        repository.insertMedication(
            MedicationEntity(
                id = 0,
                name = "아침 혈압약",
                dosage = "1정",
                time = "오전 09:00",
                takenStatus = true,
                memo = "식사 직후 드셔야 속이 편안합니다"
            )
        )
        repository.insertMedication(
            MedicationEntity(
                id = 0,
                name = "기억력 영양제 (도네페질)",
                dosage = "2알",
                time = "오후 01:00",
                takenStatus = false,
                memo = "아들이 매일 드시라고 강조한 뇌 보조 영양제"
            )
        )

        // 4. Family Members with beautiful default profiles
        repository.insertFamilyMember(
            FamilyMemberEntity(
                id = 0,
                name = "김민수",
                relationship = "큰아들",
                phone = "010-1234-5678",
                photoUrl = "son", // identifier
                description = "세심하고 효심 깊은 우리 장남. 매주 주말마다 전화를 해줍니다."
            )
        )
        repository.insertFamilyMember(
            FamilyMemberEntity(
                id = 0,
                name = "박소연",
                relationship = "며느리",
                phone = "010-9876-5432",
                photoUrl = "d_in_law", // identifier
                description = "요리를 참 잘해서 고기 반찬이나 따뜻한 죽을 매번 끓여와 줍니다."
            )
        )
        repository.insertFamilyMember(
            FamilyMemberEntity(
                id = 0,
                name = "김은지",
                relationship = "여고생 손녀",
                phone = "010-4455-8899",
                photoUrl = "granddaughter", // identifier
                description = "귀엽게 조잘조잘 학교 얘기를 해주는 예쁜 우리 손녀딸."
            )
        )

        // 5. Default Safe Zone config
        repository.insertSafeZone(
            SafeZoneEntity(
                id = 1,
                name = "집",
                address = "서울특별시 마포구 공덕동 102",
                radius = 300
            )
        )

        // 6. Memory Therapy initial entries
        repository.insertMemoryPhoto(
            MemoryPhotoEntity(
                id = 0,
                photoUrl = "jeju",
                description = "가족과 함께 제주도 협재 에메랄드빛 해변에서 활짝 웃으며 찍은 사진입니다.",
                aiResponse = "김만옥 님, 기억하시나요? 이 사진은 사랑하는 아들 민수와 함께 제주도의 푸른 바다를 보러 갔을 때 찍은 사진입니다. 시원한 파도 소리를 들으며 다 함께 미소 짓던 그 행복하고 편안했던 하루처럼, 마음속에 따뜻함이 가득 차오릅니다."
            )
        )
        repository.insertMemoryPhoto(
            MemoryPhotoEntity(
                id = 0,
                photoUrl = "birthday",
                description = "첫 손녀 은지의 생일잔치 때 가족들이 모여 촛불을 끄던 순간입니다.",
                aiResponse = "은지의 첫 돌잔치 날이네요! 분홍빛 고운 아기 한복을 입힌 은지를 품에 꼭 안고 계셨던 날입니다. 은지가 건강하게 자라라고 온 가족이 손뼉 부딪치던 따사로웠던 돌잔치의 울림이 참 은은하게 전해져 옵니다."
            )
        )

        // 7. Initial Cognitive Scores to establish historical progress
        repository.insertCognitiveScore(
            CognitiveScoreEntity(
                id = 0,
                score = 80,
                date = "6월 4일",
                feedback = "기록 이력이 매우 좋습니다! 어제 집중도 80점으로 훈련을 잘 마무리하셨습니다."
            )
        )
    }

    // --- Action functions ---

    fun updatePatientProfile(name: String, address: String, contact: String) {
        viewModelScope.launch {
            repository.insertPatient(
                PatientEntity(
                    id = 1,
                    name = name,
                    birthDate = "1952년 11월 12일",
                    homeAddress = address,
                    emergencyContact = contact
                )
            )
        }
    }

    fun updateSafeZone(radius: Int, address: String) {
        viewModelScope.launch {
            repository.insertSafeZone(
                SafeZoneEntity(
                    id = 1,
                    name = "마포구 자택",
                    address = address,
                    radius = radius
                )
            )
            // If location changed back, resolve deviation
            if (address == "서울특별시 마포구 공덕동 102") {
                _isOutsideSafeZone.value = false
            }
        }
    }

    fun addMedication(name: String, dosage: String, time: String, memo: String) {
        viewModelScope.launch {
            repository.insertMedication(
                MedicationEntity(
                    name = name,
                    dosage = dosage,
                    time = time,
                    takenStatus = false,
                    memo = memo
                )
            )
        }
    }

    fun toggleMedicationTaken(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.updateMedicationTaken(medication.id, !medication.takenStatus)
            // If checking it off, dismiss alarm if matching
            if (_activeMedicationAlarm.value?.id == medication.id) {
                _activeMedicationAlarm.value = null
            }
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }

    fun addFamilyMember(name: String, relationship: String, phone: String, photo: String, description: String) {
        viewModelScope.launch {
            repository.insertFamilyMember(
                FamilyMemberEntity(
                    name = name,
                    relationship = relationship,
                    phone = phone,
                    photoUrl = photo,
                    description = description
                )
            )
        }
    }

    fun deleteFamilyMember(member: FamilyMemberEntity) {
        viewModelScope.launch {
            repository.deleteFamilyMember(member)
        }
    }

    // --- Simulation Helpers (Interactive demo features) ---

    // Trigger Mock Location Outbound (안전구역 이탈 시뮬레이션)
    fun simulateSafeZoneDeparture() {
        viewModelScope.launch {
            _simulatedLocation.value = "용산구 한강대로 지하철역 복잡한 로터리 부근"
            _lastLocationUpdate.value = "방금 전 업데이트"
            _isOutsideSafeZone.value = true

            // Write to database Emergency Logs
            val alertLog = EmergencyLogEntity(
                location = "용산구 한강대로 부근 (집 반경 1.2km 이탈)",
                status = "안전구역 이탈 경고"
            )
            repository.insertEmergencyLog(alertLog)
        }
    }

    fun simulateSafeZoneReturn() {
        _isOutsideSafeZone.value = false
        _simulatedLocation.value = "서울특별시 마포구 공덕동 102 (거주지 안)"
        _lastLocationUpdate.value = "방금 전 업데이트"
    }

    // Trigger Patient medication alarm manually for visual demo
    fun triggerMockMedicationAlarm(medication: MedicationEntity) {
        _activeMedicationAlarm.value = medication
    }

    fun dismissMedicationAlarm() {
        _activeMedicationAlarm.value = null
    }

    // Trigger Patient SOS Emergency Alert (긴급 알림 시뮬레이션)
    fun triggerPatientEmergencySos() {
        viewModelScope.launch {
            val sosLog = EmergencyLogEntity(
                location = _simulatedLocation.value,
                status = "긴급 구조 SOS 호출"
            )
            repository.insertEmergencyLog(sosLog)
            _activeSosLog.value = sosLog
        }
    }

    fun dismissEmergencySos() {
        _activeSosLog.value = null
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearEmergencyLogs()
        }
    }

    // --- AI REST API Methods ---

    // 1. Generate Warm Photo description for Dementia Patient
    fun requestAiMemoryTherapy(photoTitle: String, description: String, relationship: String) {
        viewModelScope.launch {
            _isLoadingAi.value = true
            _aiTherapyResult.value = ""
            try {
                // Call Gemini using our service model
                val result = GeminiService.generateMemoryTherapyDescription(photoTitle, relationship)
                _aiTherapyResult.value = result
                
                // Save this to memory photos list so persistence works!
                repository.insertMemoryPhoto(
                    MemoryPhotoEntity(
                        photoUrl = "user_added",
                        description = description,
                        aiResponse = result
                    )
                )
            } catch (e: Exception) {
                _aiTherapyResult.value = "따뜻한 기억을 로딩하지 못했습니다. 하지만 가족은 항상 당신 편입니다."
            } finally {
                _isLoadingAi.value = false
            }
        }
    }

    // 2. Generate caretaker summary based on daily database entries
    fun requestGuardianSummary() {
        viewModelScope.launch {
            _isLoadingAi.value = true
            _aiSummaryText.value = ""
            try {
                // Gather database statistics
                val medList = medications.value
                val totalMeds = medList.size
                val takenMeds = medList.count { it.takenStatus }
                val logsList = emergencyLogs.value
                val safeZoneAlertsCount = logsList.count { it.status.contains("이탈") }
                val lastScoreVal = cognitiveScores.value.firstOrNull()?.score ?: 75
                val currentLoc = _simulatedLocation.value
                val patientNameValue = patient.value?.name ?: "어머니"

                val summary = GeminiService.generateGuardianDailySummary(
                    patientName = patientNameValue,
                    takenMeds = takenMeds,
                    totalMeds = totalMeds,
                    safeZoneAlerts = safeZoneAlertsCount,
                    cognitiveScore = lastScoreVal,
                    lastLocation = currentLoc
                )
                _aiSummaryText.value = summary
            } catch (e: Exception) {
                _aiSummaryText.value = "상태 기록을 분석하는 도중 오류가 발생했습니다. 수기로 기록된 대시보드를 참고하세요."
            } finally {
                _isLoadingAi.value = false
            }
        }
    }

    // Insert custom cognitive test score
    fun recordCognitiveScore(score: Int, remarks: String) {
        viewModelScope.launch {
            repository.insertCognitiveScore(
                CognitiveScoreEntity(
                    score = score,
                    date = "6월 5일",
                    feedback = remarks
                )
            )
        }
    }
}

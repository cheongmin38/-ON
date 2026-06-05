package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmergencyLogEntity
import com.example.data.FamilyMemberEntity
import com.example.data.MedicationEntity
import com.example.ui.theme.*
import com.example.ui.viewmodels.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GuardianScreens(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    // Local sub-navigation in Guardian mode using standard tabs
    var selectedTab by remember { mutableStateOf("DASHBOARD") } // DASHBOARD, SAFE_ZONE, MEDS, FAMILY

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = WarmSurface,
                modifier = Modifier
                    .shadow(12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("guardian_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = selectedTab == "DASHBOARD",
                    onClick = { selectedTab = "DASHBOARD" },
                    icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
                    label = { Text("홈 보드", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmSecondary,
                        selectedTextColor = WarmSecondary,
                        indicatorColor = WarmLightTeal
                    ),
                    modifier = Modifier.testTag("tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == "SAFE_ZONE",
                    onClick = { selectedTab = "SAFE_ZONE" },
                    icon = { Icon(Icons.Default.PinDrop, "Safe Zone") },
                    label = { Text("안심 구역", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmSecondary,
                        selectedTextColor = WarmSecondary,
                        indicatorColor = WarmLightTeal
                    ),
                    modifier = Modifier.testTag("tab_safe_zone")
                )
                NavigationBarItem(
                    selected = selectedTab == "MEDS",
                    onClick = { selectedTab = "MEDS" },
                    icon = { Icon(Icons.Default.MedicalServices, "Medications") },
                    label = { Text("복약 관리", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmSecondary,
                        selectedTextColor = WarmSecondary,
                        indicatorColor = WarmLightTeal
                    ),
                    modifier = Modifier.testTag("tab_meds")
                )
                NavigationBarItem(
                    selected = selectedTab == "FAMILY",
                    onClick = { selectedTab = "FAMILY" },
                    icon = { Icon(Icons.Default.People, "Family Memory") },
                    label = { Text("기억 보조", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmSecondary,
                        selectedTextColor = WarmSecondary,
                        indicatorColor = WarmLightTeal
                    ),
                    modifier = Modifier.testTag("tab_family")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "DASHBOARD" -> GuardianDashboardScreen(viewModel)
                "SAFE_ZONE" -> GuardianSafeZoneScreen(viewModel)
                "MEDS" -> GuardianMedsScreen(viewModel)
                "FAMILY" -> GuardianFamilyScreen(viewModel)
            }
        }
    }
}

// ---------------------------------------------------------------------
// 1. Guardian Dashboard UI (Realtime Metrics & AI Summary)
// ---------------------------------------------------------------------
@Composable
fun GuardianDashboardScreen(viewModel: AppViewModel) {
    val patient by viewModel.patient.collectAsState()
    val isOutsideSafeZone by viewModel.isOutsideSafeZone.collectAsState()
    val simulatedLocation by viewModel.simulatedLocation.collectAsState()
    val lastLocationUpdate by viewModel.lastLocationUpdate.collectAsState()
    val medicationsList by viewModel.medications.collectAsState()
    val emergencyLogs by viewModel.emergencyLogs.collectAsState()
    val cognitiveScores by viewModel.cognitiveScores.collectAsState()
    
    val aiSummaryText by viewModel.aiSummaryText.collectAsState()
    val isLoadingAi by viewModel.isLoadingAi.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Profile Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "기억지킴이 보호자용",
                    fontSize = 14.sp,
                    color = WarmGrey,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${patient?.name ?: "어머"}님의 보호 대시보드",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = WarmOnSurface
                )
            }

            // Mode switch trigger back to welcome
            OutlinedButton(
                onClick = { viewModel.setMode("START") },
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Text("모드 전환", fontSize = 12.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
            }
        }

        // Warning Alert Banner: If patient leaves Safe zone!
        if (isOutsideSafeZone) {
            Card(
                colors = CardDefaults.cardColors(containerColor = WarmLightRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, WarmAlert, RoundedCornerShape(16.dp))
                    .testTag("guardian_out_alert")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚨", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "안전구역 이탈 긴급 경고!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmAlert
                        )
                        Text(
                            text = "${patient?.name ?: "어머니"}님이 안전구역 자택을 벗어나 길을 헤매고 계실 수 있습니다. 현재 위치를 확인하세요.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WarmOnSurface
                        )
                    }
                    Button(
                        onClick = { viewModel.simulateSafeZoneReturn() },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmActiveGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("안심복구", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SECTION: Live Location and Safe Zone Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PinDrop, "Location", tint = WarmSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("실시간 환자 위치", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(
                        text = lastLocationUpdate,
                        fontSize = 12.sp,
                        color = WarmGrey,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = simulatedLocation,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmOnSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isOutsideSafeZone) WarmLightRed else WarmLightTeal,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOutsideSafeZone) WarmAlert else WarmActiveGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOutsideSafeZone) "현재 안심 구역을 벗어난 외곽 상태입니다!" else "현재 안전구역 안 안심 자택에 머무는 중",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOutsideSafeZone) WarmAlert else WarmSecondary
                    )
                }
            }
        }

        // SECTION: Medication Logs Card
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, "Medication", tint = WarmPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("오늘 복약 달성률", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (medicationsList.isEmpty()) {
                    Text("등록된 안전 상비 복약 규정이 없습니다.", fontSize = 14.sp, color = WarmGrey)
                } else {
                    val totalMeds = medicationsList.size
                    val takenMedsCount = medicationsList.count { it.takenStatus }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$totalMeds 개 약품 중 $takenMedsCount 개 안전 복용 완료",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmOnSurface
                        )
                        Box(
                            modifier = Modifier
                                .background(WarmLightAmber, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "달성률 ${(takenMedsCount.toFloat() / totalMeds * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        medicationsList.forEach { med ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(WarmBackground, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(med.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarmOnSurface)
                                    Text(med.time, fontSize = 12.sp, color = WarmGrey, fontWeight = FontWeight.SemiBold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (med.takenStatus) Icons.Default.CheckCircle else Icons.Default.Pending,
                                        contentDescription = "Med Status",
                                        tint = if (med.takenStatus) WarmActiveGreen else WarmGrey,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (med.takenStatus) "복용 완료" else "미복용",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (med.takenStatus) WarmActiveGreen else WarmGrey
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION: AI Heart Daily Summary Report Generator Card (CALLS REAL GEMINI API)
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmLightAmber),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, WarmPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 22.sp, modifier = Modifier.padding(end = 4.dp))
                        Text(
                            text = "AI 마음 안심 연동보고서 (Gemini)",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = WarmPrimary
                        )
                    }
                    
                    if (!isLoadingAi) {
                        Button(
                            onClick = { viewModel.requestGuardianSummary() },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("분석 요약", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isLoadingAi) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = WarmPrimary, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Gemini AI가 실시간 통합 데이터를 진단 분석 중...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarmGrey)
                    }
                } else if (aiSummaryText.isNotEmpty()) {
                    Text(
                        text = aiSummaryText,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                            .fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "위의 '분석 요약' 버튼을 탭하면, Gemini AI 기술이 약 복용 정보 및 구역 상태, 인지 테스트 기록을 실시간 진단 요악하여 제공합니다.",
                        fontSize = 13.sp,
                        color = WarmGrey,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // SECTION: Cognitive training results
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Healing, "Cognitive Score", tint = WarmSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("어르신 최신 인지/두뇌 훈련 성적", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                val scoreEntity = cognitiveScores.firstOrNull()
                if (scoreEntity == null) {
                    Text("치매 진단 예방을 위한 인지 놀이 기록이 아직 없습니다.", fontSize = 13.sp, color = WarmGrey)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "오늘 종합 점수 : ${scoreEntity.score}점 (쉬움 난이도)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmSecondary
                            )
                            Text(
                                text = "기록 시간: ${scoreEntity.date}",
                                fontSize = 12.sp,
                                color = WarmGrey,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(WarmLightTeal, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("성적 우수", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmSecondary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarmBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "💡 피드백: ${scoreEntity.feedback}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarmGrey
                            ),
                            modifier = Modifier.padding(10.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }

        // SECTION: Emergency Call Logs Card
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, "Alarm History", tint = WarmAlert)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("안심 긴급 호출/사건 기록", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    
                    if (emergencyLogs.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAllLogs() }) {
                            Text("전체 삭제", color = WarmAlert, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (emergencyLogs.isEmpty()) {
                    Text(
                        text = "오늘 감지된 이탈이나 비상 SOS 호출이 없어 안심입니다. 녹색 등 유지 중.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarmActiveGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        emergencyLogs.take(5).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(WarmLightRed, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.status,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarmAlert
                                    )
                                    Text(
                                        text = "위치: ${log.location}",
                                        fontSize = 12.sp,
                                        color = WarmGrey,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = SimpleDateFormat("HH:mm", Locale.KOREAN).format(Date(log.time)),
                                    fontSize = 12.sp,
                                    color = WarmGrey,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // SECTION: MVP Simulation Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, WarmBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚙️ 모바일 원격 시뮬레이터 (MVP 데모용)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = WarmGrey
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.simulateSafeZoneDeparture()
                            Toast.makeText(context, "환자용 가상 위치를 1.2km 외곽으로 이동시켰습니다.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmAlert),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("구역이탈 경보 발생", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (medicationsList.isNotEmpty()) {
                                viewModel.triggerMockMedicationAlarm(medicationsList.first { !it.takenStatus } ?: medicationsList.first())
                                Toast.makeText(context, "환자 폰 화면에 원격 복약 전송 완료!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "복약 관리에서 복용 스케줄을 추가하세요.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("원격 복약알림 발송", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 2. Guardian Safe Zone Screen
// ---------------------------------------------------------------------
@Composable
fun GuardianSafeZoneScreen(viewModel: AppViewModel) {
    val patient by viewModel.patient.collectAsState()
    val safeZone by viewModel.safeZone.collectAsState()
    
    var homeAddress by remember { mutableStateOf(patient?.homeAddress ?: "") }
    var selectedRadius by remember { mutableStateOf(safeZone?.radius ?: 300) }
    
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📐 안심 보호 구역 기하설정", fontWeight = FontWeight.Black, fontSize = 22.sp, color = WarmOnSurface)

        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("활동 범위 제한 구역 설정", face = "Roboto", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmSecondary)
                
                OutlinedTextField(
                    value = homeAddress,
                    onValueChange = { homeAddress = it },
                    label = { Text("기준점 주소 (환자 거주지)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmSecondary,
                        focusedLabelColor = WarmSecondary
                    )
                )

                Text("안심 보호 반경 선택", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(300, 500, 1000).forEach { radius ->
                        val selected = selectedRadius == radius
                        Card(
                            onClick = { selectedRadius = radius },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .height(50.dp)
                                .border(
                                    2.dp,
                                    if (selected) WarmSecondary else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) WarmLightTeal else WarmBackground
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (radius >= 1000) "1km" else "${radius}m",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) WarmSecondary else WarmGrey,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.updateSafeZone(selectedRadius, homeAddress)
                        viewModel.updatePatientProfile(
                            name = patient?.name ?: "김만옥",
                            address = homeAddress,
                            contact = patient?.emergencyContact ?: "010-1234-5678"
                        )
                        Toast.makeText(context, "자택 주소 및 지반경 ${selectedRadius}m 저장 완료!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("안심 구역 규칙 적용", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // 자주 가는 단골 장소 안심 추가
        Text("🏥 자주 방문하는 안전지대 단골장소 추가", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WarmGrey)
        
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    "공덕 메디컬 연세정형외과" to "서울특별시 마포구 마포대로 13",
                    "마포 노인종합복지관" to "서울특별시 마포구 신촌로26길 10",
                    "아들 민수 집 (둘째아들 자택)" to "서울특별시 용산구 신계동 용산 아파트"
                ).forEach { spot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarmBackground, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(spot.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarmOnSurface)
                            Text(spot.second, fontSize = 12.sp, color = WarmGrey)
                        }
                        IconButton(onClick = { Toast.makeText(context, "안심장소로 저장되었습니다.", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.AddHome, "Add Spot", tint = WarmSecondary)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 3. Guardian Medications Configuration Screen
// ---------------------------------------------------------------------
@Composable
fun GuardianMedsScreen(viewModel: AppViewModel) {
    val medicationsList by viewModel.medications.collectAsState()
    
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("1정") }
    var medTime by remember { mutableStateOf("오전 09:00") }
    var medMemo by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("💊 처방약 일정 관리 데크", fontWeight = FontWeight.Black, fontSize = 22.sp, color = WarmOnSurface)

        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("신규 처방약 정보 정밀 입력", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmPrimary)
                
                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    label = { Text("처방약 이름 (예: 당뇨약, 실버 비타민)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = medDosage,
                        onValueChange = { medDosage = it },
                        label = { Text("용량 복용량 (예: 1정, 2알)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = medTime,
                        onValueChange = { medTime = it },
                        label = { Text("알림 시간 (예: 오전 09:00)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = medMemo,
                    onValueChange = { medMemo = it },
                    label = { Text("복용 시 유의사항 메모 (예: 아침 식사 직후)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (medName.isEmpty()) {
                            Toast.makeText(context, "처방약 이름을 필히 채워 주세요.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addMedication(medName, medDosage, medTime, medMemo)
                            medName = ""
                            medMemo = ""
                            Toast.makeText(context, "새 처방약 일정이 대시보드에 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("새 복약 일정 추가", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // 현 복약 리스트 관리
        Text("📋 어르신 활성 복약 알림 목록", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WarmGrey)

        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (medicationsList.isEmpty()) {
                    Text("현재 등록 예정인 복약 일정이 비어 있습니다.", fontSize = 13.sp, color = WarmGrey)
                } else {
                    medicationsList.forEach { med ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WarmBackground, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${med.name} (${med.dosage})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = WarmOnSurface
                                )
                                Text(
                                    text = "시간: ${med.time} | 메모: ${med.memo}",
                                    fontSize = 12.sp,
                                    color = WarmGrey
                                )
                            }
                            
                            IconButton(onClick = { viewModel.deleteMedication(med) }) {
                                Icon(Icons.Default.Delete, "Delete Meds", tint = WarmAlert)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 4. Guardian Family Profile configuration screen with AI Description Tool!!
// ---------------------------------------------------------------------
@Composable
fun GuardianFamilyScreen(viewModel: AppViewModel) {
    val familyList by viewModel.familyMembers.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var relationPhoto by remember { mutableStateOf("son") } // son, d_in_law, granddaughter
    
    // AI Photo description request fields (Requirements 7)
    var photoTitle by remember { mutableStateOf("제주도 동해 여행에서 어머니랑") }
    var photoDesc by remember { mutableStateOf("가족 다같이 바다 보면서 활짝 미소짓는 따수운 모습") }
    val aiTherapyResult by viewModel.aiTherapyResult.collectAsState()
    val isLoadingAi by viewModel.isLoadingAi.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🧑‍⚕️ 가족 인물 관계 보조 설정", fontWeight = FontWeight.Black, fontSize = 22.sp, color = WarmOnSurface)

        // Add Family Form
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("가족 가계도 인물 신규 등록", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmSecondary)
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("이름 김민수") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = relation,
                        onValueChange = { relation = it },
                        label = { Text("관계 (예: 큰아들, 장녀)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("휴대전화 번호") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("화면에 크게 띄울 편안한 어르신 보조 묘사글") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Mock image picker selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("대리 사진 아바타 선택: ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarmGrey)
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf("son" to "👨 아들", "d_in_law" to "👩 며느리", "granddaughter" to "👧 손녀").forEach { option ->
                        val active = relationPhoto == option.first
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) WarmLightTeal else WarmBackground)
                                .border(1.dp, if (active) WarmSecondary else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { relationPhoto = option.first }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(option.second, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (active) WarmSecondary else WarmGrey)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (name.isEmpty() || relation.isEmpty() || phone.isEmpty()) {
                            Toast.makeText(context, "이름, 관계, 스마트폰 번호는 꼭 기재해 주세요.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addFamilyMember(name, relation, phone, relationPhoto, desc)
                            name = ""
                            relation = ""
                            phone = ""
                            desc = ""
                            Toast.makeText(context, "가족 정보가 주소록에 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("가족 보조 인물 추가", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // Section: Past Memory Photo AI therapy generation tools
        Text("🌸 AI 과거 사진 치료 묘사 생성 (Gemini)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WarmTertiary)
        
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "환자의 과거 앨범에서 사진을 올려 마음 회상을 돕는 따뜻한 보조 설명을 AI가 생성합니다.",
                    fontSize = 13.sp,
                    color = WarmGrey
                )

                OutlinedTextField(
                    value = photoTitle,
                    onValueChange = { photoTitle = it },
                    label = { Text("사진 촬영 이벤트/추억 주제") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = photoDesc,
                    onValueChange = { photoDesc = it },
                    label = { Text("사진 속 구체적 묘사 (아들 얼굴, 미소, 장소 등)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isLoadingAi) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = WarmTertiary, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini가 따뜻한 안심 묘사를 생성 중...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarmGrey)
                    }
                } else {
                    Button(
                        onClick = {
                            if (photoTitle.isEmpty()) {
                                Toast.makeText(context, "사진 주제를 필히 채워 주세요.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.requestAiMemoryTherapy(photoTitle, photoDesc, "아들")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmTertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("AI 안심 문구 생성 및 연양 등록", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                if (aiTherapyResult.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarmLightTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, WarmSecondary, RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🤖 AI가 조율한 정서 안심 묘사글:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = aiTherapyResult,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = WarmOnSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // List Current family members
        Text("📋 현재 동봉된 대시보드 인물 리스트", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WarmGrey)

        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                familyList.forEach { family ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarmBackground, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(WarmLightAmber),
                                contentAlignment = Alignment.Center
                            ) {
                                val visEmoji = when (family.photoUrl) {
                                    "son" -> "👨"
                                    "d_in_law" -> "👩"
                                    "granddaughter" -> "👧"
                                    else -> "❤️"
                                }
                                Text(visEmoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${family.name} (${family.relationship})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = WarmOnSurface
                                )
                                Text(
                                    text = "번호: ${family.phone}",
                                    fontSize = 12.sp,
                                    color = WarmGrey
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.deleteFamilyMember(family) }) {
                            Icon(Icons.Default.Delete, "Delete Member", tint = WarmAlert)
                        }
                    }
                }
            }
        }
    }
}

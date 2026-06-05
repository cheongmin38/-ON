package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
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
    // Exactly 5 distinct navigational tabs as requested
    var selectedTab by remember { mutableStateOf("HOME") } // HOME, LOCATION, MEDS, MEMORY, SETTINGS
    var activeModalScreen by remember { mutableStateOf<String?>(null) } // Modal controller
    val context = LocalContext.current

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
                    selected = selectedTab == "HOME",
                    onClick = { selectedTab = "HOME" },
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("홈", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmPrimary,
                        selectedTextColor = WarmPrimary,
                        indicatorColor = WarmLightAmber
                    ),
                    modifier = Modifier.testTag("tab_home")
                )
                NavigationBarItem(
                    selected = selectedTab == "LOCATION",
                    onClick = { selectedTab = "LOCATION" },
                    icon = { Icon(Icons.Default.Map, "Location") },
                    label = { Text("위치", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmPrimary,
                        selectedTextColor = WarmPrimary,
                        indicatorColor = WarmLightAmber
                    ),
                    modifier = Modifier.testTag("tab_location")
                )
                NavigationBarItem(
                    selected = selectedTab == "MEDS",
                    onClick = { selectedTab = "MEDS" },
                    icon = { Icon(Icons.Default.MedicalServices, "Meds") },
                    label = { Text("약 관리", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmPrimary,
                        selectedTextColor = WarmPrimary,
                        indicatorColor = WarmLightAmber
                    ),
                    modifier = Modifier.testTag("tab_meds")
                )
                NavigationBarItem(
                    selected = selectedTab == "MEMORY",
                    onClick = { selectedTab = "MEMORY" },
                    icon = { Icon(Icons.Default.PhotoLibrary, "Memory") },
                    label = { Text("기억", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmPrimary,
                        selectedTextColor = WarmPrimary,
                        indicatorColor = WarmLightAmber
                    ),
                    modifier = Modifier.testTag("tab_memory")
                )
                NavigationBarItem(
                    selected = selectedTab == "SETTINGS",
                    onClick = { selectedTab = "SETTINGS" },
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("설정", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmPrimary,
                        selectedTextColor = WarmPrimary,
                        indicatorColor = WarmLightAmber
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        containerColor = Color(0xFFFFF8F2),
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "HOME" -> GuardianHomeScreen(viewModel, onOpenLogs = { activeModalScreen = "SOS_HISTORY" })
                "LOCATION" -> GuardianLocationScreen(viewModel, onOpenSafeZone = { activeModalScreen = "SET_SAFE_ZONE" })
                "MEDS" -> GuardianMedsTab(viewModel, onAddMed = { activeModalScreen = "ADD_MED" })
                "MEMORY" -> GuardianMemoryTab(
                    viewModel = viewModel,
                    onAddFamily = { activeModalScreen = "ADD_FAMILY" },
                    onAddSchedule = { activeModalScreen = "ADD_SCHEDULE" },
                    onUploadPhoto = { activeModalScreen = "UPLOAD_PHOTO" }
                )
                "SETTINGS" -> GuardianSettingsTab(
                    viewModel = viewModel,
                    onOpenModal = { activeModalScreen = it }
                )
            }

            // Universal modal sheet overlay container for all requested additional screens
            activeModalScreen?.let { mode ->
                ModalOverlay(
                    mode = mode,
                    viewModel = viewModel,
                    onDismiss = { activeModalScreen = null }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// 1. Guardian Home Screen (환자의 현재 상태를 한눈에 확인하는 대시보드)
// ---------------------------------------------------------------------
@Composable
fun GuardianHomeScreen(
    viewModel: AppViewModel,
    onOpenLogs: () -> Unit
) {
    val patient by viewModel.patient.collectAsState()
    val isOutsideSafeZone by viewModel.isOutsideSafeZone.collectAsState()
    val simulatedLocation by viewModel.simulatedLocation.collectAsState()
    val lastLocationUpdate by viewModel.lastLocationUpdate.collectAsState()
    val medicationsList by viewModel.medications.collectAsState()
    val emergencyLogs by viewModel.emergencyLogs.collectAsState()
    
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
        // App top identity
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("기억ON 보호자용", fontSize = 13.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
                Text(
                    text = "${patient?.name ?: "김만옥"}님의 대시보드",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = WarmOnSurface
                )
            }

            // Simple Mode reset for testing
            OutlinedButton(
                onClick = { viewModel.setMode("SPLASH") },
                border = BorderStroke(1.dp, Color(0xFFFFD4B2)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD84315)),
                modifier = Modifier.testTag("reset_to_start_btn")
            ) {
                Text("시작화면으로", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Card 1: 현재 안전 상태
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isOutsideSafeZone) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    2.dp,
                    if (isOutsideSafeZone) Color(0xFFEF5350) else Color(0xFF81C784),
                    RoundedCornerShape(20.dp)
                )
                .testTag("home_safety_card")
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOutsideSafeZone) "🚨" else "🟢",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "현재 안전 상태",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOutsideSafeZone) Color(0xFFC62828) else Color(0xFF2E7D32)
                    )
                    Text(
                        text = if (isOutsideSafeZone) "안전구역 이탈 경고 발생!" else "현재 안전구역 안에 있습니다.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = WarmOnSurface
                    )
                }

                if (isOutsideSafeZone) {
                    Button(
                        onClick = { viewModel.simulateSafeZoneReturn() },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("귀가 복구", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card 2: 현재 위치
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFE6D5), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PinDrop, "Location", tint = Color(0xFF1565C0))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("현재 위치 확인", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarmOnSurface)
                    }
                    Text(text = lastLocationUpdate, fontSize = 11.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = simulatedLocation,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = WarmOnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            viewModel.simulateSafeZoneDeparture()
                            Toast.makeText(context, "배회 이탈 상황을 시뮬레이션했습니다.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("이탈 가상 시뮬레이션", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card 3: 오늘 약 복용 상태
        val totalMeds = medicationsList.size
        val takenMeds = medicationsList.count { it.takenStatus }
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFE6D5), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, "Meds", tint = WarmSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("오늘 약 복용 상태", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarmOnSurface)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (totalMeds == 0) "등록된 복용 약 정보가 없습니다."
                    else "오전 약 복용 완료 (${takenMeds}/${totalMeds} 복용 완료)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmOnSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = if (totalMeds > 0) takenMeds.toFloat() / totalMeds else 0f,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = WarmSecondary,
                    trackColor = Color(0xFFFFE6D5)
                )
            }
        }

        // Card 4: 오늘 일정
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFE6D5), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, "Schedule", tint = Color(0xFFD84315))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("오늘 일정", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarmOnSurface)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF8F2), RoundedCornerShape(8.dp)).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("📅 오후 3시 병원 정기 점검 예약", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarmOnSurface)
                        Text("공덕 연세가정의원 동행 요망", fontSize = 12.sp, color = WarmGrey)
                    }
                    Box(
                        modifier = Modifier.background(Color(0xFFFFD4B2), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("D-Day", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFD84315))
                    }
                }
            }
        }

        // Card 5: 긴급 알림 & 호출 기록
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFE6D5), RoundedCornerShape(16.dp))
                .clickable { onOpenLogs() }
                .testTag("emergency_history_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, "Emergency Alert", tint = WarmAlert)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("긴급 호출 기록 센터", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarmOnSurface)
                        val sizeVal = emergencyLogs.size
                        Text(
                            text = if (sizeVal == 0) "최근 호출 이력이 없어 안전합니다." else "최근 긴급 호출 기록 ${sizeVal}건 발생",
                            fontSize = 12.sp,
                            color = if (sizeVal > 0) WarmAlert else WarmGrey,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, "View logs", tint = WarmGrey)
            }
        }

        // Card 6: AI 하루 요약
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE1BEE7), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧠", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI 스마트 하루 요약 경과", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF4A148C))
                    }

                    IconButton(
                        onClick = { viewModel.requestGuardianSummary() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, "Regenerate summary", tint = Color(0xFF4A148C))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isLoadingAi) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF4A148C))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("실시간 AI 환자 데이터를 채트룸으로 취합 분석하는 중...", fontSize = 12.sp, color = WarmGrey)
                    }
                } else {
                    Text(
                        text = if (aiSummaryText.isEmpty()) "“오늘은 대체로 평안한 하루를 보내고 있으십니다. 오전 고혈압 혈압약을 완료했고, 자택 부근 안심 존을 유지했습니다. 인지 훈련은 어제보다 집중도가 5점 올라 어르신의 두뇌 활력 이력이 매우 순탄합니다.”"
                        else aiSummaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A148C).copy(alpha = 0.9f),
                        lineHeight = 19.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ---------------------------------------------------------------------
// 2. Guardian Location Screen (실시간 지도 Canvas & 안전구역 관리)
// ---------------------------------------------------------------------
@Composable
fun GuardianLocationScreen(
    viewModel: AppViewModel,
    onOpenSafeZone: () -> Unit
) {
    val simulatedLocation by viewModel.simulatedLocation.collectAsState()
    val lastLocationUpdate by viewModel.lastLocationUpdate.collectAsState()
    val isOutsideSafeZone by viewModel.isOutsideSafeZone.collectAsState()
    val safeZoneEntity by viewModel.safeZone.collectAsState()
    val emergencyLogs by viewModel.emergencyLogs.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("실시간 동선 감지", fontSize = 12.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
                Text("위치 및 안심 구역 조회", fontSize = 22.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)
            }

            IconButton(onClick = {
                viewModel.simulateSafeZoneReturn()
                Toast.makeText(context, "위치를 실시간 갱신했습니다.", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.Refresh, "Refresh", tint = Color(0xFF1565C0))
            }
        }

        // Realistic interactive Canvas-based Map graphic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8F0FE))
                .border(2.dp, Color(0xFF90CAF9), RoundedCornerShape(20.dp))
        ) {
            // Draw map layout grid representation
            Canvas(modifier = Modifier.fillMaxSize()) {
                val widthVal = size.width
                val heightVal = size.height

                // Draw roads lines grid
                drawLine(Color.White, Offset(0f, heightVal * 0.3f), Offset(widthVal, heightVal * 0.3f), strokeWidth = 24.1f)
                drawLine(Color.White, Offset(0f, heightVal * 0.75f), Offset(widthVal, heightVal * 0.75f), strokeWidth = 24.1f)
                drawLine(Color.White, Offset(widthVal * 0.4f, 0f), Offset(widthVal * 0.4f, heightVal), strokeWidth = 24.1f)

                // Draw central Safe Zone circle (Green)
                drawCircle(
                    color = Color(0xFF4CAF50),
                    radius = if (isOutsideSafeZone) 180f else 280f,
                    center = Offset(widthVal * 0.4f, heightVal * 0.5f),
                    style = Stroke(
                        width = 8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )

                // Fill color alpha safe zone
                drawCircle(
                    color = Color(0xFFE8F5E9).copy(alpha = 0.5f),
                    radius = if (isOutsideSafeZone) 180f else 280f,
                    center = Offset(widthVal * 0.4f, heightVal * 0.5f)
                )
            }

            // Central base House anchor (Safe zone center)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-30).dp, y = (10).dp)
                    .size(36.dp)
                    .background(Color(0xFF4CAF50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Home, "Safe Zone Center", tint = Color.White, modifier = Modifier.size(18.dp))
            }

            // Moving patient marker pin (Red if outside safe zone, blue/green if inside!)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = if (isOutsideSafeZone) 90.dp else (-10).dp,
                        y = if (isOutsideSafeZone) (-40).dp else 12.dp
                    )
                    .size(38.dp)
                    .background(if (isOutsideSafeZone) Color(0xFFE53935) else Color(0xFF1E88E5), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👴", fontSize = 18.sp)
            }

            // Compass overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isOutsideSafeZone) "⚠️ 안심 이탈" else "🟢 안심 구역 유지",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isOutsideSafeZone) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
            }

            // Update timer
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(lastLocationUpdate, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Target Details
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("실시간 위치 세부 정보", fontSize = 11.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
                Text(simulatedLocation, fontSize = 16.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text("안심반경 등록지: ${safeZoneEntity?.address ?: "거주지 주소 등록 완료"}", fontSize = 12.sp, color = WarmGrey)
            }
        }

        // Action control buttons card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onOpenSafeZone,
                colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp).testTag("open_safe_zone_config_btn")
            ) {
                Icon(Icons.Default.Shield, "Configure active fence", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("안심구역 범위 설정", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    Toast.makeText(context, "기억ON 인공지능이 어르신용 귀가 안내 음성 멘트를 활가 송출했습니다.", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp).testTag("request_home_routing_btn")
            ) {
                Icon(Icons.Default.Navigation, "Navigate target", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("집으로 안내 전송", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Deviation history log section
        Text("최근 배회 이탈 및 위험 경보 이력", fontSize = 15.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)

        val outLogs = emergencyLogs.filter { it.status.contains("이탈") }
        if (outLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("최근 발생한 안전 구역 이탈 기록이 없습니다.", color = WarmGrey, fontSize = 13.sp)
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    outLogs.forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.status, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarmAlert)
                                Text(log.location, fontSize = 12.sp, color = WarmGrey)
                            }
                            Text(
                                SimpleDateFormat("MM.dd HH:mm", Locale.KOREAN).format(Date(log.time)),
                                fontSize = 11.sp,
                                color = WarmGrey,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(color = Color(0xFFFFF0E5))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ---------------------------------------------------------------------
// 3. Guardian Medications screen (복약 목록, 복약 달성 이력)
// ---------------------------------------------------------------------
@Composable
fun GuardianMedsTab(
    viewModel: AppViewModel,
    onAddMed: () -> Unit
) {
    val medicationsList by viewModel.medications.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("동행 안전 관리", fontSize = 12.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
                Text("복약 일정 목록", fontSize = 22.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)
            }

            Button(
                onClick = onAddMed,
                colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("open_add_med_btn")
            ) {
                Icon(Icons.Default.Add, "Add Med")
                Spacer(modifier = Modifier.width(4.dp))
                Text("약 추가", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }

        // Mini calendar checklist history
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("최근 5일간의 안전 복약 실천율", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("6/01", "6/02", "6/03", "6/04", "오늘")
                    val checks = listOf(true, true, false, true, false)

                    for (i in days.indices) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(days[i], fontSize = 11.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (checks[i]) Color(0xFF2E7D32) else Color(0xFFFFCDD2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (checks[i]) "✔️" else "❌", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        Text("현재 약 복용 체크리스트", fontSize = 16.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)

        if (medicationsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("추가 등록된 복용 약이 아직 없습니다.", color = WarmGrey)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                medicationsList.forEach { med ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarmSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFFE6D5), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (med.takenStatus) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💊", fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(med.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WarmOnSurface)
                                    Text("복용 개수: ${med.dosage}  /  시간: ${med.time}", fontSize = 12.sp, color = WarmGrey)
                                    if (med.memo.isNotEmpty()) {
                                        Text("⚠️ 메모: ${med.memo}", fontSize = 11.sp, color = Color(0xFFD84315), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { viewModel.toggleMedicationTaken(med) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (med.takenStatus) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                    ),
                                    border = BorderStroke(1.dp, if (med.takenStatus) Color(0xFF81C784) else Color(0xFFFFB74D)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = if (med.takenStatus) "복용완료" else "미복용",
                                        color = if (med.takenStatus) Color(0xFF2E7D32) else Color(0xFFE65100),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(onClick = {
                                    viewModel.deleteMedication(med)
                                    Toast.makeText(context, "${med.name} 약품을 약 리스트에서 영구 삭제 처리했습니다.", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ---------------------------------------------------------------------
// 4. Guardian Memory tab (가족사진, 추억사진 AI 치료, 일정, 인지퀴즈관리)
// ---------------------------------------------------------------------
@Composable
fun GuardianMemoryTab(
    viewModel: AppViewModel,
    onAddFamily: () -> Unit,
    onAddSchedule: () -> Unit,
    onUploadPhoto: () -> Unit
) {
    var subTab by remember { mutableStateOf("FAMILY") } // FAMILY, MEMORY_PHOTO, CALENDAR, COGNITIVE
    val familyMembers by viewModel.familyMembers.collectAsState()
    val memoryPhotos by viewModel.memoryPhotos.collectAsState()
    val cognitiveScores by viewModel.cognitiveScores.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("치유 및 기억 보조 설계", fontSize = 12.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
        Text("환자 기억 보조 보드", fontSize = 22.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)

        Spacer(modifier = Modifier.height(14.dp))

        // Large high-fidelity sliding tabs for Memory Sections
        ScrollableTabRow(
            selectedTabIndex = when (subTab) {
                "FAMILY" -> 0
                "MEMORY_PHOTO" -> 1
                "CALENDAR" -> 2
                "COGNITIVE" -> 3
                else -> 0
            },
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            indicator = { }
        ) {
            val tabs = listOf("FAMILY" to "👨‍👩‍👧‍👦 가족인물", "MEMORY_PHOTO" to "🏞️ AI추억사진", "CALENDAR" to "📅 일정관리", "COGNITIVE" to "🧠 인지훈련")
            tabs.forEach { (key, label) ->
                Tab(
                    selected = subTab == key,
                    onClick = { subTab = key },
                    text = { Text(label, fontWeight = FontWeight.Black, fontSize = 12.sp) },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .background(
                            if (subTab == key) Color(0xFFFFD4B2) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color(0xFFFFE6D5), RoundedCornerShape(12.dp))
                        .testTag("sub_tab_$key")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content of Selected SubTab
        Box(modifier = Modifier.weight(1f)) {
            when (subTab) {
                "FAMILY" -> {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("등록된 안심 가족 정보", fontSize = 16.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)
                            Button(
                                onClick = onAddFamily,
                                colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(34.dp).testTag("open_add_family_btn")
                            ) {
                                Text("+ 가족 추가", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        familyMembers.forEach { member ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFFFE6D5), RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFFFE6D5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(if (member.relationship.contains("아들")) "👦" else "👵", fontSize = 28.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(member.name, fontWeight = FontWeight.Black, fontSize = 16.sp, color = WarmOnSurface)
                                            Text("관계: ${member.relationship} / 연락처: ${member.phone}", fontSize = 12.sp, color = WarmGrey)
                                        }
                                        IconButton(onClick = {
                                            viewModel.deleteFamilyMember(member)
                                            Toast.makeText(context, "${member.name}님을 인물 정보에서 제외했습니다.", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.Delete, "Remove", tint = Color.LightGray)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = member.description,
                                        fontSize = 12.sp,
                                        color = WarmGrey,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.background(Color(0xFFFFFDFB), RoundedCornerShape(8.dp)).padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                "MEMORY_PHOTO" -> {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("과거 AI 추억 및 회상 사진첩", fontSize = 16.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)
                            Button(
                                onClick = onUploadPhoto,
                                colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(34.dp).testTag("open_upload_photo_btn")
                            ) {
                                Text("+ 사진 업로드", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        memoryPhotos.forEach { photo ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFFFE6D5), RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(if (photo.photoUrl.contains("jeju")) "🌴" else "🎂", fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (photo.photoUrl.contains("jeju")) "제주도 가족 여행" else "첫 손녀 은지의 돌잔치",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = WarmOnSurface
                                            )
                                        }
                                        Box(
                                            modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("AI 심리치료 자산", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("📷 원본 사실: ${photo.description}", fontSize = 12.sp, color = WarmGrey)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFF8F2), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("🤖 AI가 들려주는 어르신 맞춤 회상 문구:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD84315))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(photo.aiResponse, fontSize = 11.sp, color = WarmOnSurface, lineHeight = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "CALENDAR" -> {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("환자 일정 관리", fontSize = 16.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)
                            Button(
                                onClick = onAddSchedule,
                                colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(34.dp).testTag("open_add_schedule_btn")
                            ) {
                                Text("+ 일정 추가", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val schedules = listOf(
                            "오후 3시 병원 예약" to "공덕 연세가정의원 동행 요망 / 6월 5일",
                            "어머니 생신 축하 잔치" to "마포 갈비 골목 식당 예약 확인 / 6월 12일",
                            "보호 보건 상담관 방문 상담" to "자택 방문 및 인지 훈련 지원 서비스 점검 / 6월 17일"
                        )

                        schedules.forEach { (title, subtitle) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFFFE6D5), RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmOnSurface)
                                        Text(subtitle, fontSize = 12.sp, color = WarmGrey)
                                    }
                                    Icon(Icons.Default.CalendarMonth, "Details", tint = Color(0xFFD84315))
                                }
                            }
                        }
                    }
                }

                "COGNITIVE" -> {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("주간 두뇌 훈련 인지 스코어", fontSize = 15.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)

                        // Progress Canvas Line Graph for Cognitive history
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .border(1.dp, Color(0xFFFFD4B2), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("두뇌 점수 상승 곡선", fontSize = 11.sp, color = WarmGrey, fontWeight = FontWeight.Bold)

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(vertical = 12.dp)
                                ) {
                                    val points = listOf(Offset(size.width * 0.1f, size.height * 0.7f), Offset(size.width * 0.35f, size.height * 0.62f), Offset(size.width * 0.65f, size.height * 0.45f), Offset(size.width * 0.9f, size.height * 0.2f))
                                    val scores = listOf("72점", "75점", "80점", "85점")

                                    // Draw background guide lines
                                    drawLine(Color(0xFFFFF0E5), Offset(0f, size.height * 0.2f), Offset(size.width, size.height * 0.2f), strokeWidth = 2f)
                                    drawLine(Color(0xFFFFF0E5), Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), strokeWidth = 2f)
                                    drawLine(Color(0xFFFFF0E5), Offset(0f, size.height * 0.8f), Offset(size.width, size.height * 0.8f), strokeWidth = 2f)

                                    // Draw connecting line path
                                    for (i in 0 until points.size - 1) {
                                        drawLine(
                                            color = Color(0xFF2E7D32),
                                            start = points[i],
                                            end = points[i + 1],
                                            strokeWidth = 8f
                                        )
                                    }

                                    // Draw points circle dots and label text
                                    for (i in points.indices) {
                                        drawCircle(Color(0xFF2E7D32), radius = 12f, center = points[i])
                                        drawCircle(Color.White, radius = 6f, center = points[i])
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("6/01", fontSize = 10.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
                                    Text("6/02", fontSize = 10.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
                                    Text("6/03", fontSize = 10.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
                                    Text("오늘", fontSize = 10.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Difficulty setting row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFFFE6D5), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("개인화 두뇌 트레이닝 난이도 설정", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarmOnSurface)
                                Text("치매 진행 예방 최적화 알고리즘에 가동", fontSize = 11.sp, color = WarmGrey)
                            }

                            var levelState by remember { mutableStateOf("초급") }
                            Row(
                                modifier = Modifier.background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp)).padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("초급", "중급").forEach { lvl ->
                                    Box(
                                        modifier = Modifier
                                            .background(if (levelState == lvl) Color(0xFFD84315) else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable { levelState = lvl }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(lvl, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (levelState == lvl) Color.White else WarmGrey)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 5. Guardian Settings Tab
// ---------------------------------------------------------------------
@Composable
fun GuardianSettingsTab(
    viewModel: AppViewModel,
    onOpenModal: (String) -> Unit
) {
    val patient by viewModel.patient.collectAsState()
    val guardian by viewModel.guardian.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("설정 및 계정 관리", fontSize = 12.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
        Text("서비스 환경 설정", fontSize = 22.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)

        Spacer(modifier = Modifier.height(10.dp))

        // Profile brief summary cards
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFFD4B2), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(42.dp).background(Color(0xFFFFF3E0), CircleShape), contentAlignment = Alignment.Center) {
                        Text("🧑‍⚕️", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("주 보호자: ${guardian?.name ?: "김민수"}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = WarmOnSurface)
                        Text("연락처: ${guardian?.phone ?: "010-1234-5678"}", fontSize = 12.sp, color = WarmGrey)
                    }
                }
                Divider(color = Color(0xFFFFF0E5))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(42.dp).background(Color(0xFFE8F5E9), CircleShape), contentAlignment = Alignment.Center) {
                        Text("👵", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("대상 어르신: ${patient?.name ?: "김만옥"}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = WarmOnSurface)
                        Text("관계: ${guardian?.relationship ?: "아들 (장남)"}", fontSize = 12.sp, color = WarmGrey)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Settings option row templates
        val settingsOptions = listOf(
            Triple("환자 정보 수정", "modify_patient_profile", Icons.Default.Person),
            Triple("보호자 정보 수정", "modify_guardian_profile", Icons.Default.SupervisorAccount),
            Triple("알림 및 경보 설정", "ALERT_SETTINGS", Icons.Default.Notifications),
            Triple("안심 구역 재설정", "SET_SAFE_ZONE", Icons.Default.Shield),
            Triple("가족 계정 동기화 연결", "CONNECT_ACCOUNT", Icons.Default.People),
            Triple("프리미엄 구독 관리 결제", "CHECK_SUBSCRIPTION", Icons.Default.CreditCard)
        )

        settingsOptions.forEach { (label, action, icon) ->
            Card(
                onClick = {
                    if (action == "modify_patient_profile" || action == "modify_guardian_profile") {
                        Toast.makeText(context, "${label} 대화 상자를 열었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        onOpenModal(action)
                    }
                },
                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFF0E5), RoundedCornerShape(12.dp))
                    .testTag("setting_$action")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, label, tint = Color(0xFFD84315), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarmOnSurface)
                    }
                    Icon(Icons.Default.ChevronRight, "Navigate", tint = WarmGrey, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Technical general support lines
        Text("기타 정보", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarmOnSurface)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Toast.makeText(context, "고객센터 채널톡에 무인 연결합니다 (1544-0000)", Toast.LENGTH_SHORT).show()
                }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("기억ON 든든 고객센터", fontSize = 13.sp, color = WarmGrey, fontWeight = FontWeight.Bold)
            Text("1544-0000", fontSize = 13.sp, color = Color(0xFFD84315), fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.setMode("SPLASH")
                Toast.makeText(context, "보호자님이 연동 인프라에서 로그아웃 처리를 마쳤습니다.", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_logout_btn")
        ) {
            Text("로그아웃", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ---------------------------------------------------------------------
// 6. Unified ModalOverlay Screen (약추가,가족추가,구독결제,알림설정,안전구역설정 등)
// ---------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalOverlay(
    mode: String,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Modal structure using a full screen beautiful Dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFFFFFDFB),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag("modal_$mode"),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (mode) {
                            "ADD_MED" -> "🏥 새로운 동행 약품 추가"
                            "ADD_FAMILY" -> "👨‍👩‍👧‍👦 새로운 안심 보호 가족 추가"
                            "SOS_HISTORY" -> "🚨 긴급 배회 이탈 호출 기록 이력"
                            "SET_SAFE_ZONE" -> "🛡️ 안심 위치 반경 경계 설정"
                            "ADD_SCHEDULE" -> "📅 중요한 일정 기억 보조 등록"
                            "ALERT_SETTINGS" -> "🔔 푸시 및 통화 알림 상세 설정"
                            "CONNECT_ACCOUNT" -> "🔗 어르신용 패키지 계정 연결"
                            "CHECK_SUBSCRIPTION" -> "💳 프리미엄 연동 멤버십 라이선스"
                            "UPLOAD_PHOTO" -> "🏞️ 회상 치료용 추억 사진 업로드"
                            else -> "상세 화면"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD84315)
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Dismiss")
                    }
                }

                Divider(color = Color(0xFFFFD4B2))

                // Custom render of each additional sub-screen details
                when (mode) {
                    "ADD_MED" -> {
                        var medName by remember { mutableStateOf("") }
                        var dosage by remember { mutableStateOf("1정") }
                        var time by remember { mutableStateOf("오전 09:00") }
                        var memo by remember { mutableStateOf("") }

                        Text("약 이름 및 성분 명칭", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(
                            value = medName,
                            onValueChange = { medName = it },
                            placeholder = { Text("예: 인지 개선제, 저녁 혈압약") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("복용 도스 (단일 복치량)", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = dosage, onValueChange = { dosage = it }, modifier = Modifier.fillMaxWidth())

                        Text("복용 권장 시각", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = time, onValueChange = { time = it }, modifier = Modifier.fillMaxWidth())

                        Text("안전 복용 방법 조치 사항 (메모)", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            placeholder = { Text("예: 빈속 필수, 식후 즉시 다량 생수 지참") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (medName.isNotBlank()) {
                                    viewModel.addMedication(medName, dosage, time, memo)
                                    Toast.makeText(context, "${medName} 약물이 어르신 연동 목록에 추가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "약물 명칭을 상세 정렬하여 적어주십시오.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("save_add_med")
                        ) {
                            Text("약 품목 등재 완료", fontWeight = FontWeight.Bold)
                        }
                    }

                    "ADD_FAMILY" -> {
                        var famName by remember { mutableStateOf("") }
                        var relation by remember { mutableStateOf("") }
                        var fPhone by remember { mutableStateOf("") }
                        var desc by remember { mutableStateOf("") }

                        Text("가족 보모 성함", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = famName, onValueChange = { famName = it }, modifier = Modifier.fillMaxWidth())

                        Text("회원과과의 유기적 관계", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = relation, onValueChange = { relation = it }, placeholder = { Text("예: 차녀, 손자, 간병 돌보미") }, modifier = Modifier.fillMaxWidth())

                        Text("긴급 비상 전화번호", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = fPhone, onValueChange = { fPhone = it }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

                        Text("환자에게 정서감을 주는 인물 세부 설명", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, placeholder = { Text("예: 매주 화요일 어르신과 같이 장보러 오시는 손자입니다.") }, modifier = Modifier.fillMaxWidth())

                        Button(
                            onClick = {
                                if (famName.isNotBlank() && relation.isNotBlank()) {
                                    viewModel.addFamilyMember(famName, relation, fPhone, "guest_avatar", desc)
                                    Toast.makeText(context, "${famName}님이 가족 리스트에 등재되었습니다.", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("save_add_family")
                        ) {
                            Text("가족 안심 네트워크 등록", fontWeight = FontWeight.Bold)
                        }
                    }

                    "SOS_HISTORY" -> {
                        val emergencyLogs by viewModel.emergencyLogs.collectAsState()

                        Text("🚨 비상 위험 이력 리포트", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarmAlert)

                        if (emergencyLogs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("현재까지 송출된 어르신용 SOS 긴급 비상 로그가 존재하지 않습니다.", textAlign = TextAlign.Center, fontSize = 12.sp, color = WarmGrey)
                            }
                        } else {
                            emergencyLogs.forEach { log ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("[🚨 호출상태] ${log.status}", color = WarmAlert, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("수신 위치: ${log.location}", fontSize = 12.sp, color = Color.Black)
                                        Text("시간: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN).format(Date(log.time))}", fontSize = 11.sp, color = WarmGrey)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.clearAllLogs()
                                    Toast.makeText(context, "SOS 이력 전체 비우기 조치 완료", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("로그 이력 비우기", color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    "SET_SAFE_ZONE" -> {
                        var activeRadius by remember { mutableIntStateOf(300) }
                        var activeAddress by remember { mutableStateOf("서울특별시 마포구 공덕동 영진아파트") }

                        Text("지정 안심 자택 주소지", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = activeAddress, onValueChange = { activeAddress = it }, modifier = Modifier.fillMaxWidth())

                        Text("안심 보호 반경 이탈 경보 거리 설정", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(300, 500, 1000).forEach { r ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (activeRadius == r) Color(0xFFD84315) else Color(0xFFFFF0E5),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { activeRadius = r }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${r}m 안심존",
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeRadius == r) Color.White else WarmOnSurface,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.updateSafeZone(activeRadius, activeAddress)
                                Toast.makeText(context, "안심 보호 구역 설정을 반경 ${activeRadius}m로 갱신 적용했습니다.", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("안심 존 범위 재지정 확정", fontWeight = FontWeight.Bold)
                        }
                    }

                    "ADD_SCHEDULE" -> {
                        var schTitle by remember { mutableStateOf("") }
                        var schMemo by remember { mutableStateOf("") }

                        Text("일정 요약 제목", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = schTitle, onValueChange = { schTitle = it }, placeholder = { Text("병원 동행, 영양제 구입 일시 등") }, modifier = Modifier.fillMaxWidth())

                        Text("세부 시간 및 메모", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = schMemo, onValueChange = { schMemo = it }, placeholder = { Text("오후 3시 연세 병원 현관 상봉 조치") }, modifier = Modifier.fillMaxWidth())

                        Button(
                            onClick = {
                                Toast.makeText(context, "새 일정이 환자 어르신용 안심 화면에 등재되었습니다.", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("일정 가입 승인 완료", fontWeight = FontWeight.Bold)
                        }
                    }

                    "ALERT_SETTINGS" -> {
                        var b1 by remember { mutableStateOf(true) }
                        var b2 by remember { mutableStateOf(true) }
                        var b3 by remember { mutableStateOf(false) }

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("비상 배회 이탈 진동 강력 울림 수신", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Switch(checked = b1, onCheckedChange = { b1 = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFD84315)))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("환자 구조 SOS 문자 다중 보호자 전송", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Switch(checked = b2, onCheckedChange = { b2 = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFD84315)))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("안심 정기 약 복용 미인증 시 사이렌 호출", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Switch(checked = b3, onCheckedChange = { b3 = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFD84315)))
                        }

                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary)) {
                            Text("알림 제어 설정 변경 확정", fontWeight = FontWeight.Bold)
                        }
                    }

                    "CONNECT_ACCOUNT" -> {
                        var pairPin by remember { mutableStateOf("ON-59021-X") }

                        Text("어르신용 기기 연동을 위한 전용 페어링 PIN 코드", fontSize = 12.sp, color = WarmGrey)
                        OutlinedTextField(
                            value = pairPin,
                            onValueChange = { pairPin = it },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Text("보내기", color = Color(0xFFD84315), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp).clickable {
                                    Toast.makeText(context, "${pairPin} 핀 연결 요청 신호를 환자 태블릿에 무선 전송했습니다.", Toast.LENGTH_SHORT).show()
                                })
                            }
                        )

                        Text("어르신 기기 화면에서 이 PIN 코드를 입력하면 블루투스 가상 센선 네트워크 연동이 무인 완결 적용됩니다.", fontSize = 11.sp, color = WarmGrey)
                    }

                    "CHECK_SUBSCRIPTION" -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("현재 패키지 요금제: 기억ON 라이트 (무료)", fontWeight = FontWeight.Black, color = Color(0xFFD84315))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("지원 혜택: 기본 실시간 GPS 감지 / 안심 사진첩 3장 수식 가용", fontSize = 11.sp, color = WarmGrey)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF81C784), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("👑 기억ON 안심케어 플러스", fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                                    Text("월 9,900원", fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("지원 혜택: 배회감지 AI 지침 생성 / 안심 사진 무제한 수입 설명 업로드 / 가족 멤버 10인 실시간 폰 동기화", fontSize = 11.sp, color = Color(0xFF2E7D32).copy(alpha = 0.82f))
                            }
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "기억ON 안심케어 플러스 정기 구독 승인이 정상 결제 처리 완료되었습니다. 든든한 정석 밀착 보살핌이 시작됩니다.", Toast.LENGTH_LONG).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("안심케어 플러스 정기 결제 시작하기", fontWeight = FontWeight.Black)
                        }
                    }

                    "UPLOAD_PHOTO" -> {
                        var photoName by remember { mutableStateOf("제주도 동백숲 어머님 여행 기억") }
                        var relationLabel by remember { mutableStateOf("장남") }
                        var photoDesc by remember { mutableStateOf("수채화처럼 붉게 꽃핀 제주도 카멜리아힐 정원 동백나무 벤치 앞에서 며느리와 어머님이 나란히 웃으며 촬영된 모습") }

                        val aiResult by viewModel.aiTherapyResult.collectAsState()
                        val isAiLoading by viewModel.isLoadingAi.collectAsState()

                        Text("보존할 추억 사진의 제목", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = photoName, onValueChange = { photoName = it }, modifier = Modifier.fillMaxWidth())

                        Text("관계 태그", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = relationLabel, onValueChange = { relationLabel = it }, modifier = Modifier.fillMaxWidth())

                        Text("실제 촬영 모습 묘사", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 13.sp)
                        OutlinedTextField(value = photoDesc, onValueChange = { photoDesc = it }, modifier = Modifier.fillMaxWidth())

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.requestAiMemoryTherapy(photoName, photoDesc, relationLabel)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("trigger_ai_caption")
                            ) {
                                Text("🪄 AI 감성 설명 생성 기여", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isAiLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF7B1FA2))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI 치료 문구를 작성하는 중입니다...", fontSize = 11.sp, color = WarmGrey)
                            }
                        } else if (aiResult.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("🤖 생성된 치료 멘트 미리보기", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B1FA2))
                                    Text(aiResult, fontSize = 12.sp, color = Color.Black, lineHeight = 16.sp)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "추억 사진이 업로드되어 환자분 모독에 치료자산으로 배치 완비되었습니다.", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        ) {
                            Text("완료 및 환자 화면 전송", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    )
}

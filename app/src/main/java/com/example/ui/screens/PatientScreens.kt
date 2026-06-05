package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FamilyMemberEntity
import com.example.data.MedicationEntity
import com.example.data.MemoryPhotoEntity
import com.example.ui.theme.*
import com.example.ui.viewmodels.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientScreens(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    // 4 Persistent Bottom Navigation Menus for Patient
    var selectedTab by remember { mutableStateOf("HOME") } // HOME, MEDS, FAMILY, HELP
    val activeAlarm by viewModel.activeMedicationAlarm.collectAsState()
    val activeSosLog by viewModel.activeSosLog.collectAsState()
    var gameActive by remember { mutableStateOf(false) } // support cognitive training if triggered

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            if (!gameActive) {
                NavigationBar(
                    containerColor = Color.White,
                    modifier = Modifier
                        .shadow(16.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("patient_bottom_nav"),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == "HOME",
                        onClick = { selectedTab = "HOME" },
                        icon = { Icon(Icons.Default.Home, "Home", modifier = Modifier.size(28.dp)) },
                        label = { Text("홈", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WarmPrimary,
                            selectedTextColor = WarmPrimary,
                            indicatorColor = WarmLightAmber
                        ),
                        modifier = Modifier.testTag("pat_tab_home")
                    )
                    NavigationBarItem(
                        selected = selectedTab == "MEDS",
                        onClick = { selectedTab = "MEDS" },
                        icon = { Icon(Icons.Default.MedicalServices, "Meds", modifier = Modifier.size(28.dp)) },
                        label = { Text("약 먹기", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WarmPrimary,
                            selectedTextColor = WarmPrimary,
                            indicatorColor = WarmLightAmber
                        ),
                        modifier = Modifier.testTag("pat_tab_meds")
                    )
                    NavigationBarItem(
                        selected = selectedTab == "FAMILY",
                        onClick = { selectedTab = "FAMILY" },
                        icon = { Icon(Icons.Default.People, "Family", modifier = Modifier.size(28.dp)) },
                        label = { Text("가족", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WarmPrimary,
                            selectedTextColor = WarmPrimary,
                            indicatorColor = WarmLightAmber
                        ),
                        modifier = Modifier.testTag("pat_tab_family")
                    )
                    NavigationBarItem(
                        selected = selectedTab == "HELP",
                        onClick = { selectedTab = "HELP" },
                        icon = { Icon(Icons.Default.Help, "Help", modifier = Modifier.size(28.dp)) },
                        label = { Text("도움말", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WarmPrimary,
                            selectedTextColor = WarmPrimary,
                            indicatorColor = WarmLightAmber
                        ),
                        modifier = Modifier.testTag("pat_tab_help")
                    )
                }
            }
        },
        containerColor = Color(0xFFFFF8F2)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (gameActive) {
                // Live overlay Game screen
                PatientGameOverlay(viewModel, onDismiss = { gameActive = false })
            } else {
                when (selectedTab) {
                    "HOME" -> PatientHomeScreen(viewModel, onStartGame = { gameActive = true })
                    "MEDS" -> PatientMedsScreen(viewModel)
                    "FAMILY" -> PatientFamilyScreen(viewModel)
                    "HELP" -> PatientHelpScreen(viewModel)
                }
            }

            // High priority overlay 1: Incoming Pill Alarm
            activeAlarm?.let { med ->
                PatientAlarmOverlay(
                    medication = med,
                    onTaken = {
                        viewModel.toggleMedicationTaken(med)
                        viewModel.dismissMedicationAlarm()
                        Toast.makeText(context, "약 복용을 정상 기록했습니다. 참 든든하고 똑똑하세요!", Toast.LENGTH_LONG).show()
                    },
                    onDelay = {
                        viewModel.dismissMedicationAlarm()
                    }
                )
            }

            // High priority overlay 2: SOS Active broadcast alert
            activeSosLog?.let { log ->
                PatientSosOverlay(
                    guardianContact = "아들 (김민수)님 및 보호 단체 무인 발송",
                    onClose = {
                        viewModel.dismissEmergencySos()
                        Toast.makeText(context, "비상 알림이 정상 종료되었습니다 어르신.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// 1. Patient Home Screen (세 알림 버튼, 초대형 시계, 간소 구조)
// ---------------------------------------------------------------------
@Composable
fun PatientHomeScreen(viewModel: AppViewModel, onStartGame: () -> Unit) {
    val patientInfo by viewModel.patient.collectAsState()
    val isOutsideSafeZone by viewModel.isOutsideSafeZone.collectAsState()

    // High contrast digital clock and simple calendar
    val dateStr = remember { SimpleDateFormat("M월 d일 EEEE", Locale.KOREAN).format(Date()) }
    val timeStr = remember { SimpleDateFormat("aa hh시 mm분", Locale.KOREAN).format(Date()) }

    var isShowingGoHomeAlert by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High visibility Greeting header card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFFFCC80), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "안녕하세요, ${patientInfo?.name ?: "김만옥 어르신"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateStr,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = WarmOnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeStr,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = WarmPrimary
                )
            }
        }

        if (isOutsideSafeZone) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFEF5350), RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "⚠️ 어르신, 안심 반경을 조금 벗어나셨어요! 하단 녹색 [집으로 가기] 버튼을 눌러 안내를 받으세요.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFC62828),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // Button 1 (🚨 SOS 호출 - RED)
        Button(
            onClick = {
                viewModel.triggerPatientEmergencySos()
            },
            colors = ButtonDefaults.buttonColors(containerColor = WarmAlert),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp))
                .testTag("sos_direct_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("🚨", fontSize = 42.sp)
                Column {
                    Text("도와주세요! (SOS)", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("가족에게 즉시 긴급 알림 연락이 갑니다", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }

        // Button 2 (🏠 집으로 가기 / 길 안내 - GREEN)
        Button(
            onClick = { isShowingGoHomeAlert = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp))
                .testTag("go_home_direct_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("🏠", fontSize = 42.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("집으로 가는 길", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(patientInfo?.homeAddress ?: "인근 거주지 찾기 안내", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }

        // Button 3 (🧠 두뇌 퀴즈 맞추기 - BLACK OR PRIMARY)
        Button(
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp))
                .testTag("game_direct_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("🧩", fontSize = 36.sp)
                Column {
                    Text("재미있는 두뇌 게임", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("기억력이 무럭무럭 자라나는 퍼즐 퍼즐!", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Modal dialog guide for map orientation
    if (isShowingGoHomeAlert) {
        AlertDialog(
            onDismissRequest = { isShowingGoHomeAlert = false },
            confirmButton = {
                Button(
                    onClick = { isShowingGoHomeAlert = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("안내 알겠어요", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            },
            title = { Text("🏠 집으로 귀가 길 안내", fontSize = 20.sp, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("어르신, 현재 등록 주소지:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = patientInfo?.homeAddress ?: "안심 거주지 (공덕동 연진아파트 부근)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "지하철 5호선 마포 공덕역 4번 출구에서 200m 직진하시면 동행 안전센터가 위치하고 있습니다.\n헤매고 있다면 길가 이웃들에게 이 화면을 그대로 보여주세요!",
                        fontSize = 14.sp
                    )
                }
            }
        )
    }
}

// ---------------------------------------------------------------------
// 2. Patient Medications screen (복약 체크리스트, 먹었어요 축하음메)
// ---------------------------------------------------------------------
@Composable
fun PatientMedsScreen(viewModel: AppViewModel) {
    val medicationsList by viewModel.medications.collectAsState()
    var showRewardSpeech by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("💊 오늘 내가 먹어야 할 약", fontSize = 26.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)
        Text("의사선생님이 가르쳐준 일시 규정이에요. 잊지말고 터치하세요!", fontSize = 13.sp, color = WarmGrey, textAlign = TextAlign.Center)

        if (medicationsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("지정된 동행 상비약이 아직 없습니다 \n보호자가 약을 등재해주길 기다립니다.", fontSize = 16.sp, textAlign = TextAlign.Center, color = WarmGrey)
            }
        } else {
            medicationsList.forEach { med ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (med.takenStatus) Color(0xFFE8F5E9) else Color(0xFFFFFDFB)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            2.dp,
                            if (med.takenStatus) Color(0xFF81C784) else Color(0xFFFFCC80),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (med.takenStatus) "🟢 복용함" else "🟡 아직 안먹음", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (med.takenStatus) Color(0xFF2E7D32) else Color(0xFFE65100))
                            Text(med.time, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarmGrey)
                        }

                        Text(med.name, fontSize = 26.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)
                        Text("복용 도스: ${med.dosage}", fontSize = 15.sp, color = WarmGrey, fontWeight = FontWeight.Bold)

                        if (med.memo.isNotEmpty()) {
                            Text("⚠️ 주의: ${med.memo}", fontSize = 13.sp, color = WarmPrimary, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = {
                                viewModel.toggleMedicationTaken(med)
                                if (!med.takenStatus) {
                                    // Reward Speech list
                                    val rewards = listOf(
                                        "어르신 대견하십니다! 이번 약을 정말 잊지 않고 잘 챙겨드셨어요. 머리와 몸에 큰 건강을 선물 보냈습니다!",
                                        "약 복용을 깔끔하게 하셨군요! 장남 아들 민수와 손주 은지가 늘 곁에서 든든하게 응원하고 있습니다.",
                                        "참 똑똑하십니다! 복약 완료 신호가 보호자 화면에 즉시 무선 공유되어 마음을 기쁘게 해드립니다."
                                    )
                                    showRewardSpeech = rewards.random()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (med.takenStatus) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                contentColor = if (med.takenStatus) Color(0xFF2E7D32) else Color(0xFFE65100)
                            ),
                            border = BorderStroke(2.dp, if (med.takenStatus) Color(0xFF81C784) else Color(0xFFFFB74D)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                if (med.takenStatus) "✔️ 복용 완료 취소" else "💊 이제 먹었어요 (꾹 터치)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }

    // Cute reward speech dialog popup
    if (showRewardSpeech != null) {
        AlertDialog(
            onDismissRequest = { showRewardSpeech = null },
            confirmButton = {
                Button(
                    onClick = { showRewardSpeech = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("고마워!", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🤖 AI 칭찬 목소리", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF4A148C))
                }
            },
            text = {
                Text(
                    showRewardSpeech!!,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmOnSurface,
                    lineHeight = 22.sp
                )
            }
        )
    }
}

// ---------------------------------------------------------------------
// 3. Patient Family screen (전화 호출, 추억 회상 치료)
// ---------------------------------------------------------------------
@Composable
fun PatientFamilyScreen(viewModel: AppViewModel) {
    val familyMembers by viewModel.familyMembers.collectAsState()
    val memoryPhotos by viewModel.memoryPhotos.collectAsState()

    var activeCallName by remember { mutableStateOf<String?>(null) }
    var activeSpeechText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("👨‍👩‍👧‍👦 나의 보고싶은 가족들", fontSize = 26.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)

        // Family Contacts call section
        familyMembers.forEach { member ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFFFCC80), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (member.relationship.contains("아들")) "👦" else "👵", fontSize = 34.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(member.name, fontSize = 22.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)
                            Text("나와의 유대: ${member.relationship}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WarmGrey)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = member.description,
                        fontSize = 14.sp,
                        color = WarmGrey,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFBF8), RoundedCornerShape(8.dp)).padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { activeCallName = member.name },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.Phone, "Call", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${member.name}님께 즉시 전화걸기", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("🏞️ 어르신의 동행 추억 사진", fontSize = 22.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)

        if (memoryPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("가족이 보내준 안심 추억 사진이 아직 없습니다.", fontSize = 14.sp, color = WarmGrey)
            }
        } else {
            memoryPhotos.forEach { photo ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFFFFD4B2), RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (photo.photoUrl.contains("jeju")) "🌴" else "🎂", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(photo.description, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarmOnSurface, textAlign = TextAlign.Center)

                        if (photo.aiResponse.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(photo.aiResponse, fontSize = 13.sp, color = WarmOnSurface, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { activeSpeechText = photo.aiResponse },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("🔊 AI가 들려주는 옛 이야기 듣기", fontSize = 15.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Call dialing overlay simulation
    if (activeCallName != null) {
        AlertDialog(
            onDismissRequest = { activeCallName = null },
            confirmButton = { },
            dismissButton = {
                Button(
                    onClick = { activeCallName = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("전화 끊기", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            },
            title = {
                Text("📞 전화 거는 전송 신호", fontSize = 20.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(72.dp).background(Color(0xFFE8F5E9), CircleShape), contentAlignment = Alignment.Center) {
                        Text("👴", fontSize = 42.sp)
                    }
                    Text(
                        text = "${activeCallName} 님과 연결 중...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1565C0)
                    )
                    Text("현재 보호자 가상 기기로 전화를 걸고 있습니다. 잠시만 귀를 가까이 대어주세요.", fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            }
        )
    }

    // Play memory caption oral explanation dialog
    if (activeSpeechText != null) {
        AlertDialog(
            onDismissRequest = { activeSpeechText = null },
            confirmButton = {
                Button(
                    onClick = { activeSpeechText = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("듣기 중단", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("🔊 따뜻한 추억 낭송 스피치 음성", fontSize = 18.sp, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF7B1FA2))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("기억ON 인공지능이 감성 주파수 목소리로 어르신 여행기를 잔잔한 음악과 함께 천천히 읽어드리는 중입니다 어르신.", fontSize = 14.sp)
                }
            }
        )
    }
}

// ---------------------------------------------------------------------
// 4. Patient Help Screen (큰 글씨 도움말 설명 소책)
// ---------------------------------------------------------------------
@Composable
fun PatientHelpScreen(viewModel: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📋记忆 기억ON 쉽게 쓰는 방법", fontSize = 26.sp, fontWeight = FontWeight.Black, color = WarmOnSurface)

        val instructions = listOf(
            "🏠 집으로 가기" to "길을 잃어버렸거나 집 위치가 생각 안 날때 녹색 버튼을 꾹 누르세요. 길 설명이 큰 글씨 주소와 함께 나타납니다.",
            "🚨 빨간색 도와주세요!" to "몸이 아프거나 길을 잃어 외롭고 무서울 땐 주저말고 빨간 SOS 버튼을 한 번 터치하세요. 아들과 동행 보호센터에 무음 문자가 즉시 수신 공유됩니다.",
            "💊 약 먹었어요" to "안심 약이 목록에 뜨면 약을 복용한 후에 주홍색 [먹었어요] 버튼을 꾹 잊지 말고 클릭하세요.",
            "🧩 재미있는 게임" to "가끔 퀴즈 게임을 즐겨주세요. 매일 5분씩 게임을 하시면 머리가 맑아지고 치매 기억력 상실을 거뜬히 방지합니다."
        )

        instructions.forEach { (title, detail) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFFFE6D5), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFD84315))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(detail, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarmGrey, lineHeight = 22.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ---------------------------------------------------------------------
// 5. Patient Cognitive Easy Game overlay
// ---------------------------------------------------------------------
@Composable
fun PatientGameOverlay(viewModel: AppViewModel, onDismiss: () -> Unit) {
    var quizNumber by remember { mutableIntStateOf(1) } // 3 steps easily
    var scoreValue by remember { mutableIntStateOf(0) }
    var selectedVal by remember { mutableIntStateOf(-1) }
    var resultStr by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("🧠 두뇌 집중 트레이닝", fontSize = 14.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Text("문제 ${quizNumber}/3", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (quizNumber) {
                1 -> {
                    Text("Q. 다음과 마주할 때 어울리는 짝꿍은 무엇일까요?", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("✏️ ( 연필 )", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFFD84315))

                    Spacer(modifier = Modifier.height(28.dp))

                    val choices = listOf("📖 교과서 공책", "👞 신발 가죽", "🔋 배터리 충전")
                    choices.forEachIndexed { idx, label ->
                        Button(
                            onClick = { selectedVal = idx },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedVal == idx) Color(0xFFD84315) else Color.White,
                                contentColor = if (selectedVal == idx) Color.White else Color.Black
                            ),
                            border = BorderStroke(2.dp, Color(0xFF81C784)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                2 -> {
                    Text("Q. 오늘은 무슨 요일일까요? \n생각해보고 찾아주세요.", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("금요일", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFFD84315))

                    Spacer(modifier = Modifier.height(28.dp))

                    val choices = listOf("일요일", "화요일", "금요일")
                    choices.forEachIndexed { idx, label ->
                        Button(
                            onClick = { selectedVal = idx },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedVal == idx) Color(0xFFD84315) else Color.White,
                                contentColor = if (selectedVal == idx) Color.White else Color.Black
                            ),
                            border = BorderStroke(2.dp, Color(0xFF81C784)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                3 -> {
                    Text("Q. 다음 숫자 배열 중 비어있는 물음표는?", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("2 - 4 - 6 - (?) - 10", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFFD84315))

                    Spacer(modifier = Modifier.height(28.dp))

                    val choices = listOf("7", "8", "9")
                    choices.forEachIndexed { idx, label ->
                        Button(
                            onClick = { selectedVal = idx },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedVal == idx) Color(0xFFD84315) else Color.White,
                                contentColor = if (selectedVal == idx) Color.White else Color.Black
                            ),
                            border = BorderStroke(2.dp, Color(0xFF81C784)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Bottom controller buttons
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (selectedVal == -1) {
                        Toast.makeText(context, "답안을 하나 꼭 터치하세요!", Toast.LENGTH_SHORT).show()
                    } else {
                        // verify logic
                        val isCorrect = when (quizNumber) {
                            1 -> selectedVal == 0
                            2 -> selectedVal == 2
                            3 -> selectedVal == 1
                            else -> false
                        }

                        if (isCorrect) {
                            scoreValue += 30
                            Toast.makeText(context, "딩동댕! 정답 우등생이네요!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "조금 오차가 있지만 뇌 운동에는 큰 활력이 되었습니다!", Toast.LENGTH_SHORT).show()
                        }

                        if (quizNumber < 3) {
                            quizNumber++
                            selectedVal = -1
                        } else {
                            // game complete
                            resultStr = "최종 두뇌 활력 점수: ${scoreValue + 10}점!\n보호자 앱으로 기억ON 소견이 무사 공유 되었습니다."
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (quizNumber == 3) "학습 학습 완료" else "다음 문제 풀기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("중단하고 메인으로", fontSize = 15.sp)
            }
        }
    }

    if (resultStr != null) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                resultStr = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismiss()
                        resultStr = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("예, 확인했습니다!", fontSize = 16.sp)
                }
            },
            title = { Text("🎉 두뇌 트레이닝 평가 완료!", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(resultStr!!, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarmOnSurface)
            }
        )
    }
}

// ---------------------------------------------------------------------
// 6. Senior Prescription alarm Overlay dialog
// ---------------------------------------------------------------------
@Composable
fun PatientAlarmOverlay(
    medication: MedicationEntity,
    onTaken: () -> Unit,
    onDelay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, Color(0xFFD84315), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("⏰ 꼭 먹어야 할 약속 시간!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD84315))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFFFE6D5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 48.sp)
                }

                Text(
                    text = medication.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = WarmOnSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "용량: ${medication.dosage}  /  규정 시간: ${medication.time}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey
                )

                if (medication.memo.isNotEmpty()) {
                    Text(
                        text = "보호자 알림: ${medication.memo}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD84315),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onTaken,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("alarm_taken_btn")
                ) {
                    Text("✔️ 약 지금 바로 먹었음 (인증)", fontSize = 22.sp, fontWeight = FontWeight.Black)
                }

                OutlinedButton(
                    onClick = onDelay,
                    border = BorderStroke(1.dp, WarmGrey),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmGrey),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("5분 후에 다시 알려줘", fontSize = 16.sp)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 7. Emergency Calling Alarm broadcast overlay
// ---------------------------------------------------------------------
@Composable
fun PatientSosOverlay(
    guardianContact: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC62828).copy(alpha = 0.96f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🚨", fontSize = 72.sp)
            }

            Text(
                text = "가족 비상 호출 신호 송출 가동 중",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "보호자 아들 핸드폰으로 실시간 긴급 문자, 위도 GPS 경보 지도, 소리 비상 사이렌 신호가 무선 공유 전송되었습니다.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Text(
                text = "안심 수신 연락: $guardianContact",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFC62828)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("sos_close_btn")
            ) {
                Text("✔️ 이제 괜찮아요 (안심 종료)", fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.FamilyMemberEntity
import com.example.data.MedicationEntity
import com.example.data.MemoryPhotoEntity
import com.example.ui.theme.*
import com.example.ui.viewmodels.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PatientScreens(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeAlarm by viewModel.activeMedicationAlarm.collectAsState()
    val activeSosLog by viewModel.activeSosLog.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Screen Router
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { screen ->
            when (screen) {
                "MAIN" -> PatientMainScreen(viewModel)
                "GO_HOME" -> PatientGoHomeScreen(viewModel)
                "CALL_FAMILY" -> PatientCallFamilyScreen(viewModel)
                "AI_THERAPY" -> PatientAiTherapyScreen(viewModel)
                "GAME" -> PatientCognitiveGameScreen(viewModel)
                else -> PatientMainScreen(viewModel)
            }
        }

        // Full Screen Overlay: Medication Alarm Notification
        activeAlarm?.let { med ->
            MedicationAlarmOverlay(
                medication = med,
                onTaken = {
                    viewModel.toggleMedicationTaken(med)
                    viewModel.dismissMedicationAlarm()
                },
                onDelay = {
                    viewModel.dismissMedicationAlarm()
                }
            )
        }

        // Full Screen Overlay: SOS Confirmation
        activeSosLog?.let { log ->
            SosEmergencyOverlay(
                guardianName = viewModel.guardian.value?.name ?: "가족",
                onClose = { viewModel.dismissEmergencySos() }
            )
        }
    }
}

// ---------------------------------------------------------------------
// 1. Patient Main Screen
// ---------------------------------------------------------------------
@Composable
fun PatientMainScreen(viewModel: AppViewModel) {
    val patientInfo by viewModel.patient.collectAsState()
    val activeAlarm by viewModel.activeMedicationAlarm.collectAsState()

    // Retrieve today's date in Korean format
    val todayFormatted = remember {
        val sdf = SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN)
        sdf.format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Block: Greetings and Date
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🌸",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "기억지킴이",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grand, highly visible greeting text
            Card(
                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "안녕하세요, ${patientInfo?.name ?: "어르신"} 님",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmOnSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "오늘은 $todayFormatted 입니다.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarmGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Center Block: Three Massive Buttons (Elderly Easy-To-Tap, size 48dp+)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Button 🏠 집으로 가기
            Card(
                onClick = { viewModel.setScreen("GO_HOME") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .border(2.dp, WarmSecondary, RoundedCornerShape(20.dp))
                    .testTag("patient_go_home_button"),
                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏠", fontSize = 42.sp)
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = "집으로 가기",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = WarmSecondary
                        )
                        Text(
                            text = "길 안내를 시작합니다",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = WarmGrey
                        )
                    }
                }
            }

            // Button 📞 가족에게 전화하기
            Card(
                onClick = { viewModel.setScreen("CALL_FAMILY") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .border(2.dp, WarmPrimary, RoundedCornerShape(20.dp))
                    .testTag("patient_call_family_button"),
                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📞", fontSize = 42.sp)
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = "가족에게 전화하기",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = WarmPrimary
                        )
                        Text(
                            text = "등록된 가족에게 전화를 겁니다",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = WarmGrey
                        )
                    }
                }
            }

            // Button 🚨 도와주세요 (緊急)
            Card(
                onClick = { viewModel.triggerPatientEmergencySos() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .border(3.dp, WarmAlert, RoundedCornerShape(20.dp))
                    .testTag("patient_sos_button"),
                colors = CardDefaults.cardColors(containerColor = WarmLightRed),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚨", fontSize = 46.sp)
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = "도와주세요",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmAlert
                        )
                        Text(
                            text = "보호자에게 알림을 보내고 119를 연결합니다",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmAlert.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Lower Block: Supportive Auxiliary Functions (두뇌 놀이, 옛날 사진)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cognitive Training Button
                Button(
                    onClick = { viewModel.setScreen("GAME") },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("patient_game_btn")
                ) {
                    Text("🧠 두뇌 놀이", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // Photo Therapy Button
                Button(
                    onClick = { viewModel.setScreen("AI_THERAPY") },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmTertiary.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("patient_therapy_btn")
                ) {
                    Text("🌸 옛날 사진", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Discrete Back to Launcher link
            TextButton(
                onClick = { viewModel.setMode("START") }
            ) {
                Text(
                    text = "시작 화면으로 돌아가기",
                    fontSize = 14.sp,
                    color = WarmGrey,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// 2. Go Home Guided Navigation Screen
// ---------------------------------------------------------------------
@Composable
fun PatientGoHomeScreen(viewModel: AppViewModel) {
    val patient by viewModel.patient.collectAsState()
    val context = LocalContext.current
    var speechGuideVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🏠 집으로 편안히 가기",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = WarmSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "현재 위치가 가족에게 안전하게 전송되었습니다.",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmActiveGreen,
                textAlign = TextAlign.Center
            )
        }

        // Calming Visual Guide Map Simulation
        Card(
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 24.dp)
                .border(2.dp, WarmBorder, RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = "Walking Home Direction",
                    tint = WarmSecondary,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "등록한 안심 자택 주소:",
                    fontSize = 15.sp,
                    color = WarmGrey,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = patient?.homeAddress ?: "등록된 주소가 없습니다.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmOnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Divider(color = WarmBorder)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "집으로 가는 길을 안내해드릴게요.\n걱정하지 마세요.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                if (speechGuideVisible) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarmLightTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔊", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                text = "\"김만옥 어르신, 마포공덕사거리에서 골목 진입 후 직진하시면 곧 안심 자택이 나옵니다. 길가에 멈춰 기다리셔도 아들 민수님이 마중 올 예정입니다.\"",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmSecondary
                            )
                        }
                    }
                }
            }
        }

        // Giant Action control deck (Max 2 buttons)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { speechGuideVisible = !speechGuideVisible },
                colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, "Speak Guide")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("귀가 친절 음성 듣기", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { viewModel.setScreen("MAIN") },
                colors = ButtonDefaults.buttonColors(containerColor = WarmGrey),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("이전 화면으로 (돌아가기)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------
// 3. Call Family Screen (가족에게 전화하기 / 가족 기억 보조)
// ---------------------------------------------------------------------
@Composable
fun PatientCallFamilyScreen(viewModel: AppViewModel) {
    val familyList by viewModel.familyMembers.collectAsState()
    var selectedFamily by remember { mutableStateOf<FamilyMemberEntity?>(null) }
    val context = LocalContext.current

    if (selectedFamily == null) {
        // List family members to choose from
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📞 가족 목록",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = WarmPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "얼굴을 보고 전화를 걸 대상을 선택해 주세요.",
                fontSize = 16.sp,
                color = WarmGrey,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(familyList) { family ->
                    Card(
                        onClick = { selectedFamily = family },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .border(2.dp, WarmBorder, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = WarmSurface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Avatar Icon representing photo URL
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(WarmLightAmber),
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarEmoji = when (family.photoUrl) {
                                    "son" -> "👨"
                                    "d_in_law" -> "👩"
                                    "granddaughter" -> "👧"
                                    else -> "❤️"
                                }
                                Text(avatarEmoji, fontSize = 32.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "${family.relationship} (${family.name})",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmOnSurface
                                )
                                Text(
                                    text = family.phone,
                                    fontSize = 15.sp,
                                    color = WarmGrey,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.setScreen("MAIN") },
                colors = ButtonDefaults.buttonColors(containerColor = WarmGrey),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("이전 화면으로 (돌아가기)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // Detailed relationship assistant photo screen (Requirements 6)
        val family = selectedFamily!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "❤️ 가족 정보 돋보기",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = WarmPrimary
            )

            // Giant Avatar presentation for visual aid
            Card(
                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 20.dp)
                    .border(2.dp, WarmPrimary, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(WarmLightAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarEmoji = when (family.photoUrl) {
                            "son" -> "👨"
                            "d_in_law" -> "👩"
                            "granddaughter" -> "👧"
                            else -> "❤️"
                        }
                        Text(avatarEmoji, fontSize = 72.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "이 사람은 ${family.relationship} ${family.name} 입니다.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmOnSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = family.description,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarmGrey,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Giant Operations deck (Limit 3 buttons)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "${family.name} 님께 모의 전화를 연결합니다.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmActiveGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, "Call")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("전화하기", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { selectedFamily = null },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("다시 보기 (목록으로)", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 4. AI recollection therapy viewer (AI 회상 치료)
// ---------------------------------------------------------------------
@Composable
fun PatientAiTherapyScreen(viewModel: AppViewModel) {
    val photoList by viewModel.memoryPhotos.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "🌸 AI 마음 회상 요법",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = WarmTertiary
        )

        if (photoList.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "등록된 즐거운 기억 사진이 없습니다.\n보호자가 사진을 등록하면 AI가 특별한 이야기를 만듭니다.",
                    fontSize = 18.sp,
                    color = WarmGrey,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val validIndex = currentIndex.coerceIn(0, photoList.size - 1)
            val memory = photoList[validIndex]

            // Main Slides Frame
            Card(
                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
                    .border(2.dp, WarmTertiary, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Photo illustration representation using high-contrast emoji graphics
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(WarmLightTeal, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val visualEmoji = when (memory.photoUrl) {
                            "jeju" -> "🌴🌊✈️"
                            "birthday" -> "🎂👨‍👩‍👧‍👦🎉"
                            else -> "🏡❤️🌸"
                        }
                        Text(visualEmoji, fontSize = 52.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "기쁜 시절의 추억:",
                        fontSize = 14.sp,
                        color = WarmTertiary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = memory.aiResponse.ifEmpty { memory.description },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmOnSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                }
            }
        }

        // Action Deck (Limit under 3 buttons)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (currentIndex > 0) currentIndex-- else currentIndex = photoList.size - 1
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmTertiary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("◀ 이전 사진", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (currentIndex < photoList.size - 1) currentIndex++ else currentIndex = 0
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmTertiary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("다음 사진 보기 ▶", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "보호자(김민수)님께 바로 전화를 겁니다.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1.2f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmActiveGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📞", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("가족에게 전화하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { viewModel.setScreen("MAIN") },
                    modifier = Modifier.weight(0.8f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGrey),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("종료", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 5. Cognitive Training Game Screen (오늘의 인지 훈련 게임)
// ---------------------------------------------------------------------
@Composable
fun PatientCognitiveGameScreen(viewModel: AppViewModel) {
    var gameState by remember { mutableStateOf("INTRO") } // INTRO, PLAYING, SUCCESS
    var selectedOrderList by remember { mutableStateListOf<Int>() }
    val numbersList = remember { listOf(2, 4, 1, 3) } // Simple numbers to click: 1 -> 2 -> 3 -> 4
    val targetOrder = listOf(1, 2, 3, 4)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "🧠 상쾌한 뇌 체조 게임",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = WarmSecondary
        )

        when (gameState) {
            "INTRO" -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp)
                        .border(2.dp, WarmBorder, RoundedCornerShape(24.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🧠", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "숫자 똑똑하게 고르기",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "화면에 있는 숫자 구슬들을\n[ 1 ➔ 2 ➔ 3 ➔ 4 ]\n순서대로 올바르게 클릭하세요.\n난이도는 '아주 쉬움' 입니다.",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmGrey,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        selectedOrderList.clear()
                        gameState = "PLAYING"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("게임 바로 시작하기", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            "PLAYING" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "아래 동그라미를 순서대로 누르세요!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmOnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "내가 주운 숫자: ${selectedOrderList.joinToString(" ➔ ")}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmSecondary,
                        modifier = Modifier
                            .background(WarmLightTeal, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 2x2 Grid of buttons
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            numbersList.take(2).forEach { num ->
                                NumberSphere(
                                    number = num,
                                    isTapped = selectedOrderList.contains(num),
                                    onTap = {
                                        if (!selectedOrderList.contains(num)) {
                                            selectedOrderList.add(num)
                                            // Check sequence correctness
                                            val index = selectedOrderList.indexOf(num)
                                            if (selectedOrderList[index] != targetOrder[index]) {
                                                Toast.makeText(context, "잘못된 순서입니다! 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                                                selectedOrderList.clear()
                                            } else if (selectedOrderList.size == 4) {
                                                gameState = "SUCCESS"
                                                viewModel.recordCognitiveScore(85, "오늘 집중력 최고! 85점을 기록하셨습니다.")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            numbersList.takeLast(2).forEach { num ->
                                NumberSphere(
                                    number = num,
                                    isTapped = selectedOrderList.contains(num),
                                    onTap = {
                                        if (!selectedOrderList.contains(num)) {
                                            selectedOrderList.add(num)
                                            val index = selectedOrderList.indexOf(num)
                                            if (selectedOrderList[index] != targetOrder[index]) {
                                                Toast.makeText(context, "잘못된 순서입니다! 처음부터 다시!", Toast.LENGTH_SHORT).show()
                                                selectedOrderList.clear()
                                            } else if (selectedOrderList.size == 4) {
                                                gameState = "SUCCESS"
                                                viewModel.recordCognitiveScore(90, "집중도가 높아요! 전일 대비 10점 우상향 중입니다.")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { gameState = "INTRO" },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGrey),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("다시시작", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            "SUCCESS" -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp)
                        .border(3.dp, WarmActiveGreen, RoundedCornerShape(24.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🎉🌟👏", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "참 잘하셨습니다!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = WarmActiveGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "오늘 인지 훈련 점수: 85점\n어제보다 5점 상승했습니다!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmOnSurface,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "기억 훈련 결과가 보호자 폰에\n자랑스럽게 전송되었습니다.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = WarmGrey,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = { viewModel.setScreen("MAIN") },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("확인 완료 (메인으로)", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NumberSphere(
    number: Int,
    isTapped: Boolean,
    onTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(if (isTapped) WarmActiveGreen else WarmSecondary)
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Black
        )
    }
}

// ---------------------------------------------------------------------
// 6. Medication Notification Alarm Overlay dialog
// ---------------------------------------------------------------------
@Composable
fun MedicationAlarmOverlay(
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
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(4.dp, WarmPrimary, RoundedCornerShape(28.dp))
                .testTag("med_alarm_overlay")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔔⏰🔔",
                    fontSize = 44.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = medication.time,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${medication.name} 드실 시간입니다.",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = WarmOnSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (medication.memo.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarmLightAmber),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 메모: ${medication.memo}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp).fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Huge action buttons for easy accessibility
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onTaken,
                        colors = ButtonDefaults.buttonColors(containerColor = WarmActiveGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .testTag("alarm_taken_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💊", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("먹었어요 (안심완료)", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onDelay,
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGrey),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("alarm_delay_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("나중에 먹을게요 (10분 후 알림)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 7. Emergency SOS confirmation layout
// ---------------------------------------------------------------------
@Composable
fun SosEmergencyOverlay(
    guardianName: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = 0.88f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(6.dp, WarmAlert, RoundedCornerShape(24.dp))
                .testTag("patient_sos_activated")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🚨 비상 알림 발생 🚨", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = WarmAlert)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "도움이 필요합니다.\n\n보호자($guardianName 님)에게\n긴급 알림과 현재 감지 위치를\n안전하게 전송했습니다.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                //Flashing 119 Call button
                Button(
                    onClick = { /* simulated dialing */ },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmAlert),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📞", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("119 바로 전화걸기", fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "보호자 통화 및 안심 상태 복구 (취소)",
                        fontSize = 16.sp,
                        color = WarmGrey,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

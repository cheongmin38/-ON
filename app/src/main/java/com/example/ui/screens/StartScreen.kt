package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.AppViewModel
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------
// 1. Splash Screen
// ---------------------------------------------------------------------
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds timer
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF8F2), Color(0xFFFFEFE3))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant, warm floating icon logo
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(Color.White, RoundedCornerShape(32.dp))
                    .border(1.dp, Color(0xFFFFD4B2), RoundedCornerShape(32.dp))
                    .shadow(4.dp, RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌸", fontSize = 68.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Name: 기억ON
            Text(
                text = "기억ON",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFD84315),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Slogan
            Text(
                text = "“기억이 흐려져도, 안심은 계속됩니다.”",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Tap background to skip immediately for comfortable UX testing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onTimeout() }
        )
    }
}

// ---------------------------------------------------------------------
// 2. Login / Sign Up Screen
// ---------------------------------------------------------------------
@Composable
fun LoginScreen(onNavigateToSelection: () -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F2))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Small logo emblem
            Text("🌸", fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))

            Text(
                text = "기억ON",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFD84315)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Essential connecting disclaimer
            Text(
                text = "보호자와 환자를 안전하게 연결합니다.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Cellular Phone Login Input Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "휴대폰 번호 로그인",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD84315),
                        fontSize = 15.sp
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = { Text("휴대폰 번호 입력 (- 제외)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD84315),
                            unfocusedBorderColor = Color(0xFFFFD4B2)
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("비밀번호 입력") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD84315),
                            unfocusedBorderColor = Color(0xFFFFD4B2)
                        )
                    )

                    Button(
                        onClick = { onNavigateToSelection() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_login_btn")
                    ) {
                        Text("로그인", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Social Login Row Titles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFFFE6D5))
                Text(
                    text = "간편 SNS 로그인",
                    fontSize = 12.sp,
                    color = WarmGrey,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontWeight = FontWeight.Medium
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFFFE6D5))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Social buttons
            // 1. Kakao (Yellow style)
            Button(
                onClick = { onNavigateToSelection() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💬", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                    Text("카카오톡으로 시작하기", color = Color(0xFF3E2723), fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Google
            Button(
                onClick = { onNavigateToSelection() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, Color(0xFFFFD4B2), RoundedCornerShape(14.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌐", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                    Text("Google 계정으로 로그인", color = Color(0xFF3E2723), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Apple
            Button(
                onClick = { onNavigateToSelection() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.Black, RoundedCornerShape(14.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🍎", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                    Text("Apple ID로 계속하기", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // SignUp & Recover Links in footer
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "회원가입",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828),
                modifier = Modifier
                    .clickable { onNavigateToSelection() }
                    .testTag("signup_link")
            )
            Text(
                "|",
                fontSize = 14.sp,
                color = Color(0xFFFFD4B2)
            )
            Text(
                text = "비밀번호 찾기",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = WarmGrey,
                modifier = Modifier.clickable { }
            )
        }
    }
}

@Composable
fun transparentButtonColors() = ButtonDefaults.buttonColors(
    containerColor = Color.Transparent,
    contentColor = Color.White
)

// ---------------------------------------------------------------------
// 3. User Type Selection Screen
// ---------------------------------------------------------------------
@Composable
fun UserTypeSelectionScreen(
    onSelectGuardian: () -> Unit,
    onSelectPatient: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F2))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Text("🌸 회원 유형 선택", fontSize = 16.sp, color = Color(0xFFD84315), fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "누가 서비스를 이용하시나요?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = WarmOnSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "보호자와 환자의 앱 화면이 다르게 맞춤 제공됩니다.",
                fontSize = 14.sp,
                color = WarmGrey,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Guardian Selection Card
            Card(
                onClick = onSelectGuardian,
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(2.dp, Color(0xFF90CAF9), RoundedCornerShape(24.dp))
                    .shadow(3.dp, RoundedCornerShape(24.dp))
                    .testTag("select_guardian_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧑‍⚕️", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "보호자입니다",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1565C0)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "가족의 위치, 약 복용, 안전 상태를 실시간 확인합니다.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Patient Selection Card
            Card(
                onClick = onSelectPatient,
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(2.dp, Color(0xFFA5D6A7), RoundedCornerShape(24.dp))
                    .shadow(3.dp, RoundedCornerShape(24.dp))
                    .testTag("select_patient_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👴", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "환자입니다",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "집으로 가기, 가족 전화, 도움 요청을 한눈에 보며 쉽게 이용합니다.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 4. Guardian Onboarding Screen
// ---------------------------------------------------------------------
@Composable
fun GuardianOnboardingScreen(onFinish: () -> Unit) {
    var pageIndex by remember { mutableStateOf(0) }
    val titles = listOf(
        "가족의 안전을 확인하세요",
        "약 복용을 놓치지 않게 도와드립니다",
        "긴급 상황을 빠르게 알립니다",
        "기억을 AI가 함께 도와드립니다"
    )
    val descriptions = listOf(
        "실시간 위치와 안전구역 이탈 상태를 보며 소중한 어르신의 배회를 방지할 수 있습니다.",
        "약 복용 예정 시간 알림과 실시간 달력 복용 이력을 보호자가 직관적으로 확인가능합니다.",
        "환자가 큰 '도와주세요' 버튼을 누르면 정밀한 GPS 위치와 함께 보호자에게 강력한 경고가 전송됩니다.",
        "가족 사진, 일상 일정, 보조 인지 퀴즈를 AI가 환자 눈높이에 맞게 심리적 안정감을 동반해 제공합니다."
    )
    val emojis = listOf("📍", "💊", "🚨", "🤖")
    val colors = listOf(Color(0xFFE3F2FD), Color(0xFFE8F5E9), Color(0xFFFFEBEE), Color(0xFFFFF3E0))
    val accentColors = listOf(Color(0xFF1565C0), Color(0xFF2E7D32), Color(0xFFC62828), Color(0xFFD84315))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[pageIndex])
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Progress bar dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..3) {
                    Box(
                        modifier = Modifier
                            .width(if (pageIndex == i) 24.dp else 8.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(if (pageIndex == i) accentColors[pageIndex] else Color.LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Graphics frame
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(emojis[pageIndex], fontSize = 72.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = titles[pageIndex],
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = accentColors[pageIndex],
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = descriptions[pageIndex],
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 22.sp
            )
        }

        // Action controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            if (pageIndex < 3) {
                Button(
                    onClick = { pageIndex++ },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColors[pageIndex]),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("다음 단계", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onFinish,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColors[pageIndex]),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("guardian_onboarding_start_btn")
                ) {
                    Text("안심 보호 시작하기", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 5. Patient Onboarding Screen
// ---------------------------------------------------------------------
@Composable
fun PatientOnboardingScreen(onFinish: () -> Unit) {
    var pageIndex by remember { mutableStateOf(0) }
    val titles = listOf(
        "집으로 쉽게 돌아갈 수 있어요",
        "가족에게 바로 전화할 수 있어요",
        "도움이 필요하면 바로 알려요"
    )
    val descriptions = listOf(
        "크고 선명한 집 버튼만 터치하면 안전하게 집이 어디인지 보며 길을 바로 알려드립니다.",
        "이름이나 번호 조작이 헷갈려도 얼굴 사진 하나로 아들/딸에게 바로 전화할 수 있습니다.",
        "마음이 불안하거나 다칠 경우 '도와주세요'를 누르면 즉시 가족 휴대폰에 든든한 알림 신호가 전달됩니다."
    )
    val emojis = listOf("🏠", "📞", "🚨")
    val colors = listOf(Color(0xFFE8F5E9), Color(0xFFE3F2FD), Color(0xFFFFEBEE))
    val accentColors = listOf(Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFFC62828))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[pageIndex])
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Highly visible giant indicator dots for elderly citizens
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (i in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (pageIndex == i) accentColors[pageIndex] else Color.LightGray.copy(alpha = 0.6f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .border(3.dp, accentColors[pageIndex], RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emojis[pageIndex], fontSize = 92.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = titles[pageIndex],
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = descriptions[pageIndex],
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF3E2723),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
                lineHeight = 26.sp
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            if (pageIndex < 2) {
                Button(
                    onClick = { pageIndex++ },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColors[pageIndex]),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text("다음 글 읽기", fontSize = 21.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            } else {
                Button(
                    onClick = onFinish,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColors[pageIndex]),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("patient_onboarding_start_btn")
                ) {
                    Text("시작하기", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 6. Guardian Basic Info Registration Screen
// ---------------------------------------------------------------------
@Composable
fun RegisterGuardianScreen(
    viewModel: AppViewModel,
    onFinish: () -> Unit
) {
    var name by remember { mutableStateOf("김민수") }
    var phone by remember { mutableStateOf("010-1234-5678") }
    var relationship by remember { mutableStateOf("장남 (아들)") }
    var inviteCode by remember { mutableStateOf("ON-983021") }
    var isNotificationAllowed by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F2))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text("🌸 간편 초기 등록", fontSize = 15.sp, color = Color(0xFFD84315), fontWeight = FontWeight.Bold)

            Text(
                text = "보호자 등록 페이지",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = WarmOnSurface
            )

            Text(
                text = "환자의 소중한 공간과 이력을 관리하기 위해 기본 인적 정보를 등록해 주세요.",
                fontSize = 13.sp,
                color = WarmGrey,
                fontWeight = FontWeight.Medium
            )

            Divider(color = Color(0xFFFFD4B2), modifier = Modifier.padding(vertical = 4.dp))

            // Fields
            Text("보호자 성함", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 14.sp)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD84315))
            )

            Text("휴대폰 번호", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 14.sp)
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD84315))
            )

            Text("환자와의 관계", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 14.sp)
            OutlinedTextField(
                value = relationship,
                onValueChange = { relationship = it },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD84315))
            )

            Text("환자 코드 입력 (초대 코드 또는 새 환자 생성)", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 14.sp)
            OutlinedTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD84315)),
                trailingIcon = {
                    Text(
                        "인증완료",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )

            // Notifications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("안심 긴급 알림 허용", fontWeight = FontWeight.Bold, color = WarmOnSurface, fontSize = 14.sp)
                    Text("배회 감지 및 긴급 SOS 경보를 즉시 푸시로 수신", fontSize = 11.sp, color = WarmGrey)
                }
                Switch(
                    checked = isNotificationAllowed,
                    onCheckedChange = { isNotificationAllowed = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFD84315))
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    viewModel.updatePatientProfile("김만옥", "서울특별시 마포구 공덕동 102", phone)
                    onFinish()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_guardian_reg_btn")
            ) {
                Text("보호 시작하기 (다음)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ---------------------------------------------------------------------
// 7. Patient Basic Info Registration Screen
// ---------------------------------------------------------------------
@Composable
fun RegisterPatientScreen(
    viewModel: AppViewModel,
    onFinish: () -> Unit
) {
    var pName by remember { mutableStateOf("김만옥") }
    var birthDate by remember { mutableStateOf("1952년 11월 12일") }
    var address by remember { mutableStateOf("서울특별시 마포구 공덕동 영진아파트") }
    var guardianContact by remember { mutableStateOf("010-1234-5678") }
    var emergencyContact by remember { mutableStateOf("119 (안심 소방망 단축코드)") }
    var placesVal by remember { mutableStateOf("가까운 공덕 장터, 영진 노인 쉼터") }
    var takingMeds by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F2))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text("🌸 어르신용 초기 등록", fontSize = 15.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Black)

            Text(
                text = "보호 대상자(어르신) 등록",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = WarmOnSurface
            )

            Text(
                text = "가족 및 구조 대원이 실종 시에 길 안내 정보를 정확히 확보할 수 있도록 꼼꼼히 적어주세요. (글씨가 아주 커서 편하게 타이핑 가능합니다)",
                fontSize = 12.sp,
                color = WarmGrey,
                fontWeight = FontWeight.Bold
            )

            Divider(color = Color(0xFFFFD4B2), modifier = Modifier.padding(vertical = 4.dp))

            Text("어르신 성함", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
            OutlinedTextField(
                value = pName,
                onValueChange = { pName = it },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth()
            )

            Text("생년월일", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth()
            )

            Text("거주지 주소 (가장 중요)", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth()
            )

            Text("보호자 휴대폰", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
            OutlinedTextField(
                value = guardianContact,
                onValueChange = { guardianContact = it },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth()
            )

            Text("응급 상황 긴급 연락처", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
            OutlinedTextField(
                value = emergencyContact,
                onValueChange = { emergencyContact = it },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth()
            )

            Text("자주 방문하는 장소", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
            OutlinedTextField(
                value = placesVal,
                onValueChange = { placesVal = it },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Checkbox(
                    checked = takingMeds,
                    onCheckedChange = { takingMeds = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E7D32))
                )
                Text(
                    text = "현재 정기적으로 매일 복용 중인 치매/지병 약이 있습니다.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    viewModel.updatePatientProfile(pName, address, guardianContact)
                    onFinish()
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("submit_patient_reg_btn")
            ) {
                Text("기억 등록 완료", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

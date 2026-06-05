package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmSecondary
import com.example.ui.theme.WarmTertiary

@Composable
fun StartScreen(
    onStartPatientMode: () -> Unit,
    onStartGuardianMode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Animated Beautiful Heart/Shield Graphic to represent Safety and Care
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer comforting circle rings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = WarmSecondary.copy(alpha = 0.1f),
                        radius = size.minDimension / 1.8f,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawCircle(
                        color = WarmPrimary.copy(alpha = 0.15f),
                        radius = size.minDimension / 2.2f,
                        style = Stroke(width = 8.dp.toPx())
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Care Logo",
                    tint = WarmPrimary,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Large Title Header
            Text(
                text = "기억지킴이",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Comforting Slogan Text
            Text(
                text = "기억을 지키고, 가족의 불안을 줄입니다.",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Large Button 1: Patient Mode Button (Very large and visible)
            Card(
                onClick = onStartPatientMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .border(3.dp, WarmPrimary, RoundedCornerShape(24.dp))
                    .testTag("start_patient_mode_btn"),
                colors = CardDefaults.cardColors(
                    containerColor = WarmPrimary
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "👴",
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "환자 모드 시작",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large Button 2: Guardian Mode Button
            Card(
                onClick = onStartGuardianMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .shadow(6.dp, RoundedCornerShape(24.dp))
                    .border(2.dp, WarmSecondary, RoundedCornerShape(24.dp))
                    .testTag("start_guardian_mode_btn"),
                colors = CardDefaults.cardColors(
                    containerColor = WarmSecondary
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🧑‍⚕️",
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "보호자 모드 시작",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
        
        // Footer signature
        Text(
            text = "AI 실시간 가족 돌봄 든든히 지킴 서비스",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            ),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
        )
    }
}

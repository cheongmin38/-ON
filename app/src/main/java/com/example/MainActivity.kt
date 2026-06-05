package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GuardianScreens
import com.example.ui.screens.PatientScreens
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full edge-to-edge draw boundary for immersive Material 3 look
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                // Initialize Central ViewModel of our local Care engine
                val viewModel: AppViewModel = viewModel()
                val currentMode by viewModel.currentMode.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    when (currentMode) {
                        "SPLASH" -> {
                            com.example.ui.screens.SplashScreen(
                                onTimeout = { viewModel.setMode("LOGIN") }
                            )
                        }
                        "LOGIN" -> {
                            com.example.ui.screens.LoginScreen(
                                onNavigateToSelection = { viewModel.setMode("USER_TYPE_SELECTION") }
                            )
                        }
                        "USER_TYPE_SELECTION" -> {
                            com.example.ui.screens.UserTypeSelectionScreen(
                                onSelectGuardian = { viewModel.setMode("ONBOARDING_GUARDIAN") },
                                onSelectPatient = { viewModel.setMode("ONBOARDING_PATIENT") }
                            )
                        }
                        "ONBOARDING_GUARDIAN" -> {
                            com.example.ui.screens.GuardianOnboardingScreen(
                                onFinish = { viewModel.setMode("REGISTER_GUARDIAN") }
                            )
                        }
                        "ONBOARDING_PATIENT" -> {
                            com.example.ui.screens.PatientOnboardingScreen(
                                onFinish = { viewModel.setMode("REGISTER_PATIENT") }
                            )
                        }
                        "REGISTER_GUARDIAN" -> {
                            com.example.ui.screens.RegisterGuardianScreen(
                                viewModel = viewModel,
                                onFinish = { viewModel.setMode("GUARDIAN") }
                            )
                        }
                        "REGISTER_PATIENT" -> {
                            com.example.ui.screens.RegisterPatientScreen(
                                viewModel = viewModel,
                                onFinish = { viewModel.setMode("PATIENT") }
                            )
                        }
                        "PATIENT" -> {
                            PatientScreens(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        "GUARDIAN" -> {
                            GuardianScreens(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

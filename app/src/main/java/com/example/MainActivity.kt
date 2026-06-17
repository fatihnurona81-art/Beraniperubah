package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

// Simple navigation states for auth flow
enum class AuthScreen {
    LOGIN,
    REGISTER
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
            val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
            val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
            val userHabits by viewModel.userHabits.collectAsStateWithLifecycle()
            val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

            val context = LocalContext.current

            // State management for Auth panels transition
            var authScreenState by remember { mutableStateOf(AuthScreen.LOGIN) }

            // Trigger standard system short toast + inline notification popup on change
            LaunchedEffect(toastMessage) {
                toastMessage?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearToast()
                }
            }

            MyApplicationTheme(appTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Dynamic Switcher for Authentic Single Page Application (SPA) flow
                        AnimatedContent(
                            targetState = loggedInUser,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                            },
                            label = "MainAppContainerTransition"
                        ) { user ->
                            if (user != null) {
                                // User is logged in -> Dashboard Home Base
                                DashboardScreen(
                                    username = user,
                                    currentLanguage = currentLanguage,
                                    currentTheme = currentTheme,
                                    userHabits = userHabits,
                                    onAddHabit = { title, desc -> viewModel.addHabit(title, desc) },
                                    onUpdateHabit = { id, title, desc -> viewModel.updateHabit(id, title, desc) },
                                    onDeleteHabit = { id -> viewModel.deleteHabit(id) },
                                    onToggleHabit = { id, dateStr -> viewModel.toggleHabitCompletion(id, dateStr) },
                                    onThemeSelect = { selectedTheme ->
                                        viewModel.setTheme(selectedTheme)
                                    },
                                    onLanguageSelect = { selectedLanguage ->
                                        viewModel.setLanguage(selectedLanguage)
                                    },
                                    onLogout = {
                                        viewModel.logout()
                                        authScreenState = AuthScreen.LOGIN
                                    }
                                )
                            } else {
                                // Session is empty -> Login & Register Deck
                                AnimatedContent(
                                    targetState = authScreenState,
                                    transitionSpec = {
                                        slideInHorizontally { width -> if (authScreenState == AuthScreen.LOGIN) -width else width } togetherWith
                                                slideOutHorizontally { width -> if (authScreenState == AuthScreen.LOGIN) width else -width }
                                    },
                                    label = "AuthScreensTransition"
                                ) { state ->
                                    when (state) {
                                        AuthScreen.LOGIN -> LoginScreen(
                                            currentLanguage = currentLanguage,
                                            currentTheme = currentTheme,
                                            onLoginClick = { username, password ->
                                                viewModel.login(username, password)
                                            },
                                            onNavigateToRegister = {
                                                authScreenState = AuthScreen.REGISTER
                                            }
                                        )

                                        AuthScreen.REGISTER -> RegisterScreen(
                                            currentLanguage = currentLanguage,
                                            currentTheme = currentTheme,
                                            onRegisterClick = { username, password ->
                                                viewModel.register(username, password)
                                            },
                                            onNavigateToLogin = {
                                                authScreenState = AuthScreen.LOGIN
                                            }
                                        )
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

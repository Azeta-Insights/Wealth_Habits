package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.theme.EditorialBorder
import com.example.ui.theme.EditorialOlive
import com.example.ui.theme.EditorialOliveContainer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sms.SmsPreferences
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.add.AddImportScreen
import com.example.ui.auth.SignInScreen
import com.example.ui.insights.InsightsScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.statement.StatementReviewScreen
import com.example.ui.theme.WealthHabitsTheme
import com.example.ui.transactions.TransactionsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WealthHabitsTheme {
                WealthHabitsApp()
            }
        }
    }
}

@Composable
fun WealthHabitsApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var isOnboardingCompleted by remember {
        mutableStateOf(SmsPreferences.isOnboardingCompleted(context))
    }

    val currentScreen by viewModel.currentScreen.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isSignInSkipped by viewModel.isSignInSkipped.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val smsEnabled by viewModel.smsEnabled.collectAsState()
    val isScanningSms by viewModel.isScanningSms.collectAsState()
    val isParsingStatement by viewModel.isParsingStatement.collectAsState()
    val statementError by viewModel.statementParseError.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val addImportTab by viewModel.addImportTab.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Permission launcher for SMS requested at onboarding or settings
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.RECEIVE_SMS] == true ||
                permissions[Manifest.permission.READ_SMS] == true
        if (granted) {
            viewModel.setSmsReadingEnabled(true)
            // Historical inbox scan so the user doesn't start from zero
            viewModel.scanSmsInbox()
        } else {
            // Graceful fallback to manual entry — never block the app
            viewModel.setSmsReadingEnabled(false)
        }
    }

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            onComplete = { requestSms ->
                SmsPreferences.setOnboardingCompleted(context, true)
                isOnboardingCompleted = true
                if (requestSms) {
                    smsPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS
                        )
                    )
                }
            }
        )
        return
    }

    // Show sign-in screen after onboarding if not signed in and not skipped, or explicitly navigating to it
    val showSignIn = (currentScreen is Screen.SignIn) || (userProfile == null && !isSignInSkipped)
    if (showSignIn) {
        SignInScreen(
            onSignInSuccess = { profile ->
                viewModel.saveUserProfile(profile)
            },
            onSkip = {
                viewModel.skipSignIn()
            }
        )
        return
    }

    // Check if statement review is active
    val activeReviewResult = (currentScreen as? Screen.StatementReview)?.result
    if (activeReviewResult != null) {
        StatementReviewScreen(
            result = activeReviewResult,
            onConfirm = { txs ->
                viewModel.confirmStatementImport(txs)
            },
            onCancel = {
                viewModel.navigateTo(Screen.AddImport)
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EditorialOlive,
                tonalElevation = 0.dp,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = EditorialBorder,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                // 1. Insights Tab
                val isInsightsSelected = currentScreen is Screen.Insights
                NavigationBarItem(
                    selected = isInsightsSelected,
                    onClick = { viewModel.navigateTo(Screen.Insights) },
                    icon = {
                        Icon(
                            imageVector = if (isInsightsSelected) Icons.Filled.Lightbulb else Icons.Outlined.Lightbulb,
                            contentDescription = "Insights"
                        )
                    },
                    label = { Text("Insights") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EditorialOlive,
                        selectedTextColor = EditorialOlive,
                        indicatorColor = EditorialOliveContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_insights")
                )

                // 2. Transactions Tab
                val isTransactionsSelected = currentScreen is Screen.Transactions
                NavigationBarItem(
                    selected = isTransactionsSelected,
                    onClick = { viewModel.navigateTo(Screen.Transactions) },
                    icon = {
                        Icon(
                            imageVector = if (isTransactionsSelected) Icons.AutoMirrored.Filled.ReceiptLong else Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = "Transactions"
                        )
                    },
                    label = { Text("Transactions") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EditorialOlive,
                        selectedTextColor = EditorialOlive,
                        indicatorColor = EditorialOliveContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_transactions")
                )

                // 3. Add / Import Tab
                val isAddSelected = currentScreen is Screen.AddImport
                NavigationBarItem(
                    selected = isAddSelected,
                    onClick = { viewModel.navigateTo(Screen.AddImport) },
                    icon = {
                        Icon(
                            imageVector = if (isAddSelected) Icons.Filled.AddCircle else Icons.Outlined.AddCircleOutline,
                            contentDescription = "Add / Import"
                        )
                    },
                    label = { Text("Add / Import") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EditorialOlive,
                        selectedTextColor = EditorialOlive,
                        indicatorColor = EditorialOliveContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_add_import")
                )

                // 4. Settings Tab
                val isSettingsSelected = currentScreen is Screen.Settings
                NavigationBarItem(
                    selected = isSettingsSelected,
                    onClick = { viewModel.navigateTo(Screen.Settings) },
                    icon = {
                        Icon(
                            imageVector = if (isSettingsSelected) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EditorialOlive,
                        selectedTextColor = EditorialOlive,
                        indicatorColor = EditorialOliveContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    is Screen.Insights -> {
                        InsightsScreen(
                            insights = insights,
                            allTransactions = allTransactions,
                            userProfile = userProfile,
                            onAnswerReflection = { id, txId, reflection ->
                                viewModel.answerReflection(id, txId, reflection)
                            },
                            onNavigateToAddTab = { tabIndex ->
                                viewModel.navigateToAddTab(tabIndex)
                            },
                            onNavigateToTransactions = {
                                viewModel.navigateTo(Screen.Transactions)
                            },
                            onNavigateToSettings = {
                                viewModel.navigateTo(Screen.Settings)
                            }
                        )
                    }
                    is Screen.Transactions -> {
                        TransactionsScreen(
                            transactions = allTransactions,
                            onToggleReflection = { tx, reflection ->
                                viewModel.toggleTransactionReflection(tx, reflection)
                            },
                            onUpdateCategory = { txId, category ->
                                viewModel.updateTransactionCategory(txId, category)
                            },
                            onDeleteTransaction = { txId ->
                                viewModel.deleteTransaction(txId)
                            }
                        )
                    }
                    is Screen.AddImport -> {
                        AddImportScreen(
                            initialTab = addImportTab,
                            smsEnabled = smsEnabled,
                            isScanningSms = isScanningSms,
                            isParsingStatement = isParsingStatement,
                            statementError = statementError,
                            onTabSelected = { tab ->
                                viewModel.setAddImportTab(tab)
                            },
                            onAddManual = { amt, cat, type, narration ->
                                viewModel.addManualTransaction(amt, cat, type, narration)
                            },
                            onToggleSms = { enable ->
                                viewModel.setSmsReadingEnabled(enable)
                            },
                            onScanSmsInbox = {
                                viewModel.scanSmsInbox()
                            },
                            onStatementUriSelected = { uri ->
                                viewModel.handleStatementUri(uri)
                            }
                        )
                    }
                    is Screen.Settings -> {
                        SettingsScreen(
                            smsEnabled = smsEnabled,
                            isScanningSms = isScanningSms,
                            userProfile = userProfile,
                            onToggleSms = { enable ->
                                viewModel.setSmsReadingEnabled(enable)
                            },
                            onScanSmsInbox = {
                                viewModel.scanSmsInbox()
                            },
                            onDeleteStatementData = {
                                viewModel.deleteStatementData()
                            },
                            onDeleteAllData = {
                                viewModel.deleteAllData()
                            },
                            onSignOut = {
                                viewModel.signOut()
                            },
                            onNavigateToSignIn = {
                                viewModel.navigateTo(Screen.SignIn)
                            }
                        )
                    }
                    is Screen.StatementReview -> {
                        // Handled above before Scaffold
                    }
                    is Screen.SignIn -> {
                        // Handled above before Scaffold
                    }
                }
            }
        }
    }
}

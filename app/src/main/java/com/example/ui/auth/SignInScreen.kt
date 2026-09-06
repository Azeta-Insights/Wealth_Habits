package com.example.ui.auth

import android.accounts.AccountManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.BuildConfig
import com.example.R
import com.example.data.model.UserProfile
import com.example.ui.theme.EditorialBorder
import com.example.ui.theme.EditorialCardBg
import com.example.ui.theme.EditorialOlive
import com.example.ui.theme.EditorialOliveContainer
import com.example.ui.theme.EditorialRust
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onSignInSuccess: (UserProfile) -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDiagnosticsSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("sign_in_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Branding
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // App Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EditorialOliveContainer,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = EditorialOlive,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Wealth Habits",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialOlive,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                Text(
                    text = "Personalize Your Wealth Journey",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EditorialOlive,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    ),
                    modifier = Modifier.testTag("sign_in_headline")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sign in with Google to personalize your daily reflection greetings and manage your wealth habits profile.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Privacy Guarantee Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_guarantee_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialCardBg),
                border = BorderStroke(1.dp, EditorialBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EditorialOliveContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EditorialOlive,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "Your Financial Data Stays On This Device",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialOlive
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Signing in only personalizes your name and profile photo on this phone. The internet permission is used solely for Google authentication and downloading Google's on-device OCR text recognition model once — your transactions, bank alerts, and statements stay 100% on your device.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (errorMessage != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("sign_in_error_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (errorMessage?.contains("No Google account") == true ||
                                errorMessage?.contains("No credentials available") == true ||
                                errorMessage?.contains("NoCredentialException") == true) {
                                Spacer(modifier = Modifier.height(10.dp))
                                TextButton(
                                    onClick = {
                                        onSignInSuccess(
                                            UserProfile(
                                                id = "demo_user",
                                                displayName = "Blessing",
                                                email = "user@example.com",
                                                photoUrl = null,
                                                givenName = "Blessing",
                                                familyName = "Azeta"
                                            )
                                        )
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Use Demo Profile Instead", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Standard Branded "Sign in with Google" Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .clickable(enabled = !isLoading) {
                            errorMessage = null
                            performGoogleSignIn(
                                context = context,
                                coroutineScope = coroutineScope,
                                onLoadingChange = { isLoading = it },
                                onSuccess = onSignInSuccess,
                                onError = { errorMessage = it }
                            )
                        }
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFF747775))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = EditorialOlive
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Signing in...",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1F1F1F)
                                )
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1F1F1F)
                                )
                            )
                        }
                    }
                }

                // Skip for now Option
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("skip_sign_in_button"),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, EditorialBorder)
                ) {
                    Text(
                        text = "Skip for now",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = EditorialOlive
                        )
                    )
                }

                Text(
                    text = "Sign-in is optional. You can use all tracking & habit features offline without signing in.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                // Diagnostics Link
                TextButton(
                    onClick = { showDiagnosticsSheet = true },
                    modifier = Modifier.testTag("sign_in_diagnostics_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = EditorialOlive
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sign-In Diagnostics",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = EditorialOlive,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }

    // Diagnostics Bottom Sheet
    if (showDiagnosticsSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showDiagnosticsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            SignInDiagnosticsContent(
                context = context,
                onDismiss = { showDiagnosticsSheet = false }
            )
        }
    }
}

@Composable
private fun SignInDiagnosticsContent(
    context: Context,
    onDismiss: () -> Unit
) {
    val playServicesCode = remember(context) {
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    }

    val playServicesStatus = remember(playServicesCode) {
        getPlayServicesStatusName(playServicesCode)
    }

    val gmsVersionInfo = remember(context) {
        try {
            val pInfo = context.packageManager.getPackageInfo("com.google.android.gms", 0)
            val versionCodeStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toString()
            }
            "${pInfo.versionName ?: "Unknown"} (Build $versionCodeStr)"
        } catch (e: Exception) {
            "Not Installed / Unavailable (${e.javaClass.simpleName})"
        }
    }

    val googleAccountCountStr = remember(context) {
        try {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            "${accounts.size} account(s) detected on device"
        } catch (e: Exception) {
            "Count unavailable (${e.javaClass.simpleName}: ${e.message})"
        }
    }

    val clientIdConfigured = remember(context) {
        val clientId = getGoogleWebClientId(context)
        if (clientId.isNotBlank() && clientId != "YOUR_GOOGLE_WEB_CLIENT_ID") {
            "Configured (${clientId.take(12)}...)"
        } else {
            "Not Configured"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Google Sign-In Diagnostics",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = EditorialOlive
            )
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EditorialCardBg),
            border = BorderStroke(1.dp, EditorialBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DiagnosticItemRow(
                    label = "Google Play Services Status",
                    value = playServicesStatus,
                    isSuccess = playServicesCode == ConnectionResult.SUCCESS
                )

                DiagnosticItemRow(
                    label = "Installed Play Services Version",
                    value = gmsVersionInfo,
                    isSuccess = !gmsVersionInfo.contains("Not Installed")
                )

                DiagnosticItemRow(
                    label = "Detected Google Accounts",
                    value = googleAccountCountStr,
                    isSuccess = !googleAccountCountStr.contains("0 account(s)")
                )

                DiagnosticItemRow(
                    label = "OAuth Web Client ID",
                    value = clientIdConfigured,
                    isSuccess = clientIdConfigured.startsWith("Configured")
                )
            }
        }

        Text(
            text = "Note: Account addresses are never logged or stored for privacy. Play Services availability code 0 indicates Google Play Services is fully operational.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, EditorialBorder)
        ) {
            Text("Close Diagnostics", style = MaterialTheme.typography.labelLarge.copy(color = EditorialOlive))
        }
    }
}

@Composable
private fun DiagnosticItemRow(
    label: String,
    value: String,
    isSuccess: Boolean
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSuccess) EditorialOlive else MaterialTheme.colorScheme.error
            )
        )
    }
}

private fun getPlayServicesStatusName(code: Int): String {
    return when (code) {
        ConnectionResult.SUCCESS -> "SUCCESS (Code 0)"
        ConnectionResult.SERVICE_MISSING -> "SERVICE_MISSING (Code 1)"
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "SERVICE_VERSION_UPDATE_REQUIRED (Code 2)"
        ConnectionResult.SERVICE_DISABLED -> "SERVICE_DISABLED (Code 3)"
        ConnectionResult.SERVICE_INVALID -> "SERVICE_INVALID (Code 9)"
        ConnectionResult.SERVICE_MISSING_PERMISSION -> "SERVICE_MISSING_PERMISSION (Code 19)"
        else -> "RESULT_CODE_$code"
    }
}

private fun getGoogleWebClientId(context: Context): String {
    // 1. Check BuildConfig
    val fromBuildConfig = try {
        BuildConfig.GOOGLE_WEB_CLIENT_ID
    } catch (_: Throwable) {
        ""
    }
    if (fromBuildConfig.isNotBlank() && fromBuildConfig != "YOUR_GOOGLE_WEB_CLIENT_ID") {
        return fromBuildConfig.trim()
    }

    // 2. Check strings.xml
    return try {
        val resId = context.resources.getIdentifier("google_web_client_id", "string", context.packageName)
        if (resId != 0) {
            context.getString(resId).trim()
        } else {
            ""
        }
    } catch (_: Throwable) {
        ""
    }
}

private fun performGoogleSignIn(
    context: Context,
    coroutineScope: CoroutineScope,
    onLoadingChange: (Boolean) -> Unit,
    onSuccess: (UserProfile) -> Unit,
    onError: (String) -> Unit
) {
    // 1. Check Google Play Services status before attempting sign-in
    val availabilityCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    if (availabilityCode != ConnectionResult.SUCCESS) {
        val statusName = getPlayServicesStatusName(availabilityCode)
        onError("Google Play Services needs to be updated on this device for sign-in to work (Result code: $availabilityCode - $statusName).")
        return
    }

    val serverClientId = getGoogleWebClientId(context)
    if (serverClientId.isBlank() || serverClientId == "YOUR_GOOGLE_WEB_CLIENT_ID") {
        onError("Google OAuth Client ID is not configured yet. Set 'google_web_client_id' in strings.xml or BuildConfig to connect with Google Cloud.")
        return
    }

    coroutineScope.launch {
        onLoadingChange(true)
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val displayName = googleIdTokenCredential.displayName
                    ?: googleIdTokenCredential.givenName
                    ?: "Friend"
                val email = googleIdTokenCredential.id
                val photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                val givenName = googleIdTokenCredential.givenName
                val familyName = googleIdTokenCredential.familyName

                val profile = UserProfile(
                    id = "primary_user",
                    displayName = displayName,
                    email = email,
                    photoUrl = photoUrl,
                    givenName = givenName,
                    familyName = familyName
                )
                onSuccess(profile)
            } else {
                onError("Received unexpected credential type.")
            }
        } catch (e: NoCredentialException) {
            val errType = e.type
            val errMessage = e.errorMessage?.toString() ?: e.message ?: e.localizedMessage ?: "No credentials available"
            Log.w("SignInScreen", "NoCredentialException [Type: $errType]: $errMessage", e)
            onError("CredentialManager error [$errType]: $errMessage. Add a Google account in Android Settings or tap 'Use Demo Profile' below.")
        } catch (e: GetCredentialCancellationException) {
            // User cancelled the prompt, no error message needed
            Log.d("SignInScreen", "User cancelled Google credential picker")
        } catch (e: GetCredentialException) {
            Log.e("SignInScreen", "CredentialManager failed [Type: ${e.type}]", e)
            val msg = e.errorMessage?.toString() ?: e.localizedMessage ?: "Could not complete sign-in."
            onError("Sign-in failed [${e.type}]: $msg")
        } catch (e: Exception) {
            Log.e("SignInScreen", "Sign in error", e)
            onError(e.localizedMessage ?: "An error occurred during sign-in.")
        } finally {
            onLoadingChange(false)
        }
    }
}


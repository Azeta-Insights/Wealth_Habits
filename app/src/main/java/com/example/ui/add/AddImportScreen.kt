package com.example.ui.add

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.insights.InsightEngine
import com.example.ui.theme.BadgeBusinessBg
import com.example.ui.theme.BadgeDataBg
import com.example.ui.theme.BadgeFoodBg
import com.example.ui.theme.BadgeOtherBg
import com.example.ui.theme.BadgeRentBg
import com.example.ui.theme.BadgeTransportBg
import com.example.ui.theme.EditorialBorder
import com.example.ui.theme.EditorialCardBg
import com.example.ui.theme.EditorialOlive
import com.example.ui.theme.EditorialOliveContainer
import com.example.ui.theme.EditorialRust
import com.example.ui.theme.EditorialRustContainer
import com.example.ui.theme.EditorialSlate
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.WarmClaySecondary
import androidx.compose.ui.text.font.FontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddImportScreen(
    initialTab: Int = 0,
    smsEnabled: Boolean,
    isScanningSms: Boolean,
    isParsingStatement: Boolean,
    statementError: String?,
    onTabSelected: (Int) -> Unit = {},
    onAddManual: (amount: Double, category: TransactionCategory, type: TransactionType, narration: String) -> Unit,
    onToggleSms: (Boolean) -> Unit,
    onScanSmsInbox: () -> Unit,
    onStatementUriSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }

    // SAF File Picker for Bank Statements (PDF & CSV)
    val statementPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onStatementUriSelected(uri)
        }
    }

    // Runtime Permission Launcher for SMS
    var hasSmsPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.RECEIVE_SMS] == true ||
                      permissions[Manifest.permission.READ_SMS] == true
        hasSmsPermissions = granted
        if (granted) {
            onToggleSms(true)
            onScanSmsInbox() // Historical scan on first grant!
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("add_import_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add Transactions",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EditorialOlive
                    )
                )
                Text(
                    text = "Choose from 3 real input methods — use any combination anytime",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Equal Visual Weight Tab Selector
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = EditorialCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = EditorialOlive,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = EditorialOlive,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            onTabSelected(0)
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Manual",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) EditorialOlive else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_manual_entry")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onTabSelected(1)
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "SMS Alerts",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) EditorialOlive else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_sms_alerts")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            onTabSelected(2)
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Statement",
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 2) EditorialOlive else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_statement_upload")
                    )
                }
            }
        }

        // ---------------------------------------------------------------------
        // PATH 1: MANUAL ENTRY (Fast 3-tap flow: amount, category icon, done)
        // ---------------------------------------------------------------------
        if (selectedTab == 0) {
            item {
                ManualEntrySection(onAddManual = onAddManual)
            }
        }

        // ---------------------------------------------------------------------
        // PATH 2: AUTOMATIC SMS READING
        // ---------------------------------------------------------------------
        if (selectedTab == 1) {
            item {
                SmsReadingSection(
                    smsEnabled = smsEnabled,
                    hasPermissions = hasSmsPermissions,
                    isScanning = isScanningSms,
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_SMS
                            )
                        )
                    },
                    onToggle = onToggleSms,
                    onScanNow = onScanSmsInbox
                )
            }
        }

        // ---------------------------------------------------------------------
        // PATH 3: BANK STATEMENT UPLOAD (PDF or CSV)
        // ---------------------------------------------------------------------
        if (selectedTab == 2) {
            item {
                StatementUploadSection(
                    isParsing = isParsingStatement,
                    error = statementError,
                    onPickFile = {
                        statementPicker.launch(
                            arrayOf(
                                "application/pdf",
                                "text/csv",
                                "text/comma-separated-values",
                                "text/plain",
                                "*/*"
                            )
                        )
                    }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: PATH 1 (Manual Entry)
// -----------------------------------------------------------------------------
@Composable
fun ManualEntrySection(
    onAddManual: (amount: Double, category: TransactionCategory, type: TransactionType, narration: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.FOOD) }
    var narrationText by remember { mutableStateOf("") }
    var isDebit by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fast 3-Tap Entry",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EditorialOlive
                    )
                )

                // Debit / Credit toggle
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(EditorialCardBg)
                        .border(1.dp, EditorialBorder, CircleShape)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isDebit) EditorialRust else Color.Transparent)
                            .clickable { isDebit = true }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Spend",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isDebit) Color.White else EditorialOlive,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (!isDebit) EditorialOlive else Color.Transparent)
                            .clickable { isDebit = false }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (!isDebit) Color.White else EditorialOlive,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step 1: Amount input
            Text(
                text = "1. ENTER AMOUNT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EditorialRust,
                    letterSpacing = 1.2.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { c -> c.isDigit() || c == '.' }
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_amount_input"),
                leadingIcon = {
                    Text(
                        text = "₦",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = EditorialOlive
                        )
                    )
                },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = EditorialOlive
                ),
                placeholder = {
                    Text(
                        "0",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EditorialOlive,
                    unfocusedBorderColor = EditorialBorder,
                    focusedContainerColor = EditorialCardBg,
                    unfocusedContainerColor = EditorialCardBg
                )
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 2: Category Icon
            Text(
                text = "2. TAP CATEGORY ICON",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EditorialRust,
                    letterSpacing = 1.2.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            val categories = TransactionCategory.entries
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryPill(
                        category = categories[0],
                        selected = selectedCategory == categories[0],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedCategory = categories[0] }
                    )
                    CategoryPill(
                        category = categories[1],
                        selected = selectedCategory == categories[1],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedCategory = categories[1] }
                    )
                    CategoryPill(
                        category = categories[2],
                        selected = selectedCategory == categories[2],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedCategory = categories[2] }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryPill(
                        category = categories[3],
                        selected = selectedCategory == categories[3],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedCategory = categories[3] }
                    )
                    CategoryPill(
                        category = categories[4],
                        selected = selectedCategory == categories[4],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedCategory = categories[4] }
                    )
                    CategoryPill(
                        category = categories[5],
                        selected = selectedCategory == categories[5],
                        modifier = Modifier.weight(1f),
                        onClick = { selectedCategory = categories[5] }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Optional note
            OutlinedTextField(
                value = narrationText,
                onValueChange = { narrationText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_note_input"),
                placeholder = { Text("What was this for? (e.g. Chowdeck, Danfo, Fuel)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EditorialOlive,
                    unfocusedBorderColor = EditorialBorder,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Step 3: Done Button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }
                    val type = if (isDebit) TransactionType.DEBIT else TransactionType.CREDIT
                    onAddManual(amount, selectedCategory, type, narrationText)
                    amountText = ""
                    narrationText = ""
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_manual_transaction_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = EditorialOlive),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Done — Record Habit",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun CategoryPill(
    category: TransactionCategory,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val categoryBadgeBg = when (category) {
        TransactionCategory.FOOD -> BadgeFoodBg
        TransactionCategory.TRANSPORT -> BadgeTransportBg
        TransactionCategory.DATA_AIRTIME -> BadgeDataBg
        TransactionCategory.RENT -> BadgeRentBg
        TransactionCategory.BUSINESS -> BadgeBusinessBg
        TransactionCategory.OTHER -> BadgeOtherBg
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) EditorialOlive else EditorialBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag("category_pill_${category.idName}"),
        color = if (selected) categoryBadgeBg else EditorialCardBg
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = category.emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.displayName.split(" ").first(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) EditorialOlive else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: PATH 2 (Automatic SMS Reading)
// -----------------------------------------------------------------------------
@Composable
fun SmsReadingSection(
    smsEnabled: Boolean,
    hasPermissions: Boolean,
    isScanning: Boolean,
    onRequestPermission: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onScanNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Automatic SMS Reading",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EditorialOlive
                        )
                    )
                    Text(
                        text = if (smsEnabled && hasPermissions) "Active & listening for bank alerts" else "Off — manual permission required",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (smsEnabled && hasPermissions) EditorialOlive else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Switch(
                    checked = smsEnabled && hasPermissions,
                    onCheckedChange = { enable ->
                        if (enable && !hasPermissions) {
                            onRequestPermission()
                        } else {
                            onToggle(enable)
                        }
                    },
                    modifier = Modifier.testTag("sms_reading_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EditorialOlive
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = EditorialCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EditorialOlive,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Supported Nigerian Banks",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialOlive
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "GTBank, Access, Zenith, Kuda, OPay, PalmPay, UBA, First Bank, Stanbic IBTC, Moniepoint, Fidelity",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All SMS parsing happens strictly on your device. Texts never leave your phone.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EditorialRust,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (!hasPermissions) {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("grant_sms_permissions_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialOlive)
                ) {
                    Text(
                        "Grant Android SMS Permissions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onScanNow,
                    enabled = !isScanning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("scan_historical_sms_button"),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = EditorialOlive,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scanning Inbox...", color = EditorialOlive)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = EditorialOlive,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Scan Existing SMS Inbox for Past Alerts",
                            color = EditorialOlive,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: PATH 3 (Bank Statement Upload)
// -----------------------------------------------------------------------------
@Composable
fun StatementUploadSection(
    isParsing: Boolean,
    error: String?,
    onPickFile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = "Bank Statement Upload",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EditorialOlive
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Upload an official PDF or CSV bank statement. Data is extracted locally on-device and the original file is deleted immediately after extraction.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = EditorialRustContainer.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EditorialRust.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Review Before Saving",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EditorialRust
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You will see a summary of total debits, credits, net change, and 1-tap category mapping to review before anything is saved.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onPickFile,
                enabled = !isParsing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("upload_statement_file_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = EditorialRust)
            ) {
                if (isParsing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extracting Statement on Device...", color = Color.White)
                } else {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select PDF or CSV Statement",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

package com.example.ui.insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ReflectionType
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.insights.InsightType
import com.example.insights.WealthInsight
import com.example.ui.theme.BadgeBusinessBg
import com.example.ui.theme.BadgeDataBg
import com.example.ui.theme.BadgeFoodBg
import com.example.ui.theme.BadgeOtherBg
import com.example.ui.theme.BadgeRentBg
import com.example.ui.theme.BadgeTransportBg
import com.example.ui.theme.EditorialBorder
import com.example.ui.theme.EditorialCardBg
import com.example.ui.theme.EditorialDivider
import com.example.ui.theme.EditorialEyebrowStyle
import com.example.ui.theme.EditorialMonoAmountStyle
import com.example.ui.theme.EditorialNeutralContainer
import com.example.ui.theme.EditorialOlive
import com.example.ui.theme.EditorialOliveContainer
import com.example.ui.theme.EditorialOliveLight
import com.example.ui.theme.EditorialQuoteStyle
import com.example.ui.theme.EditorialRust
import com.example.ui.theme.EditorialRustContainer
import com.example.ui.theme.EditorialSlate
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(
    insights: List<WealthInsight>,
    allTransactions: List<TransactionEntity>,
    userProfile: UserProfile? = null,
    onAnswerReflection: (insightId: String, relatedTxId: Long?, reflection: ReflectionType) -> Unit,
    onNavigateToAddTab: (tabIndex: Int) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val needsCount = allTransactions.count { it.reflection == ReflectionType.NEED }
    val wantsCount = allTransactions.count { it.reflection == ReflectionType.WANT }
    val totalReflected = needsCount + wantsCount

    val heroInsight = insights.firstOrNull { it.type == InsightType.CATEGORY_WEEK_CHANGE || it.type == InsightType.TRANSACTION_SPIKE }
        ?: insights.firstOrNull()

    val remainingInsights = if (heroInsight != null) {
        insights.filter { it.id != heroInsight.id }
    } else {
        emptyList()
    }

    val recentTransactions = allTransactions.take(4)

    val firstName = userProfile?.givenName?.takeIf { it.isNotBlank() }
        ?: userProfile?.displayName?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("insights_feed_list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Editorial Aesthetic Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    if (firstName != null) {
                        Text(
                            text = "Welcome back, $firstName",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = EditorialOlive,
                                letterSpacing = (-0.5).sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("app_header_title")
                        )
                        Text(
                            text = "Wealth Habits",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    } else {
                        Text(
                            text = "Wealth Habits",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = EditorialOlive,
                                letterSpacing = (-0.5).sp
                            ),
                            modifier = Modifier.testTag("app_header_title")
                        )
                    }
                }

                // Editorial profile / avatar badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EditorialOliveContainer)
                        .clickable { onNavigateToSettings() }
                        .testTag("header_avatar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userProfile?.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = userProfile?.photoUrl,
                            contentDescription = "Settings & Profile",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Settings & Profile",
                            tint = EditorialOlive,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 2. Hero Editorial Insight Card (Pull-Quote style with Need/Want capsule buttons)
        if (heroInsight != null) {
            item {
                HeroEditorialInsightCard(
                    insight = heroInsight,
                    onAnswerReflection = { reflection ->
                        onAnswerReflection(heroInsight.id, heroInsight.relatedTransactionId, reflection)
                    }
                )
            }
        }

        // 3. Editorial 3-Column Quick Data Input Row
        item {
            EditorialInputMethodsGrid(
                onSelectManual = { onNavigateToAddTab(0) },
                onSelectSms = { onNavigateToAddTab(1) },
                onSelectUpload = { onNavigateToAddTab(2) }
            )
        }

        // 4. Recent Habits Section
        if (recentTransactions.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Recent Habits",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialOlive,
                                fontSize = 18.sp
                            )
                        )

                        Text(
                            text = "View History",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = EditorialRust,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier
                                .clickable { onNavigateToTransactions() }
                                .testTag("view_history_button")
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, EditorialBorder, RoundedCornerShape(20.dp))
                    ) {
                        recentTransactions.forEachIndexed { index, tx ->
                            EditorialHabitRow(
                                transaction = tx,
                                isLast = index == recentTransactions.size - 1,
                                onClick = { onNavigateToTransactions() }
                            )
                        }
                    }
                }
            }
        }

        // 5. Reflection Pulse Overview
        item {
            EditorialReflectionPulseCard(
                totalReflected = totalReflected,
                needsCount = needsCount,
                wantsCount = wantsCount,
                transactionCount = allTransactions.size,
                onNavigateToAdd = { onNavigateToAddTab(0) }
            )
        }

        // 6. Remaining Editorial Insight Cards
        if (remainingInsights.isNotEmpty()) {
            item {
                Text(
                    text = "More Reflections",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EditorialOlive,
                        fontSize = 17.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(remainingInsights, key = { it.id }) { insight ->
                StandardEditorialInsightCard(
                    insight = insight,
                    onAnswerReflection = { reflection ->
                        onAnswerReflection(insight.id, insight.relatedTransactionId, reflection)
                    }
                )
            }
        }
    }
}

/**
 * Editorial Hero Card:
 * Features a warm stone background (#F4F1EE), rounded-32px borders,
 * uppercase terracotta eyebrow label, serif italic pull quote,
 * and capsule Need / Want interactive buttons.
 */
@Composable
fun HeroEditorialInsightCard(
    insight: WealthInsight,
    onAnswerReflection: (ReflectionType) -> Unit
) {
    val eyebrowText = when (insight.type) {
        InsightType.CATEGORY_WEEK_CHANGE -> "${insight.category?.displayName ?: "WEEKLY"} INSIGHT"
        InsightType.TRANSACTION_SPIKE -> "SPENDING SPIKE"
        InsightType.WEEKLY_SUMMARY -> "WEEKLY INSIGHT"
        InsightType.MONTHLY_SUMMARY -> "MONTHLY RHYTHM"
        InsightType.WELCOME_GUIDE -> "GETTING STARTED"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_editorial_insight_card"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = EditorialCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Eyebrow label
            Text(
                text = eyebrowText.uppercase(Locale.ROOT),
                style = EditorialEyebrowStyle.copy(color = EditorialRust),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Serif Italic pull-quote headline
            val quoteContent = if (insight.message.contains(insight.reflectiveQuestion)) {
                insight.message
            } else {
                "${insight.message} ${insight.reflectiveQuestion}"
            }

            Text(
                text = "“$quoteContent”",
                style = EditorialQuoteStyle.copy(color = EditorialOlive),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Capsule Need / Want reflection buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isNeed = insight.reflection == ReflectionType.NEED
                val isWant = insight.reflection == ReflectionType.WANT

                // Need Button
                Button(
                    onClick = { onAnswerReflection(ReflectionType.NEED) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("hero_need_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNeed) EditorialOliveLight else EditorialOlive,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isNeed) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isNeed) "It was a Need" else "It was a Need",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                // Want Button
                if (isWant) {
                    Button(
                        onClick = { onAnswerReflection(ReflectionType.WANT) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hero_want_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EditorialRust,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Just a Want",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { onAnswerReflection(ReflectionType.WANT) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hero_want_button"),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(2.dp, EditorialOlive),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = EditorialOlive
                        )
                    ) {
                        Text(
                            text = "Just a Want",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialOlive,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Editorial 3-Column Grid for Data Inputs:
 * Manual, SMS Sync, Statement Upload
 */
@Composable
fun EditorialInputMethodsGrid(
    onSelectManual: () -> Unit,
    onSelectSms: () -> Unit,
    onSelectUpload: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Manual Entry Tile
        InputMethodTile(
            modifier = Modifier.weight(1f),
            iconBackground = EditorialOliveContainer,
            iconTint = EditorialOlive,
            icon = Icons.Default.Add,
            label = "Manual",
            labelColor = EditorialOlive,
            testTag = "editorial_input_tile_manual",
            onClick = onSelectManual
        )

        // 2. SMS Sync Tile
        InputMethodTile(
            modifier = Modifier.weight(1f),
            iconBackground = EditorialRustContainer,
            iconTint = EditorialRust,
            icon = Icons.AutoMirrored.Filled.Message,
            label = "SMS Sync",
            labelColor = EditorialRust,
            testTag = "editorial_input_tile_sms",
            onClick = onSelectSms
        )

        // 3. Upload Statement Tile
        InputMethodTile(
            modifier = Modifier.weight(1f),
            iconBackground = EditorialNeutralContainer,
            iconTint = EditorialSlate,
            icon = Icons.Default.Upload,
            label = "Upload",
            labelColor = EditorialSlate,
            testTag = "editorial_input_tile_upload",
            onClick = onSelectUpload
        )
    }
}

@Composable
fun InputMethodTile(
    modifier: Modifier = Modifier,
    iconBackground: Color,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    labelColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = label.uppercase(Locale.ROOT),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = labelColor
                )
            )
        }
    }
}

/**
 * Editorial Habit Row for Recent Transactions:
 * Clean row with emoji badge, narration, timestamp, and monospace currency format.
 */
@Composable
fun EditorialHabitRow(
    transaction: TransactionEntity,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val categoryBadgeBg = when (transaction.category) {
        TransactionCategory.FOOD -> BadgeFoodBg
        TransactionCategory.TRANSPORT -> BadgeTransportBg
        TransactionCategory.DATA_AIRTIME -> BadgeDataBg
        TransactionCategory.RENT -> BadgeRentBg
        TransactionCategory.BUSINESS -> BadgeBusinessBg
        TransactionCategory.OTHER -> BadgeOtherBg
    }

    val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    val isDebit = transaction.type == TransactionType.DEBIT
    val amountPrefix = if (isDebit) "-₦" else "+₦"
    val amountColor = if (isDebit) EditorialOlive else EditorialOliveLight

    val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(transaction.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category emoji rounded badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryBadgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = transaction.category.emoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.narration,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "$formattedDate • ${transaction.category.displayName.uppercase(Locale.ROOT)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Monospace amount
            Text(
                text = "$amountPrefix${currencyFormat.format(transaction.amount)}",
                style = EditorialMonoAmountStyle.copy(
                    color = amountColor,
                    fontSize = 15.sp
                )
            )
        }

        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(EditorialDivider)
            )
        }
    }
}

/**
 * Editorial Reflection Pulse Summary Card
 */
@Composable
fun EditorialReflectionPulseCard(
    totalReflected: Int,
    needsCount: Int,
    wantsCount: Int,
    transactionCount: Int,
    onNavigateToAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reflection_pulse_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EditorialOliveContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = EditorialOlive,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Habit Reflection",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EditorialOlive
                        )
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = EditorialCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder)
                ) {
                    Text(
                        text = "$totalReflected answered",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EditorialOlive
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (transactionCount == 0) {
                Text(
                    text = "No transactions recorded yet. Tap below to start your financial rhythm.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onNavigateToAdd,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialOlive)
                ) {
                    Text("Add First Transaction", color = Color.White)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Needs pill
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = EditorialOliveContainer.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialOliveContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Needs",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = EditorialOlive,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "$needsCount",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialOlive
                                )
                            )
                            Text(
                                text = "Essential foundation",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Wants pill
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = EditorialRustContainer.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialRustContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Wants",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = EditorialRust,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "$wantsCount",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialRust
                                )
                            )
                            Text(
                                text = "Joy & discretion",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standard Editorial Insight Card for additional insights in the feed.
 */
@Composable
fun StandardEditorialInsightCard(
    insight: WealthInsight,
    onAnswerReflection: (ReflectionType) -> Unit
) {
    val isDebitSpike = (insight.diffAmount ?: 0.0) > 0
    val iconColor = if (isDebitSpike) EditorialRust else EditorialOlive
    val iconBg = if (isDebitSpike) EditorialRustContainer else EditorialOliveContainer

    val iconVector = when (insight.type) {
        InsightType.CATEGORY_WEEK_CHANGE -> {
            if (isDebitSpike) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
        }
        InsightType.TRANSACTION_SPIKE -> Icons.Default.FlashOn
        InsightType.WEEKLY_SUMMARY -> Icons.Default.Lightbulb
        InsightType.MONTHLY_SUMMARY -> Icons.Default.CheckCircle
        InsightType.WELCOME_GUIDE -> Icons.AutoMirrored.Filled.HelpOutline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("insight_card_${insight.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EditorialOlive
                        )
                    )
                }

                insight.category?.let { cat ->
                    Surface(
                        shape = CircleShape,
                        color = EditorialCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder)
                    ) {
                        Text(
                            text = "${cat.emoji} ${cat.displayName}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Serif Italic message
            Text(
                text = insight.message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reflective question & Need/Want capsule buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = EditorialCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = insight.reflectiveQuestion,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = EditorialOlive
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isNeed = insight.reflection == ReflectionType.NEED
                        val isWant = insight.reflection == ReflectionType.WANT

                        // Need chip
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(CircleShape)
                                .clickable { onAnswerReflection(ReflectionType.NEED) }
                                .testTag("tag_need_${insight.id}"),
                            shape = CircleShape,
                            color = if (isNeed) EditorialOlive else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isNeed) EditorialOlive else EditorialBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isNeed) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "Need",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNeed) Color.White else EditorialOlive
                                        )
                                    )
                                }
                            }
                        }

                        // Want chip
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(CircleShape)
                                .clickable { onAnswerReflection(ReflectionType.WANT) }
                                .testTag("tag_want_${insight.id}"),
                            shape = CircleShape,
                            color = if (isWant) EditorialRust else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isWant) EditorialRust else EditorialBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isWant) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "Want",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWant) Color.White else EditorialRust
                                        )
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

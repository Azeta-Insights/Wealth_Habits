package com.example.ui.transactions

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReflectionType
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    onToggleReflection: (TransactionEntity, ReflectionType) -> Unit,
    onUpdateCategory: (Long, TransactionCategory) -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<TransactionCategory?>(null) }
    var selectedTxForAction by remember { mutableStateOf<TransactionEntity?>(null) }
    var isRecategorizing by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.ENGLISH) }

    val filtered = transactions.filter { tx ->
        val matchesCategory = selectedCategoryFilter == null || tx.category == selectedCategoryFilter
        val matchesSearch = searchQuery.isBlank() ||
                tx.narration.contains(searchQuery, ignoreCase = true) ||
                (tx.bankName?.contains(searchQuery, ignoreCase = true) == true) ||
                tx.category.displayName.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transactions_list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EditorialOlive
                    )
                )
                Text(
                    text = "${transactions.size} records • Tap Need or Want to reflect",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transactions_search_input"),
                placeholder = { Text("Search transactions or merchants...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = EditorialOlive
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EditorialOlive,
                    unfocusedBorderColor = EditorialBorder,
                    focusedContainerColor = EditorialCardBg,
                    unfocusedContainerColor = EditorialCardBg
                )
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = {
                            Text(
                                "All",
                                fontWeight = if (selectedCategoryFilter == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EditorialOlive,
                            selectedLabelColor = Color.White,
                            containerColor = EditorialCardBg,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = EditorialBorder,
                            selectedBorderColor = EditorialOlive,
                            enabled = true,
                            selected = selectedCategoryFilter == null
                        ),
                        modifier = Modifier.testTag("filter_all")
                    )
                }
                items(TransactionCategory.entries) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        label = {
                            Text(
                                "${cat.emoji} ${cat.displayName.split(" ").first()}",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EditorialOlive,
                            selectedLabelColor = Color.White,
                            containerColor = EditorialCardBg,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = EditorialBorder,
                            selectedBorderColor = EditorialOlive,
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.testTag("filter_${cat.idName}")
                    )
                }
            }
        }

        // Empty state
        if (filtered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EditorialCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🍃", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (transactions.isEmpty()) "No transactions yet" else "No matching transactions found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialOlive
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (transactions.isEmpty()) "Add a quick manual entry, scan SMS bank alerts, or upload a statement." else "Try adjusting your search or category filter.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // List of transaction items
        items(filtered, key = { it.id }) { tx ->
            TransactionItemRow(
                transaction = tx,
                formattedDate = dateFormat.format(Date(tx.timestamp)),
                onToggleReflection = { onToggleReflection(tx, it) },
                onTapDetails = { selectedTxForAction = tx }
            )
        }
    }

    // Transaction Details & Action Dialog
    selectedTxForAction?.let { tx ->
        AlertDialog(
            onDismissRequest = {
                selectedTxForAction = null
                isRecategorizing = false
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = tx.narration,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EditorialOlive
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Amount: ${InsightEngine.formatNaira(tx.amount)} (${tx.type.name})",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = EditorialOlive
                        )
                    )
                    Text(
                        text = "Source: ${tx.source.label}${if (tx.bankName != null) " • ${tx.bankName}" else ""}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "Current Category: ${tx.category.emoji} ${tx.category.displayName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    if (isRecategorizing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Choose New Category:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EditorialOlive
                            )
                        )
                        TransactionCategory.entries.forEach { cat ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onUpdateCategory(tx.id, cat)
                                        isRecategorizing = false
                                        selectedTxForAction = null
                                    },
                                color = if (tx.category == cat) EditorialOliveContainer else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = cat.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (tx.category == cat) FontWeight.Bold else FontWeight.Normal,
                                            color = if (tx.category == cat) EditorialOlive else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!isRecategorizing) {
                    TextButton(onClick = { isRecategorizing = true }) {
                        Text("Change Category", color = EditorialOlive, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            onDeleteTransaction(tx.id)
                            selectedTxForAction = null
                        }
                    ) {
                        Text("Delete", color = EditorialRust)
                    }
                    TextButton(onClick = { selectedTxForAction = null }) {
                        Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )
    }
}

@Composable
fun TransactionItemRow(
    transaction: TransactionEntity,
    formattedDate: String,
    onToggleReflection: (ReflectionType) -> Unit,
    onTapDetails: () -> Unit
) {
    val isDebit = transaction.type == TransactionType.DEBIT

    val categoryBadgeBg = when (transaction.category) {
        TransactionCategory.FOOD -> BadgeFoodBg
        TransactionCategory.TRANSPORT -> BadgeTransportBg
        TransactionCategory.DATA_AIRTIME -> BadgeDataBg
        TransactionCategory.RENT -> BadgeRentBg
        TransactionCategory.BUSINESS -> BadgeBusinessBg
        TransactionCategory.OTHER -> BadgeOtherBg
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTapDetails() }
            .testTag("tx_item_${transaction.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
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
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$formattedDate • ${transaction.source.label}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Amount
                Text(
                    text = (if (isDebit) "- " else "+ ") + InsightEngine.formatNaira(transaction.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isDebit) EditorialRust else EditorialOlive
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Need / Want Reflective Selector Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = EditorialCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EditorialBorder)
                ) {
                    Text(
                        text = transaction.category.displayName,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EditorialSlate,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isNeed = transaction.reflection == ReflectionType.NEED
                    val isWant = transaction.reflection == ReflectionType.WANT

                    // Need chip
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onToggleReflection(ReflectionType.NEED) }
                            .testTag("tx_need_btn_${transaction.id}"),
                        color = if (isNeed) EditorialOlive else EditorialCardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isNeed) EditorialOlive else EditorialBorder
                        ),
                        shape = CircleShape
                    ) {
                        Text(
                            text = if (isNeed) "✓ Need" else "Need?",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isNeed) FontWeight.Bold else FontWeight.Medium,
                                color = if (isNeed) Color.White else EditorialOlive
                            )
                        )
                    }

                    // Want chip
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onToggleReflection(ReflectionType.WANT) }
                            .testTag("tx_want_btn_${transaction.id}"),
                        color = if (isWant) EditorialRust else EditorialCardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isWant) EditorialRust else EditorialBorder
                        ),
                        shape = CircleShape
                    ) {
                        Text(
                            text = if (isWant) "✓ Want" else "Want?",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isWant) FontWeight.Bold else FontWeight.Medium,
                                color = if (isWant) Color.White else EditorialRust
                            )
                        )
                    }
                }
            }
        }
    }
}

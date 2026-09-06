package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val narration: String,
    val bankName: String? = null,
    val source: TransactionSource = TransactionSource.MANUAL,
    val timestamp: Long = System.currentTimeMillis(),
    val reflection: ReflectionType? = null,
    val deduplicationHash: String? = null
)

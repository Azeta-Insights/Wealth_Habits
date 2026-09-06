package com.example.insights

import com.example.data.model.ReflectionType
import com.example.data.model.TransactionCategory

enum class InsightType {
    CATEGORY_WEEK_CHANGE,
    TRANSACTION_SPIKE,
    WEEKLY_SUMMARY,
    MONTHLY_SUMMARY,
    WELCOME_GUIDE
}

data class WealthInsight(
    val id: String,
    val type: InsightType,
    val title: String,
    val message: String,
    val reflectiveQuestion: String,
    val category: TransactionCategory? = null,
    val relatedTransactionId: Long? = null,
    val amount: Double? = null,
    val diffAmount: Double? = null,
    val percentChange: Double? = null,
    var reflection: ReflectionType? = null,
    val timestamp: Long = System.currentTimeMillis()
)

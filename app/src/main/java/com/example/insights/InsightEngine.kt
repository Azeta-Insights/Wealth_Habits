package com.example.insights

import com.example.data.model.ReflectionType
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

object InsightEngine {

    private val nairaFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatNaira(amount: Double): String {
        return "₦" + nairaFormat.format(amount)
    }

    /**
     * Generates on-device plain-language insights from transaction history.
     */
    fun generateInsights(
        allTransactions: List<TransactionEntity>,
        answeredReflections: Map<String, ReflectionType> = emptyMap()
    ): List<WealthInsight> {
        val insights = mutableListOf<WealthInsight>()
        if (allTransactions.isEmpty()) {
            insights.add(
                WealthInsight(
                    id = "welcome_seed",
                    type = InsightType.WELCOME_GUIDE,
                    title = "A Warm Welcome to Wealth Habits",
                    message = "Every money alert tells a story. As your bank alerts arrive, statements are uploaded, or manual taps are recorded, we'll turn them into quiet, gentle reflections right here.",
                    reflectiveQuestion = "Ready to start noticing what's a need and what's a want?",
                    reflection = answeredReflections["welcome_seed"]
                )
            )
            return insights
        }

        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000
        val oneWeek = 7L * oneDay
        val currentWeekStart = now - oneWeek
        val priorWeekStart = now - (2L * oneWeek)

        val debits = allTransactions.filter { it.type == TransactionType.DEBIT }
        val currentWeekDebits = debits.filter { it.timestamp in currentWeekStart..now }
        val priorWeekDebits = debits.filter { it.timestamp in priorWeekStart until currentWeekStart }

        // 1. CATEGORY WEEK-OVER-WEEK COMPARISONS (Changes >= 20% and >= ₦500)
        for (category in TransactionCategory.entries) {
            val currentSum = currentWeekDebits.filter { it.category == category }.sumOf { it.amount }
            val priorSum = priorWeekDebits.filter { it.category == category }.sumOf { it.amount }

            if (priorSum > 0.0) {
                val diff = currentSum - priorSum
                val absDiff = kotlin.math.abs(diff)
                val percentChange = (absDiff / priorSum) * 100.0

                if (percentChange >= 20.0 && absDiff >= 500.0) {
                    val insightId = "cat_change_${category.name}_${now / (oneDay * 3)}"
                    if (diff > 0) {
                        val messages = listOf(
                            "You spent ${formatNaira(absDiff)} more on ${category.displayName.lowercase()} this week than last week.",
                            "Your ${category.displayName.lowercase()} spending went up by ${formatNaira(absDiff)} compared to last week.",
                            "Looks like ${category.displayName.lowercase()} took a bit more of your attention this week, about ${formatNaira(absDiff)} more than last week."
                        )
                        val message = pickTemplate(messages, category.ordinal)
                        insights.add(
                            WealthInsight(
                                id = insightId,
                                type = InsightType.CATEGORY_WEEK_CHANGE,
                                title = "${category.emoji} ${category.displayName} Shift",
                                message = message,
                                reflectiveQuestion = "Was that a need or a want?",
                                category = category,
                                diffAmount = absDiff,
                                percentChange = percentChange,
                                reflection = answeredReflections[insightId]
                            )
                        )
                    } else {
                        val messages = listOf(
                            "You cut down on ${category.displayName.lowercase()} by ${formatNaira(absDiff)} compared to last week.",
                            "You kept ${category.displayName.lowercase()} lower this week, spending ${formatNaira(absDiff)} less than last week.",
                            "Great balance: your ${category.displayName.lowercase()} spending dropped by ${formatNaira(absDiff)} from last week."
                        )
                        val message = pickTemplate(messages, category.ordinal)
                        insights.add(
                            WealthInsight(
                                id = insightId,
                                type = InsightType.CATEGORY_WEEK_CHANGE,
                                title = "${category.emoji} ${category.displayName} Saved",
                                message = message,
                                reflectiveQuestion = "Was trimming that an intentional need or a want?",
                                category = category,
                                diffAmount = absDiff,
                                percentChange = percentChange,
                                reflection = answeredReflections[insightId]
                            )
                        )
                    }
                }
            } else if (currentSum >= 1500.0 && priorSum == 0.0) {
                // New spending in category this week
                val insightId = "cat_new_${category.name}_${now / (oneDay * 3)}"
                insights.add(
                    WealthInsight(
                        id = insightId,
                        type = InsightType.CATEGORY_WEEK_CHANGE,
                        title = "${category.emoji} ${category.displayName}",
                        message = "You spent ${formatNaira(currentSum)} on ${category.displayName.lowercase()} this week after none last week.",
                        reflectiveQuestion = "Was that a need or a want?",
                        category = category,
                        diffAmount = currentSum,
                        percentChange = 100.0,
                        reflection = answeredReflections[insightId]
                    )
                )
            }
        }

        // 2. TRANSACTION SPIKES (Single transaction >= 1.5x category average and >= ₦3,000)
        for (category in TransactionCategory.entries) {
            val categoryDebits = debits.filter { it.category == category }
            if (categoryDebits.size >= 2) {
                val average = categoryDebits.map { it.amount }.average()
                val recentTransactions = categoryDebits.filter { it.timestamp >= now - (5L * oneDay) }

                for (tx in recentTransactions) {
                    if (tx.amount >= (1.5 * average) && tx.amount >= 3000.0) {
                        val insightId = "spike_${tx.id}"
                        val narrationClean = if (tx.narration.isNotBlank() && tx.narration != "Debit Transaction") {
                            "for ${tx.narration}"
                        } else {
                            "in ${category.displayName.lowercase()}"
                        }
                        val messages = listOf(
                            "A ${formatNaira(tx.amount)} payment $narrationClean caught our eye — it's higher than your usual ${formatNaira(average)} rhythm.",
                            "You spent ${formatNaira(tx.amount)} $narrationClean, which is about ${"%.1f".format(tx.amount / average)}x your usual ${category.displayName.lowercase()} average.",
                            "That ${formatNaira(tx.amount)} payment $narrationClean stood out from your typical habits."
                        )
                        insights.add(
                            WealthInsight(
                                id = insightId,
                                type = InsightType.TRANSACTION_SPIKE,
                                title = "Unusual ${category.displayName} Alert",
                                message = pickTemplate(messages, tx.id.toInt()),
                                reflectiveQuestion = "Looking back at that moment, was it a need or a want?",
                                category = category,
                                relatedTransactionId = tx.id,
                                amount = tx.amount,
                                reflection = tx.reflection ?: answeredReflections[insightId]
                            )
                        )
                    }
                }
            }
        }

        // 3. WEEKLY SUMMARY (Combining top 2 categories with biggest changes)
        val categoryShifts = mutableListOf<Pair<TransactionCategory, Double>>()
        for (category in TransactionCategory.entries) {
            val currentSum = currentWeekDebits.filter { it.category == category }.sumOf { it.amount }
            val priorSum = priorWeekDebits.filter { it.category == category }.sumOf { it.amount }
            val diff = currentSum - priorSum
            if (kotlin.math.abs(diff) >= 500.0) {
                categoryShifts.add(Pair(category, diff))
            }
        }

        if (categoryShifts.size >= 2) {
            categoryShifts.sortByDescending { kotlin.math.abs(it.second) }
            val top1 = categoryShifts[0]
            val top2 = categoryShifts[1]

            val desc1 = if (top1.second > 0) "+${formatNaira(top1.second)}" else "-${formatNaira(-top1.second)}"
            val desc2 = if (top2.second > 0) "+${formatNaira(top2.second)}" else "-${formatNaira(-top2.second)}"

            val weekId = "weekly_summary_${now / oneWeek}"
            val messages = listOf(
                "Looking at your week, the biggest shifts were in ${top1.first.displayName.lowercase()} ($desc1) and ${top2.first.displayName.lowercase()} ($desc2).",
                "This week's spending rhythm moved most around ${top1.first.displayName.lowercase()} ($desc1) and ${top2.first.displayName.lowercase()} ($desc2)."
            )
            insights.add(
                WealthInsight(
                    id = weekId,
                    type = InsightType.WEEKLY_SUMMARY,
                    title = "Weekly Rhythm Check",
                    message = pickTemplate(messages, (now / oneWeek).toInt()),
                    reflectiveQuestion = "Reflecting on this past week, were these mostly needs or wants?",
                    reflection = answeredReflections[weekId]
                )
            )
        }

        // 4. MONTHLY SUMMARY (Month-over-month total debits/credits, highlighting top category)
        val thirtyDaysAgo = now - (30L * oneDay)
        val sixtyDaysAgo = now - (60L * oneDay)

        val currentMonthTxs = allTransactions.filter { it.timestamp in thirtyDaysAgo..now }
        val priorMonthTxs = allTransactions.filter { it.timestamp in sixtyDaysAgo until thirtyDaysAgo }

        if (currentMonthTxs.isNotEmpty()) {
            val totalDebitsMonth = currentMonthTxs.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
            val totalCreditsMonth = currentMonthTxs.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }

            // Find top spending category
            val topCategory = currentMonthTxs.filter { it.type == TransactionType.DEBIT }
                .groupBy { it.category }
                .maxByOrNull { entry -> entry.value.sumOf { it.amount } }

            val topCatText = if (topCategory != null) {
                "${topCategory.key.displayName.lowercase()} (${formatNaira(topCategory.value.sumOf { it.amount })})"
            } else {
                "day-to-day essentials"
            }

            val monthId = "monthly_summary_${now / (thirtyDaysAgo)}"
            val message = if (totalCreditsMonth > 0) {
                "Over the past 30 days, ${formatNaira(totalCreditsMonth)} came in and ${formatNaira(totalDebitsMonth)} went out. Your largest area of focus was $topCatText."
            } else {
                "Over the past 30 days, your recorded spending totaled ${formatNaira(totalDebitsMonth)}, with $topCatText taking the largest portion."
            }

            insights.add(
                WealthInsight(
                    id = monthId,
                    type = InsightType.MONTHLY_SUMMARY,
                    title = "Monthly Overview",
                    message = message,
                    reflectiveQuestion = "Looking back over the month, how did that balance feel — mostly needs or wants?",
                    reflection = answeredReflections[monthId]
                )
            )
        }

        // If we only had 1 or 2 insights, add a friendly reflection prompt on top transactions
        if (insights.isEmpty() && debits.isNotEmpty()) {
            val latest = debits.first()
            val singleId = "single_tx_${latest.id}"
            insights.add(
                WealthInsight(
                    id = singleId,
                    type = InsightType.TRANSACTION_SPIKE,
                    title = "Latest Transaction",
                    message = "You recently recorded a ${formatNaira(latest.amount)} payment for ${latest.narration} (${latest.category.displayName}).",
                    reflectiveQuestion = "Was that a need or a want?",
                    category = latest.category,
                    relatedTransactionId = latest.id,
                    amount = latest.amount,
                    reflection = latest.reflection ?: answeredReflections[singleId]
                )
            )
        }

        return insights
    }

    private fun pickTemplate(templates: List<String>, seed: Int): String {
        val index = kotlin.math.abs(seed) % templates.size
        return templates[index]
    }
}

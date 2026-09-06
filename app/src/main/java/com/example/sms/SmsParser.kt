package com.example.sms

import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType
import java.security.MessageDigest
import java.util.Locale
import java.util.regex.Pattern

data class BankSenderRule(
    val bankName: String,
    val senderPatterns: List<String>
)

object SmsParser {

    private val BANK_RULES = listOf(
        BankSenderRule("GTBank", listOf("gtbank", "gtb", "014264", "gtworld")),
        BankSenderRule("Access Bank", listOf("access", "accessbank", "access bank")),
        BankSenderRule("Zenith Bank", listOf("zenith", "zenithbank", "zenith direct")),
        BankSenderRule("Kuda", listOf("kuda", "kudabank", "kuda microfinance")),
        BankSenderRule("OPay", listOf("opay", "opay digital", "opayng")),
        BankSenderRule("PalmPay", listOf("palmpay", "palm pay")),
        BankSenderRule("UBA", listOf("uba", "ubagroup", "united bank for africa")),
        BankSenderRule("First Bank", listOf("firstbank", "first bank", "firstmonie")),
        BankSenderRule("Stanbic IBTC", listOf("stanbic", "stanbicibtc", "stanbic ibtc")),
        BankSenderRule("Moniepoint", listOf("moniepoint", "monie point")),
        BankSenderRule("Fidelity Bank", listOf("fidelity", "fidelitybank", "fidelity bank"))
    )

    fun isBankSender(sender: String?, body: String?): Boolean {
        if (sender.isNullOrBlank() && body.isNullOrBlank()) return false
        val s = (sender ?: "").lowercase(Locale.ROOT)
        val b = (body ?: "").lowercase(Locale.ROOT)

        for (rule in BANK_RULES) {
            for (pattern in rule.senderPatterns) {
                if (s.contains(pattern) || b.contains(pattern)) {
                    return true
                }
            }
        }
        // General Nigerian bank alert keywords
        return (b.contains("acct") || b.contains("account")) &&
                (b.contains("debited") || b.contains("credited") || b.contains("debit") || b.contains("credit") || b.contains("txn:"))
    }

    fun identifyBankName(sender: String?, body: String?): String {
        val s = (sender ?: "").lowercase(Locale.ROOT)
        val b = (body ?: "").lowercase(Locale.ROOT)

        for (rule in BANK_RULES) {
            for (pattern in rule.senderPatterns) {
                if (s.contains(pattern) || b.contains(pattern)) {
                    return rule.bankName
                }
            }
        }
        return "Bank Alert"
    }

    fun parseSms(sender: String?, body: String, timestamp: Long): TransactionEntity? {
        if (body.isBlank()) return null
        val lowerBody = body.lowercase(Locale.ROOT)

        // Must look like an alert with monetary movement
        val isDebit = isDebitAlert(lowerBody)
        val isCredit = isCreditAlert(lowerBody)
        if (!isDebit && !isCredit) return null

        val type = if (isDebit) TransactionType.DEBIT else TransactionType.CREDIT
        val amount = extractAmount(body) ?: return null
        if (amount <= 0.0) return null

        val bankName = identifyBankName(sender, body)
        val narration = extractNarration(body, isDebit)
        val category = TransactionCategory.matchFromNarration(narration).let {
            if (it == TransactionCategory.OTHER) TransactionCategory.matchFromNarration(body) else it
        }

        val hash = generateHash(bankName, amount, type, timestamp, narration)

        return TransactionEntity(
            amount = amount,
            type = type,
            category = category,
            narration = narration,
            bankName = bankName,
            source = TransactionSource.SMS,
            timestamp = timestamp,
            deduplicationHash = hash
        )
    }

    private fun isDebitAlert(lower: String): Boolean {
        // High confidence debit indicators
        val debitKeywords = listOf(
            "debited", "debit", "dr:", "dr.", "dr ", "txn:debit", "withdrawal",
            "sent to", "paid to", "transferred to", "transfer to", "purchased",
            "pos purchase", "web purchase", "you just spent", "you sent"
        )
        return debitKeywords.any { lower.contains(it) }
    }

    private fun isCreditAlert(lower: String): Boolean {
        val creditKeywords = listOf(
            "credited", "credit", "cr:", "cr.", "cr ", "txn:credit", "deposit",
            "received from", "received", "acct credited", "reversal credit",
            "refund", "inward transfer"
        )
        return creditKeywords.any { lower.contains(it) }
    }

    fun extractAmount(text: String): Double? {
        // Look for formats: NGN 3,200.00, ₦3,200.00, N3,200.00, Amt: 3,200.00, Amount: 3200
        val patterns = listOf(
            Pattern.compile("""(?:NGN|₦|N|Amt|Amount|value|of)\s*[:]?\s*(?:NGN|₦|N)?\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:debited|credited|spent|sent|received|paid)\s*(?:with|for)?\s*(?:NGN|₦|N)?\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""([0-9]{1,3}(?:,[0-9]{3})+(?:\.[0-9]{2}))"""),
            Pattern.compile("""(?:NGN|₦)\s*([0-9]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val rawNumber = matcher.group(1)?.replace(",", "")?.trim()
                val parsed = rawNumber?.toDoubleOrNull()
                if (parsed != null && parsed > 0.0) {
                    return parsed
                }
            }
        }
        return null
    }

    fun extractNarration(text: String, isDebit: Boolean): String {
        val lines = text.split("\n")
        // Check for specific labels in lines
        for (line in lines) {
            val l = line.trim()
            val lower = l.lowercase(Locale.ROOT)
            val prefixMatch = listOf(
                "desc:", "desc -", "description:", "narration:", "narr:",
                "details:", "remarks:", "info:", "to:", "from:", "ref:"
            ).find { lower.startsWith(it) }

            if (prefixMatch != null) {
                val cleaned = l.substring(prefixMatch.length).trim()
                if (cleaned.isNotBlank()) {
                    return sanitizeNarration(cleaned)
                }
            }
        }

        // Search within single line using regex
        val regex = Pattern.compile("""(?:desc|narration|details|narr|to|at|paid to|for)\s*[:\-]?\s*([A-Za-z0-9\s/_\-]+?)(?:\s+on|\.|\sat|\sBal|Date:|Bal:|\n|$)""", Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)
        if (matcher.find()) {
            val found = matcher.group(1)?.trim()
            if (!found.isNullOrBlank() && found.length > 2 && !found.equals("acct", ignoreCase = true)) {
                return sanitizeNarration(found)
            }
        }

        // Fallback for app notifications (e.g. "You just spent ₦2,500 on Chowdeck.")
        val spentOn = Pattern.compile("""(?:spent\s+(?:NGN|₦|N)?\s*[0-9,.]+\s+on|paid\s+(?:NGN|₦|N)?\s*[0-9,.]+\s+to|from\s+[A-Za-z0-9\s]+\s+for)\s+([A-Za-z0-9\s]+)""", Pattern.CASE_INSENSITIVE)
        val spentMatcher = spentOn.matcher(text)
        if (spentMatcher.find()) {
            val merchant = spentMatcher.group(1)?.trim()
            if (!merchant.isNullOrBlank()) {
                return sanitizeNarration(merchant)
            }
        }

        return if (isDebit) "Debit Transaction" else "Credit Received"
    }

    private fun sanitizeNarration(raw: String): String {
        // Strip out noisy date/time/account tokens at end
        var clean = raw
            .replace(Regex("""Date:.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""Bal:.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""Avail.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""Ref:.*""", RegexOption.IGNORE_CASE), "")
            .trim()
        if (clean.length > 50) {
            clean = clean.substring(0, 50).trim()
        }
        return clean.ifBlank { "Bank Transaction" }
    }

    private fun generateHash(bankName: String, amount: Double, type: TransactionType, timestamp: Long, narration: String): String {
        // Bucket timestamp within 1 hour to prevent duplicate SMS processing
        val timeBucket = timestamp / (1000 * 60 * 60)
        val raw = "$bankName-$amount-${type.name}-$timeBucket-$narration"
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(raw.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            raw
        }
    }
}

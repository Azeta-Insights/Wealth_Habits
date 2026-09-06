package com.example.statement

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class StatementParseResult(
    val transactions: List<TransactionEntity>,
    val fileName: String,
    val totalDebits: Double,
    val totalCredits: Double,
    val netChange: Double,
    val isPdf: Boolean = false,
    val errorMessage: String? = null
)

object StatementParser {

    suspend fun parseUri(context: Context, uri: Uri): StatementParseResult = withContext(Dispatchers.IO) {
        val fileName = getFileName(context, uri)
        val lowerName = fileName.lowercase(Locale.ROOT)

        val isPdf = lowerName.endsWith(".pdf")
        var errorMessage: String? = null

        val rawTransactions = if (isPdf) {
            try {
                parsePdf(context, uri)
            } catch (e: Exception) {
                Log.e("StatementParser", "PDF parsing failed with exception", e)
                errorMessage = e.localizedMessage ?: e.message ?: "PDF OCR parsing error: ${e.javaClass.simpleName}"
                emptyList()
            }
        } else {
            parseCsv(context, uri)
        }

        // Clean up: delete any temporary cache files that might have been copied
        try {
            val cacheDir = context.cacheDir
            cacheDir.listFiles { file -> file.name.startsWith("statement_upload_") }?.forEach {
                it.delete()
            }
        } catch (_: Exception) {}

        var totalDebits = 0.0
        var totalCredits = 0.0
        for (tx in rawTransactions) {
            if (tx.type == TransactionType.DEBIT) {
                totalDebits += tx.amount
            } else {
                totalCredits += tx.amount
            }
        }

        StatementParseResult(
            transactions = rawTransactions,
            fileName = fileName,
            totalDebits = totalDebits,
            totalCredits = totalCredits,
            netChange = totalCredits - totalDebits,
            isPdf = isPdf,
            errorMessage = errorMessage
        )
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "statement.csv"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        name = it.getString(nameIndex) ?: name
                    }
                }
            }
        } catch (_: Exception) {}
        return name
    }

    // ----------------------------------------------------
    // CSV PARSER
    // ----------------------------------------------------
    fun parseCsv(context: Context, uri: Uri): List<TransactionEntity> {
        val results = mutableListOf<TransactionEntity>()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()

        inputStream.use { stream ->
            val reader = BufferedReader(InputStreamReader(stream))
            val lines = reader.readLines()
            if (lines.isEmpty()) return emptyList()

            var headerIndex = -1
            var dateCol = -1
            var descCol = -1
            var debitCol = -1
            var creditCol = -1
            var amountCol = -1
            var typeCol = -1

            // Find header line
            for (i in 0 until minOf(lines.size, 15)) {
                val row = parseCsvRow(lines[i])
                val lowerRow = row.map { it.lowercase(Locale.ROOT).trim() }

                val dIdx = lowerRow.indexOfFirst { it.contains("date") || it.contains("txn date") || it.contains("value date") }
                val nIdx = lowerRow.indexOfFirst { it.contains("narration") || it.contains("desc") || it.contains("details") || it.contains("remark") || it.contains("particular") }

                if (dIdx >= 0 || nIdx >= 0) {
                    headerIndex = i
                    dateCol = dIdx
                    descCol = nIdx
                    debitCol = lowerRow.indexOfFirst { it == "debit" || it == "dr" || it.contains("withdrawal") || it.contains("debit (ngn)") }
                    creditCol = lowerRow.indexOfFirst { it == "credit" || it == "cr" || it.contains("deposit") || it.contains("credit (ngn)") }
                    amountCol = lowerRow.indexOfFirst { it == "amount" || it.contains("amount (ngn)") || it.contains("tran amount") }
                    typeCol = lowerRow.indexOfFirst { it == "type" || it == "cr/dr" || it == "dr/cr" || it == "txn type" }
                    break
                }
            }

            val startIndex = if (headerIndex >= 0) headerIndex + 1 else 0

            for (i in startIndex until lines.size) {
                val rawLine = lines[i].trim()
                if (rawLine.isBlank()) continue
                val row = parseCsvRow(rawLine)
                if (row.isEmpty()) continue

                try {
                    val dateStr = if (dateCol in row.indices) row[dateCol].trim() else ""
                    val narration = if (descCol in row.indices) row[descCol].trim() else "Statement Transaction"
                    val timestamp = parseDate(dateStr)

                    var amount = 0.0
                    var type = TransactionType.DEBIT

                    if (debitCol >= 0 && debitCol in row.indices && row[debitCol].isNotBlank()) {
                        val parsed = cleanAmount(row[debitCol])
                        if (parsed != null && parsed > 0) {
                            amount = parsed
                            type = TransactionType.DEBIT
                        }
                    }

                    if (amount == 0.0 && creditCol >= 0 && creditCol in row.indices && row[creditCol].isNotBlank()) {
                        val parsed = cleanAmount(row[creditCol])
                        if (parsed != null && parsed > 0) {
                            amount = parsed
                            type = TransactionType.CREDIT
                        }
                    }

                    if (amount == 0.0 && amountCol >= 0 && amountCol in row.indices && row[amountCol].isNotBlank()) {
                        val parsed = cleanAmount(row[amountCol])
                        if (parsed != null && parsed != 0.0) {
                            amount = kotlin.math.abs(parsed)
                            type = if (parsed < 0) TransactionType.DEBIT else TransactionType.CREDIT

                            if (typeCol >= 0 && typeCol in row.indices) {
                                val tStr = row[typeCol].lowercase(Locale.ROOT)
                                if (tStr.contains("cr") || tStr.contains("credit")) {
                                    type = TransactionType.CREDIT
                                } else if (tStr.contains("dr") || tStr.contains("debit")) {
                                    type = TransactionType.DEBIT
                                }
                            }
                        }
                    }

                    // If neither worked, try finding any numeric amount in row
                    if (amount == 0.0) {
                        for (cell in row) {
                            val candidate = cleanAmount(cell)
                            if (candidate != null && candidate > 0) {
                                amount = candidate
                                break
                            }
                        }
                    }

                    if (amount > 0.0) {
                        val category = TransactionCategory.matchFromNarration(narration)
                        results.add(
                            TransactionEntity(
                                amount = amount,
                                type = type,
                                category = category,
                                narration = narration.ifBlank { "Statement Transaction" },
                                bankName = "Bank Statement",
                                source = TransactionSource.STATEMENT,
                                timestamp = timestamp
                            )
                        )
                    }
                } catch (_: Exception) {}
            }
        }
        return results
    }

    private fun parseCsvRow(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false

        for (c in line) {
            when (c) {
                '"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        sb.append(c)
                    } else {
                        tokens.add(sb.toString().trim().removeSurrounding("\""))
                        sb.clear()
                    }
                }
                else -> sb.append(c)
            }
        }
        tokens.add(sb.toString().trim().removeSurrounding("\""))
        return tokens
    }

    // ----------------------------------------------------
    // ON-DEVICE OCR PDF STATEMENT EXTRACTION (ML Kit + PdfRenderer)
    // ----------------------------------------------------
    suspend fun parsePdf(context: Context, uri: Uri): List<TransactionEntity> {
        return PdfOcrProcessor.processPdf(context, uri)
    }

    private fun cleanAmount(raw: String?): Double? {
        if (raw == null) return null
        val cleaned = raw.replace("NGN", "", ignoreCase = true)
            .replace("₦", "")
            .replace("N", "")
            .replace(",", "")
            .trim()
        return cleaned.toDoubleOrNull()
    }

    private fun parseDate(dateStr: String): Long {
        val formats = listOf(
            "dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "dd-MMM-yyyy",
            "dd/MMM/yyyy", "d-MMM-yy", "d-MM-yyyy", "yyyy/MM/dd"
        )
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                sdf.isLenient = true
                val d = sdf.parse(dateStr)
                if (d != null) return d.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }
}

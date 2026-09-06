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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

/**
 * PdfOcrProcessor converts PDF pages into Bitmaps using Android's built-in PdfRenderer
 * and applies Google ML Kit's TextRecognition to extract plain text content page-by-page.
 *
 * It processes pages sequentially and recycles Bitmaps immediately to prevent high memory usage.
 * Extracted text lines are passed through a regex-based line parser to extract financial transactions.
 */
class PdfOcrProcessor(private val context: Context) {

    suspend fun processPdf(uri: Uri): List<TransactionEntity> = withContext(Dispatchers.IO) {
        // 1. Check Google Play Services availability first
        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (availability != ConnectionResult.SUCCESS) {
            Log.e("PdfOcrProcessor", "Google Play Services unavailable. ConnectionResult code: $availability")
            throw IllegalStateException("Google Play Services needs to be updated for text recognition to work")
        }

        val tempFile = File(context.cacheDir, "pdf_ocr_${System.currentTimeMillis()}.pdf")
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext emptyList()
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY) ?: return@withContext emptyList()
            renderer = PdfRenderer(pfd)
            val pageCount = renderer.pageCount
            val fullRecognizedText = StringBuilder()

            for (pageIndex in 0 until pageCount) {
                val page = renderer.openPage(pageIndex)
                var pageBitmap: Bitmap? = null
                try {
                    // Render page at 2x scale for crisp OCR recognition
                    val scale = 2.0f
                    val targetWidth = (page.width * scale).toInt().coerceIn(400, 2400)
                    val targetHeight = (page.height * scale).toInt().coerceIn(400, 3200)

                    pageBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(pageBitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val inputImage = InputImage.fromBitmap(pageBitmap, 0)

                    // Retry up to 3 times for model downloading / transient ML Kit initialization condition
                    var visionText: Text? = null
                    var lastException: Exception? = null

                    for (attempt in 1..3) {
                        try {
                            visionText = Tasks.await(recognizer.process(inputImage))
                            lastException = null
                            break
                        } catch (e: Exception) {
                            lastException = e
                            Log.e("PdfOcrProcessor", "ML Kit OCR attempt $attempt/3 failed on page $pageIndex", e)
                            if (attempt < 3) {
                                Log.w("PdfOcrProcessor", "Retrying ML Kit recognition in 2 seconds... (Attempt $attempt)")
                                delay(2000)
                            }
                        }
                    }

                    if (visionText == null && lastException != null) {
                        val cause = lastException.cause ?: lastException
                        val rawMsg = cause.localizedMessage ?: cause.message ?: cause.toString()
                        val truncatedMsg = if (rawMsg.length > 150) rawMsg.substring(0, 150) + "..." else rawMsg
                        val displayError = "ML Kit OCR error (${cause.javaClass.simpleName}): $truncatedMsg"
                        Log.e("PdfOcrProcessor", "All 3 ML Kit retries failed: $displayError", lastException)
                        throw IllegalStateException(displayError, lastException)
                    }

                    if (visionText != null) {
                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                val lineText = line.text.trim()
                                if (lineText.isNotEmpty()) {
                                    fullRecognizedText.append(lineText).append("\n")
                                }
                            }
                        }
                    }
                } finally {
                    // Free/recycle each page Bitmap immediately after OCR to manage memory
                    pageBitmap?.recycle()
                    page.close()
                }
            }

            val extractedText = fullRecognizedText.toString()
            if (extractedText.isBlank()) {
                emptyList()
            } else {
                parseTextLinesIntoTransactions(extractedText)
            }
        } catch (e: Exception) {
            Log.e("PdfOcrProcessor", "PDF OCR processing failed", e)
            throw e
        } finally {
            try { recognizer.close() } catch (_: Exception) {}
            try { renderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
            try {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Regex-based line parser matching dates, amounts, and narrations from OCR text lines.
     */
    fun parseTextLinesIntoTransactions(text: String): List<TransactionEntity> {
        val transactions = mutableListOf<TransactionEntity>()
        val lines = text.split("\n")

        val datePattern = Pattern.compile("""\b([0-9]{1,2}[-/][0-9A-Za-z]{2,3}[-/][0-9]{2,4})\b|\b([0-9]{4}[-/][0-9]{2}[-/][0-9]{2})\b""")
        val amountPattern = Pattern.compile("""\b([0-9]{1,3}(?:,[0-9]{3})+(?:\.[0-9]{2})|[0-9]+\.[0-9]{2})\b""")

        for (line in lines) {
            val l = line.trim()
            if (l.isBlank() || l.length < 10) continue

            val dateMatcher = datePattern.matcher(l)
            val amtMatcher = amountPattern.matcher(l)

            if (dateMatcher.find() && amtMatcher.find()) {
                val dateStr = dateMatcher.group(0) ?: ""
                val amountStr = amtMatcher.group(0) ?: ""
                val amount = cleanAmount(amountStr) ?: continue
                if (amount <= 0.0) continue

                val lowerLine = l.lowercase(Locale.ROOT)
                val isCredit = lowerLine.contains(" cr") || lowerLine.contains("credit") || lowerLine.contains("dep")
                val isDebit = lowerLine.contains(" dr") || lowerLine.contains("debit") || lowerLine.contains("wth") || !isCredit

                val type = if (isDebit) TransactionType.DEBIT else TransactionType.CREDIT

                // Clean line to leave narration
                var narration = l.replace(dateStr, "")
                    .replace(amountStr, "")
                    .replace(Regex("""\b(CR|DR|DEBIT|CREDIT)\b""", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("""[|,\t]+"""), " ")
                    .trim()

                if (narration.length > 50) {
                    narration = narration.substring(0, 50).trim()
                }

                if (narration.isBlank()) {
                    narration = if (type == TransactionType.DEBIT) "Debit Transaction" else "Credit Received"
                }

                val category = TransactionCategory.matchFromNarration(narration)
                val timestamp = parseDate(dateStr)

                transactions.add(
                    TransactionEntity(
                        amount = amount,
                        type = type,
                        category = category,
                        narration = narration,
                        bankName = "Bank Statement",
                        source = TransactionSource.STATEMENT,
                        timestamp = timestamp
                    )
                )
            }
        }

        return transactions
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

    companion object {
        suspend fun processPdf(context: Context, uri: Uri): List<TransactionEntity> {
            return PdfOcrProcessor(context).processPdf(uri)
        }
    }
}

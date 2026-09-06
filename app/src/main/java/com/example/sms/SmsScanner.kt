package com.example.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsScanner {

    suspend fun scanInboxForBankAlerts(
        context: Context,
        repository: TransactionRepository,
        maxMessagesToScan: Int = 200
    ): Int = withContext(Dispatchers.IO) {
        var importedCount = 0
        try {
            val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.BODY,
                Telephony.Sms.Inbox.DATE
            )
            val sortOrder = "${Telephony.Sms.Inbox.DATE} DESC"

            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val addressCol = c.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)
                val bodyCol = c.getColumnIndex(Telephony.Sms.Inbox.BODY)
                val dateCol = c.getColumnIndex(Telephony.Sms.Inbox.DATE)

                var scanned = 0
                while (c.moveToNext() && scanned < maxMessagesToScan) {
                    scanned++
                    val address = if (addressCol >= 0) c.getString(addressCol) else null
                    val body = if (bodyCol >= 0) c.getString(bodyCol) else null
                    val date = if (dateCol >= 0) c.getLong(dateCol) else System.currentTimeMillis()

                    if (body != null && SmsParser.isBankSender(address, body)) {
                        val transaction = SmsParser.parseSms(address, body, date)
                        if (transaction != null) {
                            val hash = transaction.deduplicationHash
                            if (hash == null || !repository.existsByHash(hash)) {
                                repository.insertTransaction(transaction)
                                importedCount++
                            }
                        }
                    }
                }
            }
        } catch (_: SecurityException) {
            // Permission not granted, graceful fallback
        } catch (_: Exception) {
            // Handle any reader anomaly gracefully
        }
        importedCount
    }
}

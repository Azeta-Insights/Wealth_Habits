package com.example.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import com.example.data.db.AppDatabase
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // Check if user has enabled SMS reading in Settings
        if (!SmsPreferences.isSmsReadingEnabled(context)) return

        val messages: Array<SmsMessage>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } else {
            val bundle = intent.extras
            val pdus = bundle?.get("pdus") as? Array<*>
            pdus?.mapNotNull { pdu ->
                @Suppress("DEPRECATION")
                SmsMessage.createFromPdu(pdu as ByteArray)
            }?.toTypedArray()
        }

        if (messages.isNullOrEmpty()) return

        val fullBody = StringBuilder()
        var sender: String? = null
        var timestamp = System.currentTimeMillis()

        for (msg in messages) {
            sender = msg.originatingAddress ?: sender
            fullBody.append(msg.messageBody)
            if (msg.timestampMillis > 0) {
                timestamp = msg.timestampMillis
            }
        }

        val body = fullBody.toString()

        // Check if sender or message looks like a recognized Nigerian bank
        if (!SmsParser.isBankSender(sender, body)) return

        val transaction = SmsParser.parseSms(sender, body, timestamp) ?: return

        // Save transaction asynchronously in local Room database
        val dao = AppDatabase.getInstance(context).transactionDao()
        val repository = TransactionRepository(dao)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (transaction.deduplicationHash == null || !repository.existsByHash(transaction.deduplicationHash)) {
                    repository.insertTransaction(transaction)
                }
            } catch (_: Exception) {
                // Ignore transient database error
            } finally {
                pendingResult.finish()
            }
        }
    }
}

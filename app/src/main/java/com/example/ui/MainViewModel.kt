package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ReflectionType
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.repository.TransactionRepository
import com.example.insights.InsightEngine
import com.example.insights.WealthInsight
import com.example.sms.SmsPreferences
import com.example.sms.SmsScanner
import com.example.statement.StatementParseResult
import com.example.statement.StatementParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Insights : Screen()
    object Transactions : Screen()
    object AddImport : Screen()
    object Settings : Screen()
    object SignIn : Screen()
    data class StatementReview(val result: StatementParseResult) : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository

    val allTransactions: StateFlow<List<TransactionEntity>>
    val userProfile: StateFlow<UserProfile?>
    val currentScreen = MutableStateFlow<Screen>(Screen.Insights)

    private val _isSignInSkipped = MutableStateFlow<Boolean>(false)
    val isSignInSkipped: StateFlow<Boolean> = _isSignInSkipped.asStateFlow()

    private val _answeredReflections = MutableStateFlow<Map<String, ReflectionType>>(emptyMap())
    val answeredReflections = _answeredReflections.asStateFlow()

    private val _smsEnabled = MutableStateFlow(SmsPreferences.isSmsReadingEnabled(application))
    val smsEnabled = _smsEnabled.asStateFlow()

    private val _isScanningSms = MutableStateFlow(false)
    val isScanningSms = _isScanningSms.asStateFlow()

    private val _isParsingStatement = MutableStateFlow(false)
    val isParsingStatement = _isParsingStatement.asStateFlow()

    private val _statementParseError = MutableStateFlow<String?>(null)
    val statementParseError = _statementParseError.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    private val _addImportTab = MutableStateFlow(0)
    val addImportTab = _addImportTab.asStateFlow()

    fun setAddImportTab(tab: Int) {
        _addImportTab.value = tab
    }

    fun navigateToAddTab(tab: Int) {
        _addImportTab.value = tab
        currentScreen.value = Screen.AddImport
    }

    init {
        val db = AppDatabase.getInstance(application)
        repository = TransactionRepository(db.transactionDao(), db.userProfileDao())

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        _isSignInSkipped.value = SmsPreferences.isSignInSkipped(application)
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            SmsPreferences.setSignInSkipped(getApplication(), false)
            _isSignInSkipped.value = false
            currentScreen.value = Screen.Insights
        }
    }

    fun skipSignIn() {
        SmsPreferences.setSignInSkipped(getApplication(), true)
        _isSignInSkipped.value = true
        currentScreen.value = Screen.Insights
    }

    fun signOut() {
        viewModelScope.launch {
            repository.clearUserProfile()
            SmsPreferences.setSignInSkipped(getApplication(), false)
            _isSignInSkipped.value = false
            currentScreen.value = Screen.SignIn
        }
    }

    val insights: StateFlow<List<WealthInsight>> = combine(
        allTransactions,
        _answeredReflections
    ) { txs, reflections ->
        InsightEngine.generateInsights(txs, reflections)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun navigateTo(screen: Screen) {
        currentScreen.value = screen
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // ----------------------------------------------------
    // MANUAL ENTRY (Fast 3-tap flow)
    // ----------------------------------------------------
    fun addManualTransaction(
        amount: Double,
        category: TransactionCategory,
        type: TransactionType = TransactionType.DEBIT,
        narration: String = ""
    ) {
        viewModelScope.launch {
            val cleanNarration = narration.ifBlank {
                "${category.displayName} (Manual)"
            }
            val tx = TransactionEntity(
                amount = amount,
                type = type,
                category = category,
                narration = cleanNarration,
                bankName = null,
                source = TransactionSource.MANUAL,
                timestamp = System.currentTimeMillis()
            )
            repository.insertTransaction(tx)
            _statusMessage.value = "Saved ${InsightEngine.formatNaira(amount)} for ${category.displayName}"
        }
    }

    // ----------------------------------------------------
    // REFLECTIONS (Need vs Want)
    // ----------------------------------------------------
    fun answerReflection(insightId: String, relatedTxId: Long?, reflection: ReflectionType) {
        val current = _answeredReflections.value.toMutableMap()
        current[insightId] = reflection
        _answeredReflections.value = current

        if (relatedTxId != null) {
            viewModelScope.launch {
                repository.updateReflection(relatedTxId, reflection)
            }
        }
    }

    fun toggleTransactionReflection(transaction: TransactionEntity, reflection: ReflectionType) {
        viewModelScope.launch {
            val updated = if (transaction.reflection == reflection) null else reflection
            repository.updateReflection(transaction.id, updated)
        }
    }

    fun updateTransactionCategory(transactionId: Long, category: TransactionCategory) {
        viewModelScope.launch {
            repository.updateCategory(transactionId, category)
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(transactionId)
        }
    }

    // ----------------------------------------------------
    // SMS READING
    // ----------------------------------------------------
    fun setSmsReadingEnabled(enabled: Boolean) {
        SmsPreferences.setSmsReadingEnabled(getApplication(), enabled)
        _smsEnabled.value = enabled
    }

    fun scanSmsInbox() {
        viewModelScope.launch {
            _isScanningSms.value = true
            try {
                val count = SmsScanner.scanInboxForBankAlerts(getApplication(), repository)
                SmsPreferences.setInitialSmsScanned(getApplication(), true)
                _statusMessage.value = if (count > 0) {
                    "Discovered $count bank alert${if (count > 1) "s" else ""} in your inbox"
                } else {
                    "No new bank alerts found in inbox"
                }
            } catch (_: Exception) {
                _statusMessage.value = "Could not scan SMS inbox"
            } finally {
                _isScanningSms.value = false
            }
        }
    }

    // ----------------------------------------------------
    // BANK STATEMENT UPLOAD
    // ----------------------------------------------------
    fun handleStatementUri(uri: Uri) {
        viewModelScope.launch {
            _isParsingStatement.value = true
            _statementParseError.value = null
            try {
                val result = StatementParser.parseUri(getApplication(), uri)
                currentScreen.value = Screen.StatementReview(result)
            } catch (e: Exception) {
                _statementParseError.value = "Could not parse statement file."
            } finally {
                _isParsingStatement.value = false
            }
        }
    }

    fun confirmStatementImport(transactions: List<TransactionEntity>) {
        viewModelScope.launch {
            repository.insertTransactions(transactions)
            _statusMessage.value = "Successfully imported ${transactions.size} transactions"
            currentScreen.value = Screen.Transactions
        }
    }

    // ----------------------------------------------------
    // SETTINGS & DATA CONTROLS
    // ----------------------------------------------------
    fun deleteStatementData() {
        viewModelScope.launch {
            val count = repository.deleteStatementData()
            _statusMessage.value = "Removed $count statement transactions"
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
            _answeredReflections.value = emptyMap()
            _statusMessage.value = "All transaction history cleared"
        }
    }
}

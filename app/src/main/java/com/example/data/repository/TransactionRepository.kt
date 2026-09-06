package com.example.data.repository

import com.example.data.db.TransactionDao
import com.example.data.db.UserProfileDao
import com.example.data.model.ReflectionType
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionSource
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val userProfileDao: UserProfileDao? = null
) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val userProfile: Flow<UserProfile?> = userProfileDao?.getUserProfileFlow() ?: flowOf(null)

    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao?.saveUserProfile(profile)
    }

    suspend fun clearUserProfile() {
        userProfileDao?.clearUserProfile()
    }

    suspend fun getAllTransactionsSnapshot(): List<TransactionEntity> {
        return transactionDao.getAllTransactionsSnapshot()
    }

    suspend fun getTransactionsSince(sinceTimestamp: Long): List<TransactionEntity> {
        return transactionDao.getTransactionsSince(sinceTimestamp)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long> {
        return transactionDao.insertTransactions(transactions)
    }

    suspend fun updateReflection(id: Long, reflection: ReflectionType?) {
        transactionDao.updateReflection(id, reflection)
    }

    suspend fun updateCategory(id: Long, category: TransactionCategory) {
        transactionDao.updateCategory(id, category)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun deleteStatementData(): Int {
        return transactionDao.deleteTransactionsBySource(TransactionSource.STATEMENT)
    }

    suspend fun deleteAllData() {
        transactionDao.deleteAllTransactions()
    }

    suspend fun existsByHash(hash: String): Boolean {
        return transactionDao.countTransactionsWithHash(hash) > 0
    }
}

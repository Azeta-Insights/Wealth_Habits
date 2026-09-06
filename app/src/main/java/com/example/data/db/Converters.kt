package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.ReflectionType
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = try {
        TransactionType.valueOf(value)
    } catch (_: Exception) {
        TransactionType.DEBIT
    }

    @TypeConverter
    fun fromTransactionCategory(category: TransactionCategory): String = category.name

    @TypeConverter
    fun toTransactionCategory(value: String): TransactionCategory = try {
        TransactionCategory.valueOf(value)
    } catch (_: Exception) {
        TransactionCategory.OTHER
    }

    @TypeConverter
    fun fromTransactionSource(source: TransactionSource): String = source.name

    @TypeConverter
    fun toTransactionSource(value: String): TransactionSource = try {
        TransactionSource.valueOf(value)
    } catch (_: Exception) {
        TransactionSource.MANUAL
    }

    @TypeConverter
    fun fromReflectionType(reflection: ReflectionType?): String? = reflection?.name

    @TypeConverter
    fun toReflectionType(value: String?): ReflectionType? = value?.let {
        try {
            ReflectionType.valueOf(it)
        } catch (_: Exception) {
            null
        }
    }
}

package com.example.data.model

enum class TransactionSource(val label: String) {
    MANUAL("Manual Entry"),
    SMS("Bank Alert SMS"),
    STATEMENT("Bank Statement")
}

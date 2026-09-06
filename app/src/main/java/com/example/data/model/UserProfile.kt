package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: String = "primary_user",
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val givenName: String? = null,
    val familyName: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

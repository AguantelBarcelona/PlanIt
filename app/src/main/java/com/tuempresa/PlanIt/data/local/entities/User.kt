package com.tuempresa.PlanIt.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val displayName: String? = null,
    val profilePictureUri: String? = null
)

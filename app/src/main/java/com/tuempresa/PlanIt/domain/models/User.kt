package com.tuempresa.PlanIt.domain.models

data class User(
    val uid: String,
    val username: String,
    val email: String,
    val displayName: String? = null,
    val profilePictureUrl: String? = null
)

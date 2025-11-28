package com.tuempresa.PlanIt.domain.repository

import com.google.firebase.auth.AuthResult
import com.tuempresa.PlanIt.domain.models.User

interface AuthRepository {
    val currentUser: User?
    suspend fun login(email: String, password: String): Result<AuthResult>
    suspend fun register(email: String, password: String, username: String): Result<AuthResult>
    suspend fun logout()
}
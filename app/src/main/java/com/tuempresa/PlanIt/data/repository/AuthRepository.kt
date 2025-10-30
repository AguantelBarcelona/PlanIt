package com.tuempresa.PlanIt.data.repository

import com.tuempresa.PlanIt.data.local.entities.User

interface AuthRepository {
    suspend fun findUserByUsername(username: String): User?
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: Int)
}
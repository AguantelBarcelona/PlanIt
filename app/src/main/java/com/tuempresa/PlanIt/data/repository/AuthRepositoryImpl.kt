package com.tuempresa.PlanIt.data.repository

import com.tuempresa.PlanIt.data.local.dao.UserDao
import com.tuempresa.PlanIt.data.local.entities.User

class AuthRepositoryImpl(private val userDao: UserDao) : AuthRepository {
    override suspend fun findUserByUsername(username: String): User? {
        return userDao.findByUsername(username)
    }

    override suspend fun createUser(user: User) {
        userDao.insertUser(user)
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    override suspend fun deleteUser(userId: Int) {
        userDao.deleteUser(userId)
    }
}
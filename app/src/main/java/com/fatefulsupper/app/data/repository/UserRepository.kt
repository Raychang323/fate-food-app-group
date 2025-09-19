package com.fatefulsupper.app.data.repository

import com.fatefulsupper.app.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    // Flow to observe the current logged-in user state
    fun getCurrentUser(): Flow<User?>

    // suspend fun login(username: String, password: String): Result<User>
    suspend fun login(username: String, password: String): Boolean // Simplified for now

    // suspend fun register(username: String, email: String, password: String): Result<User>
    suspend fun register(username: String, email: String, password: String): Boolean // Simplified for now

    suspend fun sendVerificationEmail(email: String): Boolean

    // suspend fun verifyEmailCode(email: String, code: String): Result<Boolean>
    suspend fun verifyEmailCode(email: String, code: String): Boolean // Simplified for now

    suspend fun resendVerificationEmail(email: String): Boolean

    suspend fun logout()

    // Add other user-related operations like:
    // suspend fun updateProfile(user: User): Result<Unit>
    // suspend fun forgotPassword(email: String): Result<Unit>
}

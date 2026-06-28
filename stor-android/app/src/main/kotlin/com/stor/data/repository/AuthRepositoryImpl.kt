package com.stor.data.repository

import com.stor.data.preferences.AuthPreferences
import com.stor.data.remote.api.StorApi
import com.stor.data.remote.api.getErrorMessage
import com.stor.data.remote.dto.LoginRequest
import com.stor.data.remote.dto.RefreshRequest
import com.stor.data.remote.dto.RegisterRequest
import com.stor.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: StorApi,
    private val prefs: AuthPreferences
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = api.login(LoginRequest(email, password))
        if (!response.isSuccessful) {
            throw Exception(response.getErrorMessage())
        }
        val body = response.body() ?: error("Empty response body")
        prefs.saveTokens(body.accessToken, body.refreshToken)
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> = runCatching {
        val response = api.register(RegisterRequest(name, email, password))
        if (!response.isSuccessful) {
            throw Exception(response.getErrorMessage())
        }
        val body = response.body() ?: error("Empty response body")
        prefs.saveTokens(body.accessToken, body.refreshToken)
    }

    override suspend fun logout() = prefs.clearTokens()

    override fun isLoggedIn(): Flow<Boolean> = prefs.accessToken.map { !it.isNullOrBlank() }
}


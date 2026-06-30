package com.stor.data.remote.interceptors

import com.stor.data.preferences.AuthPreferences
import com.stor.data.remote.dto.RefreshRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator that intercepts 401 responses and transparently
 * refreshes the JWT access token using the stored refresh token.
 *
 * Flow:
 *  1. A request returns HTTP 401.
 *  2. This authenticator fires and calls POST /auth/refresh with the stored refresh_token.
 *  3. On success, the new access_token is persisted and the original request is retried.
 *  4. If refresh fails (e.g. refresh token also expired), returns null → the caller
 *     receives the 401 and should log the user out.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val authPreferences: AuthPreferences
) : Authenticator {

    companion object {
        private const val BASE_URL = "https://stor-8r4z.onrender.com/api/v1/"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite loops — if the failing request was itself the refresh call, bail out.
        if (response.request.url.encodedPath.contains("auth/refresh")) return null

        // Only one coroutine/thread should refresh at a time.
        return synchronized(this) {
            runBlocking {
                val refreshToken = authPreferences.refreshToken.first()
                if (refreshToken.isNullOrBlank()) return@runBlocking null

                val newAccessToken = tryRefresh(refreshToken)
                if (newAccessToken != null) {
                    // Retry the original request with the new token
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                } else {
                    // Refresh failed — clear credentials so the app can redirect to login
                    authPreferences.clearTokens()
                    null
                }
            }
        }
    }

    /** Calls the refresh endpoint synchronously (we are already on a background thread). */
    private fun tryRefresh(refreshToken: String): String? {
        return try {
            val json = """{"refresh_token":"$refreshToken"}"""
            val body = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("${BASE_URL}auth/refresh")
                .post(body)
                .build()

            // Use a plain OkHttpClient (no auth interceptor) to avoid circular calls
            val client = okhttp3.OkHttpClient()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return null

            val responseBody = response.body?.string() ?: return null
            val jsonElement = Json { ignoreUnknownKeys = true }.parseToJsonElement(responseBody)
            val accessToken = jsonElement.jsonObject["access_token"]?.jsonPrimitive?.content
                ?: return null
            val newRefreshToken = jsonElement.jsonObject["refresh_token"]?.jsonPrimitive?.content

            // Persist new tokens
            runBlocking {
                if (newRefreshToken != null) {
                    authPreferences.saveTokens(accessToken, newRefreshToken)
                } else {
                    authPreferences.saveTokens(accessToken, refreshToken) // keep old refresh token
                }
            }
            accessToken
        } catch (e: Exception) {
            null
        }
    }
}

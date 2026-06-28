package com.stor.data.remote.api

import retrofit2.Response
import org.json.JSONObject

fun <T> Response<T>.getErrorMessage(): String {
    val rawError = errorBody()?.string()
    if (!rawError.isNullOrBlank()) {
        try {
            val json = JSONObject(rawError)
            val errorMsg = json.optString("error")
            if (!errorMsg.isNullOrBlank()) {
                return errorMsg
            }
        } catch (e: Exception) {
            // Ignore and fallback
        }
    }
    val msg = message()
    return if (!msg.isNullOrBlank()) msg else "An unknown error occurred"
}

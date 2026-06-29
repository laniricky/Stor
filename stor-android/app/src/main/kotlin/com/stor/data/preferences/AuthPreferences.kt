package com.stor.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val MONTHLY_BUDGET_KEY = doublePreferencesKey("monthly_budget")
        private val LOAN_REMINDERS_KEY = booleanPreferencesKey("loan_reminders")
        private val BUDGET_ALERTS_KEY = booleanPreferencesKey("budget_alerts")
    }

    val accessToken: Flow<String?> = dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    val refreshToken: Flow<String?> = dataStore.data.map { it[REFRESH_TOKEN_KEY] }
    val userName: Flow<String?> = dataStore.data.map { it[USER_NAME_KEY] }
    val userEmail: Flow<String?> = dataStore.data.map { it[USER_EMAIL_KEY] }
    val monthlyBudget: Flow<Double> = dataStore.data.map { it[MONTHLY_BUDGET_KEY] ?: 0.0 }
    val loanReminders: Flow<Boolean> = dataStore.data.map { it[LOAN_REMINDERS_KEY] ?: true }
    val budgetAlerts: Flow<Boolean> = dataStore.data.map { it[BUDGET_ALERTS_KEY] ?: true }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun saveUser(id: String, name: String, email: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = id
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
        }
    }

    suspend fun updateUserName(name: String) {
        dataStore.edit { it[USER_NAME_KEY] = name }
    }

    suspend fun updateUserEmail(email: String) {
        dataStore.edit { it[USER_EMAIL_KEY] = email }
    }

    suspend fun setMonthlyBudget(amount: Double) {
        dataStore.edit { it[MONTHLY_BUDGET_KEY] = amount }
    }

    suspend fun setLoanReminders(enabled: Boolean) {
        dataStore.edit { it[LOAN_REMINDERS_KEY] = enabled }
    }

    suspend fun setBudgetAlerts(enabled: Boolean) {
        dataStore.edit { it[BUDGET_ALERTS_KEY] = enabled }
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }
}

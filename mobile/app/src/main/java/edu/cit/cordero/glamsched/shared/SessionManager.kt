package edu.cit.cordero.glamsched.shared

import android.content.Context

object SessionManager {

    private const val PREFS_NAME = "glamsched_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    fun saveSession(
        context: Context,
        userId: Long,
        userName: String,
        userEmail: String,
        userRole: String
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            putString(KEY_USER_ROLE, userRole)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(context: Context): Long =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_USER_ID, 0L)

    fun getUserName(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_NAME, "User") ?: "User"

    fun getUserEmail(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_EMAIL, "") ?: ""

    fun getUserRole(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_ROLE, "CLIENT") ?: "CLIENT"

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

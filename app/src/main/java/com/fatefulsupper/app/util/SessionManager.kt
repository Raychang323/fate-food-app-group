package com.fatefulsupper.app.util

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "FatefulSupperSession"
    private const val USER_TOKEN = "user_token"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAuthToken(context: Context, token: String) {
        val editor = getPreferences(context).edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun getAuthToken(context: Context): String? {
        return getPreferences(context).getString(USER_TOKEN, null)
    }

    fun clearAuthToken(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(USER_TOKEN)
        editor.apply()
    }
}

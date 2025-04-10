package by.dis.birdvoice.helpers.utils

import android.content.Context
import android.content.SharedPreferences

class LoginManager(context: Context) {

    companion object {
        private const val LOGIN = "login"
        private const val PASSWORD = "password"
        private const val PREFS_NAME = "login_prefs"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveTokens(login: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString(LOGIN, login)
        editor.putString(PASSWORD, password)
        editor.apply()
    }

    fun getTokens(onSuccess: (String, String) -> Unit) {
        val login = sharedPreferences.getString(LOGIN, "") ?: ""
        val password = sharedPreferences.getString(PASSWORD, "") ?: ""

        onSuccess(login, password)
    }

    fun removeTokens() {
        val editor = sharedPreferences.edit()
        editor.apply {
            remove(LOGIN)
            remove(PASSWORD)
            apply()
        }
    }
}
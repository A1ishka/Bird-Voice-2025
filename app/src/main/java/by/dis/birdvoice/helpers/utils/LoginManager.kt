package by.dis.birdvoice.helpers.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import by.dis.birdvoice.helpers.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class LoginManager(private val context: Context) {

    companion object {
        private val LOGIN = stringPreferencesKey("login")
        private val PASSWORD = stringPreferencesKey("password")
    }

    fun saveTokens(login: String, password: String) = runBlocking {
        context.dataStore.edit { prefs ->
            prefs[LOGIN] = login
            prefs[PASSWORD] = password
        }
    }

    fun getTokens(onSuccess: (String, String) -> Unit) = runBlocking {
        val login = context.dataStore
            .data.map{ it[LOGIN] ?: "" }
            .first()
        val password = context.dataStore
            .data.map { it[PASSWORD] ?: "" }
            .first()

        onSuccess(login, password)
    }

    fun removeTokens() = runBlocking {
        context.dataStore.edit {
            it.remove(LOGIN)
            it.remove(PASSWORD)
        }
    }
}
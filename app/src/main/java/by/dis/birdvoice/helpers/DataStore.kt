package by.dis.birdvoice.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.Locale

val Context.dataStore by preferencesDataStore("login_prefs")

val LOCALE_CODE_KEY = stringPreferencesKey("locale")

@SuppressLint("ObsoleteSdkInt")
fun Context.wrapInLocale(locale: Locale): Context {
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    Locale.setDefault(locale)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        createConfigurationContext(config)
    } else {
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        this
    }
}
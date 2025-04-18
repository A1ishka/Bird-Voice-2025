package by.dis.birdvoice.app

import android.app.Application
import android.content.Context
import by.dis.birdvoice.helpers.LOCALE_CODE_KEY
import by.dis.birdvoice.helpers.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

class MainApp : Application() {

    val constLocale = "locale"
    val constLaunches = "launches"

    private var localeInt = 0
    val localeFlow = MutableStateFlow<Locale>(Locale.getDefault())

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.data.map { prefs ->
                Locale(prefs[LOCALE_CODE_KEY] ?: "en")
            }.collect {
                localeFlow.value = it
            }
        }
    }

    fun setLocaleInt(locale: String) {
        when (locale) {
            "be" -> localeInt = 1
            "en" -> localeInt = 2
            "ru" -> localeInt = 3
        }
    }

    fun getLocaleInt() = localeInt

    private lateinit var context: Context

    fun getContext() = context
    fun setContext(context: Context){
        this.context = context
    }
}
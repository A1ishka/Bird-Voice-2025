package by.dis.birdvoice.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class MainApp : Application() {

    val constPreferences = "preferences"
    val constLocale = "locale"
    val constLaunches = "launches"

    private var locale = Locale("en")
    private val config = Configuration()
    private var localeInt = 0

    override fun onCreate() {
        super.onCreate()

        Locale.setDefault(locale)
    }

    fun setLocale(loc: Locale){
        locale = loc
        config.setLocale(loc)
        context.resources.configuration.setLocale(loc)
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
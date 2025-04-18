package by.dis.birdvoice

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import by.dis.birdvoice.app.MainApp
import by.dis.birdvoice.helpers.wrapInLocale
import kotlinx.coroutines.launch
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {

    private val appLocaleFlow by lazy {
        (application as MainApp).localeFlow
    }

    override fun attachBaseContext(newBase: Context) {
        val app = newBase.applicationContext as MainApp
        val locale = app.localeFlow.value ?: Locale.getDefault()
        app.setLocaleInt(locale.language)

        val context = newBase.wrapInLocale(locale)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            appLocaleFlow.collect { newLocale ->
                val currentLocale = resources.configuration.locales[0]
                if (currentLocale.language != newLocale.language) {
                    recreate()
                }
            }
        }
    }
}
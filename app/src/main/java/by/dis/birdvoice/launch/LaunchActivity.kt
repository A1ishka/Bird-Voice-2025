package by.dis.birdvoice.launch

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import by.dis.birdvoice.app.MainApp
import by.dis.birdvoice.databinding.ActivityLaunchBinding
import by.dis.birdvoice.helpers.utils.LoginManager
import by.dis.birdvoice.launch.vm.LaunchVM
import by.dis.birdvoice.main.MainActivity
import java.util.*

@SuppressLint("CustomSplashScreen")
class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding
    private lateinit var controller: WindowInsetsControllerCompat
    private lateinit var loginManager: LoginManager

    private val mainApp = MainApp()
    private val launchVM: LaunchVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)

        mainApp.setContext(this@LaunchActivity)
        loadPreferences()

        launchVM.activityBinding = binding
        setContentView(binding.root)

        loginManager = LoginManager(mainApp.getContext())

        controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    @Suppress("DEPRECATION")
    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences(mainApp.constPreferences, MODE_PRIVATE)
        val locale = sharedPreferences.getString(mainApp.constLocale, "en")
        locale?.let { Locale(it) }?.let { mainApp.setLocale(it) }

        val launchesCount = sharedPreferences.getInt(mainApp.constLaunches, 0)
        with (sharedPreferences.edit()) {
            putInt(mainApp.constLaunches, (launchesCount + 1))
            apply()
        }

        val config = mainApp.getContext().resources.configuration
        config.setLocale(Locale(locale!!))
        Locale.setDefault(Locale(locale))

        mainApp.getContext().resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun hideStatusBar(){
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    fun showStatusBar(){
        controller.show(WindowInsetsCompat.Type.systemBars())
    }

    fun setPopBackCallback(anim: () -> Unit){
        launchVM.setNavUpAnimLambda(anim)
        onBackPressedDispatcher.addCallback(this, launchVM.onMapBackPressedCallback)
    }

    fun deletePopBackCallback(){
        if (launchVM.onMapBackPressedCallback.isEnabled) launchVM.onMapBackPressedCallback.remove()
    }

    fun moveToMainActivity(regOrLogToken: Int = 0, recognitionToken: String, refreshToken: String, username: String, accountId: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("userRegisterToken", regOrLogToken)
        intent.putExtra("access", recognitionToken)
        intent.putExtra("refresh", refreshToken)
        intent.putExtra("username", username)
        intent.putExtra("accountId", accountId)
        startActivity(intent)
    }

    fun getLoginManager() = loginManager
    fun getApp() = mainApp
}
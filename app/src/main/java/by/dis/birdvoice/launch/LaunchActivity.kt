package by.dis.birdvoice.launch

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.dis.birdvoice.BaseActivity
import by.dis.birdvoice.R
import by.dis.birdvoice.app.MainApp
import by.dis.birdvoice.databinding.ActivityLaunchBinding
import by.dis.birdvoice.helpers.utils.LoginManager
import by.dis.birdvoice.helpers.utils.NetworkMonitor
import by.dis.birdvoice.launch.vm.LaunchVM
import by.dis.birdvoice.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class LaunchActivity : BaseActivity() {

    private lateinit var binding: ActivityLaunchBinding
    private lateinit var controller: WindowInsetsControllerCompat
    private lateinit var loginManager: LoginManager

    private val mainApp = MainApp()
    private val launchVM: LaunchVM by viewModels()

    private lateinit var monitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)

        launchVM.title.observe(this) { title ->
            binding.launchTitle.text = title
        }

        mainApp.setContext(this@LaunchActivity)

        launchVM.activityBinding = binding
        setContentView(binding.root)

        monitor = NetworkMonitor(this)

        lifecycleScope.launch {
            delay(2000)
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                monitor.isOnline.collect { online ->
                    toggleBanner(show = !online)
                }
            }
        }

        loginManager = LoginManager(mainApp.getContext())

        controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun toggleBanner(show: Boolean) {
        val trans = Slide(Gravity.TOP).apply {
            duration = 200
            addTarget(R.id.offline_banner)
        }
        TransitionManager.beginDelayedTransition(binding.launchGroup, trans)
        binding.offlineBanner.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun hideStatusBar() {
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    fun showStatusBar() {
        controller.show(WindowInsetsCompat.Type.systemBars())
    }

    fun setPopBackCallback(anim: () -> Unit) {
        launchVM.setNavUpAnimLambda(anim)
        onBackPressedDispatcher.addCallback(this, launchVM.onMapBackPressedCallback)
    }

    fun deletePopBackCallback() {
        if (launchVM.onMapBackPressedCallback.isEnabled) launchVM.onMapBackPressedCallback.remove()
    }

    fun moveToMainActivity(
        regOrLogToken: Int = 0,
        recognitionToken: String,
        refreshToken: String,
        email: String,
        accountId: Int
    ) {
        Log.d("log", refreshToken)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("userRegisterToken", regOrLogToken)
        intent.putExtra("access", recognitionToken)
        intent.putExtra("refresh", refreshToken)
        intent.putExtra("email", email)
        intent.putExtra("accountId", accountId)
        startActivity(intent)
    }

    fun getLoginManager() = loginManager
    fun getApp() = mainApp
}
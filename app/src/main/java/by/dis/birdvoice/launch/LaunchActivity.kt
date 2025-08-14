package by.dis.birdvoice.launch

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import by.dis.birdvoice.BaseActivity
import by.dis.birdvoice.app.MainApp
import by.dis.birdvoice.databinding.ActivityLaunchBinding
import by.dis.birdvoice.helpers.utils.LoginManager
import by.dis.birdvoice.launch.vm.LaunchVM
import by.dis.birdvoice.main.MainActivity

@SuppressLint("CustomSplashScreen")
class LaunchActivity : BaseActivity() {

    private lateinit var binding: ActivityLaunchBinding
    private lateinit var controller: WindowInsetsControllerCompat
    private lateinit var loginManager: LoginManager

    private val mainApp = MainApp()
    private val launchVM: LaunchVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)

        launchVM.title.observe(this) { title ->
            binding.launchTitle.text = title
        }

        mainApp.setContext(this@LaunchActivity)

        launchVM.activityBinding = binding
        setContentView(binding.root)

        loginManager = LoginManager(mainApp.getContext())

        controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
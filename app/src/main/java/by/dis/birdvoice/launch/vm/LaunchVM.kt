package by.dis.birdvoice.launch.vm

import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import by.dis.birdvoice.R
import by.dis.birdvoice.app.MainApp
import by.dis.birdvoice.databinding.ActivityLaunchBinding
import kotlinx.coroutines.*

class LaunchVM: ViewModel() {

    private lateinit var mainApp: MainApp
    fun setMainApp(app: MainApp) { mainApp = app }

    //Activity Elements
    var boolPopBack = true
    var boolArrowHide = false
    var activityBinding: ActivityLaunchBinding? = null
    fun showTop(){
        val arrowAnim = AnimationUtils.loadAnimation(mainApp.getContext(), R.anim.common_left_obj_enter)
        activityBinding?.apply {
            launcherArrowBack.startAnimation(arrowAnim)
            launcherArrowBack.visibility = View.VISIBLE
        }
    }
    fun hideTop(){
        val arrowAnim = AnimationUtils.loadAnimation(mainApp.getContext(), R.anim.common_left_obj_out)
        activityBinding?.apply {
            launcherArrowBack.startAnimation(arrowAnim)
            launcherArrowBack.visibility = View.INVISIBLE
        }
    }
    fun setArrowAction(action: () -> Unit){
        activityBinding?.launcherArrowBack?.setOnClickListener {
            action()
            activityBinding!!.launcherArrowBack.isClickable = false
        }
    }

    //NavController set
    private lateinit var navController: NavController
    fun setNavController(controller: NavController){
        navController = controller
    }
    fun navigate(address: Int){
        navController.navigate(address)
    }
    fun navigateToWithDelay(address: Int){
        scope.launch {
            delay(600)
            navigate(address)
        }
    }
    fun navigateUpWithDelay(){
        scope.launch {
            delay(600)
            navController.popBackStack()
        }
    }

    //Scope
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    fun getScope() = scope

    //OnBackPressedCallback
    val onMapBackPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            if (activityBinding != null && boolArrowHide){
                hideTop()
            }
            navUpAnimLambda()
            navigateUpWithDelay()
        }
    }
    private var navUpAnimLambda = {}
    fun setNavUpAnimLambda(anim: () -> Unit){
        navUpAnimLambda = anim
    }
}
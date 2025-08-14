package by.dis.birdvoice.main.vm

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import by.dis.birdvoice.R
import by.dis.birdvoice.client.loginization.LogoutClient
import by.dis.birdvoice.databinding.ActivityMainBinding
import by.dis.birdvoice.databinding.DialogLanguageBinding
import by.dis.birdvoice.db.objects.RecognizedBird
import by.dis.birdvoice.helpers.dataStore
import by.dis.birdvoice.helpers.utils.DialogCommonInitiator
import by.dis.birdvoice.launch.LaunchActivity
import by.dis.birdvoice.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainVM : ViewModel() {

    //Activity elements
    var activityBinding: ActivityMainBinding? = null
    private val toolbarTitle = MutableLiveData<String>()
    fun setToolbarTitle(title: String) {
        toolbarTitle.value = title
    }

    fun setToolbarTitleObserver(
        toolbarTitleView: TextView,
        activity: MainActivity
    ) {
        toolbarTitle.observe(activity) {
            toolbarTitleView.text = it
        }
    }

    fun setupDrawer(binding: ActivityMainBinding, activity: MainActivity) {
        binding.apply {
            drawerButtonLanguage.setOnClickListener {
                DialogCommonInitiator().initDialog(activity) { dialog ->
                    val dialogBinding = DialogLanguageBinding.inflate(LayoutInflater.from(activity))
                    dialog.setContentView(dialogBinding.root)

                    dialogBinding.apply {
                        dialogLangButtonBy.setOnClickListener {
                            initDrawerLanguageDialogAction(
                                "be",
                                activity,
                                dialog
                            )
                        }
                        dialogLangButtonEn.setOnClickListener {
                            initDrawerLanguageDialogAction(
                                "en",
                                activity,
                                dialog
                            )
                        }
                        dialogLangButtonRu.setOnClickListener {
                            initDrawerLanguageDialogAction(
                                "ru",
                                activity,
                                dialog
                            )
                        }
                    }
                }
            }

            drawerButtonInstruction.setOnClickListener {
                initDrawerButtonAction(
                    R.id.informPageFragment,
                    activity
                )
            }

            drawerButtonFeedback.setOnClickListener {
                initDrawerButtonAction(
                    R.id.feedbackFragment,
                    activity
                )
            }

            drawerButtonLogOut.setOnClickListener {
                val dialogLogOutLanguageArray = arrayListOf(
                    ContextCompat.getString(activity, R.string.sign_out),
                    ContextCompat.getString(activity, R.string.are_you_sure_you_want_to_log_out),
                    ContextCompat.getString(activity, R.string.dialog_cancel),
                    ContextCompat.getString(activity, R.string.yes)
                )
                if (refresh == "firebase_refresh") {
                    DialogCommonInitiator().initCommonDialog(activity, dialogLogOutLanguageArray, {
                        it.dismiss()
                        activity.closeDrawer()
                        Log.d("Logout refreshToken", "refreshToken 000")
                        try {
                            activity.getLoginManager().removeTokens()
                            intentBack(activity)
                        } catch (e: Exception) {
                            Log.d("Account throw Google exception", e.message.toString())
                            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    DialogCommonInitiator().initCommonDialog(activity, dialogLogOutLanguageArray, {
                        it.dismiss()
                        activity.closeDrawer()
                        LogoutClient().logOut(refresh, {
                            activity.getLoginManager().removeTokens()
                            intentBack(activity)
                        }, { message ->
                            activity.runOnUiThread {
                                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    })
                }
            }

            drawerButtonDelete.setOnClickListener {
                val dialogLogOutLanguageArray = arrayListOf(
                    ContextCompat.getString(activity, R.string.delete),
                    ContextCompat.getString(activity, R.string.are_you_sure_you_want_to_delete),
                    ContextCompat.getString(activity, R.string.dialog_cancel),
                    ContextCompat.getString(activity, R.string.yes)
                )

                DialogCommonInitiator().initCommonDialog(activity, dialogLogOutLanguageArray, {
                    it.dismiss()
                    activity.closeDrawer()

                    LogoutClient().deleteUser(accountId!!, refresh, {
                        activity.getLoginManager().removeTokens()
                        intentBack(activity)
                    }, { message ->
                        activity.runOnUiThread {
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                        }
                    })
                })
            }
        }
    }

    private fun initDrawerButtonAction(route: Int, activity: MainActivity) {
        navController.navigate(route)
        activity.hideBottomNav()
        activity.closeDrawer()
    }

    private fun initDrawerLanguageDialogAction(
        language: String,
        activity: MainActivity,
        dialog: Dialog
    ) {
        dialog.dismiss()
        activity.closeDrawer()
        savePreferences(language, activity)
    }

    private fun intentBack(activity: MainActivity) {
        val intent = Intent(activity, LaunchActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    private fun savePreferences(locale: String, activity: MainActivity) {
        activity.lifecycleScope.launch {
            activity.dataStore.edit {
                it[stringPreferencesKey(activity.getApp().constLocale)] = locale
            }

            activity.recreate()
        }
    }

    //Fragment elements
    val recognition2Value = MutableLiveData<Boolean>()
    var feedbackValue = 0

    //NavController set
    private lateinit var navController: NavController
    fun setNavController(controller: NavController) {
        navController = controller
    }

    fun navigateToWithDelay(address: Int) {
        scope.launch {
            delay(600)
            navController.navigate(address)
        }
    }

    fun navigateToWithDelay(address: Int, bundle: Bundle) {
        scope.launch {
            delay(600)
            navController.navigate(address, bundle)
        }
    }

    fun navigateUpWithDelay() {
        scope.launch {
            delay(600)
            navController.popBackStack()
        }
    }

    fun navigateUpDelay() {
        navController.popBackStack()
    }

    //Scope
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    fun getScope() = scope

    //OnBackPressedCallback
    val onMapBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navUpAnimLambda()
            navigateUpWithDelay()
        }
    }
    private var navUpAnimLambda = {}
    fun setNavUpLambda(anim: () -> Unit) {
        navUpAnimLambda = anim
    }

    //AudioRecord
    private var tempAudioFile: File? = null
    fun getAudioFile() = tempAudioFile
    fun setAudioFile(file: File) {
        tempAudioFile = file
    }

    //AudioPick
    val observableFileToken = MutableLiveData<Boolean>()
    private var uri: Uri? = null
    fun setUri(uri: Uri? = null) {
        this.uri = uri
    }

    fun getUri() = uri

    //Token
    private var access: String = ""
    private var refresh: String = ""
    private var accountId: Int? = 0
    fun getAccessToken() = access
    fun setTokens(access: String, refresh: String, accountId: Int?) {
        this.access = access
        this.refresh = refresh
        this.accountId = accountId
    }

    //Results
    private var listOfResults: ArrayList<RecognizedBird> = arrayListOf()
    fun getResults(): ArrayList<RecognizedBird> {
        val list = arrayListOf<RecognizedBird>()
        return if (listOfResults.size > 3) {
            for (i in 0 until 3) list.add(listOfResults[i])
            list
        } else listOfResults
    }

    fun setList(list: ArrayList<RecognizedBird>) {
        listOfResults = list
    }

    val isCollectionEmptyInt = MutableLiveData<Int>()

    fun createMutableLiveInt() = MutableLiveData<Int>()
}
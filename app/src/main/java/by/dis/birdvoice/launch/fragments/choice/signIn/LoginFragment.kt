package by.dis.birdvoice.launch.fragments.choice.signIn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import by.dis.birdvoice.R
import by.dis.birdvoice.client.loginization.LoginClient
import by.dis.birdvoice.databinding.FragmentLoginBinding
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.launch.fragments.BaseLaunchFragment

class LoginFragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentLoginBinding
    override lateinit var arrayOfViews: ArrayList<ViewObject>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginBinding.inflate(layoutInflater)
        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(loginWelcomeText),
                ViewObject(loginBottomLeftCloud, "lc1"),
                ViewObject(loginBottomRightCloud, "rc2"),
                ViewObject(loginTopRightCloud, "rc1"),
                ViewObject(loginBird),
                ViewObject(loginUsernameTitle),
                ViewObject(loginUsernameInput),
                ViewObject(loginPasswordTitle),
                ViewObject(loginPasswordInput),
                ViewObject(loginShowPasswordButton),
                ViewObject(loginRememberMe),
                ViewObject(loginSignInButton)
            )
        }

        animationUtils.commonDefineObjectsVisibility(arrayOfViews)
        animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews, true)

        binding.loginUsernameInput.filters = helpFunctions.getLoginFilters()
        binding.loginPasswordInput.filters = helpFunctions.getPasswordFilters()

        if (launchVM.boolPopBack) launchVM.showTop()
        binding.loginBird.animation.setAnimationListener(helpFunctions.createAnimationEndListener {
            launchVM.setArrowAction {
                navigationBackAction {
                    animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
                    launchVM.hideTop()
                    errorViewOut(checkLogin = true, checkPassword = true)
                }
            }

            binding.loginSignInButton.setOnClickListener {
                checkLogin {
                    val login = binding.loginUsernameInput.text.toString()
                    val password = binding.loginPasswordInput.text.toString()
                    LoginClient.post(login, password, { access, refresh, username, id ->
                        if (binding.loginRememberMe.isChecked) activityLaunch.getLoginManager().saveTokens(login, password)
                        activityLaunch.runOnUiThread {
                            activityLaunch.moveToMainActivity(recognitionToken = access, refreshToken = refresh, username = username, accountId = id)
                        }
                    }, { helpFunctions.checkLoginInput(binding.loginUsernameInput, binding.loginUsernameErrorMessage, it, activityLaunch, binding) })
                }
            }
        })

        helpFunctions.controlPopBack(launchVM, true)

        binding.loginShowPasswordButton.setOnClickListener {
            helpFunctions.setPasswordShowButtonAction(binding.loginPasswordInput, binding.loginShowPasswordButton)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.loginMain.setOnClickListener { helpFunctions.hideKeyboard(binding.root, activityLaunch) }

        activityLaunch.setPopBackCallback {
            animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
            errorViewOut(checkLogin = true, checkPassword = true)
        }
    }

    private fun checkLogin(onSuccess: () -> Unit){
        var errorValue = 0

        setEditTextListeners()

        errorValue += helpFunctions.checkLoginInput(binding.loginUsernameInput, binding.loginUsernameErrorMessage, activity = activityLaunch, binding = binding)
        errorValue += helpFunctions.checkPasswordInput(binding.loginPasswordInput, binding.loginPasswordErrorMessage, resources, activityLaunch)

        if (errorValue == 0) onSuccess()
    }

    private fun errorViewOut(checkLogin: Boolean = false, checkPassword: Boolean = false){
        if (checkLogin) helpFunctions.checkErrorViewAvailability(binding.loginUsernameErrorMessage)
        if (checkPassword) helpFunctions.checkErrorViewAvailability(binding.loginPasswordErrorMessage)
    }

    private fun setEditTextListeners(){
        binding.loginUsernameInput.addTextChangedListener(helpFunctions.createEditTextListener ({
            errorViewOut(checkLogin = true)
            binding.loginUsernameInput.setTextColor(ContextCompat.getColor(activityLaunch, R.color.primary_blue)) }, {}))
        binding.loginPasswordInput.addTextChangedListener(helpFunctions.createEditTextListener ({
            errorViewOut(checkPassword = true)
            binding.loginPasswordInput.setTextColor(ContextCompat.getColor(activityLaunch, R.color.primary_blue)) }, {}))
    }
}
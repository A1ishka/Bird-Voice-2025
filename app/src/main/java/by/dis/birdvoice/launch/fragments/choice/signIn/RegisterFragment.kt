package by.dis.birdvoice.launch.fragments.choice.signIn

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import by.dis.birdvoice.R
import by.dis.birdvoice.client.loginization.LoginClient
import by.dis.birdvoice.client.loginization.RegistrationClient
import by.dis.birdvoice.databinding.FragmentRegisterBinding
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.launch.fragments.BaseLaunchFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterFragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentRegisterBinding
    override lateinit var arrayOfViews: ArrayList<ViewObject>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRegisterBinding.inflate(layoutInflater)
        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(registerBird),
                ViewObject(registerBottomLeftCloud, "lc2"),
                ViewObject(registerTopRightCloud, "rc1"),
                ViewObject(registerBottomRightCloud, "rc2"),
                ViewObject(registerNewAccountText),
                ViewObject(registerUsernameTitle),
                ViewObject(registerUsernameInput),
                ViewObject(registerPasswordTitle),
                ViewObject(registerPasswordInput),
                ViewObject(registerShowPasswordButton),
                ViewObject(registerCreateButton),
                ViewObject(registerPrivacyPolicy)
            )

            registerUsernameInput.filters = helpFunctions.getLoginFilters()
            registerPasswordInput.filters = helpFunctions.getPasswordFilters()

            registerPrivacyPolicy.movementMethod = LinkMovementMethod.getInstance()
        }

        animationUtils.commonDefineObjectsVisibility(arrayOfViews)
        animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews, true)

        if (launchVM.boolPopBack) {
            launchVM.showTop()
        }
        binding.registerBird.animation.setAnimationListener(helpFunctions.createAnimationEndListener {
            launchVM.setArrowAction {
                navigationBackAction {
                    animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
                    launchVM.hideTop()
                    errorViewOut(checkUsername = true, checkPassword = true)
                }
            }

            binding.registerCreateButton.setOnClickListener {
                checkRegister {
                    RegistrationClient.post(binding.registerUsernameInput.text.toString(), binding.registerPasswordInput.text.toString(), {
                        //OnSuccess
                        LoginClient.post(binding.registerUsernameInput.text.toString(), binding.registerPasswordInput.text.toString(), { access, refresh, username, id ->
                            launchVM.getScope().launch {
                                delay(200)
                                binding.registerCreateButton.isClickable = false
                                launchVM.activityBinding?.launcherArrowBack?.isClickable = false
                                animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
                                activityLaunch.moveToMainActivity(recognitionToken = access, refreshToken = refresh, username = username, accountId = id)
                            } }, { activityLaunch.runOnUiThread { Toast.makeText(activityLaunch, it, Toast.LENGTH_SHORT).show() } })
                    }, {
                        helpFunctions.checkLoginInput(binding.registerUsernameInput, binding.registerUsernameErrorMessage, it, activityLaunch, binding)
                    })
                }
            }
        })

        helpFunctions.controlPopBack(launchVM, true)

        binding.registerShowPasswordButton.setOnClickListener { helpFunctions.setPasswordShowButtonAction(binding.registerPasswordInput, binding.registerShowPasswordButton) }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.registerMain.setOnClickListener { helpFunctions.hideKeyboard(binding.root, activityLaunch) }

        activityLaunch.setPopBackCallback {
            animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
            errorViewOut(checkUsername = true, checkPassword = true)
        }
    }

    private fun checkRegister(onSuccess: () -> Unit){
        var errorValue = 0

        setEditTextListeners()

        errorValue += helpFunctions.checkLoginInput(binding.registerUsernameInput, binding.registerUsernameErrorMessage, activity = activityLaunch, binding = binding)
        errorValue += helpFunctions.checkPasswordInput(binding.registerPasswordInput, binding.registerPasswordErrorMessage, resources, activityLaunch)

        if (errorValue == 0) onSuccess()
    }

    private fun errorViewOut(checkUsername: Boolean = false, checkPassword: Boolean = false){
        if (checkUsername) helpFunctions.checkErrorViewAvailability(binding.registerUsernameErrorMessage)
        if (checkPassword) helpFunctions.checkErrorViewAvailability(binding.registerPasswordErrorMessage)
    }

    private fun setEditTextListeners(){
        binding.registerUsernameInput.addTextChangedListener(helpFunctions.createEditTextListener ({
            errorViewOut(checkUsername = true)
            binding.registerUsernameInput.setTextColor(ContextCompat.getColor(activityLaunch, R.color.primary_blue)) }, {}))
        binding.registerPasswordInput.addTextChangedListener(helpFunctions.createEditTextListener ({
            errorViewOut(checkPassword = true)
            binding.registerPasswordInput.setTextColor(ContextCompat.getColor(activityLaunch, R.color.primary_blue)) }, {}))
    }
}
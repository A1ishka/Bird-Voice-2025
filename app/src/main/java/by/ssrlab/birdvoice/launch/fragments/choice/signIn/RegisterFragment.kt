package by.ssrlab.birdvoice.launch.fragments.choice.signIn

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import by.ssrlab.birdvoice.R
import by.ssrlab.birdvoice.client.loginization.LoginClient
import by.ssrlab.birdvoice.client.loginization.RegistrationClient
import by.ssrlab.birdvoice.databinding.FragmentRegisterBinding
import by.ssrlab.birdvoice.helpers.utils.ViewObject
import by.ssrlab.birdvoice.launch.fragments.BaseLaunchFragment
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
                ViewObject(registerSignUpText),
                ViewObject(registerUsernameTitle),
                ViewObject(registerUsernameInput),
                ViewObject(registerPasswordTitle),
                ViewObject(registerPasswordInput),
                ViewObject(registerShowPasswordButton),
                ViewObject(registerCreateButton),
                ViewObject(registerPrivacyPolicy)
            )

            registerUsernameInput.filters = helpFunctions.editTextFilters
            registerPasswordInput.filters = helpFunctions.editTextFilters

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
                        LoginClient.post(binding.registerUsernameInput.text.toString(), binding.registerPasswordInput.text.toString(), {
                            launchVM.getScope().launch {
                                delay(200)
                                binding.registerCreateButton.isClickable = false
                                launchVM.activityBinding?.launcherArrowBack?.isClickable = false
                                animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
                                activityLaunch.moveToMainActivity(recognitionToken = it)
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

    override fun onResume() {
        super.onResume()

        activityLaunch.setPopBackCallback {
            animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
            errorViewOut(checkUsername = true, checkPassword = true)
        }
    }

    private fun checkRegister(onSuccess: () -> Unit){
        var errorValue = 0

        setEditTextListeners()

        errorValue += helpFunctions.checkLoginInput(binding.registerUsernameInput, binding.registerUsernameErrorMessage, activity = activityLaunch, binding = binding)
        errorValue += helpFunctions.checkPasswordInput(binding.registerPasswordInput.text, binding.registerPasswordErrorMessage, resources)

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
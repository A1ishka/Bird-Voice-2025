package by.dis.birdvoice.launch.fragments.choice.signIn

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import by.dis.birdvoice.R
import by.dis.birdvoice.client.loginization.LoginClient
import by.dis.birdvoice.client.loginization.RegistrationClient
import by.dis.birdvoice.databinding.FragmentLoginBinding
import by.dis.birdvoice.helpers.utils.CustomToast
import by.dis.birdvoice.helpers.utils.FIREBASE_CLIENT_ID
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.launch.fragments.BaseLaunchFragment
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginFragment : BaseLaunchFragment() {

    private lateinit var binding: FragmentLoginBinding
    override lateinit var arrayOfViews: ArrayList<ViewObject>
    private lateinit var credentialManager: CredentialManager
    private lateinit var googleIdOption: GetGoogleIdOption

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        credentialManager = CredentialManager.create(requireContext())

        googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(FIREBASE_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()

        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(loginBottomLeftCloud, "lc1"),
                ViewObject(loginBottomRightCloud, "rc2"),
                ViewObject(loginTopRightCloud, "rc1"),
                ViewObject(loginGoogleClickable),
                ViewObject(loginBird),
                ViewObject(loginEmailTitle),
                ViewObject(loginEmailInput),
                ViewObject(loginPasswordTitle),
                ViewObject(loginPasswordInput),
                ViewObject(loginShowPasswordButton),
                ViewObject(loginRememberMe),
                ViewObject(loginSignInButton)
            )
        }

        animationUtils.commonDefineObjectsVisibility(arrayOfViews)
        animationUtils.commonObjectAppear(
            activityLaunch.getApp().getContext(),
            arrayOfViews,
            true
        )

        binding.loginEmailInput.filters = helpFunctions.getLoginFilters()
        binding.loginPasswordInput.filters = helpFunctions.getPasswordFilters()

        launchVM.setTitle(getString(R.string.welcome_back))

        if (launchVM.boolPopBack) {
            launchVM.showTopTitle()
            launchVM.showTop()
        }

        binding.loginBird.animation.setAnimationListener(
            helpFunctions.createAnimationEndListener {
                launchVM.setArrowAction {
                    navigationBackAction {
                        animationUtils.commonObjectAppear(
                            activityLaunch.getApp().getContext(),
                            arrayOfViews
                        )
                        launchVM.hideTopTitle()
                        launchVM.hideTop()
                        errorViewOut(checkLogin = true, checkPassword = true)
                    }
                }

                binding.loginSignInButton.setOnClickListener {
                    checkLogin {
                        val login = binding.loginEmailInput.text.toString()
                        val password = binding.loginPasswordInput.text.toString()
                        LoginClient.post(
                            login,
                            password,
                            { access, refresh, email, id ->
                                if (binding.loginRememberMe.isChecked) {
                                    activityLaunch.getLoginManager()
                                        .saveTokens(login, password)
                                }
                                activityLaunch.runOnUiThread {
                                    activityLaunch.moveToMainActivity(
                                        recognitionToken = access,
                                        refreshToken = refresh,
                                        email = email,
                                        accountId = id
                                    )
                                }
                            },
                            {
                                helpFunctions.checkLoginInput(
                                    binding.loginEmailInput,
                                    binding.loginEmailErrorMessage,
                                    it,
                                    activityLaunch,
                                    binding
                                )
                            }
                        )
                    }
                }
            }
        )

        helpFunctions.controlPopBack(launchVM, true)

        binding.loginGoogleClickable.setOnClickListener {
            signInWithGoogle()
        }

        binding.loginShowPasswordButton.setOnClickListener {
            helpFunctions.setPasswordShowButtonAction(
                binding.loginPasswordInput,
                binding.loginShowPasswordButton
            )
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.loginMain.setOnClickListener {
            helpFunctions.hideKeyboard(
                binding.root,
                activityLaunch
            )
        }

        activityLaunch.setPopBackCallback {
            animationUtils.commonObjectAppear(
                activityLaunch.getApp().getContext(),
                arrayOfViews
            )
            errorViewOut(checkLogin = true, checkPassword = true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun signInWithGoogle() {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext()
                )
                handleCredentialResult(result)
            } catch (e: GetCredentialException) {
                Log.d("GoogleSignIn", "GetCredentialException: ${e.message}")
                CustomToast.show(
                    requireContext(),
                    getString(R.string.google_sign_in_cancelled)
                )
            } catch (e: Exception) {
                Log.d("GoogleSignIn", "Exception: ${e.message}")
                CustomToast.show(
                    requireContext(),
                    getString(R.string.google_sign_in_cancelled)
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun handleCredentialResult(result: GetCredentialResponse) {
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
                ) {
                    val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleCred.idToken

                    firebaseAuthWithGoogle(idToken, googleCred)
                } else {
                    Log.d(
                        "GoogleSignIn",
                        "Unexpected custom credential type: ${credential.type}"
                    )
                    CustomToast.show(
                        requireContext(),
                        getString(R.string.google_sign_in_cancelled)
                    )
                }
            }

            else -> {
                Log.d("GoogleSignIn", "Unsupported credential: $credential")
                CustomToast.show(
                    requireContext(),
                    getString(R.string.google_sign_in_cancelled)
                )
            }
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String,
        googleCred: GoogleIdTokenCredential
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->

                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser

                    createUserInCommonDB(googleCred)

                    val accountId = try {
                        user?.uid?.toInt()
                    } catch (_: NumberFormatException) {
                        Log.d("NumberFormatException", "NumberFormatException")
                        null
                    }

                    activityLaunch.moveToMainActivity(
                        recognitionToken = "firebase_token",
                        refreshToken = "firebase_refresh",
                        email = user?.email ?: "",
                        accountId = accountId ?: 0
                    )
                } else {
                    Log.d(
                        "FirebaseAuthWithGoogle",
                        "exception: ${task.exception?.message}"
                    )
                    CustomToast.show(
                        requireContext(),
                        getString(R.string.google_sign_in_cancelled)
                    )
                }
            }
    }

    private fun createUserInCommonDB(googleCred: GoogleIdTokenCredential) {
        val email = googleCred.id
        var password = "123456789AA"
        try {
            password =
                googleCred.idToken.takeLast(8) +
                        (googleCred.profilePictureUri?.toString()?.takeLast(8) ?: "") +
                        (googleCred.familyName?.takeLast(8) ?: "")
        } catch (e: Exception) {
            Log.d("Create user from Firebase Exception", e.message.toString())
        }

        try {
            RegistrationClient.post(email, password, {}, {})
        } catch (e: Exception) {
            Log.d(
                "Google account not added in DB",
                e.localizedMessage?.toString() ?: ""
            )
        }
    }

    private fun checkLogin(onSuccess: () -> Unit) {
        var errorValue = 0

        setEditTextListeners()

        errorValue += helpFunctions.checkLoginInput(
            binding.loginEmailInput,
            binding.loginEmailErrorMessage,
            activity = activityLaunch,
            binding = binding
        )
        errorValue += helpFunctions.checkPasswordInputForLogin(
            binding.loginPasswordInput,
            binding.loginPasswordErrorMessage,
            resources,
            activityLaunch
        )

        if (errorValue == 0) onSuccess()
    }

    private fun errorViewOut(checkLogin: Boolean = false, checkPassword: Boolean = false) {
        if (checkLogin) helpFunctions.checkErrorViewAvailability(binding.loginEmailErrorMessage)
        if (checkPassword) helpFunctions.checkErrorViewAvailability(binding.loginPasswordErrorMessage)
    }

    private fun setEditTextListeners() {
        binding.loginEmailInput.addTextChangedListener(
            helpFunctions.createEditTextListener(onTextChangedFun = {
                errorViewOut(checkLogin = true)
                binding.loginEmailInput.setTextColor(
                    ContextCompat.getColor(
                        activityLaunch,
                        R.color.primary_blue
                    )
                )
            }, afterTextChangedFun = {})
        )
        binding.loginPasswordInput.addTextChangedListener(
            helpFunctions.createEditTextListener(onTextChangedFun = {
                errorViewOut(checkPassword = true)
                binding.loginPasswordInput.setTextColor(
                    ContextCompat.getColor(
                        activityLaunch,
                        R.color.primary_blue
                    )
                )
            }, afterTextChangedFun = {})
        )
    }
}
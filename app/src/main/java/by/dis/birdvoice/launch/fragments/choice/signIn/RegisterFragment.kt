package by.dis.birdvoice.launch.fragments.choice.signIn

import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
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
import by.dis.birdvoice.databinding.FragmentRegisterBinding
import by.dis.birdvoice.helpers.utils.CustomToast
import by.dis.birdvoice.helpers.utils.FIREBASE_CLIENT_ID
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.launch.fragments.BaseLaunchFragment
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class RegisterFragment : BaseLaunchFragment() {

    private val registerOnce = AtomicBoolean(false)

    private lateinit var binding: FragmentRegisterBinding
    override lateinit var arrayOfViews: ArrayList<ViewObject>

    private lateinit var credentialManager: CredentialManager
    private lateinit var googleIdOption: GetGoogleIdOption

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        credentialManager = CredentialManager.create(requireContext())
        googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(FIREBASE_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()

        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(registerBird),
                ViewObject(registerBottomLeftCloud, "lc2"),
                ViewObject(registerTopRightCloud, "rc1"),
                ViewObject(registerBottomRightCloud, "rc2"),
                ViewObject(registerGoogleClickable),
                ViewObject(registerEmailTitle),
                ViewObject(registerEmailInput),
                ViewObject(registerPasswordTitle),
                ViewObject(registerPasswordInput),
                ViewObject(registerShowPasswordButton),
                ViewObject(registerCreateButton),
                ViewObject(registerPrivacyPolicy)
            )

            registerEmailInput.filters = helpFunctions.getLoginFilters()
            registerPasswordInput.filters = helpFunctions.getPasswordFilters()

            registerPrivacyPolicy.movementMethod = LinkMovementMethod.getInstance()
        }

        animationUtils.commonDefineObjectsVisibility(arrayOfViews)
        animationUtils.commonObjectAppear(
            activityLaunch.getApp().getContext(),
            arrayOfViews,
            true
        )

        launchVM.setTitle(getString(R.string.new_account))

        if (launchVM.boolPopBack) {
            launchVM.showTopTitle()
            launchVM.showTop()
        }

        binding.registerBird.animation.setAnimationListener(
            helpFunctions.createAnimationEndListener {
                launchVM.setArrowAction {
                    navigationBackAction {
                        animationUtils.commonObjectAppear(
                            activityLaunch.getApp().getContext(),
                            arrayOfViews
                        )
                        launchVM.hideTopTitle()
                        launchVM.hideTop()
                        errorViewOut(checkEmail = true, checkPassword = true)
                    }
                }

                binding.registerGoogleClickable.setOnClickListener {
                    signInWithGoogle()
                }

                binding.registerCreateButton.setOnClickListener {
                    if (!registerOnce.compareAndSet(false, true)) return@setOnClickListener

                    checkRegister {
                        RegistrationClient.post(
                            binding.registerEmailInput.text.toString(),
                            binding.registerPasswordInput.text.toString(),
                            {
                                LoginClient.post(
                                    binding.registerEmailInput.text.toString(),
                                    binding.registerPasswordInput.text.toString(),
                                    once4 { access, refresh, email, id ->
                                        lifecycleScope.launch {
                                            withContext(Dispatchers.Main) {
                                                animationUtils.commonObjectAppear(
                                                    activityLaunch.getApp().getContext(),
                                                    arrayOfViews
                                                )
                                                activityLaunch.moveToMainActivity(
                                                    recognitionToken = access,
                                                    refreshToken = refresh,
                                                    email = email,
                                                    accountId = id
                                                )
                                            }
                                        }
                                    },
                                    once1 { error ->
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            CustomToast.show(activityLaunch, error)
                                            registerOnce.set(false)
                                        }
                                    }
                                )
                            },
                            { registrationError ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    helpFunctions.checkLoginInput(
                                        binding.registerEmailInput,
                                        binding.registerEmailErrorMessage,
                                        registrationError,
                                        activityLaunch,
                                        binding
                                    )
                                    registerOnce.set(false)
                                }
                            }
                        )
                    }
                    lifecycleScope.launch {
                        delay(1500)
                        registerOnce.set(false)
                    }
                }
            }
        )

        helpFunctions.controlPopBack(launchVM, true)
        binding.registerShowPasswordButton.setOnClickListener {
            helpFunctions.setPasswordShowButtonAction(
                binding.registerPasswordInput,
                binding.registerShowPasswordButton
            )
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.registerMain.setOnClickListener {
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
            errorViewOut(checkEmail = true, checkPassword = true)
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

    private fun checkRegister(onSuccess: () -> Unit) {
        var errorValue = 0

        setEditTextListeners()

        errorValue += helpFunctions.checkLoginInput(
            binding.registerEmailInput,
            binding.registerEmailErrorMessage,
            activity = activityLaunch,
            binding = binding
        )
        errorValue += helpFunctions.checkPasswordInput(
            binding.registerPasswordInput,
            binding.registerPasswordErrorMessage,
            resources,
            activityLaunch
        )

        if (errorValue == 0) onSuccess()
    }

    private fun errorViewOut(checkEmail: Boolean = false, checkPassword: Boolean = false) {
        if (checkEmail) helpFunctions.checkErrorViewAvailability(binding.registerEmailErrorMessage)
        if (checkPassword) helpFunctions.checkErrorViewAvailability(binding.registerPasswordErrorMessage)
    }

    private fun setEditTextListeners() {
        binding.registerEmailInput.addTextChangedListener(
            helpFunctions.createEditTextListener(onTextChangedFun = {
                errorViewOut(checkEmail = true)
                binding.registerEmailInput.setTextColor(
                    ContextCompat.getColor(
                        activityLaunch,
                        R.color.primary_blue
                    )
                )
            }, afterTextChangedFun = {})
        )
        binding.registerPasswordInput.addTextChangedListener(
            helpFunctions.createEditTextListener(onTextChangedFun = {
                errorViewOut(checkPassword = true)
                binding.registerPasswordInput.setTextColor(
                    ContextCompat.getColor(
                        activityLaunch,
                        R.color.primary_blue
                    )
                )
            }, afterTextChangedFun = {})
        )
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

    private fun <A, B, C, D> once4(fn: (A, B, C, D) -> Unit): (A, B, C, D) -> Unit {
        val fired = AtomicBoolean(false)
        return { a, b, c, d -> if (fired.compareAndSet(false, true)) fn(a, b, c, d) }
    }

    private fun <T> once1(fn: (T) -> Unit): (T) -> Unit {
        val fired = AtomicBoolean(false)
        return { x -> if (fired.compareAndSet(false, true)) fn(x) }
    }
}
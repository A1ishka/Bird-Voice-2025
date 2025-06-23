package by.dis.birdvoice.launch.fragments.choice.signIn

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import by.dis.birdvoice.R
import by.dis.birdvoice.client.loginization.LoginClient
import by.dis.birdvoice.client.loginization.RegistrationClient
import by.dis.birdvoice.databinding.FragmentLoginBinding
import by.dis.birdvoice.helpers.utils.FIREBASE_CLIENT_ID
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.launch.fragments.BaseLaunchFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : BaseLaunchFragment() {

    private lateinit var binding: FragmentLoginBinding
    override lateinit var arrayOfViews: ArrayList<ViewObject>

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account.idToken!!)

                        createUserInCommonDB(account)
                    } catch (e: ApiException) {
                        Toast.makeText(
                            requireContext(),
                            "Google sign in failed: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.d(
                        "GoogleSignIn",
                        "Result code: ${result.resultCode}, Intent: ${result.data}"
                    )
                    Toast.makeText(requireContext(), "Google sign-in cancelled", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(FIREBASE_CLIENT_ID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding = FragmentLoginBinding.inflate(layoutInflater)
        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(loginWelcomeText),
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
        animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews, true)

        binding.loginEmailInput.filters = helpFunctions.getLoginFilters()
        binding.loginPasswordInput.filters = helpFunctions.getPasswordFilters()

        if (launchVM.boolPopBack) launchVM.showTop()
        binding.loginBird.animation.setAnimationListener(helpFunctions.createAnimationEndListener {
            launchVM.setArrowAction {
                navigationBackAction {
                    animationUtils.commonObjectAppear(
                        activityLaunch.getApp().getContext(),
                        arrayOfViews
                    )
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
                            if (binding.loginRememberMe.isChecked) activityLaunch.getLoginManager()
                                .saveTokens(login, password)
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
                        })
                }
            }
        })

        helpFunctions.controlPopBack(launchVM, true)

        binding.loginGoogleClickable.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
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
            animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
            errorViewOut(checkLogin = true, checkPassword = true)
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
        errorValue += helpFunctions.checkPasswordInput(
            binding.loginPasswordInput,
            binding.loginPasswordErrorMessage,
            resources,
            activityLaunch
        )

        if (errorValue == 0) onSuccess()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser

                    val accountId = try {
                        user?.uid?.toInt()
                    } catch (e: NumberFormatException) {
                        Log.d("NumberFormatException", "NumberFormatException")
                    }

                    activityLaunch.moveToMainActivity(
                        recognitionToken = "firebase_token",
                        refreshToken = "firebase_refresh",
                        email = user?.email ?: "",
                        accountId = accountId ?: 0
                    )
                } else {
                    Toast.makeText(requireContext(), "Authentication Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun createUserInCommonDB(account: GoogleSignInAccount) {
        val email = account.email ?: " @ "
        var password = "123456789AA"
        try {
            password = (account.idToken?.takeLast(8) + account.photoUrl?.userInfo.toString()
                .takeLast(8) + account.familyName?.takeLast(8)) ?: email.substringBefore("@")
        } catch (e: Exception) {
            Log.d("Create user from Firebase Exception", e.message.toString())
        }

        try {
            RegistrationClient.post(email, password, {}, {})
        } catch (e: Exception) {
            Log.d(
                "Google account not added in DB", e.localizedMessage?.toString() ?: ""
            )
        }
    }

    private fun errorViewOut(checkLogin: Boolean = false, checkPassword: Boolean = false) {
        if (checkLogin) helpFunctions.checkErrorViewAvailability(binding.loginEmailErrorMessage)
        if (checkPassword) helpFunctions.checkErrorViewAvailability(binding.loginPasswordErrorMessage)
    }

    private fun setEditTextListeners() {
        binding.loginEmailInput.addTextChangedListener(helpFunctions.createEditTextListener({
            errorViewOut(checkLogin = true)
            binding.loginEmailInput.setTextColor(
                ContextCompat.getColor(
                    activityLaunch,
                    R.color.primary_blue
                )
            )
        }, {}))
        binding.loginPasswordInput.addTextChangedListener(helpFunctions.createEditTextListener({
            errorViewOut(checkPassword = true)
            binding.loginPasswordInput.setTextColor(
                ContextCompat.getColor(
                    activityLaunch,
                    R.color.primary_blue
                )
            )
        }, {}))
    }
}
package by.dis.birdvoice.launch.fragments.choice.signIn

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
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
import by.dis.birdvoice.databinding.FragmentRegisterBinding
import by.dis.birdvoice.helpers.utils.FIREBASE_CLIENT_ID
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.launch.fragments.BaseLaunchFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterFragment : BaseLaunchFragment() {

    private lateinit var binding: FragmentRegisterBinding
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
                    } catch (e: ApiException) {
                        Toast.makeText(
                            requireContext(),
                            "Google sign in failed: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.d("GoogleSignIn", "Result code: ${result.resultCode}, Intent: ${result.data}")
                    Toast.makeText(requireContext(), "Google sign-in cancelled", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(FIREBASE_CLIENT_ID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding = FragmentRegisterBinding.inflate(layoutInflater)
        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(registerBird),
                ViewObject(registerBottomLeftCloud, "lc2"),
                ViewObject(registerTopRightCloud, "rc1"),
                ViewObject(registerBottomRightCloud, "rc2"),
                ViewObject(registerNewAccountText),
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
        animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews, true)

        if (launchVM.boolPopBack) {
            launchVM.showTop()
        }
        binding.registerBird.animation.setAnimationListener(helpFunctions.createAnimationEndListener {
            launchVM.setArrowAction {
                navigationBackAction {
                    animationUtils.commonObjectAppear(
                        activityLaunch.getApp().getContext(),
                        arrayOfViews
                    )
                    launchVM.hideTop()
                    errorViewOut(checkEmail = true, checkPassword = true)
                }
            }

            binding.registerGoogleClickable.setOnClickListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }

            binding.registerCreateButton.setOnClickListener {
                checkRegister {
                    RegistrationClient.post(
                        binding.registerEmailInput.text.toString(),
                        binding.registerPasswordInput.text.toString(),
                        {
                            //OnSuccess
                            LoginClient.post(
                                binding.registerEmailInput.text.toString(),
                                binding.registerPasswordInput.text.toString(),
                                { access, refresh, email, id ->
                                    launchVM.getScope().launch {
                                        delay(200)
                                        binding.registerCreateButton.isClickable = false
                                        launchVM.activityBinding?.launcherArrowBack?.isClickable =
                                            false
                                        animationUtils.commonObjectAppear(
                                            activityLaunch.getApp().getContext(), arrayOfViews
                                        )
                                        activityLaunch.moveToMainActivity(
                                            recognitionToken = access,
                                            refreshToken = refresh,
                                            email = email,
                                            accountId = id
                                        )
                                    }
                                },
                                {
                                    activityLaunch.runOnUiThread {
                                        Toast.makeText(
                                            activityLaunch,
                                            it,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                        },
                        {
                            helpFunctions.checkLoginInput(
                                binding.registerEmailInput,
                                binding.registerEmailErrorMessage,
                                it,
                                activityLaunch,
                                binding
                            )
                        })
                }
            }
        })

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
            animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
            errorViewOut(checkEmail = true, checkPassword = true)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    activityLaunch.moveToMainActivity(
                        recognitionToken = "firebase_token",
                        refreshToken = "firebase_refresh",
                        email = user?.email ?: "",
                        accountId = user?.uid?.toInt() ?: 0
                    )
                } else {
                    Toast.makeText(requireContext(), "Authentication Failed", Toast.LENGTH_SHORT)
                        .show()
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
        binding.registerEmailInput.addTextChangedListener(helpFunctions.createEditTextListener({
            errorViewOut(checkEmail = true)
            binding.registerEmailInput.setTextColor(
                ContextCompat.getColor(
                    activityLaunch,
                    R.color.primary_blue
                )
            )
        }, {}))
        binding.registerPasswordInput.addTextChangedListener(helpFunctions.createEditTextListener({
            errorViewOut(checkPassword = true)
            binding.registerPasswordInput.setTextColor(
                ContextCompat.getColor(
                    activityLaunch,
                    R.color.primary_blue
                )
            )
        }, {}))
    }
}
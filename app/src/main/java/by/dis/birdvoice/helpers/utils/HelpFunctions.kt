package by.dis.birdvoice.helpers.utils

import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import by.dis.birdvoice.R
import by.dis.birdvoice.app.MainApp
import by.dis.birdvoice.databinding.FragmentLoginBinding
import by.dis.birdvoice.databinding.FragmentRegisterBinding
import by.dis.birdvoice.launch.LaunchActivity
import by.dis.birdvoice.launch.vm.LaunchVM
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HelpFunctions(private val mainApp: MainApp) {

    fun getLoginFilters() = arrayOf(
        NoSpaceFilter(mainApp),
        LengthFilter(mainApp.getContext().resources.getInteger(R.integer.maxLoginLength))
    )

    fun getPasswordFilters() = arrayOf(
        NoSpaceFilter(mainApp),
        LengthFilter(mainApp.getContext().resources.getInteger(R.integer.maxPasswordLength))
    )

    fun controlPopBack(launchVM: LaunchVM, bool: Boolean) {
        launchVM.boolPopBack = bool
        launchVM.boolArrowHide = bool
    }

    fun setPasswordShowButtonAction(
        passwordInput: AppCompatEditText,
        passwordShowButton: ImageButton
    ) {
        if (passwordInput.transformationMethod == PasswordTransformationMethod.getInstance()) {
            passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
            passwordShowButton.setImageResource(R.drawable.ic_launcher_hide_password)
        } else {
            passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            passwordShowButton.setImageResource(R.drawable.ic_launcher_show_password)
        }
    }

    fun checkErrorViewAvailability(errorView: TextView) {
        val errorAlphaOut =
            AnimationUtils.loadAnimation(mainApp.getContext(), R.anim.common_alpha_out)
        if (errorView.visibility == View.VISIBLE) {
            errorView.startAnimation(errorAlphaOut)
            errorView.visibility = View.INVISIBLE
        }
    }

    fun convertToTimerMode(duration: Int): String {
        val minute = duration % (1000 * 60 * 60) / (1000 * 60)
        val seconds = duration % (1000 * 60 * 60) % (1000 * 60) / 1000

        var finalString = ""
        finalString += "$minute:"
        if (seconds < 10) finalString += "0"
        finalString += "$seconds"

        return finalString
    }

    fun createEditTextListener(
        onTextChangedFun: () -> Unit,
        afterTextChangedFun: (Editable?) -> Unit
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChangedFun()
            }

            override fun afterTextChanged(s: Editable?) {
                afterTextChangedFun(s)
            }
        }
    }

    fun createAnimationEndListener(onAnimationEndFun: () -> Unit): Animation.AnimationListener {
        return object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                onAnimationEndFun()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        }
    }

    fun createSeekBarProgressListener(onProgressChangeFun: (Int) -> Unit): OnSeekBarChangeListener {
        return object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    onProgressChangeFun(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
    }

    fun checkLoginInput(
        checkableView: AppCompatEditText,
        errorView: TextView,
        errorMessage: String? = null,
        activity: LaunchActivity,
        binding: ViewBinding
    ): Int {
        val errorAnim =
            AnimationUtils.loadAnimation(activity, R.anim.common_error_message_animation)
        val emailRegex = Regex(".+@[a-zA-Z][a-zA-Z]+\\.[a-zA-Z][a-zA-Z]+")

        if (checkableView.text!!.isEmpty()) {
            errorView.text = activity.resources.getText(R.string.this_field_must_be_not_empty)
            errorView.startAnimation(errorAnim)
            errorView.visibility = View.VISIBLE
            return 1
        } else if (errorMessage != null) {
            if (binding is FragmentLoginBinding) {
                activity.runOnUiThread {
                    setErrorView(
                        binding.loginUsernameInput,
                        binding.loginUsernameErrorMessage,
                        errorMessage,
                        activity
                    )
                    setErrorView(
                        binding.loginPasswordInput,
                        binding.loginPasswordErrorMessage,
                        errorMessage,
                        activity
                    )
                }
            } else if (binding is FragmentRegisterBinding) {
                activity.runOnUiThread {
                    when (errorMessage) {
                        "Password should be at least 8 characters" ->
                            setErrorView(
                                binding.registerPasswordInput,
                                binding.registerPasswordErrorMessage,
                                errorMessage,
                                activity
                            )

                        "Username already in use" ->
                            setErrorView(
                                binding.registerUsernameInput,
                                binding.registerUsernameErrorMessage,
                                errorMessage,
                                activity
                            )

                        else -> {
                            setErrorView(
                                binding.registerUsernameInput,
                                binding.registerUsernameErrorMessage,
                                errorMessage,
                                activity
                            )
                            setErrorView(
                                binding.registerPasswordInput,
                                binding.registerPasswordErrorMessage,
                                errorMessage,
                                activity
                            )
                        }
                    }
                }
            }
            return 1
        } else if (checkableView.text!!.isNotEmpty()) {
            if (!emailRegex.matches(checkableView.text!!)) {
                if (binding is FragmentLoginBinding)
                    setErrorView(
                        binding.loginUsernameInput,
                        binding.loginUsernameErrorMessage,
                        ContextCompat.getString(activity, R.string.email_error_valid),
                        activity
                    )
                else if (binding is FragmentRegisterBinding)
                    setErrorView(
                        binding.registerUsernameInput,
                        binding.registerUsernameErrorMessage,
                        ContextCompat.getString(activity, R.string.email_error_valid),
                        activity
                    )
                return 1
            } else return 0
        } else return 0
    }

    fun checkPasswordInput(
        checkableView: AppCompatEditText,
        errorView: TextView,
        resources: Resources,
        context: Context
    ): Int {
        val errorAnim = AnimationUtils.loadAnimation(
            mainApp.getContext(),
            R.anim.common_error_message_animation
        )
        return if (checkableView.text?.isEmpty() == true) {
            val errorText = resources.getString(R.string.this_field_must_be_not_empty)

            errorView.text = errorText
            errorView.startAnimation(errorAnim)
            errorView.visibility = View.VISIBLE
            1
        } else if (checkableView.text?.length!! < 8) {
            val errorText = resources.getString(R.string.password_length_error)

            errorView.text = errorText
            errorView.startAnimation(errorAnim)
            errorView.visibility = View.VISIBLE

            checkableView.setTextColor(ContextCompat.getColor(context, R.color.primary_red))
            1
        } else 0
    }

    fun checkInput(
        checkableText: Editable?,
        errorView: TextView,
        resources: Resources,
        errorText: String = resources.getString(R.string.this_field_must_be_not_empty)
    ): Int {
        val errorAnim = AnimationUtils.loadAnimation(
            mainApp.getContext(),
            R.anim.common_error_message_animation
        )
        return if (checkableText?.isEmpty() == true) {
            errorView.text = errorText
            errorView.startAnimation(errorAnim)
            errorView.visibility = View.VISIBLE
            1
        } else 0
    }

    private fun setErrorView(
        errorInput: AppCompatEditText,
        errorView: TextView,
        errorMessage: String,
        activity: LaunchActivity
    ) {
        errorInput.setTextColor(ContextCompat.getColor(activity, R.color.primary_red))
        val errorAnim =
            AnimationUtils.loadAnimation(activity, R.anim.common_error_message_animation)

        errorView.text = errorMessage
        errorView.startAnimation(errorAnim)
        errorView.visibility = View.VISIBLE
    }

    fun hideKeyboard(view: View?, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = Date()
        return dateFormat.format(currentTime)
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    fun getAudioName(): String {
        val currentDate = Date()
        return "Bird Voice $currentDate.mp3"
    }
}
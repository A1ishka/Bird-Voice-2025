package by.dis.birdvoice.launch.fragments.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.dis.birdvoice.R
import by.dis.birdvoice.client.loginization.LoginClient
import by.dis.birdvoice.databinding.FragmentStartBinding
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.launch.fragments.BaseLaunchFragment
import kotlinx.coroutines.*

class StartFragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentStartBinding
    override var arrayOfViews = arrayListOf<ViewObject>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentStartBinding.inflate(layoutInflater)

        activityLaunch.hideStatusBar()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        launchVM.getScope().launch {
            delay(3000)
            binding.logoLottie.animate().apply {
                duration = 500
                alpha(0f)

                withEndAction {
                    activityLaunch.apply {
                        getLoginManager().getTokens { login, password ->
                            if (login.isNotEmpty() && password.isNotEmpty()) {
                                LoginClient.post(login, password, { access, refresh, username, id ->
                                    moveToMainActivity(recognitionToken = access, refreshToken = refresh, username = username, accountId = id)
                                }, {
                                    activityLaunch.runOnUiThread {
                                        launchVM.navigate(R.id.action_logoFragment_to_choiceFragment)
                                    }
                                })
                            } else {
                                activityLaunch.runOnUiThread {
                                    launchVM.navigate(R.id.action_logoFragment_to_choiceFragment)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
package by.dis.birdvoice.launch.fragments.choice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.dis.birdvoice.R
import by.dis.birdvoice.databinding.FragmentChoiceBinding
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.launch.fragments.BaseLaunchFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChoiceFragment : BaseLaunchFragment() {

    private lateinit var binding: FragmentChoiceBinding
    override lateinit var arrayOfViews: ArrayList<ViewObject>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentChoiceBinding.inflate(layoutInflater)
        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(choiceBottomLeftCloud, "lc1"),
                ViewObject(choiceTopRightCloud, "rc1"),
                ViewObject(choiceBottomRightCloud, "rc2"),
                ViewObject(choiceBird),
                ViewObject(choiceLoginButton),
                ViewObject(choiceRegisterButton)
            )
        }

        activityLaunch.showStatusBar()
        animationUtils.commonDefineObjectsVisibility(arrayOfViews)
        animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews, true)

        binding.choiceBird.animation.setAnimationListener(helpFunctions.createAnimationEndListener {
            binding.choiceLoginButton.setOnClickListener {
                launchVM.getScope().launch {
                    delay(200)
                    animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
                    binding.choiceLoginButton.isClickable = false
                    launchVM.navigateToWithDelay(R.id.action_choiceFragment_to_loginFragment)
                }
            }

            binding.choiceRegisterButton.setOnClickListener {
                launchVM.getScope().launch {
                    delay(200)
                    animationUtils.commonObjectAppear(activityLaunch.getApp().getContext(), arrayOfViews)
                    binding.choiceRegisterButton.isClickable = false
                    launchVM.navigateToWithDelay(R.id.action_choiceFragment_to_registerFragment) }
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        activityLaunch.deletePopBackCallback()
    }
}
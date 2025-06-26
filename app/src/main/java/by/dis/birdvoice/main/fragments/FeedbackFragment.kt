package by.dis.birdvoice.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.dis.birdvoice.R
import by.dis.birdvoice.databinding.FragmentFeedbackBinding
import by.dis.birdvoice.helpers.utils.ViewObject

class FeedbackFragment: BaseMainFragment() {

    private lateinit var binding: FragmentFeedbackBinding
    private lateinit var toHideArray: ArrayList<ViewObject>
    private lateinit var toShowArray: ArrayList<ViewObject>
    private lateinit var shownArray: ArrayList<ViewObject>
    override lateinit var arrayOfViews: ArrayList<ViewObject>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFeedbackBinding.inflate(layoutInflater)
        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(feedback1Bird),
                ViewObject(feedbackInput),
                ViewObject(feedbackSendButton),
                ViewObject(feedbackBottomLeftCloud, "lc1"),
                ViewObject(feedbackTopRightCloud, "rc2"),
                ViewObject(feedbackBottomRightCloud, "rc1")
            )

            toHideArray = arrayListOf(
                ViewObject(feedbackInput),
                ViewObject(feedback1Bird),
                ViewObject(feedbackSendButton)
            )

            toShowArray = arrayListOf(
                ViewObject(feedback2Bird),
                ViewObject(feedbackCongratsHolder)
            )

            shownArray = arrayListOf(
                ViewObject(feedbackBottomLeftCloud, "lc1"),
                ViewObject(feedbackTopRightCloud, "rc2"),
                ViewObject(feedbackBottomRightCloud, "rc1"),
                ViewObject(feedback2Bird),
                ViewObject(feedbackCongratsHolder)
            )
        }

        animationUtils.commonDefineObjectsVisibility(arrayOfViews)
        animationUtils.commonObjectAppear(activityMain.getApp().getContext(), arrayOfViews, true)

        activityMain.setPopBackCallback {
            if (mainVM.feedbackValue == 0) animationUtils.commonObjectAppear(activityMain.getApp().getContext(), arrayOfViews)
            else {
                animationUtils.commonObjectAppear(activityMain.getApp().getContext(), shownArray)
                mainVM.feedbackValue = 0
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.feedbackMain.setOnClickListener { helpFunctions.hideKeyboard(binding.root, activityMain) }

        mainVM.setToolbarTitle(resources.getString(R.string.feedback_title))
        activityMain.setToolbarAction(R.drawable.ic_arrow_back){
            navigationBackAction {
                if (mainVM.feedbackValue == 0) {
                    animationUtils.commonObjectAppear(activityMain.getApp().getContext(), arrayOfViews)
                    errorViewOut()
                }
                else {
                    animationUtils.commonObjectAppear(activityMain.getApp().getContext(), shownArray)
                    mainVM.feedbackValue = 0
                }
            }
        }

        binding.feedbackSendButton.setOnClickListener {
            checkInput()

            if (binding.feedbackErrorMessage.visibility == View.INVISIBLE) {
                helpFunctions.hideKeyboard(view, activityMain.getApp().getContext())

                animationUtils.commonObjectAppear(activityMain.getApp().getContext(), toHideArray)
                animationUtils.commonObjectAppear(activityMain.getApp().getContext(), toShowArray, true)
            }
        }

        binding.feedbackInput.addTextChangedListener(helpFunctions.createEditTextListener({ errorViewOut() }) {
            if (it?.isNotEmpty() == true) mainVM.feedbackValue = 1
            else mainVM.feedbackValue = 0
        })
    }


    private fun checkInput() {
        helpFunctions.checkInput(binding.feedbackInput.text, binding.feedbackErrorMessage, resources)
    }

    private fun errorViewOut() {
        helpFunctions.checkErrorViewAvailability(binding.feedbackErrorMessage)
    }
}
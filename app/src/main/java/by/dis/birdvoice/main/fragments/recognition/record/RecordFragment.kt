package by.dis.birdvoice.main.fragments.recognition.record

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.dis.birdvoice.R
import by.dis.birdvoice.databinding.FragmentRecordBinding
import by.dis.birdvoice.helpers.recorder.AudioRecorder
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.main.fragments.BaseMainFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RecordFragment : BaseMainFragment() {

    private lateinit var binding: FragmentRecordBinding
    override lateinit var arrayOfViews: ArrayList<ViewObject>

    private val recorder: AudioRecorder by viewModels()
    private var pressedBool = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRecordBinding.inflate(layoutInflater)

        recorder.setMainApp(activityMain.getApp())

        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(recBird),
                ViewObject(recTopRightCloud, "rc1"),
                ViewObject(recBottomLeftCloud, "lc2"),
                ViewObject(recBottomRightCloud, "rc2"),
                ViewObject(recRecordButtonIcon),
                ViewObject(recRecordButtonContainer)
            )
        }

        animationUtils.commonDefineObjectsVisibility(arrayOfViews)
        if (activityMain.getRegValue() == 0) {
            animationUtils.commonObjectAppear(
                activityMain.getApp().getContext(),
                arrayOfViews,
                true
            )

            binding.recBird.animation.setAnimationListener(helpFunctions.createAnimationEndListener {
                binding.recRecordButtonIcon.setOnClickListener { buttonAction() }
            })
        }

        activityMain.deletePopBackCallback()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        activityMain.showBottomNav()

        mainVM.getScope().launch {
            delay(200)
            mainVM.setToolbarTitle(resources.getString(R.string.record_your_environment))
        }
        activityMain.setToolbarAction(R.drawable.ic_menu) { activityMain.openDrawer() }

        if (activityMain.getRegValue() == 1) {
            mainVM.navigateToWithDelay(R.id.informPageFragment)
            activityMain.hideBottomNav()
        }

        activityMain.switchToolbarUploadVisibility(false)
        mainVM.observableFileToken.observe(activityMain) {
            if (it) {
                binding.recRecordButtonIcon.isClickable = false
                animationUtils.commonObjectAppear(activityMain.getApp().getContext(), arrayOfViews)
                mainVM.navigateToWithDelay(R.id.action_recordFragment_to_editRecordFragment, bundleOf(Pair("picked_audio", true)))

                mainVM.observableFileToken.value = false
            }
        }
    }

    override fun onStop() {
        super.onStop()

        if (!pressedBool) {
            recorder.stop(mainVM.getScope()) {
                pressedBool = !pressedBool
                binding.recRecordButtonIcon.setImageResource(R.drawable.ic_rec_start)
            }
        }

        activityMain.switchToolbarUploadVisibility()
    }

    private fun startRecord() {
        binding.recRecordButtonIcon.setImageResource(
            if (pressedBool) R.drawable.ic_rec_stop
            else R.drawable.ic_rec_start
        )

        if (!pressedBool) {
            stopRecording()
        } else {
            binding.recRecordButtonIcon.isClickable = false
            mainVM.getScope().launch {
                delay(1000)
                withContext(Dispatchers.Main) {
                    binding.recRecordButtonIcon.isClickable = true
                }
            }

            File(activityMain.cacheDir, "audio.mp3").also {
                recorder.start(it)
                mainVM.setAudioFile(it)
            }
        }

        pressedBool = !pressedBool
    }

    private fun stopRecording() {
        recorder.stop(mainVM.getScope()) {
            binding.recRecordButtonIcon.isClickable = false
            animationUtils.commonObjectAppear(activityMain.getApp().getContext(), arrayOfViews)
            mainVM.navigateToWithDelay(R.id.action_recordFragment_to_editRecordFragment)
        }
    }

    private fun requestRecordPermission(onSuccess: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                activityMain.getApp().getContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activityMain,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        } else onSuccess()
    }

    private fun buttonAction() {
        if (ContextCompat.checkSelfPermission(
                activityMain.getApp().getContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) startRecord()
        else requestRecordPermission { startRecord() }
    }
}
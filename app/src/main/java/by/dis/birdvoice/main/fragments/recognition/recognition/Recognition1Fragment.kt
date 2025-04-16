package by.dis.birdvoice.main.fragments.recognition.recognition

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import by.dis.birdvoice.R
import by.dis.birdvoice.client.recognition.RecognitionClient
import by.dis.birdvoice.databinding.FragmentRecognition1Binding
import by.dis.birdvoice.helpers.utils.DialogCommonInitiator
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.main.fragments.BaseMainFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class Recognition1Fragment: BaseMainFragment() {

    private lateinit var binding: FragmentRecognition1Binding
    override lateinit var arrayOfViews: ArrayList<ViewObject>
    private var breakableMarker = false

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var goNext = true
    private var isAudioPicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isAudioPicked = arguments?.getBoolean("picked_audio") ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRecognition1Binding.inflate(layoutInflater)
        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(recognitionBird),
                ViewObject(recognitionText),
                ViewObject(recognitionLoaderHolder),
                ViewObject(recognitionBottomLeftCloud, "lc1"),
                ViewObject(recognitionTopLeftCloud, "lc2"),
                ViewObject(recognitionTopRightCloud, "rc1")
            )

            recognitionLoaderIc.startAnimation(AnimationUtils.loadAnimation(activityMain, R.anim.recognition_loader_animation))
        }

        animationUtils.commonDefineObjectsVisibility(arrayOfViews)
        animationUtils.commonObjectAppear(activityMain.getApp().getContext(), arrayOfViews, true)

        activityMain.setPopBackCallback {
            initBackDialog()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        mainVM.setToolbarTitle(resources.getString(R.string.recognition_service))
        activityMain.setToolbarAction(R.drawable.ic_arrow_back) {
            initBackDialog()
        }

        recognizeAudio()

        mainVM.recognition2Value.value = false
    }

    override fun onDestroy() {
        super.onDestroy()

        breakableMarker = true
        goNext = false
        activityMain.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initBackDialog() {
        goNext = false
        val dialogResources = arrayListOf(
            ContextCompat.getString(activityMain, R.string.dialog_r1_title),
            ContextCompat.getString(activityMain, R.string.dialog_r1_body),
            ContextCompat.getString(activityMain, R.string.dialog_cancel),
            ContextCompat.getString(activityMain, R.string.dialog_stop)
        )
        DialogCommonInitiator().initCommonDialog(activityMain, dialogResources, {
            it.dismiss()
            popBackAction()
        }, {
            goNext = true
            navigateAction()
        })
    }

    private fun recognizeAudio() {
        if (isAudioPicked) {
            val inputStream = mainVM.getUri()?.let { activityMain.contentResolver.openInputStream(it) }
            val file = File(activityMain.getExternalFilesDir(Environment.DIRECTORY_DCIM), "bird_voice_recognition_temp_file.mp3")
            file.outputStream().use {
                it.write(inputStream?.readBytes())
                it.close()
            }

            RecognitionClient.sendToDatabase(file, activityMain.getUsername(), activityMain.getApp().getLocaleInt(), { list ->
                mainVM.setList(list)
                file.delete()
                navigateAction()
            }, { string ->
                activityMain.runOnUiThread { Toast.makeText(activityMain, string, Toast.LENGTH_SHORT).show() }
                file.delete()
                navigateAction()
            })
        } else {
            mainVM.getAudioFile()?.let { RecognitionClient.sendToDatabase(it, activityMain.getUsername(), activityMain.getApp().getLocaleInt(), { list ->
                mainVM.setList(list)
                navigateAction()
            }) { string ->
                activityMain.runOnUiThread { Toast.makeText(activityMain, string, Toast.LENGTH_SHORT).show() }
                navigateAction()
            }}
        }
    }

    private fun navigateAction() {
        scope.launch {
            delay(500)
            if (goNext) {
                activityMain.apply {
                    runOnUiThread {
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    }
                }
                animationUtils.commonObjectAppear(activityMain.getApp().getContext(), arrayOfViews)
                binding.recognitionLoaderHolder.visibility = View.INVISIBLE
                mainVM.navigateToWithDelay(R.id.action_recognitionFragment1_to_recognitionFragment2)
            }
        }
    }

    private fun popBackAction() {
        animationUtils.commonObjectAppear(activityMain.getApp().getContext(), arrayOfViews)
        breakableMarker = true
        binding.recognitionLoaderHolder.visibility = View.INVISIBLE

        mainVM.navigateUpDelay()
    }
}
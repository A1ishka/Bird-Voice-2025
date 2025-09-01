package by.dis.birdvoice.main.fragments.recognition.recognition

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.dis.birdvoice.R
import by.dis.birdvoice.client.recognition.RecognitionClient
import by.dis.birdvoice.databinding.FragmentRecognition1Binding
import by.dis.birdvoice.helpers.utils.CustomToast
import by.dis.birdvoice.helpers.utils.DialogCommonInitiator
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.main.fragments.BaseMainFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class Recognition1Fragment : BaseMainFragment() {

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

            recognitionLoaderIc.startAnimation(
                AnimationUtils.loadAnimation(
                    activityMain,
                    R.anim.recognition_loader_animation
                )
            )
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
        if (!isNetworkAvailable()) {
            CustomToast.show(requireContext(), getString(R.string.internet_connection_issue))
            goBackSafe()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            var tempFile: File? = null
            try {
                val audioFile: File = if (isAudioPicked) {

                    val uri = mainVM.getUri()
                    val inputStream = uri?.let { activityMain.contentResolver.openInputStream(it) }
                    if (inputStream == null) {
                        CustomToast.show(requireContext(), getString(R.string.no_audio_selected))
                        goBackSafe()
                        return@launch
                    }
                    withContext(Dispatchers.IO) {
                        val recordedAudio = File(
                            activityMain.getExternalFilesDir(Environment.DIRECTORY_DCIM),
                            "bird_voice_recognition_temp_file.mp3"
                        )
                        recordedAudio.outputStream().use { out -> inputStream.use { inp -> inp.copyTo(out) } }
                        tempFile = recordedAudio
                        recordedAudio
                    }
                } else {
                    mainVM.getAudioFile() ?: run {
                        CustomToast.show(requireContext(), getString(R.string.no_audio_selected))
                        goBackSafe()
                        return@launch
                    }
                }

                mainVM.clearResults()

                RecognitionClient.sendToDatabase(
                    audioFile = audioFile,
                    email = activityMain.getEmail(),
                    language = activityMain.getApp().getLocaleInt(),
                    uiScope = viewLifecycleOwner.lifecycleScope,
                    onSuccess = { list ->
                        mainVM.setList(list)
                        tempFile?.delete()
                        navigateAction()
                    },
                    onFailure = { msg ->
                        when {
                            msg.equals("Birds were not recognized", ignoreCase = true) -> {
                                mainVM.clearResults()
                                CustomToast.show(requireContext(), getString(R.string.no_birds_found))
                                navigateAction()
                            }

                            msg.contains("unable to resolve", true) ||
                                    msg.contains("connect", true) ||
                                    msg.contains("timeout", true) -> {
                                CustomToast.show(requireContext(), getString(R.string.internet_connection_issue))
                                goBackSafe()
                            }

                            else -> {
                                CustomToast.show(requireContext(), msg)
                                goBackSafe()
                            }
                        }
                        tempFile?.delete()
                    }
                )
            } catch (e: Exception) {
                CustomToast.show(requireContext(), e.message ?: "Error")
                tempFile?.delete()
                goBackSafe()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activityMain.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun goBackSafe() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main.immediate) {
            runCatching {
                val nav = findNavController()
                if (nav.currentDestination != null) {
                    nav.popBackStack()
                }
            }
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
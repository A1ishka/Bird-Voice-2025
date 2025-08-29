package by.dis.birdvoice.main.fragments.recognition.record

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.dis.birdvoice.R
import by.dis.birdvoice.databinding.FragmentEditRecordBinding
import by.dis.birdvoice.helpers.utils.DialogCommonInitiator
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.main.fragments.BaseMainFragment
import by.dis.birdvoice.main.vm.PlayerVM
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class EditRecordFragment : BaseMainFragment() {

    private val setClickOnce = AtomicBoolean(false)
    private val startClickOnce = AtomicBoolean(false)

    private lateinit var binding: FragmentEditRecordBinding
    override lateinit var arrayOfViews: ArrayList<ViewObject>

    private val playerVM: PlayerVM by viewModels()
    private var isPlaying = false

    private var isAudioPicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (playerVM.getRotationValue() != resources.configuration.orientation) playerVM.saveRotationValue(
            resources.configuration.orientation
        )
        else playerVM.clear()

        isAudioPicked = arguments?.getBoolean("picked_audio") ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (playerVM.checkIfBindingSaved()) {
            binding = FragmentEditRecordBinding.inflate(layoutInflater)

            binding.apply {
                arrayOfViews = arrayListOf(
                    ViewObject(editRecConcaveTimeHolder),
                    ViewObject(editRecConcaveDateHolder),
                    ViewObject(editRecConcavePlaceHolder),
                    ViewObject(editRecConcaveWaveHolder),
                    ViewObject(editRecTopHolder),
                    ViewObject(editRecBottomHolder),
                    ViewObject(editRecPlayButton),
                    ViewObject(editRecTimer),
                    ViewObject(editRecAudioProgress),
                    ViewObject(editRecStartButton)
                )
            }

            playerVM.saveArrayOfViews(arrayOfViews)
        } else {
            binding = playerVM.getBinding()
            arrayOfViews = playerVM.getArrayOfViews()!!
        }

        animationUtils.commonDefineObjectsVisibility(arrayOfViews)
        animationUtils.commonObjectAppear(activityMain.getApp().getContext(), arrayOfViews, true)

        binding.editRecTopHolder.animation.setAnimationListener(
            helpFunctions.createAnimationEndListener {
                if (!setClickOnce.compareAndSet(false, true)) return@createAnimationEndListener

                binding.editRecStartButton.setOnClickListener {
                    if (!startClickOnce.compareAndSet(false, true)) return@setOnClickListener

                    lifecycleScope.launch {
                        binding.editRecWaveAnimation.apply {
                            if (isPlaying) {
                                pauseAnimation()
                                isPlaying = !isPlaying
                                animationUtils.commonObjectAppear(
                                    activityMain.getApp().getContext(), arrayOfViews
                                )
                                mainVM.navigateToWithDelay(
                                    R.id.action_editRecordFragment_to_recognitionFragment1,
                                    bundleOf(Pair("picked_audio", isAudioPicked))
                                )
                            } else {
                                animationUtils.commonObjectAppear(
                                    activityMain.getApp().getContext(), arrayOfViews
                                )
                                mainVM.navigateToWithDelay(
                                    R.id.action_editRecordFragment_to_recognitionFragment1,
                                    bundleOf(Pair("picked_audio", isAudioPicked))
                                )
                            }
                        }
                    }
                }
            }
        )

        activityMain.setPopBackCallback {
            animationUtils.commonObjectAppear(
                activityMain.getApp().getContext(), arrayOfViews
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPlayer()
    }

    override fun onStart() {
        super.onStart()

        activityMain.hideBottomNav()
    }

    override fun onResume() {
        super.onResume()

        mainVM.setToolbarTitle(resources.getString(R.string.listen_to_your_record))
        activityMain.setToolbarAction(R.drawable.ic_arrow_back) {
            val dialogEditRecordStringResources = arrayListOf(
                ContextCompat.getString(activityMain, R.string.dialog_er_title),
                ContextCompat.getString(activityMain, R.string.dialog_er_body),
                ContextCompat.getString(activityMain, R.string.dialog_cancel),
                ContextCompat.getString(activityMain, R.string.dialog_go_main)
            )
            DialogCommonInitiator().initCommonDialog(
                activityMain,
                dialogEditRecordStringResources,
                {
                    it.dismiss()
                    navigationBackAction {
                        animationUtils.commonObjectAppear(
                            activityMain.getApp().getContext(),
                            arrayOfViews
                        )
                    }
                })
        }

        setUpSaveButton()

        binding.editRecConcaveTimeValue.text = helpFunctions.getCurrentTime()
        binding.editRecConcaveDateValue.text = helpFunctions.getCurrentDate()
        binding.editRecConcavePlaceValue.text = resources.getText(R.string.belarus)
    }

    override fun onStop() {
        super.onStop()

        playerVM.saveBinding(binding)
        if (playerVM.getRotationValue() != resources.configuration.orientation) playerVM.saveRotationValue(
            resources.configuration.orientation
        )
        else playerVM.clear()
    }

    private fun setupPlayer() {
        if (isAudioPicked) {
            playerVM.initializeMediaPlayer(mainVM.getUri()!!, binding, activityMain.getApp())
            binding.editRecPlayButton.setOnClickListener { playerVM.playAudio(binding) }
        } else if (mainVM.getAudioFile() != null) {
            playerVM.initializeMediaPlayer(
                mainVM.getAudioFile()!!.toUri(),
                binding,
                activityMain.getApp()
            )
            binding.editRecPlayButton.setOnClickListener { playerVM.playAudio(binding) }
        }
    }

    private fun setUpSaveButton() {
        if (isAudioPicked) binding.editRecSaveButton.visibility = View.GONE
        else {
            binding.editRecSaveButton.setOnClickListener {
                val fileUri = mainVM.getAudioFile()!!.toUri()
                val audioName = helpFunctions.getAudioName()
                val contentResolver = activityMain.contentResolver

                saveFile(audioName, contentResolver, fileUri)
            }
        }
    }

    private fun saveFile(audioName: String, contentResolver: ContentResolver, fileUri: Uri) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, audioName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
        }

        val audioCollection =
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val audioUri = contentResolver.insert(audioCollection, contentValues)

        audioUri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val stream = contentResolver.openInputStream(fileUri)
                stream?.copyTo(outputStream)

                stream?.close().apply {
                    showFileSavedToast()
                }
            }
        }
    }

    private fun showFileSavedToast() {
        Toast.makeText(
            activityMain,
            ContextCompat.getString(activityMain, R.string.file_saved),
            Toast.LENGTH_SHORT
        ).show()
    }
}
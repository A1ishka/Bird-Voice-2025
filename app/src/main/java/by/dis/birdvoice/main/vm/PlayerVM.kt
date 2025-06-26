package by.dis.birdvoice.main.vm

import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import by.dis.birdvoice.R
import by.dis.birdvoice.app.MainApp
import by.dis.birdvoice.databinding.FragmentEditRecordBinding
import by.dis.birdvoice.helpers.utils.HelpFunctions
import by.dis.birdvoice.helpers.utils.ViewObject
import com.airbnb.lottie.LottieDrawable
import kotlinx.coroutines.*

class PlayerVM: ViewModel() {

    private var mpStatus = "play"
    private var viewModelPlayerStatus = 0
    private var rotationValue = 0
    private var arrayOfViews: ArrayList<ViewObject>? = null

    private val mediaJob = Job()
    private val mediaScope = CoroutineScope(Dispatchers.Main + mediaJob)
    private var mediaPlayer: MediaPlayer? = null

    private var binding: FragmentEditRecordBinding? = null
    private lateinit var helpFunctions : HelpFunctions
    private lateinit var mainApp: MainApp

    fun initializeMediaPlayer(uri: Uri, binding: FragmentEditRecordBinding, mainApp: MainApp){

        this.mainApp = mainApp
        helpFunctions = HelpFunctions(mainApp)

        if (viewModelPlayerStatus == 0) {

            mpStatus = "play"

            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(this.mainApp.getContext(), uri)
            mediaPlayer!!.prepare()

            this.binding = binding

            binding.apply {
                editRecAudioProgress.max = mediaPlayer!!.duration
                editRecAudioProgress.progress = 0
                editRecPlayButton.setImageResource(R.drawable.ic_play_button)
            }

            listenProgress(mediaPlayer!!)

            viewModelPlayerStatus = 1
        }
    }

    fun playAudio(binding: FragmentEditRecordBinding){
        mediaScope.launch {
            when (mpStatus) {

                "pause" -> {
                    mediaPlayer!!.pause()
                    binding.apply {
                        editRecPlayButton.setImageResource(R.drawable.ic_play_button)
                        editRecWaveAnimation.pauseAnimation()
                    }
                    mpStatus = "continue"
                }
                "continue" -> {
                    mediaPlayer!!.start()
                    binding.apply {
                        editRecPlayButton.setImageResource(R.drawable.ic_pause_button)
                        editRecWaveAnimation.playAnimation()
                    }
                    mpStatus = "pause"
                    mediaScope.launch { initProgressListener(mediaPlayer!!, binding) }
                }
                "play" -> {
                    try {
                        mediaPlayer!!.start()

                        mediaScope.launch { initProgressListener(mediaPlayer!!, binding) }

                        binding.apply {
                            editRecPlayButton.setImageResource(R.drawable.ic_pause_button)
                            editRecWaveAnimation.apply {
                                playAnimation()
                                repeatMode = LottieDrawable.REVERSE
                                repeatCount = LottieDrawable.INFINITE
                            }
                        }

                        mpStatus = "pause"

                    } catch (e: Exception){
                        Toast.makeText(mainApp.getContext(), "Player error: ${e.message.toString()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun mpStop(binding: FragmentEditRecordBinding){
        mpStatus = "stop"

        if (mediaPlayer?.isPlaying == true){
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
        } else mediaPlayer?.release()

        binding.editRecWaveAnimation.apply {
            pauseAnimation()
            cancelAnimation()
        }
    }

    private suspend fun initProgressListener(mediaPlayer: MediaPlayer, binding: FragmentEditRecordBinding){
        while (mpStatus == "pause") {
            binding.apply {
                editRecTimer.text = helpFunctions.convertToTimerMode(mediaPlayer.currentPosition)
                editRecAudioProgress.progress = mediaPlayer.currentPosition
            }
            delay(250)

            binding.editRecAudioProgress.apply {
                if (progress == max) {
                    mpStatus = "play"
                    delay(250)

                    mediaPlayer.seekTo(0)
                    binding.apply {
                        editRecPlayButton.setImageResource(R.drawable.ic_play_button)
                        editRecAudioProgress.progress = 0
                        editRecTimer.text = helpFunctions.convertToTimerMode(mediaPlayer.currentPosition)
                        editRecWaveAnimation.apply {
                            pauseAnimation()
                            cancelAnimation()
                        }
                    }
                }
            }
        }
    }

    private fun listenProgress(mediaPlayer: MediaPlayer){
        binding!!.editRecAudioProgress.setOnSeekBarChangeListener(helpFunctions.createSeekBarProgressListener {
            mediaPlayer.seekTo(it)
            binding!!.editRecTimer.text = helpFunctions.convertToTimerMode(mediaPlayer.currentPosition)
        })
    }

    fun clear() {
        binding?.let { mpStop(it) }

        mpStatus = "play"
        viewModelPlayerStatus = 0
        arrayOfViews = null
        mediaPlayer = null
        binding = null
    }
    fun saveRotationValue(value: Int){
        rotationValue = value
    }
    fun getRotationValue() = rotationValue

    fun saveArrayOfViews(arrayOfViews: ArrayList<ViewObject>){ this.arrayOfViews = arrayOfViews }
    fun getArrayOfViews() = arrayOfViews

    fun saveBinding(b: FragmentEditRecordBinding){
        binding = b
    }
    fun getBinding() = binding!!
    fun checkIfBindingSaved(): Boolean = binding == null

    override fun onCleared() {
        super.onCleared()

        binding?.let { mpStop(it) }
    }
}
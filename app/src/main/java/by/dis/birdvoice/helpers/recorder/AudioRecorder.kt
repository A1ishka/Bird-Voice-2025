package by.dis.birdvoice.helpers.recorder

import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import by.dis.birdvoice.app.MainApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AudioRecorder : ViewModel(), AudioRecorderInterface {

    private var recorder: MediaRecorder? = null
    private lateinit var mainApp: MainApp

    fun setMainApp(app: MainApp) { mainApp = app }

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(mainApp.getContext())
        } else MediaRecorder()
    }

    override fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setAudioSamplingRate(44100)
            setAudioChannels(1)
            setAudioEncodingBitRate(128000)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()

            recorder = this
        }
    }

    override fun stop(scope: CoroutineScope, actionOnStop: () -> Unit) {
        try {
            stopRecording()
            actionOnStop()
        } catch (e: Exception) {
            scope.launch {
                delay(2000)
                stopRecording()

                withContext(Dispatchers.Main) {
                    actionOnStop()
                }
            }
        }
    }

    private fun stopRecording() {
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        recorder = null
    }
}
package by.dis.birdvoice.helpers.recorder

import kotlinx.coroutines.CoroutineScope
import java.io.File

interface AudioRecorderInterface {
    fun start(outputFile: File)
    fun stop(scope: CoroutineScope, actionOnStop: () -> Unit)
}
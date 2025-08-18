package by.dis.birdvoice.client.recognition

import android.util.Log
import by.dis.birdvoice.db.objects.RecognizedBird
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object RecognitionClient {

    private var recognitionClient = OkHttpClient.Builder()
        .callTimeout(3, TimeUnit.MINUTES)
        .connectTimeout(3, TimeUnit.MINUTES)
        .readTimeout(3, TimeUnit.MINUTES)
        .writeTimeout(3, TimeUnit.MINUTES)
        .build()

    fun sendToDatabase(
        audioFile: File,
        email: String,
        language: Int,
        onSuccess: (ArrayList<RecognizedBird>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val username = try {
            email.substringBefore("@")
        } catch (e: NumberFormatException) {
            Log.d("sendToDatabase NumberFormatException", e.message.toString())
        } ?: " "

        val audioType = audioFile.extension

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "audio_to_recognize",
                "$username audio.$audioType",
                audioFile.asRequestBody("audio/mpeg".toMediaType())
            )
            .addFormDataPart("email", email)
            .addFormDataPart("language", language.toString())
            .build()

        val request = Request.Builder()
            .url("https://bird-sounds-database.intelligent.by/api/recognize/")
            .post(body)
            .build()

        recognitionClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                try {
                    onFailure(e.message.toString())
                } catch (e: IOException) {
                    Log.d("Failure error message", e.message.toString())
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val raw = response.body?.string()
                val mid = raw?.substring(1, raw.length - 1)
                val finalString = mid?.let { decodeUnicode(it) }?.replace("\\", "")
                Log.d("Error message", finalString.toString())

                try {
                    if (finalString.isNullOrBlank() || finalString == "{}") {
                        onFailure("Birds were not recognized")
                        return
                    }

                    val jObject = JSONObject(finalString)
                    val keys = jObject.keys()

                    val arrayOfResults = arrayListOf<RecognizedBird>()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val birdInfoArray = jObject.getString(key)

                        val recognizedBird = RecognizedBird(image = birdInfoArray, name = key)
                        if (recognizedBird.name != "unknown" && recognizedBird.name != "background") {
                            arrayOfResults.add(recognizedBird)
                        }
                    }

                    if (arrayOfResults.isEmpty()) {
                        onFailure("Birds were not recognized")
                        return
                    }

                    onSuccess(arrayOfResults)

                } catch (e: JSONException) {
                    onFailure(e.message ?: "Parse error")
                }
            }
        })
    }

    private fun decodeUnicode(input: String): String {
        val pattern = Pattern.compile("\\\\u(\\p{XDigit}{4})")
        val matcher = pattern.matcher(input)
        val buffer = StringBuffer(input.length)
        while (matcher.find()) {
            val hex = matcher.group(1)
            val codePoint = hex?.let { Integer.parseInt(it, 16) }
            matcher.appendReplacement(buffer, "")

            if (codePoint != null) {
                buffer.appendCodePoint(codePoint)
            }
        }
        matcher.appendTail(buffer)
        return buffer.toString()
    }
}
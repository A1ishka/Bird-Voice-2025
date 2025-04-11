package by.dis.birdvoice.client.recognition

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

    fun sendToDatabase(audioFile: File, token: String, username: String, language: Int, onSuccess: (ArrayList<RecognizedBird>) -> Unit, onFailure: (String) -> Unit) {

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("audio_to_recognize", "$username audio", audioFile.asRequestBody("application/octet-stream".toMediaType()))
            .addFormDataPart("access",token)
            .addFormDataPart("username",username)
            .addFormDataPart("language", language.toString())
            .build()

        val request = Request.Builder()
            .url("https://bird-sounds-database.intelligent.by/api/recognize/")
            .post(body)
            .addHeader("Cookie", "csrftoken=GnJmqyQOhvOhzOQ39Dczs8EorOUWAUr3A1UyxuZ6LVFOWFWwshjVEmyGbCEEfK0o")
            .build()

        recognitionClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val midString = responseBody?.substring(1, responseBody.length - 1)
                val finalString = (midString?.let { decodeUnicode(it) })?.replace("\\", "")

                try {
                    val jObject = finalString?.let { JSONObject(it) }
                    val keys = jObject?.keys()

                    val arrayOfResults = arrayListOf<RecognizedBird>()
                    while (keys?.hasNext() == true) {
                        val key = keys.next()
                        val birdInfoArray = jObject.getString(key)

                        val recognizedBird = RecognizedBird(
                            image = birdInfoArray,
                            name = key
                        )

                        if (recognizedBird.name != "unknown" && recognizedBird.name != "background")
                            arrayOfResults.add(recognizedBird)
                    }

                    onSuccess(arrayOfResults)

                } catch (e: JSONException) {
                    e.printStackTrace()
                    onFailure(e.message.toString())
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
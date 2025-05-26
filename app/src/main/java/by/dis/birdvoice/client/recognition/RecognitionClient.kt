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
        token: String,
        language: Int,
        onSuccess: (ArrayList<RecognizedBird>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val username = email.substringBefore("@")

        val audioType = audioFile.extension

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "audio_file",
                "$username audio.$audioType",
                audioFile.asRequestBody("audio/mpeg".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("https://apiptushki.intelligent.by/predict2")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        recognitionClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                try {
                    val jObject = responseBody?.let { JSONObject(it) }
                    val predictions = jObject?.getJSONObject("predictions")

                    val arrayOfResults = arrayListOf<RecognizedBird>()
                    val keys = predictions?.keys()

                    while (keys?.hasNext() == true) {
                        val key = keys.next()
                        val birdInfoArray = predictions.getJSONArray(key)

                        val commonNamesArray = birdInfoArray.getJSONArray(0)
                        val name = if (language in 0 until commonNamesArray.length())
                            commonNamesArray.getString(language).trim()
                        else
                            key

                        val imageUrl = birdInfoArray.getString(1)

                        if (name.lowercase() != "unknown" && name.lowercase() != "background") {
                            val recognizedBird = RecognizedBird(
                                name = name,
                                image = imageUrl
                            )
                            arrayOfResults.add(recognizedBird)
                        }
                    }

                    onSuccess(arrayOfResults)

                } catch (e: JSONException) {
                    e.printStackTrace()
                    onFailure(e.message.toString())
                }
            }
        })
    }
}
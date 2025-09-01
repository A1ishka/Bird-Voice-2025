package by.dis.birdvoice.client.recognition

import by.dis.birdvoice.db.objects.RecognizedBird
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.net.ssl.SSLException

object RecognitionClient {

    private val recognitionClient = OkHttpClient.Builder()
        .callTimeout(3, TimeUnit.MINUTES)
        .connectTimeout(3, TimeUnit.MINUTES)
        .readTimeout(3, TimeUnit.MINUTES)
        .writeTimeout(3, TimeUnit.MINUTES)
        .retryOnConnectionFailure(false)
        .build()

    fun sendToDatabase(
        audioFile: File,
        email: String,
        language: Int,
        uiScope: CoroutineScope,
        onSuccess: (ArrayList<RecognizedBird>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val username = runCatching { email.substringBefore("@") }.getOrNull() ?: " "
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "audio_to_recognize",
                "$username audio.${audioFile.extension}",
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
                val msg = mapError(e)
                uiScope.launch(Dispatchers.Main) { onFailure(msg) }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    try {
                        if (!resp.isSuccessful) {
                            uiScope.launch(Dispatchers.Main) {
                                onFailure("HTTP ${resp.code}")
                            }
                            return
                        }

                        val raw = resp.body?.string().orEmpty().trim()
                        val dequoted = if (raw.length >= 2 &&
                            ((raw.first() == '"' && raw.last() == '"') ||
                                    (raw.first() == '\'' && raw.last() == '\''))) {
                            raw.substring(1, raw.length - 1)
                        } else raw

                        val finalString = decodeUnicode(dequoted).replace("\\", "")

                        if (finalString.isBlank() || finalString == "{}") {
                            uiScope.launch(Dispatchers.Main) {
                                onFailure("Birds were not recognized")
                            }
                            return
                        }

                        val jObject = JSONObject(finalString)
                        val results = arrayListOf<RecognizedBird>()
                        val keys = jObject.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val valStr = jObject.getString(key)
                            val recognizedBird = RecognizedBird(image = valStr, name = key)
                            if (recognizedBird.name != "unknown" && recognizedBird.name != "background") {
                                results.add(recognizedBird)
                            }
                        }
                        if (results.isEmpty()) {
                            uiScope.launch(Dispatchers.Main) {
                                onFailure("Birds were not recognized")
                            }
                            return
                        }

                        uiScope.launch(Dispatchers.Main) { onSuccess(results) }

                    } catch (e: JSONException) {
                        uiScope.launch(Dispatchers.Main) {
                            onFailure(e.message ?: "Parse error")
                        }
                    } catch (e: Exception) {
                        uiScope.launch(Dispatchers.Main) {
                            onFailure(mapError(e))
                        }
                    }
                }
            }
        })
    }

    private fun mapError(e: Throwable): String = when (e) {
        is UnknownHostException,
        is ConnectException,
        is NoRouteToHostException,
        is SSLException,
        is SocketTimeoutException -> "Unable to resolve host or connect"
        else -> e.message ?: "Request failed"
    }

    private fun decodeUnicode(input: String): String {
        val pattern = Pattern.compile("\\\\u(\\p{XDigit}{4})")
        val matcher = pattern.matcher(input)
        val buf = StringBuffer(input.length)
        while (matcher.find()) {
            val code = matcher.group(1)?.let { Integer.parseInt(it, 16) }
            matcher.appendReplacement(buf, "")
            if (code != null) buf.appendCodePoint(code)
        }
        matcher.appendTail(buf)
        return buf.toString()
    }
}
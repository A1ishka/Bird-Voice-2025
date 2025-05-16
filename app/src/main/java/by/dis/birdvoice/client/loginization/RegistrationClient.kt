package by.dis.birdvoice.client.loginization

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object RegistrationClient {

    private var registrationClient: OkHttpClient? = null

    fun post(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {

        if (registrationClient == null) registrationClient = OkHttpClient.Builder().build()

        val mediaType = "application/json".toMediaType()
        val body = "{\"email\":\"$email\",\"password\":\"$password\",\"first_name\":\"null\",\"last_name\":\"null\",\"email\":\"$email\"}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://bird-sounds-database.intelligent.by/api/user-create/")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        registrationClient!!.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message!!)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string()
                    Log.d("registration on resp", responseBody?:"no resp")
                    val jObject = responseBody?.let { it1 -> JSONObject(it1) }
                    if (jObject?.getString("message") == "Registration successful") onSuccess()
                    else jObject?.getString("message")?.let { it1 -> onFailure(it1) }
                }
            }
        })
    }
}
package by.ssrlab.birdvoice.client.loginization

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object LoginClient {

    private var loginClient: OkHttpClient? = null

    fun post(email: String, password: String, onSuccess: (String, String, String, Int) -> Unit, onFailure: (String) -> Unit) {

        if (loginClient == null) loginClient = OkHttpClient.Builder().build()

        val nickname = email.substringBefore("@")

        val mediaType = "application/json".toMediaType()
        val body = "{\"username\":\"$nickname\",\"password\":\"$password\"}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://bird-sounds-database.ssrlab.by/api/login-api/")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        loginClient!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message!!)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string()
                    val jObject = responseBody?.let { it1 -> JSONObject(it1) }
                    if (jObject?.getString("message") == "Login successfull") onSuccess(
                        jObject.getJSONObject("token").getString("access"),
                        jObject.getJSONObject("token").getString("refresh"),
                        nickname,
                        jObject.getJSONObject("user").getJSONArray("account").getJSONObject(0).getInt("id")
                    )
                    else jObject?.getString("message")?.let { it1 -> onFailure(it1) }
                }
            }
        })
    }
}
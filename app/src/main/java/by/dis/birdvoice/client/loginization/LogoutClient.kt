package by.dis.birdvoice.client.loginization

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class LogoutClient {

    private var logoutClient: OkHttpClient? = null

    fun logOut(refreshToken: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {

        if (logoutClient == null) logoutClient = OkHttpClient.Builder().build()

        val mediaType = "application/json".toMediaType()
        val body = "{\"refresh\":\"$refreshToken\"}".toRequestBody(mediaType)
//        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
//            .addFormDataPart("refresh", refreshToken)
//            .build()

        val request = Request.Builder()
            .url("https://bird-sounds-database.ssrlab.by/api/logout/")
            .post(body)
            .build()

        logoutClient?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
//                onFailure(e.message.toString())
                if (e.message?.matches(Regex("^HTTP 204 had non-zero.+")) == true)
                    onSuccess()
                else onFailure(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
//                    val responseBody = response.body?.string()
//                    val jObject = responseBody?.let { it1 -> JSONObject(it1) }
//                    if (jObject?.getString("message") == "Logout success") onSuccess()
//                    else jObject?.getString("message")?.let { it1 -> onFailure(it1) }
                }
            }
        })
    }

    fun deleteUser(accountId: Int, refreshToken: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {

        if (logoutClient == null) logoutClient = OkHttpClient.Builder().build()

        val mediaType = "application/json".toMediaType()
        val body = "{\"id\":\"$accountId\",\"refresh\":\"$refreshToken\"}".toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://bird-sounds-database.ssrlab.by/api/delete-user/")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        logoutClient?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
//                onFailure(e.message.toString())
                if (e.message?.matches(Regex("^HTTP 204 had non-zero.+")) == true)
                    onSuccess()
                else onFailure(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
//                    val responseBody = response.body?.string()
//                    val jObject = responseBody?.let { it1 -> JSONObject(it1) }
//                    if (jObject?.getString("message") == "Account deleted") onSuccess()
//                    else jObject?.getString("message")?.let { it1 -> onFailure(it1) }
                }
            }
        })
    }
}
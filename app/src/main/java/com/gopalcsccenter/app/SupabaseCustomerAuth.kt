package com.gopalcsccenter.app

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.util.concurrent.Executors

/**
 * Supabase phone/password authentication.
 * Uses only the publishable client key; never put a service-role key in the APK.
 * Phone confirmation must be configured deliberately in Supabase Auth settings.
 */
class SupabaseCustomerAuth {
    companion object {
        private const val BASE_URL = "https://hubunxrjnuntkgxxwxix.supabase.co"
        private const val PUBLISHABLE_KEY = "sb_publishable_tLAgdWYFEE3w9wdZFqEA0g_3smuC8D8"
    }

    data class Result(val success: Boolean, val accessToken: String?, val userId: String?, val message: String)

    private val executor = Executors.newSingleThreadExecutor()

    fun signIn(phone: String, password: String, callback: (Result) -> Unit) {
        request("/auth/v1/token?grant_type=password", JSONObject()
            .put("phone", phone).put("password", password), callback)
    }

    fun signUp(phone: String, password: String, callback: (Result) -> Unit) {
        request("/auth/v1/signup", JSONObject()
            .put("phone", phone).put("password", password), callback)
    }

    private fun request(path: String, body: JSONObject, callback: (Result) -> Unit) {
        executor.execute {
            var connection: HttpURLConnection? = null
            val result = try {
                connection = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 15000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("apikey", PUBLISHABLE_KEY)
                    setRequestProperty("Authorization", "Bearer " + PUBLISHABLE_KEY)
                }
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val json = try { JSONObject(text) } catch (_: Exception) { JSONObject() }
                if (code in 200..299 && json.has("access_token")) {
                    val user = json.optJSONObject("user")
                    Result(true, json.optString("access_token"), user?.optString("id"), "Login यशस्वी.")
                } else if (code in 200..299) {
                    Result(false, null, null, "खाते तयार झाले; पण तत्काळ Login session मिळाला नाही. Supabase मधील Phone confirmation सेटिंग तपासा.")
                } else {
                    val message = json.optString("msg", json.optString("message", json.optString("error_description", "सेवा उपलब्ध नाही.")))
                    Result(false, null, null, message)
                }
            } catch (_: Exception) {
                Result(false, null, null, "इंटरनेट किंवा Supabase कनेक्शन तपासा.")
            } finally {
                connection?.disconnect()
            }
            callback(result)
        }
    }
}

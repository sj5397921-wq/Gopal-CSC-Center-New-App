package com.gopalcsccenter.app

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.util.concurrent.Executors

/**
 * Customer sign-in using the existing Supabase Email provider without OTP.
 *
 * The customer enters a phone number in the app. It is normalized to digits and
 * mapped to a deterministic, non-deliverable synthetic email address for Supabase
 * Email/Password Auth. Email confirmation must remain disabled in Supabase.
 *
 * IMPORTANT: this does not verify ownership of the phone number. Admin should
 * verify customer identity/number before attaching sensitive application records.
 * Never place a Supabase service-role key in this Android app.
 */
class SupabaseCustomerAuth {
    companion object {
        private const val BASE_URL = "https://hubunxrjnuntkgxxwxix.supabase.co"
        private const val PUBLISHABLE_KEY = "sb_publishable_tLAgdWYFEE3w9wdZFqEA0g_3smuC8D8"
        private const val LOGIN_DOMAIN = "customer.gopal-csc.invalid"
    }

    data class Result(val success: Boolean, val accessToken: String?, val userId: String?, val message: String)

    private val executor = Executors.newSingleThreadExecutor()

    private fun loginEmail(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return "phone-$digits@$LOGIN_DOMAIN"
    }

    fun signIn(phone: String, password: String, callback: (Result) -> Unit) {
        request(
            "/auth/v1/token?grant_type=password",
            JSONObject().put("email", loginEmail(phone)).put("password", password),
            callback
        )
    }

    fun signUp(phone: String, password: String, fullName: String, callback: (Result) -> Unit) {
        request(
            "/auth/v1/signup",
            JSONObject()
                .put("email", loginEmail(phone))
                .put("password", password)
                .put("data", JSONObject()
                    .put("full_name", fullName)
                    .put("phone_number", phone)),
            callback
        )
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
                val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val json = try { JSONObject(responseText) } catch (_: Exception) { JSONObject() }
                if (code in 200..299 && json.has("access_token")) {
                    val user = json.optJSONObject("user")
                    Result(true, json.optString("access_token"), user?.optString("id"), "Login यशस्वी.")
                } else if (code in 200..299) {
                    Result(false, null, null, "खाते तयार झाले; Login session मिळाला नाही. Supabase मध्ये Email confirmation बंद आहे का तपासा.")
                } else {
                    val message = json.optString(
                        "msg",
                        json.optString("message", json.optString("error_description", json.optString("error", "Login अयशस्वी. माहिती तपासा.")))
                    )
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

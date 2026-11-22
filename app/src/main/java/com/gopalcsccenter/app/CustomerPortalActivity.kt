package com.gopalcsccenter.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.IOException
import java.util.Locale

class CustomerPortalActivity : Activity() {
    companion object {
        private const val BASE_URL = "https://hubunxrjnuntkgxxwxix.supabase.co"
        private const val PUBLISHABLE_KEY = "sb_publishable_tLAgdWYFEE3w9wdZFqEA0g_3smuC8D8"
        private const val BUCKET = "customer-documents"
        private const val PICK_NEW_DOCUMENT = 901
        private const val PICK_REPLACEMENT = 902
        private const val MAX_FILE_BYTES = 15 * 1024 * 1024
    }

    private var accessToken = ""
    private var userId = ""
    private var phone = ""
    private var fullName = ""
    private var selectedService: String? = null
    private var pendingDocumentType: String? = null
    private var pendingRejectedDocument: JSONObject? = null
    private val selectedFiles = linkedMapOf<String, Uri>()
    private lateinit var root: LinearLayout

    private val services = listOf(
        "आधार कार्ड सेवा",
        "पॅन कार्ड सेवा",
        "MahaDBT अर्ज",
        "जात प्रमाणपत्र",
        "उत्पन्न प्रमाणपत्र",
        "रहिवासी प्रमाणपत्र",
        "रेल्वे तिकीट बुकिंग",
        "बस तिकीट बुकिंग",
        "विमान तिकीट बुकिंग",
        "ऑनलाइन फॉर्म",
        "प्रिंट आणि झेरॉक्स"
    )

    private val requiredDocuments = mapOf(
        "आधार कार्ड सेवा" to listOf("आधार कार्ड (असल्यास)", "जन्म प्रमाणपत्र (बाल आधारसाठी)", "मोबाईल नंबर", "पत्ता / ओळख पुरावा (सेवेनुसार)"),
        "पॅन कार्ड सेवा" to listOf("आधार कार्ड", "मोबाईल नंबर", "ई-मेल आयडी", "फोटो व स्वाक्षरी (लागू असल्यास)"),
        "MahaDBT अर्ज" to listOf("आधार कार्ड", "बँक पासबुक", "7/12 उतारा (लागू असल्यास)", "8-A उतारा (लागू असल्यास)", "Farmer ID / जात प्रमाणपत्र (योजनेनुसार)"),
        "जात प्रमाणपत्र" to listOf("आधार कार्ड", "रहिवासी / पत्ता पुरावा", "शाळा सोडल्याचा दाखला (लागू असल्यास)", "वडिलांचे / नातेवाईकांचे जात प्रमाणपत्र (लागू असल्यास)", "पासपोर्ट फोटो"),
        "उत्पन्न प्रमाणपत्र" to listOf("आधार कार्ड", "रहिवासी पुरावा", "उत्पन्नाचा पुरावा", "रेशन कार्ड (लागू असल्यास)", "पासपोर्ट फोटो"),
        "रहिवासी प्रमाणपत्र" to listOf("आधार कार्ड", "पत्ता पुरावा", "रेशन कार्ड / वीज बिल (लागू असल्यास)", "पासपोर्ट फोटो"),
        "रेल्वे तिकीट बुकिंग" to listOf("प्रवाशाचे पूर्ण नाव", "प्रवाशाचे वय", "प्रवासाची तारीख", "प्रवासाचे ठिकाण व वेळ", "मोबाईल नंबर"),
        "बस तिकीट बुकिंग" to listOf("प्रवाशाचे पूर्ण नाव", "प्रवासाची तारीख", "प्रवासाचे ठिकाण", "मोबाईल नंबर"),
        "विमान तिकीट बुकिंग" to listOf("ओळखपत्रानुसार पूर्ण नाव", "प्रवासाची तारीख", "प्रस्थान व गंतव्य ठिकाण", "मोबाईल नंबर", "ई-मेल आयडी"),
        "ऑनलाइन फॉर्म" to listOf("आधार कार्ड (लागू असल्यास)", "मोबाईल नंबर", "ई-मेल आयडी", "संबंधित अर्जासाठी आवश्यक कागदपत्रे"),
        "प्रिंट आणि झेरॉक्स" to listOf("मूळ कागदपत्रे किंवा स्पष्ट PDF / फोटो", "प्रिंटसाठी आवश्यक पानांची माहिती")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accessToken = intent.getStringExtra("access_token").orEmpty()
        userId = intent.getStringExtra("user_id").orEmpty()
        phone = intent.getStringExtra("phone").orEmpty()
        fullName = intent.getStringExtra("full_name").orEmpty()
        if (accessToken.isBlank() || userId.isBlank()) {
            Toast.makeText(this, "ग्राहक Login पुन्हा करा.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        showLoading("ग्राहक खाते तपासत आहे...")
        Thread {
            try {
                val result = api("GET", "/rest/v1/customer_profiles?select=id,full_name,phone&id=eq.${enc(userId)}", null)
                val rows = JSONArray(result)
                if (rows.length() > 0) {
                    val profile = rows.getJSONObject(0)
                    fullName = profile.optString("full_name", fullName)
                    phone = profile.optString("phone", phone)
                    runOnUiThread { showServices() }
                } else {
                    runOnUiThread { showProfileSetup() }
                }
            } catch (e: Exception) {
                runOnUiThread { showError("ग्राहक खाते लोड झाले नाही: ${e.message ?: "कनेक्शन तपासा"}") }
            }
        }.start()
    }

    private fun newPage(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(20, 22, 20, 20)
            setBackgroundColor(Color.WHITE)
        }
        val heading = TextView(this).apply {
            text = title
            textSize = 23f
            setTextColor(Color.rgb(20, 80, 160))
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 18)
        }
        root.addView(heading)
        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(root)
        })
    }

    private fun addText(parent: LinearLayout, value: String, size: Float = 15f, color: Int = Color.DKGRAY) {
        parent.addView(TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            setPadding(8, 7, 8, 7)
        })
    }

    private fun addButton(parent: LinearLayout, label: String, action: () -> Unit) {
        parent.addView(Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 15f
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 5, 0, 7) })
    }

    private fun showLoading(message: String) {
        newPage("GOPAL CSC CENTER")
        addText(root, message, 16f)
    }

    private fun showError(message: String) {
        newPage("ग्राहक खाते")
        addText(root, message, 15f, Color.RED)
        addButton(root, "पुन्हा प्रयत्न करा") { loadProfileAgain() }
        addButton(root, "Login बंद करा") { finish() }
    }

    private fun loadProfileAgain() {
        showLoading("खाते पुन्हा तपासत आहे...")
        Thread {
            try {
                val rows = JSONArray(api("GET", "/rest/v1/customer_profiles?select=id,full_name,phone&id=eq.${enc(userId)}", null))
                if (rows.length() > 0) {
                    val profile = rows.getJSONObject(0)
                    fullName = profile.optString("full_name", fullName)
                    phone = profile.optString("phone", phone)
                    runOnUiThread { showServices() }
                } else runOnUiThread { showProfileSetup() }
            } catch (e: Exception) {
                runOnUiThread { showError("खाते तपासता आले नाही: ${e.message}") }
            }
        }.start()
    }

    private fun showProfileSetup() {
        newPage("ग्राहक माहिती पूर्ण करा")
        addText(root, "पहिल्यांदा वापरताना आपले नाव आणि मोबाईल नंबर सेव्ह करा.", 14f)
        val nameField = EditText(this).apply {
            hint = "पूर्ण नाव"
            setText(fullName)
            textSize = 16f
        }
        val phoneField = EditText(this).apply {
            hint = "मोबाईल नंबर (+91 सह)"
            setText(phone)
            textSize = 16f
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        root.addView(nameField)
        root.addView(phoneField)
        addButton(root, "माहिती Save करा") {
            val name = nameField.text.toString().trim()
            val mobile = phoneField.text.toString().trim()
            if (name.isBlank()) {
                nameField.error = "पूर्ण नाव भरा"
                return@addButton
            }
            if (!mobile.matches(Regex("^\\+91[0-9]{10}$"))) {
                phoneField.error = "+91 नंतर 10 अंक भरा"
                return@addButton
            }
            fullName = name
            phone = mobile
            showLoading("माहिती सेव्ह होत आहे...")
            Thread {
                try {
                    val body = JSONObject().put("id", userId).put("full_name", fullName).put("phone", phone)
                    api("POST", "/rest/v1/customer_profiles?on_conflict=id", body.toString().toByteArray(), "application/json", "resolution=merge-duplicates,return=minimal")
                    runOnUiThread { showServices() }
                } catch (e: Exception) {
                    runOnUiThread { showError("माहिती सेव्ह झाली नाही: ${e.message}") }
                }
            }.start()
        }
    }

    private fun showServices() {
        selectedService = null
        newPage("ग्राहक Login — उपलब्ध सेवा")
        addText(root, "नमस्कार ${fullName}! खालीलपैकी आवश्यक सेवा निवडा.", 15f)
        services.forEachIndexed { index, service ->
            addButton(root, "${index + 1}. ${service}") {
                selectedFiles.clear()
                showServiceDetails(service)
            }
        }
        addButton(root, "माझे अर्ज / कागदपत्रांची स्थिती") { showMyDocuments() }
        addButton(root, "Logout") { finish() }
    }

    private fun showServiceDetails(service: String) {
        selectedService = service
        newPage(service)
        addText(root, "या सेवेसाठी आवश्यक कागदपत्रे. प्रत्येक कागदासमोर PDF किंवा फोटो निवडा.", 14f)
        val docs = requiredDocuments[service].orEmpty()
        docs.forEach { document ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(8, 7, 8, 7)
                setBackgroundColor(Color.rgb(242, 246, 250))
            }
            addText(card, document, 15f, Color.BLACK)
            addButton(card, "＋ PDF / फोटो Upload करा") { chooseNewFile(document) }
            root.addView(card, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 4, 0, 6) })
        }
        addText(root, "टीप: लागू नसलेले कागदपत्र नंतरही देता येईल. किमान एक फाइल निवडून अर्ज पाठवा.", 12f, Color.GRAY)
        addButton(root, "निवडलेले कागदपत्रे आणि अर्ज पाठवा") { submitApplication(service) }
        addButton(root, "सेवा यादीकडे परत") { showServices() }
    }

    private fun chooseNewFile(documentType: String) {
        pendingDocumentType = documentType
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/jpeg", "image/png", "image/webp"))
        }
        startActivityForResult(intent, PICK_NEW_DOCUMENT)
    }

    @Deprecated("Compatibility with minimum SDK")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(uri, data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) { }
        if (requestCode == PICK_NEW_DOCUMENT) {
            val type = pendingDocumentType ?: return
            selectedFiles[type] = uri
            selectedService?.let { showServiceDetailsWithSelection(it) }
        } else if (requestCode == PICK_REPLACEMENT) {
            val rejected = pendingRejectedDocument ?: return
            uploadReplacement(uri, rejected)
        }
    }

    private fun showServiceDetailsWithSelection(service: String) {
        showServiceDetails(service)
        selectedFiles.forEach { (type, uri) ->
            addText(root, "निवडले: ${type} — ${displayName(uri)}", 12f, Color.rgb(20, 120, 70))
        }
    }

    private fun submitApplication(service: String) {
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, "किमान एक PDF किंवा फोटो निवडा.", Toast.LENGTH_LONG).show()
            return
        }
        showLoading("अर्ज आणि कागदपत्रे Upload होत आहेत...")
        Thread {
            try {
                val applicationNo = "GAPP" + System.currentTimeMillis().toString()
                val body = JSONObject()
                    .put("customer_id", userId)
                    .put("application_no", applicationNo)
                    .put("service_name", service)
                val appRows = JSONArray(api(
                    "POST",
                    "/rest/v1/customer_applications?select=id",
                    body.toString().toByteArray(),
                    "application/json",
                    "return=representation"
                ))
                if (appRows.length() == 0) throw IOException("अर्ज क्रमांक तयार झाला नाही.")
                val applicationId = appRows.getJSONObject(0).getString("id")
                var uploaded = 0
                selectedFiles.forEach { (documentType, uri) ->
                    uploadDocument(applicationId, documentType, uri, null)
                    uploaded++
                }
                runOnUiThread {
                    Toast.makeText(this, "अर्ज ${applicationNo} पाठवला. ${uploaded} कागदपत्रे Upload झाली.", Toast.LENGTH_LONG).show()
                    selectedFiles.clear()
                    showMyDocuments()
                }
            } catch (e: Exception) {
                runOnUiThread { showError("अर्ज पाठवताना अडचण: ${e.message ?: "पुन्हा प्रयत्न करा"}") }
            }
        }.start()
    }

    private fun uploadDocument(applicationId: String, documentType: String, uri: Uri, replacesId: String?) {
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("फाइल वाचता आली नाही.")
        if (bytes.isEmpty() || bytes.size > MAX_FILE_BYTES) {
            throw IOException("फाइल रिकामी आहे किंवा 15 MB पेक्षा मोठी आहे.")
        }
        val originalName = displayName(uri)
        val safeName = originalName.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "document" }
        val storagePath = "${userId}/${applicationId}/${System.currentTimeMillis()}_${safeName}"
        val mime = contentResolver.getType(uri) ?: "application/octet-stream"
        apiBytes("POST", "/storage/v1/object/${BUCKET}/${storagePath.split("/").joinToString("/") { enc(it) }}", bytes, mime)
        val record = JSONObject()
            .put("application_id", applicationId)
            .put("customer_id", userId)
            .put("document_type", documentType)
            .put("storage_path", storagePath)
        if (!replacesId.isNullOrBlank()) record.put("replaces_document_id", replacesId)
        api("POST", "/rest/v1/customer_documents", record.toString().toByteArray(), "application/json", "return=minimal")
    }

    private fun showMyDocuments() {
        showLoading("आपले अर्ज आणि कागदपत्रे लोड होत आहेत...")
        Thread {
            try {
                val query = "/rest/v1/customer_documents?select=id,application_id,document_type,review_status,rejection_reason,replaces_document_id,uploaded_at,customer_applications(service_name,application_no)&customer_id=eq.${enc(userId)}&order=uploaded_at.desc"
                val docs = JSONArray(api("GET", query, null))
                runOnUiThread { renderMyDocuments(docs) }
            } catch (e: Exception) {
                runOnUiThread { showError("कागदपत्रांची स्थिती मिळाली नाही: ${e.message}") }
            }
        }.start()
    }

    private fun renderMyDocuments(docs: JSONArray) {
        newPage("माझे अर्ज / कागदपत्रे")
        if (docs.length() == 0) addText(root, "अद्याप कोणतेही कागदपत्र Upload केलेले नाही.", 14f)
        val replacedIds = mutableSetOf<String>()
        for (i in 0 until docs.length()) {
            val row = docs.optJSONObject(i) ?: continue
            val replacement = row.optString("replaces_document_id")
            if (replacement.isNotBlank() && replacement != "null") replacedIds.add(replacement)
        }
        for (i in 0 until docs.length()) {
            val row = docs.optJSONObject(i) ?: continue
            val app = row.optJSONObject("customer_applications") ?: JSONObject()
            val service = app.optString("service_name", "सेवा")
            val appNo = app.optString("application_no", "")
            val docType = row.optString("document_type", "कागदपत्र")
            val status = row.optString("review_status", "Pending")
            val reason = row.optString("rejection_reason", "")
            addText(root, "सेवा: ${service}\nअर्ज: ${appNo}\nकागदपत्र: ${docType}\nस्थिती: ${marathiStatus(status)}", 14f)
            if (status == "Rejected") {
                addText(root, "नाकारण्याचे कारण: ${reason.ifBlank { "कारण दिलेले नाही" }}", 13f, Color.RED)
                val id = row.optString("id")
                if (!replacedIds.contains(id)) {
                    addButton(root, "हा नाकारलेला कागद पुन्हा Upload करा") {
                        pendingRejectedDocument = row
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/jpeg", "image/png", "image/webp"))
                        }
                        startActivityForResult(intent, PICK_REPLACEMENT)
                    }
                }
            }
            addText(root, "────────────────────────", 10f, Color.LTGRAY)
        }
        addButton(root, "उपलब्ध सेवा") { showServices() }
    }

    private fun uploadReplacement(uri: Uri, rejected: JSONObject) {
        showLoading("नाकारलेले कागद पुन्हा Upload होत आहे...")
        Thread {
            try {
                uploadDocument(
                    rejected.getString("application_id"),
                    rejected.getString("document_type"),
                    uri,
                    rejected.getString("id")
                )
                runOnUiThread {
                    Toast.makeText(this, "नवीन कागदपत्र तपासणीसाठी पाठवले.", Toast.LENGTH_LONG).show()
                    pendingRejectedDocument = null
                    showMyDocuments()
                }
            } catch (e: Exception) {
                runOnUiThread { showError("पुन्हा Upload झाले नाही: ${e.message}") }
            }
        }.start()
    }

    private fun marathiStatus(status: String): String = when (status) {
        "Approved" -> "मंजूर"
        "Rejected" -> "नाकारले"
        else -> "तपासणी बाकी"
    }

    private fun displayName(uri: Uri): String {
        var name = uri.lastPathSegment ?: "document"
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) name = cursor.getString(index) ?: name
            }
        } catch (_: Exception) { }
        return name
    }

    private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    private fun api(method: String, path: String, body: ByteArray?, contentType: String = "application/json", prefer: String? = null): String {
        return apiRequest(method, path, body, contentType, prefer)
    }

    private fun apiBytes(method: String, path: String, body: ByteArray, contentType: String): String {
        return apiRequest(method, path, body, contentType, null)
    }

    private fun apiRequest(method: String, path: String, body: ByteArray?, contentType: String, prefer: String?): String {
        val connection = (URL(BASE_URL + path).openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = method
            connection.connectTimeout = 20000
            connection.readTimeout = 30000
            connection.setRequestProperty("apikey", PUBLISHABLE_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${accessToken}")
            connection.setRequestProperty("Content-Type", contentType)
            if (prefer != null) connection.setRequestProperty("Prefer", prefer)
            if (body != null) {
                connection.doOutput = true
                connection.setFixedLengthStreamingMode(body.size)
                connection.outputStream.use { it.write(body) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val detail = try {
                    val json = JSONObject(response)
                    json.optString("message", json.optString("msg", json.optString("error_description", json.optString("error", response))))
                } catch (_: Exception) { response }
                throw IOException("HTTP ${code}: ${detail.take(350)}")
            }
            return response
        } finally {
            connection.disconnect()
        }
    }
}

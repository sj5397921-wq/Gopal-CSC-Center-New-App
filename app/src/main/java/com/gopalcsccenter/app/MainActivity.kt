package com.gopalcsccenter.app

import android.os.Bundle
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

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

    private lateinit var layout: LinearLayout
    private var pendingPdfFile: File? = null
    private val createPdfRequest = 701

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun createLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 30, 24, 24)
            setBackgroundColor(Color.WHITE)
        }
    }

    private fun addButton(parent: LinearLayout, text: String, action: () -> Unit) {
        val button = Button(this).apply {
            this.text = text
            textSize = 16f
            isAllCaps = false
            setOnClickListener { action() }
        }
        parent.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 10, 0, 10) }
        )
    }

    private fun addTitle(parent: LinearLayout, text: String) {
        val title = TextView(this).apply {
            this.text = text
            textSize = 25f
            setTextColor(Color.rgb(20, 80, 160))
            gravity = Gravity.CENTER
            setPadding(0, 15, 0, 20)
        }
        parent.addView(title)
    }

    private fun addField(parent: LinearLayout, hintText: String, fieldInputType: Int = InputType.TYPE_CLASS_TEXT): EditText {
        val field = EditText(this).apply {
            hint = hintText
            textSize = 17f
            inputType = fieldInputType
            setSingleLine(true)
            setPadding(14, 12, 14, 12)
        }
        parent.addView(
            field,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 6, 0, 6) }
        )
        return field
    }

    private fun showHome() {
        layout = createLayout()
        addTitle(layout, "GOPAL CSC CENTER")

        val subtitle = TextView(this).apply {
            text = "आपल्या सर्व डिजिटल सेवा एकाच ठिकाणी"
            textSize = 17f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }
        layout.addView(subtitle)

        addButton(layout, "उपलब्ध सेवा") { showServices() }
        addButton(layout, "ग्राहक / बिल इतिहास") { showBillHistory() }
        addButton(layout, "दैनिक उत्पन्न अहवाल") { showDailyIncomeReport() }
        addButton(layout, "Admin Panel") { showAdminInfo() }
        addButton(layout, "Document Scanner") {
            showMessage("Document Scanner पुढील टप्प्यात जोडला जाईल.")
        }
        setContentView(layout)
    }

    private fun showServices() {
        val page = createLayout()
        addTitle(page, "उपलब्ध सेवा")

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        services.forEachIndexed { index, service ->
            val item = TextView(this).apply {
                text = "${index + 1}. $service"
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(12, 18, 12, 18)
                setBackgroundColor(Color.rgb(240, 245, 250))
                isClickable = true
                isFocusable = true
                setOnClickListener { showCustomerForm(service) }
            }
            list.addView(
                item,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 5, 0, 5) }
            )
        }

        scroll.addView(list)
        page.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )
        addButton(page, "मुख्य पानावर जा") { showHome() }
        setContentView(page)
    }

    private fun showCustomerForm(service: String) {
        val page = createLayout()
        addTitle(page, "ग्राहक माहिती")

        val selected = TextView(this).apply {
            text = "निवडलेली सेवा: $service"
            textSize = 18f
            setTextColor(Color.rgb(20, 80, 160))
            setPadding(0, 8, 0, 12)
        }
        page.addView(selected)

        val scroll = ScrollView(this)
        val fields = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val customerName = addField(fields, "ग्राहकाचे पूर्ण नाव")
        val phone = addField(fields, "मोबाईल नंबर", InputType.TYPE_CLASS_PHONE)
        val fee = addField(fields, "सेवा शुल्क (₹)", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val reference = addField(fields, "अर्ज / संदर्भ क्रमांक (ऐच्छिक)")

        val paymentLabel = TextView(this).apply {
            text = "पेमेंट स्थिती"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 12, 0, 4)
        }
        fields.addView(paymentLabel)
        val paymentStatus = Spinner(this)
        val statuses = listOf("बाकी", "रोख भरले", "UPI भरले")
        paymentStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, statuses
        )
        fields.addView(paymentStatus)

        scroll.addView(fields)
        page.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        addButton(page, "बिल तयार करा") {
            val name = customerName.text.toString().trim()
            val mobile = phone.text.toString().trim()
            val amountText = fee.text.toString().trim()
            val amount = amountText.toDoubleOrNull()

            if (name.isEmpty()) {
                customerName.error = "ग्राहकाचे नाव भरा"
                return@addButton
            }
            if (!mobile.matches(Regex("[0-9]{10}"))) {
                phone.error = "10 अंकी मोबाईल नंबर भरा"
                return@addButton
            }
            if (amount == null || amount < 0) {
                fee.error = "योग्य सेवा शुल्क भरा"
                return@addButton
            }

            showBill(
                service,
                name,
                mobile,
                amount,
                reference.text.toString().trim(),
                paymentStatus.selectedItem.toString()
            )
        }
        addButton(page, "मागे जा") { showServices() }
        setContentView(page)
    }

    private fun showBill(service: String, customerName: String, phone: String, amount: Double, reference: String, paymentStatus: String) {
        val billNumber = "GCSC" + System.currentTimeMillis().toString().takeLast(8)
        val date = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale("mr", "IN")).format(Date())
        val amountText = String.format(Locale.US, "%.2f", amount)
        val receipt = """
            GOPAL CSC CENTER
            ------------------------------
            बिल क्रमांक: $billNumber
            दिनांक: $date
            ग्राहक: $customerName
            मोबाईल: $phone
            सेवा: $service
            संदर्भ क्रमांक: ${reference.ifBlank { "नाही" }}
            सेवा शुल्क: ₹$amountText
            पेमेंट स्थिती: $paymentStatus
            संचालक: Suresh Jadhav
            मोबाईल: 7875277904
            UPI: 7875277904@ybl
            ------------------------------
            धन्यवाद!
        """.trimIndent()
        saveBillRecord(billNumber, date, customerName, phone, service, amount, reference, paymentStatus, receipt)
        val pdf = createReceiptPdf(receipt, billNumber)
        AlertDialog.Builder(this).setTitle("बिल तयार झाले").setMessage(receipt)
            .setPositiveButton("PDF डाउनलोड") { _, _ -> savePdf(pdf, billNumber) }
            .setNeutralButton("प्रिंट") { _, _ -> openPdf(pdf) }
            .setNegativeButton("WhatsApp PDF शेअर") { _, _ -> sharePdf(pdf) }.show()
    }

    private fun saveBillRecord(billNumber: String, date: String, customerName: String, phone: String, service: String, amount: Double, reference: String, paymentStatus: String, receipt: String) {
        val prefs = getSharedPreferences("gopal_csc_records", MODE_PRIVATE)
        val records = try { JSONArray(prefs.getString("bills", "[]") ?: "[]") } catch (_: Exception) { JSONArray() }
        val record = JSONObject()
        record.put("billNumber", billNumber)
        record.put("date", date)
        record.put("createdAt", System.currentTimeMillis())
        record.put("customerName", customerName)
        record.put("phone", phone)
        record.put("service", service)
        record.put("amount", amount)
        record.put("reference", reference)
        record.put("paymentStatus", paymentStatus)
        record.put("receipt", receipt)
        records.put(record)
        prefs.edit().putString("bills", records.toString()).apply()
    }

    private fun loadBillRecords(): JSONArray {
        val raw = getSharedPreferences("gopal_csc_records", MODE_PRIVATE).getString("bills", "[]")
        return try { JSONArray(raw ?: "[]") } catch (_: Exception) { JSONArray() }
    }

    private fun showBillHistory() {
        val page = createLayout()
        addTitle(page, "ग्राहक / बिल इतिहास")

        val search = addField(page, "ग्राहकाचे नाव किंवा मोबाईल नंबर शोधा")
        val dateLabel = TextView(this).apply {
            text = "निवडलेली तारीख: सर्व दिवस"
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(4, 8, 4, 8)
        }
        page.addView(dateLabel)
        var selectedDate = ""

        val dateButtons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val chooseDate = Button(this).apply {
            text = "तारीख निवडा"
            isAllCaps = false
        }
        val clearDate = Button(this).apply {
            text = "तारीख काढा"
            isAllCaps = false
        }
        dateButtons.addView(chooseDate, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        dateButtons.addView(clearDate, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        page.addView(dateButtons)

        val scroll = ScrollView(this)
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(results)
        page.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        fun renderResults() {
            results.removeAllViews()
            val records = loadBillRecords()
            val query = search.text.toString().trim()
            var count = 0
            for (i in records.length() - 1 downTo 0) {
                val item = records.optJSONObject(i) ?: continue
                val name = item.optString("customerName")
                val phone = item.optString("phone")
                val matchesText = query.isEmpty() || name.contains(query, ignoreCase = true) || phone.contains(query)
                val matchesDate = selectedDate.isEmpty() ||
                    SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(item.optLong("createdAt", 0L))) == selectedDate
                if (!matchesText || !matchesDate) continue
                count++
                val receipt = item.optString("receipt")
                val row = TextView(this).apply {
                    text = name + " | " + phone + "\n" +
                        item.optString("billNumber") + " | ₹" + item.optString("amount") +
                        "\n" + item.optString("date") + " | " + item.optString("paymentStatus")
                    textSize = 16f
                    setTextColor(Color.BLACK)
                    setPadding(14, 14, 14, 14)
                    setBackgroundColor(Color.rgb(240, 245, 250))
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        AlertDialog.Builder(this@MainActivity).setTitle("बिल तपशील")
                            .setMessage(receipt).setPositiveButton("बंद", null).show()
                    }
                }
                results.addView(row, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 5, 0, 5) })
            }
            if (count == 0) {
                val empty = TextView(this).apply {
                    text = "या शोधासाठी कोणतेही बिल सापडले नाही."
                    textSize = 16f
                    setTextColor(Color.DKGRAY)
                    gravity = Gravity.CENTER
                    setPadding(12, 24, 12, 24)
                }
                results.addView(empty)
            }
            dateLabel.text = if (selectedDate.isEmpty()) "निवडलेली तारीख: सर्व दिवस"
                else "निवडलेली तारीख: " + SimpleDateFormat("dd-MM-yyyy", Locale.US).format(
                    SimpleDateFormat("yyyyMMdd", Locale.US).parse(selectedDate)!!
                )
            page.contentDescription = "एकूण सापडलेली बिले: $count"
        }

        chooseDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val selected = java.util.Calendar.getInstance().apply {
                    set(year, month, day, 12, 0, 0)
                }
                selectedDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(selected.time)
                renderResults()
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }
        clearDate.setOnClickListener {
            selectedDate = ""
            renderResults()
        }
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { renderResults() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        renderResults()
        addButton(page, "मुख्य पानावर जा") { showHome() }
        setContentView(page)
    }

    private fun showDailyIncomeReport() {
        val records = loadBillRecords()
        val today = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        var totalBills = 0
        var paidBills = 0
        var pendingBills = 0
        var received = 0.0
        var pending = 0.0
        for (i in 0 until records.length()) {
            val item = records.optJSONObject(i) ?: continue
            if (SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(item.optLong("createdAt", 0L))) != today) continue
            totalBills++
            val amount = item.optDouble("amount", 0.0)
            val status = item.optString("paymentStatus")
            if (status == "रोख भरले" || status == "UPI भरले") { paidBills++; received += amount }
            else { pendingBills++; pending += amount }
        }
        val report = "आजची बिले: " + totalBills + "\nपेमेंट मिळाले: " + paidBills + "\nपेमेंट बाकी: " + pendingBills + "\n\nजमा उत्पन्न: Rs." + String.format(Locale.US, "%.2f", received) + "\nबाकी रक्कम: Rs." + String.format(Locale.US, "%.2f", pending) + "\nएकूण बिल रक्कम: Rs." + String.format(Locale.US, "%.2f", received + pending)
        AlertDialog.Builder(this).setTitle("दैनिक उत्पन्न अहवाल").setMessage(report).setPositiveButton("ठीक आहे", null).show()
    }
    private fun createReceiptPdf(receipt: String, billNumber: String): File {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = 16f }
        var y = 65f
        receipt.lines().forEach { line -> page.canvas.drawText(line, 40f, y, paint); y += 30f }
        document.finishPage(page)
        val file = File(cacheDir, "Gopal-CSC-$billNumber.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun savePdf(file: File, billNumber: String) {
        pendingPdfFile = file
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Gopal-CSC-$billNumber.pdf")
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, createPdfRequest)
    }

    @Deprecated("Compatibility with minimum SDK")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == createPdfRequest && resultCode == RESULT_OK) {
            val source = pendingPdfFile ?: return
            val uri = data?.data ?: return
            contentResolver.openOutputStream(uri)?.use { out -> FileInputStream(source).use { it.copyTo(out) } }
            showMessage("बिल PDF सेव्ह झाले.")
        }
    }

    private fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "PDF बिल शेअर करा"))
    }

    private fun openPdf(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try { startActivity(intent) } catch (e: Exception) { showMessage("PDF viewer उपलब्ध नाही. WhatsApp शेअर वापरा.") }
    }
    private fun showAdminInfo() {
        AlertDialog.Builder(this)
            .setTitle("Admin Login")
            .setMessage(
                "अधिकृत Admin नंबर: 7875277904\n\n" +
                "सुरक्षित OTP Login साठी Firebase जोडणे आवश्यक आहे.\n" +
                "OTP पडताळणी पूर्ण होईपर्यंत Admin प्रवेश बंद आहे."
            )
            .setPositiveButton("ठीक आहे", null)
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

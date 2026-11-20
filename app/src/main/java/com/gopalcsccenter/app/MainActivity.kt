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
        addButton(layout, "ग्राहक यादी / संपूर्ण इतिहास") { showCustomerDirectory() }
        addButton(layout, "अर्ज ट्रॅकिंग") { showApplicationTracking() }
        addButton(layout, "दैनिक उत्पन्न अहवाल") { showDailyIncomeReport() }
        addButton(layout, "सेवानुसार उत्पन्न अहवाल") { showServiceIncomeReport() }
        addButton(layout, "Document Checklist") { showDocumentChecklist() }
        addButton(layout, "डेटा बॅकअप / Restore") { showBackupRestore() }
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
            scroll,            LinearLayout.LayoutParams(
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
            अर्ज क्रमांक: GAPP${billNumber.takeLast(8)}
            अर्ज स्थिती: अर्ज प्राप्त
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
        record.put("applicationNo", "GAPP" + billNumber.takeLast(8))
        record.put("applicationStatus", "अर्ज प्राप्त")
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

    private fun showCustomerDirectory() {
        val page = createLayout()
        addTitle(page, "ग्राहक यादी / संपूर्ण इतिहास")
        val search = addField(page, "ग्राहकाचे नाव किंवा मोबाईल नंबर शोधा")
        val summary = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.rgb(20, 80, 160))
            setPadding(4, 8, 4, 12)
        }
        page.addView(summary)
        val scroll = ScrollView(this)
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(results)
        page.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        fun renderCustomers() {
            results.removeAllViews()
            val records = loadBillRecords()
            val grouped = linkedMapOf<String, MutableList<JSONObject>>()
            for (i in 0 until records.length()) {
                val item = records.optJSONObject(i) ?: continue
                val phone = item.optString("phone").trim()
                if (phone.isBlank()) continue
                grouped.getOrPut(phone) { mutableListOf() }.add(item)
            }

            val query = search.text.toString().trim()
            val customers = grouped.entries.filter { entry ->
                val latest = entry.value.maxByOrNull { it.optLong("createdAt", 0L) }
                val name = latest?.optString("customerName") ?: ""
                query.isEmpty() || name.contains(query, ignoreCase = true) || entry.key.contains(query)
            }.sortedByDescending { entry ->
                entry.value.maxOfOrNull { it.optLong("createdAt", 0L) } ?: 0L
            }

            val allBills = customers.sumOf { it.value.size }
            val totalAmount = customers.sumOf { entry ->
                entry.value.sumOf { it.optDouble("amount", 0.0) }
            }
            summary.text = "ग्राहक: ${customers.size}  •  बिले: $allBills\nएकूण सेवा शुल्क: ₹" +
                String.format(Locale.US, "%.2f", totalAmount)

            if (customers.isEmpty()) {
                val empty = TextView(this).apply {
                    text = "ग्राहक नोंदी उपलब्ध नाहीत किंवा शोध जुळत नाही."
                    textSize = 16f
                    setTextColor(Color.DKGRAY)
                    gravity = Gravity.CENTER
                    setPadding(12, 24, 12, 24)
                }
                results.addView(empty)
                return
            }

            customers.forEach { entry ->
                val phone = entry.key
                val bills = entry.value
                val latest = bills.maxByOrNull { it.optLong("createdAt", 0L) }!!
                val name = latest.optString("customerName", "ग्राहक")
                val total = bills.sumOf { it.optDouble("amount", 0.0) }
                val received = bills.filter {
                    val status = it.optString("paymentStatus")
                    status == "रोख भरले" || status == "UPI भरले"
                }.sumOf { it.optDouble("amount", 0.0) }
                val pending = bills.filter { it.optString("paymentStatus") == "बाकी" }
                    .sumOf { it.optDouble("amount", 0.0) }
                val latestService = latest.optString("service")
                val row = TextView(this).apply {
                    text = "$name\nमोबाईल: $phone\nएकूण बिले: ${bills.size}  |  एकूण: ₹" +
                        String.format(Locale.US, "%.2f", total) +
                        "\nजमा: ₹" + String.format(Locale.US, "%.2f", received) +
                        "  |  बाकी: ₹" + String.format(Locale.US, "%.2f", pending) +
                        "\nशेवटची सेवा: $latestService\nतपशील पाहण्यासाठी येथे टॅप करा"
                    textSize = 15f
                    setTextColor(Color.BLACK)
                    setPadding(14, 14, 14, 14)
                    setBackgroundColor(Color.rgb(240, 245, 250))
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        val details = bills.sortedByDescending { it.optLong("createdAt", 0L) }
                            .joinToString("\n\n") { bill ->
                                "बिल: ${bill.optString("billNumber")}\nदिनांक: ${bill.optString("date")}" +
                                    "\nसेवा: ${bill.optString("service")}\nरक्कम: ₹" +
                                    String.format(Locale.US, "%.2f", bill.optDouble("amount", 0.0)) +
                                    "\nपेमेंट: ${bill.optString("paymentStatus")}" +
                                    "\nअर्ज क्रमांक: ${bill.optString("applicationNo")}"
                            }
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("$name - ग्राहक इतिहास")
                            .setMessage("मोबाईल: $phone\nएकूण बिले: ${bills.size}\nएकूण: ₹" +
                                String.format(Locale.US, "%.2f", total) +
                                "\nजमा: ₹" + String.format(Locale.US, "%.2f", received) +
                                "\nबाकी: ₹" + String.format(Locale.US, "%.2f", pending) +
                                "\n\n$details")
                            .setPositiveButton("बंद", null).show()
                    }
                }
                results.addView(row, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 5, 0, 5) })
            }
        }

        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { renderCustomers() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        renderCustomers()
        addButton(page, "मुख्य पानावर जा") { showHome() }
        setContentView(page)
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
            override fun afterTextChanged(s: android.text.Editable?) {}        })

        renderResults()
        addButton(page, "मुख्य पानावर जा") { showHome() }
        setContentView(page)
    }

    private fun showApplicationTracking() {
        val page = createLayout()
        addTitle(page, "अर्ज ट्रॅकिंग")
        val note = TextView(this).apply {
            text = "अर्ज क्रमांक किंवा ग्राहकाचा मोबाईल नंबर टाकून अर्ज शोधा.\nस्थिती बदलण्यासाठी अर्जावर टॅप करा.\n\nटीप: सध्या नोंदी या फोनमध्येच सेव्ह होतात."
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(4, 4, 4, 12)
        }
        page.addView(note)
        val search = addField(page, "अर्ज क्रमांक / मोबाईल नंबर")
        var selectedDate = ""
        val dateLabel = TextView(this).apply {
            text = "निवडलेली तारीख: सर्व दिवस"
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(4, 6, 4, 6)
        }
        page.addView(dateLabel)
        val dateButtons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        val chooseDate = Button(this).apply { text = "तारीख निवडा"; isAllCaps = false }
        val clearDate = Button(this).apply { text = "तारीख काढा"; isAllCaps = false }
        dateButtons.addView(chooseDate, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        dateButtons.addView(clearDate, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        page.addView(dateButtons)
        val scroll = ScrollView(this)
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(results)
        page.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        val statuses = arrayOf("अर्ज प्राप्त", "प्रक्रिया सुरू", "अधिक कागदपत्रे आवश्यक", "मंजूर", "नामंजूर", "पूर्ण")
        fun render() {
            results.removeAllViews()
            val records = loadBillRecords()
            val query = search.text.toString().trim()
            var count = 0
            for (i in records.length() - 1 downTo 0) {
                val item = records.optJSONObject(i) ?: continue
                val appNo = item.optString("applicationNo", "GAPP" + item.optString("billNumber").takeLast(8))
                val phone = item.optString("phone")
                val name = item.optString("customerName")
                val matchesQuery = query.isEmpty() || appNo.contains(query, true) || phone.contains(query) || name.contains(query, true)
                val matchesDate = selectedDate.isEmpty() || SimpleDateFormat("yyyyMMdd", Locale.US)
                    .format(Date(item.optLong("createdAt", 0L))) == selectedDate
                if (!matchesQuery || !matchesDate) continue
                count++
                val billNo = item.optString("billNumber")
                val status = item.optString("applicationStatus", "अर्ज प्राप्त")
                val row = TextView(this).apply {
                    text = "अर्ज क्र.: " + appNo + "\nग्राहक: " + name + "\nमोबाईल: " + phone + "\nसेवा: " + item.optString("service") + "\nस्थिती: " + status
                    textSize = 16f
                    setTextColor(Color.BLACK)
                    setPadding(14, 14, 14, 14)
                    setBackgroundColor(Color.rgb(240, 245, 250))
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("अर्ज पर्याय")
                            .setItems(arrayOf("अर्जाची स्थिती बदला", "अर्ज पावती PDF / WhatsApp")) { _, choice ->
                                if (choice == 0) {
                                    val selected = statuses.indexOf(status).let { if (it < 0) 0 else it }
                                    AlertDialog.Builder(this@MainActivity).setTitle("अर्ज स्थिती बदला")
                                        .setSingleChoiceItems(statuses, selected) { dialog, which ->
                                            updateApplicationStatus(billNo, statuses[which])
                                            dialog.dismiss()
                                            render()
                                        }.setNegativeButton("रद्द", null).show()
                                } else {
                                    showApplicationReceipt(item)
                                }
                            }.setNegativeButton("बंद", null).show()
                    }
                }
                results.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 5, 0, 5) })
            }
            if (count == 0) results.addView(TextView(this).apply { text = "अर्ज सापडला नाही."; textSize = 16f; setTextColor(Color.DKGRAY); gravity = Gravity.CENTER; setPadding(12, 24, 12, 24) })
            dateLabel.text = if (selectedDate.isEmpty()) "निवडलेली तारीख: सर्व दिवस" else
                "निवडलेली तारीख: " + SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    .format(SimpleDateFormat("yyyyMMdd", Locale.US).parse(selectedDate)!!)
        }
        chooseDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val chosen = java.util.Calendar.getInstance().apply { set(year, month, day, 12, 0, 0) }
                selectedDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(chosen.time)
                render()
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }
        clearDate.setOnClickListener { selectedDate = ""; render() }
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { render() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        render()
        addButton(page, "मुख्य पानावर जा") { showHome() }
        setContentView(page)
    }

    private fun showApplicationReceipt(item: JSONObject) {
        val appNo = item.optString("applicationNo", "GAPP" + item.optString("billNumber").takeLast(8))
        val billNo = item.optString("billNumber")
        val date = item.optString("date")
        val receipt = """
            GOPAL CSC CENTER
            ==============================
                 अर्ज प्राप्ती पावती
            ==============================
            अर्ज क्रमांक: $appNo
            बिल क्रमांक: $billNo
            दिनांक: $date

            ग्राहकाचे नाव: ${item.optString("customerName")}
            मोबाईल नंबर: ${item.optString("phone")}
            सेवा: ${item.optString("service")}
            संदर्भ क्रमांक: ${item.optString("reference").ifBlank { "नाही" }}

            अर्जाची सद्यस्थिती:
            ${item.optString("applicationStatus", "अर्ज प्राप्त")}

            सेवा शुल्क: Rs. ${String.format(Locale.US, "%.2f", item.optDouble("amount", 0.0))}
            पेमेंट स्थिती: ${item.optString("paymentStatus")}

            संचालक: Suresh Jadhav
            संपर्क: 7875277904
            UPI: 7875277904@ybl
            ==============================
                 धन्यवाद!
        """.trimIndent()
        val safeName = appNo.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val pdf = createReceiptPdf(receipt, "Application-$safeName")
        AlertDialog.Builder(this).setTitle("अर्ज पावती")
            .setMessage(receipt)
            .setItems(arrayOf("PDF डाउनलोड", "प्रिंट", "WhatsApp शेअर")) { _, choice ->
                when (choice) {
                    0 -> savePdf(pdf, "Arj-Pavati-$safeName")
                    1 -> openPdf(pdf)
                    2 -> sharePdf(pdf)
                }
            }
            .setNegativeButton("बंद", null).show()
    }

    private fun updateApplicationStatus(billNumber: String, status: String) {
        val prefs = getSharedPreferences("gopal_csc_records", MODE_PRIVATE)
        val records = loadBillRecords()
        for (i in 0 until records.length()) {
            val item = records.optJSONObject(i) ?: continue
            if (item.optString("billNumber") == billNumber) { item.put("applicationStatus", status); break }
        }
        prefs.edit().putString("bills", records.toString()).apply()
        showMessage("अर्ज स्थिती अपडेट झाली.")
    }


    private var pendingBackupJson: String? = null

    private fun showBackupRestore() {
        AlertDialog.Builder(this)
            .setTitle("डेटा बॅकअप / Restore")
            .setMessage("बिलांची आणि Document Checklist मधील माहितीचा JSON बॅकअप तयार करा. Restore केल्यास बॅकअपमधील माहिती या फोनवर परत येईल.")
            .setPositiveButton("बॅकअप तयार करा") { _, _ -> createBackupFile() }
            .setNeutralButton("बॅकअप Restore") { _, _ -> chooseBackupFile() }
            .setNegativeButton("बंद", null)
            .show()
    }

    private fun createBackupFile() {
        val root = JSONObject()
        root.put("app", "Gopal CSC Center")
        root.put("backupVersion", 1)
        root.put("createdAt", System.currentTimeMillis())
        val records = JSONObject()
        records.put("bills", getSharedPreferences("gopal_csc_records", MODE_PRIVATE).getString("bills", "[]") ?: "[]")
        root.put("records", records)

        val checklistPrefs = getSharedPreferences("gopal_csc_checklists", MODE_PRIVATE)
        val checklist = JSONObject()
        checklistPrefs.all.forEach { (key, value) ->
            when (value) {
                is String -> checklist.put(key, JSONObject().put("type", "string").put("value", value))
                is Set<*> -> {
                    val values = JSONArray()
                    value.filterIsInstance<String>().forEach { values.put(it) }
                    checklist.put(key, JSONObject().put("type", "set").put("value", values))
                }
            }
        }
        root.put("checklists", checklist)
        pendingBackupJson = root.toString(2)

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "Gopal-CSC-Backup.json")
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, 702)
    }

    private fun chooseBackupFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, 703)
    }

    private fun restoreBackup(uri: android.net.Uri) {
        try {
            val jsonText = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: throw Exception("फाइल वाचता आली नाही")
            val root = JSONObject(jsonText)
            if (root.optString("app") != "Gopal CSC Center" || !root.has("records") || !root.has("checklists")) {
                throw Exception("ही Gopal CSC Center ची वैध बॅकअप फाइल नाही.")
            }
            val bills = root.getJSONObject("records").optString("bills", "[]")
            JSONArray(bills)
            val checklist = root.getJSONObject("checklists")
            val recordsPrefs = getSharedPreferences("gopal_csc_records", MODE_PRIVATE)
            val checklistPrefs = getSharedPreferences("gopal_csc_checklists", MODE_PRIVATE)
            val checklistEditor = checklistPrefs.edit().clear()
            val keys = checklist.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val entry = checklist.getJSONObject(key)
                when (entry.optString("type")) {
                    "string" -> checklistEditor.putString(key, entry.optString("value"))
                    "set" -> {
                        val values = entry.optJSONArray("value") ?: JSONArray()
                        val set = mutableSetOf<String>()
                        for (i in 0 until values.length()) set.add(values.optString(i))
                        checklistEditor.putStringSet(key, set)
                    }
                }
            }
            recordsPrefs.edit().putString("bills", bills).apply()
            checklistEditor.apply()
            showMessage("बॅकअप Restore यशस्वी झाले.")
            showHome()
        } catch (e: Exception) {
            showMessage("Restore अयशस्वी: " + (e.message ?: "फाइल तपासा"))
        }
    }

    private fun showDocumentChecklist() {
        val page = createLayout()
        addTitle(page, "आवश्यक कागदपत्रांची यादी")

        val serviceLabel = TextView(this).apply {
            text = "सेवा निवडा"
            textSize = 16f
            setTextColor(Color.DKGRAY)
        }
        page.addView(serviceLabel)
        val serviceSpinner = Spinner(this)
        serviceSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, services
        )
        page.addView(serviceSpinner)

        val hint = TextView(this).apply {
            text = "कागदपत्रे टिकमार्क करा. नवीन कागदपत्र Add करा किंवा नको असलेले Delete करा."
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 10, 0, 10)
        }
        page.addView(hint)

        val addRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val newDocument = EditText(this).apply {
            setHint("नवीन कागदपत्राचे नाव")
            textSize = 16f
            setSingleLine(true)
            setPadding(10, 8, 10, 8)
        }
        addRow.addView(newDocument, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val addDocument = Button(this).apply {
            text = "Add"
            isAllCaps = false        }
        addRow.addView(addDocument)
        page.addView(addRow)

        val scroll = ScrollView(this)
        val checklistLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(checklistLayout)
        page.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        val defaultChecklist = mapOf(
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

        val prefs = getSharedPreferences("gopal_csc_checklists", MODE_PRIVATE)
        var currentService = services.first()

        fun documentKey(service: String) = "documents_" + service.hashCode()
        fun checkedKey(service: String) = "checked_" + service.hashCode()

        fun loadDocuments(service: String): MutableList<String> {
            val savedJson = prefs.getString(documentKey(service), null)
            if (savedJson != null) {
                return try {
                    val array = JSONArray(savedJson)
                    MutableList(array.length()) { index -> array.optString(index) }
                        .filter { it.isNotBlank() }.toMutableList()
                } catch (_: Exception) {
                    (defaultChecklist[service] ?: listOf("संबंधित सेवेसाठी आवश्यक कागदपत्रे")).toMutableList()
                }
            }
            return (defaultChecklist[service] ?: listOf("संबंधित सेवेसाठी आवश्यक कागदपत्रे")).toMutableList()
        }

        fun saveDocuments(service: String, documents: List<String>) {
            val array = JSONArray()
            documents.forEach { array.put(it) }
            prefs.edit().putString(documentKey(service), array.toString()).apply()
        }

        fun renderChecklist(service: String) {
            currentService = service
            checklistLayout.removeAllViews()
            val documents = loadDocuments(service)
            val checked = prefs.getStringSet(checkedKey(service), emptySet())?.toMutableSet() ?: mutableSetOf()

            documents.forEach { document ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(4, 2, 4, 2)
                }
                val check = CheckBox(this).apply {
                    text = document
                    textSize = 16f
                    isChecked = checked.contains(document)
                    setPadding(8, 8, 8, 8)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) checked.add(document) else checked.remove(document)
                        prefs.edit().putStringSet(checkedKey(service), HashSet(checked)).apply()
                    }
                }
                row.addView(check, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                val delete = Button(this).apply {
                    text = "Delete"
                    textSize = 12f
                    isAllCaps = false
                    setOnClickListener {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("कागदपत्र Delete करायचे?")
                            .setMessage(document)
                            .setPositiveButton("Delete") { _, _ ->
                                val updated = loadDocuments(service).filterNot { it == document }
                                saveDocuments(service, updated)
                                checked.remove(document)
                                prefs.edit().putStringSet(checkedKey(service), HashSet(checked)).apply()
                                renderChecklist(service)
                                showMessage("कागदपत्र Delete झाले.")
                            }
                            .setNegativeButton("रद्द", null)
                            .show()
                    }
                }
                row.addView(delete)
                checklistLayout.addView(row, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ))
            }

            val clear = Button(this).apply {
                text = "या सेवेची टिकमार्क यादी रीसेट करा"
                isAllCaps = false
                setOnClickListener {
                    prefs.edit().remove(checkedKey(service)).apply()
                    renderChecklist(service)
                }
            }
            checklistLayout.addView(clear)
        }

        addDocument.setOnClickListener {
            val name = newDocument.text.toString().trim()
            if (name.isEmpty()) {
                newDocument.error = "कागदपत्राचे नाव टाका"
                return@setOnClickListener
            }
            val documents = loadDocuments(currentService)
            if (documents.any { it.equals(name, ignoreCase = true) }) {
                newDocument.error = "हे कागदपत्र आधीच आहे"
                return@setOnClickListener
            }
            documents.add(name)
            saveDocuments(currentService, documents)
            newDocument.text.clear()
            renderChecklist(currentService)
            showMessage("नवीन कागदपत्र Add झाले.")
        }

        serviceSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                renderChecklist(services[position])
            }
        }

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
    private fun showServiceIncomeReport() {
        val records = loadBillRecords()
        val totals = linkedMapOf<String, DoubleArray>()
        var allBills = 0
        var allReceived = 0.0
        var allPending = 0.0

        for (i in 0 until records.length()) {
            val item = records.optJSONObject(i) ?: continue
            val service = item.optString("service", "इतर सेवा")
            val amount = item.optDouble("amount", 0.0)
            val status = item.optString("paymentStatus")
            val values = totals.getOrPut(service) { doubleArrayOf(0.0, 0.0, 0.0) }
            values[0] += 1.0
            allBills++
            if (status == "रोख भरले" || status == "UPI भरले") {
                values[1] += amount
                allReceived += amount
            } else {
                values[2] += amount
                allPending += amount
            }
        }

        val report = StringBuilder()
        report.append("एकूण बिले: ").append(allBills).append("\n")
        report.append("एकूण जमा: ₹").append(String.format(Locale.US, "%.2f", allReceived)).append("\n")
        report.append("एकूण बाकी: ₹").append(String.format(Locale.US, "%.2f", allPending)).append("\n")
        report.append("एकूण रक्कम: ₹").append(String.format(Locale.US, "%.2f", allReceived + allPending))
        report.append("\n\nसेवानुसार तपशील\n------------------------------")
        val orderedServices = services.filter { totals.containsKey(it) } + totals.keys.filter { it !in services }
        orderedServices.forEach { service ->
            val values = totals[service] ?: return@forEach
            report.append("\n\n").append(service)
            report.append("\nबिले: ").append(values[0].toInt())
            report.append("\nजमा: ₹").append(String.format(Locale.US, "%.2f", values[1]))
            report.append("\nबाकी: ₹").append(String.format(Locale.US, "%.2f", values[2]))
        }
        if (allBills == 0) report.append("\n\nअद्याप कोणतेही बिल तयार झालेले नाही.")

        val content = TextView(this).apply {
            text = report.toString()
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(28, 16, 28, 16)
        }
        val scroll = ScrollView(this).apply { addView(content) }
        AlertDialog.Builder(this)
            .setTitle("सेवानुसार उत्पन्न अहवाल")
            .setView(scroll)
            .setPositiveButton("ठीक आहे", null)
            .show()
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
        if (requestCode == 702 && resultCode == RESULT_OK) {
            val uri = data?.data
            val json = pendingBackupJson
            if (uri != null && json != null) {
                contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                showMessage("डेटा बॅकअप सेव्ह झाला.")
            }
            pendingBackupJson = null
        }
        if (requestCode == 703 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            AlertDialog.Builder(this)
                .setTitle("बॅकअप Restore करायचा?")
                .setMessage("Restore केल्यास या फोनवरील सध्याची बिले आणि Document Checklist बॅकअपमधील माहितीद्वारे बदलली जातील.")
                .setPositiveButton("Restore करा") { _, _ -> restoreBackup(uri) }
                .setNegativeButton("रद्द", null)
                .show()
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
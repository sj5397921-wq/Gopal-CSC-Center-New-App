package com.gopalcsccenter.app

import android.os.Bundle
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private fun showBill(
        service: String,
        customerName: String,
        phone: String,
        amount: Double,
        reference: String,
        paymentStatus: String
    ) {
        val billNumber = "GCSC" + System.currentTimeMillis().toString().takeLast(8)
        val date = SimpleDateFormat("dd-MM-yyyy  hh:mm a", Locale("mr", "IN")).format(Date())
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
            ------------------------------
            धन्यवाद!
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("बिल तयार झाले")
            .setMessage(receipt)
            .setPositiveButton("ठीक आहे", null)
            .setNeutralButton("WhatsApp शेअर") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, receipt)
                }
                startActivity(android.content.Intent.createChooser(intent, "बिल शेअर करा"))
            }
            .show()
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

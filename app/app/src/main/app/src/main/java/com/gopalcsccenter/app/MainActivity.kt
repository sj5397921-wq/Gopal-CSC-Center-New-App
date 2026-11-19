package com.gopalcsccenter.app

import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog

class MainActivity : ComponentActivity() {

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

    private fun addButton(
        parent: LinearLayout,
        text: String,
        action: () -> Unit
    ) {
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
            ).apply {
                setMargins(0, 10, 0, 10)
            }
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

        addButton(layout, "उपलब्ध सेवा") {
            showServices()
        }

        addButton(layout, "Admin Panel") {
            showAdminInfo()
        }

        addButton(layout, "Document Scanner") {
            showMessage("Document Scanner पुढील टप्प्यात जोडला जाईल.")
        }

        setContentView(layout)
    }

    private fun showServices() {
        val page = createLayout()
        addTitle(page, "उपलब्ध सेवा")

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        services.forEachIndexed { index, service ->
            val item = TextView(this).apply {
                text = "${index + 1}. $service"
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(12, 18, 12, 18)
                setBackgroundColor(Color.rgb(240, 245, 250))
            }

            list.addView(
                item,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 5, 0, 5)
                }
            )
        }

        scroll.addView(list)
        page.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        addButton(page, "मुख्य पानावर जा") {
            showHome()
        }

        setContentView(page)
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

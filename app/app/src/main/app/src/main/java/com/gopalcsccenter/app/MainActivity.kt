package com.gopalcsccenter.app

import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "GOPAL CSC CENTER"
            textSize = 27f
            setTextColor(Color.rgb(20, 80, 160))
            gravity = Gravity.CENTER
        }

        layout.addView(title)

        val subtitle = TextView(this).apply {
            text = "आपल्या सर्व डिजिटल सेवा एकाच ठिकाणी"
            textSize = 17f
            gravity = Gravity.CENTER
        }

        layout.addView(subtitle)

        val services = Button(this).apply {
            text = "उपलब्ध सेवा"
            setOnClickListener {
                showMessage("सेवा विभाग लवकरच तयार होईल")
            }
        }

        val admin = Button(this).apply {
            text = "Admin Panel"
            setOnClickListener {
                showMessage("Admin Login विभाग लवकरच तयार होईल")
            }
        }

        val scanner = Button(this).apply {
            text = "Document Scanner"
            setOnClickListener {
                showMessage("Scanner विभाग लवकरच तयार होईल")
            }
        }

        layout.addView(services)
        layout.addView(admin)
        layout.addView(scanner)

        setContentView(layout)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

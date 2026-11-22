package in.gopalcsccenter.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val navy = Color.rgb(18, 49, 91)
    private val blue = Color.rgb(30, 105, 190)
    private val ink = Color.rgb(35, 45, 60)
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun baseScreen(): LinearLayout {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(30), dp(24), dp(24))
            setBackgroundColor(Color.rgb(246, 249, 253))
        }
        return page
    }

    private fun showHome() {
        root = baseScreen()
        val header = TextView(this).apply {
            text = "GOPAL CSC CENTER"
            textSize = 25f
            setTextColor(navy)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        root.addView(header, matchWrap())
        addText(root, "आपल्या डिजिटल सेवा केंद्रात स्वागत आहे", 16f, ink, 24)
        addText(root, "कृपया पुढे जाण्यासाठी लॉगिन निवडा", 14f, Color.DKGRAY, 34)

        addAction(root, "ग्राहक लॉगिन", true) { showLogin(customer = true) }
        addAction(root, "Admin लॉगिन", false) { showLogin(customer = false) }

        addText(root, "सुरक्षित आणि सोपी सेवा", 13f, Color.GRAY, 18)
        setContentView(root)
    }

    private fun showLogin(customer: Boolean) {
        root = baseScreen()
        val title = if (customer) "ग्राहक लॉगिन" else "Admin लॉगिन"
        addText(root, "GOPAL CSC CENTER", 22f, navy, 12, true)
        addText(root, title, 21f, blue, 26, true)

        val user = EditText(this).apply {
            textSize = 16f
            singleLine = true
            hint = if (customer) "मोबाईल नंबर" else "Admin Login ID"
            inputType = if (customer) 3 else 1
        }
        root.addView(user, matchWrap(54))
        val password = EditText(this).apply {
            textSize = 16f
            singleLine = true
            hint = "पासवर्ड"
            inputType = 129
        }
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54))
        p.topMargin = dp(12)
        root.addView(password, p)

        addAction(root, "लॉगिन", true) {
            Toast.makeText(this, "लॉगिन व्यवस्था पुढील टप्प्यात सुरक्षितपणे जोडली जाईल.", Toast.LENGTH_LONG).show()
        }
        addAction(root, "मागे जा", false) { showHome() }
        addText(root, "टीप: सध्या ही पहिली स्क्रीन आवृत्ती आहे. प्रत्यक्ष खाते पडताळणी अजून जोडलेली नाही.", 13f, Color.DKGRAY, 20)
        setContentView(root)
    }

    private fun addText(parent: LinearLayout, value: String, size: Float, color: Int, top: Int, bold: Boolean = false) {
        val view = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
        val params = matchWrap()
        params.topMargin = dp(top)
        parent.addView(view, params)
    }

    private fun addAction(parent: LinearLayout, label: String, primary: Boolean, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 17f
            setTextColor(if (primary) Color.WHITE else navy)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(if (primary) blue else Color.rgb(224, 235, 248)))
            setOnClickListener { action() }
        }
        val params = matchWrap(58)
        params.topMargin = dp(16)
        parent.addView(button, params)
    }

    private fun matchWrap(height: Int = -2): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, if (height == -2) LinearLayout.LayoutParams.WRAP_CONTENT else dp(height))

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

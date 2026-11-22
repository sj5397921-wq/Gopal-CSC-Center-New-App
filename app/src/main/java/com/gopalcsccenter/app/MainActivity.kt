package com.gopalcsccenter.app

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class MainActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val blue = Color.rgb(22, 91, 166)
    private val dark = Color.rgb(35, 48, 64)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun basePage(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(24, 28, 24, 24)
        setBackgroundColor(Color.rgb(247, 250, 255))
    }

    private fun addHeading(parent: LinearLayout, title: String, subtitle: String? = null) {
        parent.addView(TextView(this).apply {
            text = title
            textSize = 26f
            setTextColor(blue)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 10)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        if (!subtitle.isNullOrBlank()) {
            parent.addView(TextView(this).apply {
                text = subtitle
                textSize = 15f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 22)
            })
        }
    }

    private fun addField(
        parent: LinearLayout,
        hint: String,
        type: Int = InputType.TYPE_CLASS_TEXT,
        maxLength: Int? = null
    ): EditText {
        val field = EditText(this).apply {
            this.hint = hint
            inputType = type
            textSize = 17f
            setSingleLine(true)
            setPadding(16, 12, 16, 12)
            if (maxLength != null) filters = arrayOf(InputFilter.LengthFilter(maxLength))
        }
        parent.addView(field, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 7, 0, 7) })
        return field
    }

    private fun addAction(parent: LinearLayout, label: String, action: () -> Unit) {
        parent.addView(Button(this).apply {
            text = label
            textSize = 16f
            isAllCaps = false
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 8, 0, 8) })
    }

    private fun showHome() {
        val page = basePage()
        addHeading(page, "GOPAL CSC CENTER", "आपल्या सर्व डिजिटल सेवा एकाच ठिकाणी")
        val spacer = TextView(this).apply { text = ""; height = 28 }
        page.addView(spacer)
        addAction(page, "ग्राहक Login") { showCustomerLogin() }
        addAction(page, "Admin Login") { showAdminLogin() }
        page.addView(TextView(this).apply {
            text = "जलद सेवा • योग्य मार्गदर्शन • विश्वासार्ह सेवा"
            textSize = 13f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 26, 0, 0)
        })
        setContentView(page)
    }

    private fun mobileEmail(mobile: String): String = "91${mobile}@gopalcsccenter.app"

    private fun validMobile(mobile: String): Boolean =
        mobile.length == 10 && mobile.all { it in '0'..'9' }

    private fun showCustomerLogin() {
        val page = basePage()
        addHeading(page, "ग्राहक Login", "आपल्या मोबाईल नंबरने लॉगिन करा")
        val mobile = addField(page, "10 अंकी मोबाईल नंबर", InputType.TYPE_CLASS_NUMBER, 10)
        val password = addField(page, "पासवर्ड", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        addAction(page, "Login") {
            val number = mobile.text.toString().trim()
            val pass = password.text.toString()
            if (!validMobile(number)) {
                mobile.error = "फक्त 10 अंकी मोबाईल नंबर टाका"
            } else if (pass.isBlank()) {
                password.error = "पासवर्ड टाका"
            } else {
                auth.signInWithEmailAndPassword(mobileEmail(number), pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) showCustomerWelcome()
                        else Toast.makeText(this, "मोबाईल नंबर किंवा पासवर्ड चुकीचा आहे.", Toast.LENGTH_LONG).show()
                    }
            }
        }
        addAction(page, "नवीन खाते तयार करा") { showCreateCustomerAccount() }
        addAction(page, "मुख्य पानावर जा") { showHome() }
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun showCreateCustomerAccount() {
        val page = basePage()
        addHeading(page, "नवीन ग्राहक खाते", "खाते तयार करण्यासाठी माहिती भरा")
        val name = addField(page, "ग्राहकाचे पूर्ण नाव")
        val mobile = addField(page, "10 अंकी मोबाईल नंबर", InputType.TYPE_CLASS_NUMBER, 10)
        val password = addField(page, "पासवर्ड (किमान 8 अक्षरे)", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val confirm = addField(page, "पासवर्ड पुन्हा टाका", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

        addAction(page, "खाते तयार करा") {
            val customerName = name.text.toString().trim()
            val number = mobile.text.toString().trim()
            val pass = password.text.toString()
            val confirmPass = confirm.text.toString()

            when {
                customerName.isBlank() -> name.error = "पूर्ण नाव टाका"
                !validMobile(number) -> mobile.error = "फक्त 10 अंकी मोबाईल नंबर टाका"
                pass.length < 8 -> password.error = "पासवर्ड किमान 8 अक्षरांचा असावा"
                pass != confirmPass -> confirm.error = "दोन्ही पासवर्ड जुळत नाहीत"
                else -> {
                    auth.createUserWithEmailAndPassword(mobileEmail(number), pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val profile = UserProfileChangeRequest.Builder()
                                    .setDisplayName(customerName)
                                    .build()
                                auth.currentUser?.updateProfile(profile)?.addOnCompleteListener {
                                    Toast.makeText(this, "ग्राहक खाते तयार झाले.", Toast.LENGTH_LONG).show()
                                    showCustomerWelcome()
                                } ?: showCustomerWelcome()
                            } else {
                                Toast.makeText(this, task.exception?.localizedMessage ?: "खाते तयार झाले नाही.", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }
        addAction(page, "Login पानावर जा") { showCustomerLogin() }
        addAction(page, "मुख्य पानावर जा") { showHome() }
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun showAdminLogin() {
        val page = basePage()
        addHeading(page, "Admin Login", "फक्त अधिकृत Admin साठी")
        val adminId = addField(page, "Admin ID", InputType.TYPE_CLASS_NUMBER)
        val password = addField(page, "पासवर्ड", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        addAction(page, "Admin Login") {
            val id = adminId.text.toString().trim()
            val pass = password.text.toString()
            if (id.isBlank()) adminId.error = "Admin ID टाका"
            else if (pass.isBlank()) password.error = "पासवर्ड टाका"
            else auth.signInWithEmailAndPassword("${id}@gopalcsccenter.app", pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && id == "636657750019") showAdminWelcome()
                    else {
                        auth.signOut()
                        Toast.makeText(this, "Admin ID किंवा पासवर्ड चुकीचा आहे.", Toast.LENGTH_LONG).show()
                    }
                }
        }
        addAction(page, "मुख्य पानावर जा") { showHome() }
        setContentView(page)
    }

    private fun showCustomerWelcome() {
        val page = basePage()
        addHeading(page, "ग्राहक Login यशस्वी", "नमस्कार, ${auth.currentUser?.displayName ?: "ग्राहक"}")
        addAction(page, "Logout") { auth.signOut(); showHome() }
        setContentView(page)
    }

    private fun showAdminWelcome() {
        val page = basePage()
        addHeading(page, "Admin Login यशस्वी", "Admin डॅशबोर्ड पुढील टप्प्यात तयार करू.")
        addAction(page, "Logout") { auth.signOut(); showHome() }
        setContentView(page)
    }
}

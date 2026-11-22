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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun homeCard(parent: LinearLayout, icon: String, title: String, subtitle: String, buttonText: String, accent: Int, action: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(22).toFloat()
                setStroke(dp(1), Color.rgb(229, 235, 245))
            }
            elevation = dp(5).toFloat()
        }
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val iconView = TextView(this).apply {
            text = icon
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(accent, Color.rgb(105, 125, 230))
            ).apply { cornerRadius = dp(18).toFloat() }
        }
        top.addView(iconView, LinearLayout.LayoutParams(dp(62), dp(62)))
        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), 0, 0, 0)
        }
        texts.addView(TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(Color.rgb(29, 43, 67))
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        texts.addView(TextView(this).apply {
            text = subtitle
            textSize = 13f
            setTextColor(Color.rgb(105, 117, 138))
            setPadding(0, dp(4), 0, 0)
        })
        top.addView(texts, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        card.addView(top)
        val button = TextView(this).apply {
            text = "$buttonText     ›"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(accent)
                cornerRadius = dp(14).toFloat()
            }
            setPadding(dp(12), dp(13), dp(12), dp(13))
            setOnClickListener { action() }
        }
        card.addView(button, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(18)
        })
        parent.addView(card, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(18)
        })
        card.setOnClickListener { action() }
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(244, 247, 253))
        }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(24))
        }

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(30), dp(20), dp(30))
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(17, 73, 145), Color.rgb(92, 75, 190), Color.rgb(111, 89, 210))
            ).apply {
                cornerRadius = dp(28).toFloat()
            }
        }
        hero.addView(TextView(this).apply {
            text = "G"
            textSize = 36f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(30, 76, 145))
            setTypeface(null, android.graphics.Typeface.BOLD)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                shape = android.graphics.drawable.GradientDrawable.OVAL
            }
        }, LinearLayout.LayoutParams(dp(76), dp(76)))
        hero.addView(TextView(this).apply {
            text = "GOPAL CSC CENTER"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(16), 0, dp(6))
        })
        hero.addView(TextView(this).apply {
            text = "आपल्या डिजिटल सेवांचे विश्वासार्ह केंद्र"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(230, 235, 255))
        })
        content.addView(hero, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(28)
        })

        content.addView(TextView(this).apply {
            text = "आपले स्वागत आहे!"
            textSize = 24f
            setTextColor(Color.rgb(29, 43, 67))
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        content.addView(TextView(this).apply {
            text = "पुढे जाण्यासाठी आपला पर्याय निवडा"
            textSize = 14f
            setTextColor(Color.rgb(112, 123, 143))
            setPadding(0, dp(5), 0, dp(20))
        })

        homeCard(content, "👤", "ग्राहक लॉगिन", "आपल्या खात्यात प्रवेश करा", "ग्राहक लॉगिन करा", Color.rgb(24, 119, 210)) { showCustomerLogin() }
        homeCard(content, "🔐", "ॲडमिन लॉगिन", "अधिकृत व्यवस्थापनासाठी", "ॲडमिन लॉगिन करा", Color.rgb(104, 78, 190)) { showAdminLogin() }

        content.addView(TextView(this).apply {
            text = "जलद सेवा  •  योग्य मार्गदर्शन  •  विश्वासार्ह सेवा"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(125, 135, 153))
            setPadding(0, dp(8), 0, dp(10))
        })
        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
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

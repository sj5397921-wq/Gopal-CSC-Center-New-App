package com.gopalcsccenter.app

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.google.firebase.storage.FirebaseStorage
import android.text.InputFilter
import android.text.InputType
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var serviceListener: ListenerRegistration? = null
    private var activeServiceName: String = ""
    private var pendingDocumentName: String = ""
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val adminEmail = "636657750019@gopalcsccenter.app"
    private val filePickerRequestCode = 741
    private val blue = Color.rgb(22, 91, 166)
    private val dark = Color.rgb(35, 48, 64)

    private fun firestoreErrorMessage(error: Exception?): String {
        val code = (error as? com.google.firebase.firestore.FirebaseFirestoreException)?.code?.name ?: "UNKNOWN"
        val detail = error?.localizedMessage?.takeIf { it.isNotBlank() } ?: "अज्ञात त्रुटी"
        return "Firestore त्रुटी ($code): $detail"
    }

    private var languageIndex: Int = 0
    private var currentScreen: String = "home"
    private val screenHistory = mutableListOf<String>()

    private fun recordScreen(screen: String) {
        if (currentScreen != screen) {
            screenHistory.add(currentScreen)
            currentScreen = screen
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (screenHistory.isNotEmpty()) {
            val previous = screenHistory.removeAt(screenHistory.lastIndex)
            when (previous) {
                "home" -> showHome()
                "customer_login" -> showCustomerLogin()
                "create_account" -> showCreateCustomerAccount()
                "admin_login" -> showAdminLogin()
                "customer_welcome" -> showCustomerWelcome()
                "admin_welcome" -> showAdminWelcome()
                "admin_documents" -> showAdminDocuments(activeServiceName)
                "admin_fee" -> showAdminWelcome()
                "customer_documents" -> showCustomerDocuments(activeServiceName)
                else -> showHome()
            }
        } else if (currentScreen != "home") {
            showHome()
        } else {
            super.onBackPressed()
        }
    }

    private fun t(key: String): String {
        val words = mapOf(
            "tagline" to listOf("आपल्या डिजिटल सेवांचे विश्वासार्ह केंद्र", "Your trusted digital services center", "आपकी विश्वसनीय डिजिटल सेवा केंद्र"),
            "welcome" to listOf("आपले स्वागत आहे!", "Welcome!", "आपका स्वागत है!"),
            "choose" to listOf("पुढे जाण्यासाठी आपला पर्याय निवडा", "Choose an option to continue", "आगे बढ़ने के लिए विकल्प चुनें"),
            "customer_login" to listOf("ग्राहक लॉगिन", "Customer Login", "ग्राहक लॉगिन"),
            "customer_sub" to listOf("आपल्या खात्यात प्रवेश करा", "Access your account", "अपने खाते में प्रवेश करें"),
            "customer_button" to listOf("ग्राहक लॉगिन करा", "Customer Login", "ग्राहक लॉगिन करें"),
            "admin_login" to listOf("ॲडमिन लॉगिन", "Admin Login", "एडमिन लॉगिन"),
            "admin_sub" to listOf("अधिकृत व्यवस्थापनासाठी", "For authorized management", "अधिकृत प्रबंधन के लिए"),
            "admin_button" to listOf("ॲडमिन लॉगिन करा", "Admin Login", "एडमिन लॉगिन करें"),
            "footer" to listOf("जलद सेवा  •  योग्य मार्गदर्शन  •  विश्वासार्ह सेवा", "Fast Service  •  Right Guidance  •  Trusted Service", "त्वरित सेवा  •  सही मार्गदर्शन  •  विश्वसनीय सेवा"),
            "customer_login_sub" to listOf("आपल्या मोबाईल नंबरने लॉगिन करा", "Login with your mobile number", "अपने मोबाइल नंबर से लॉगिन करें"),
            "mobile_hint" to listOf("10 अंकी मोबाईल नंबर", "10-digit mobile number", "10 अंकों का मोबाइल नंबर"),
            "password" to listOf("पासवर्ड", "Password", "पासवर्ड"),
            "login" to listOf("लॉगिन", "Login", "लॉगिन"),
            "mobile_error" to listOf("फक्त 10 अंकी मोबाईल नंबर टाका", "Enter exactly 10 digits", "केवल 10 अंकों का मोबाइल नंबर दर्ज करें"),
            "password_error" to listOf("पासवर्ड टाका", "Enter password", "पासवर्ड दर्ज करें"),
            "login_error" to listOf("मोबाईल नंबर किंवा पासवर्ड चुकीचा आहे.", "Incorrect mobile number or password.", "मोबाइल नंबर या पासवर्ड गलत है।"),
            "create_account" to listOf("नवीन खाते तयार करा", "Create New Account", "नया खाता बनाएँ"),
            "home" to listOf("मुख्य पानावर जा", "Go to Home", "मुख्य पृष्ठ पर जाएँ"),
            "new_account" to listOf("नवीन ग्राहक खाते", "New Customer Account", "नया ग्राहक खाता"),
            "account_sub" to listOf("खाते तयार करण्यासाठी माहिती भरा", "Enter details to create an account", "खाता बनाने के लिए जानकारी भरें"),
            "full_name" to listOf("ग्राहकाचे पूर्ण नाव", "Customer full name", "ग्राहक का पूरा नाम"),
            "password_min" to listOf("पासवर्ड (किमान 8 अक्षरे)", "Password (minimum 8 characters)", "पासवर्ड (कम से कम 8 अक्षर)"),
            "confirm_password" to listOf("पासवर्ड पुन्हा टाका", "Confirm password", "पासवर्ड फिर से दर्ज करें"),
            "create" to listOf("खाते तयार करा", "Create Account", "खाता बनाएँ"),
            "name_error" to listOf("पूर्ण नाव टाका", "Enter full name", "पूरा नाम दर्ज करें"),
            "password_min_error" to listOf("पासवर्ड किमान 8 अक्षरांचा असावा", "Password must be at least 8 characters", "पासवर्ड कम से कम 8 अक्षरों का होना चाहिए"),
            "password_match_error" to listOf("दोन्ही पासवर्ड जुळत नाहीत", "Passwords do not match", "दोनों पासवर्ड मेल नहीं खाते"),
            "account_created" to listOf("ग्राहक खाते तयार झाले.", "Customer account created.", "ग्राहक खाता बन गया।"),
            "account_failed" to listOf("खाते तयार झाले नाही.", "Account could not be created.", "खाता नहीं बन पाया।"),
            "back_login" to listOf("Login पानावर जा", "Back to Login", "लॉगिन पृष्ठ पर जाएँ"),
            "admin_id" to listOf("Admin ID", "Admin ID", "एडमिन आईडी"),
            "admin_id_error" to listOf("Admin ID टाका", "Enter Admin ID", "एडमिन आईडी दर्ज करें"),
            "admin_id_invalid" to listOf("Admin ID नेमका 12 अंकी आणि अधिकृत असावा", "Enter the authorized 12-digit Admin ID", "सही 12 अंकों की एडमिन आईडी दर्ज करें"),
            "admin_error" to listOf("Admin ID किंवा पासवर्ड चुकीचा आहे.", "Incorrect Admin ID or password.", "एडमिन आईडी या पासवर्ड गलत है।"),
            "customer_success" to listOf("ग्राहक लॉगिन यशस्वी", "Customer Login Successful", "ग्राहक लॉगिन सफल"),
            "hello" to listOf("नमस्कार", "Hello", "नमस्ते"),
            "customer" to listOf("ग्राहक", "Customer", "ग्राहक"),
            "logout" to listOf("लॉगआउट", "Logout", "लॉगआउट"),
            "admin_success" to listOf("Admin लॉगिन यशस्वी", "Admin Login Successful", "एडमिन लॉगिन सफल"),
            "admin_success_sub" to listOf("Admin डॅशबोर्ड पुढील टप्प्यात तयार करू.", "Admin dashboard will be added in the next phase.", "एडमिन डैशबोर्ड अगले चरण में जोड़ा जाएगा।")
        )
        return words[key]?.get(languageIndex) ?: key
    }

    private fun setLanguage(index: Int) {
        languageIndex = index
        getSharedPreferences("gopal_settings", MODE_PRIVATE).edit().putInt("language", index).apply()
        showHome()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageIndex = getSharedPreferences("gopal_settings", MODE_PRIVATE).getInt("language", 0).coerceIn(0, 2)

        // Firebase keeps the signed-in session across app restarts.
        // Restore the correct screen instead of showing the login home again.
        val signedInUser = auth.currentUser
        if (signedInUser != null) {
            if (signedInUser.email == "636657750019@gopalcsccenter.app") {
                showAdminWelcome()
            } else {
                showCustomerWelcome()
            }
        } else {
            showHome()
        }
    }

    private fun basePage(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(24, 28, 24, 24)
        background = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.rgb(12, 74, 145), Color.rgb(83, 69, 174), Color.rgb(126, 82, 196))
        )

    }

    private fun addHeading(parent: LinearLayout, title: String, subtitle: String? = null) {
        parent.addView(TextView(this).apply {
            text = title
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 10)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        if (!subtitle.isNullOrBlank()) {
            parent.addView(TextView(this).apply {
                text = subtitle
                textSize = 15f
                setTextColor(Color.rgb(235, 238, 255))
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
            setTextColor(Color.rgb(35, 48, 64))
            setHintTextColor(Color.rgb(120, 130, 150))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), Color.rgb(220, 226, 240))
            }
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
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(37, 112, 205))
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
        recordScreen("home")
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(244, 247, 253))
        }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(24))
        }


        val languageBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(12))
        }
        languageBar.addView(TextView(this).apply {
            text = "Language / भाषा"
            textSize = 11f
            setTextColor(Color.rgb(45, 57, 88))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, dp(7), 0)
        })
        listOf("मराठी", "English", "हिंदी").forEachIndexed { index, label ->
            languageBar.addView(TextView(this).apply {
                text = label
                textSize = 11f
                gravity = Gravity.CENTER
                setPadding(dp(9), dp(7), dp(9), dp(7))
                setTextColor(if (languageIndex == index) Color.WHITE else Color.rgb(45, 57, 88))
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (languageIndex == index) Color.rgb(37, 92, 177) else Color.rgb(230, 235, 248))
                    cornerRadius = dp(14).toFloat()
                }
                setOnClickListener { setLanguage(index) }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(3), 0, 0, 0)
            })
        }
        content.addView(languageBar)

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
            text = t("tagline")
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(230, 235, 255))
        })
        content.addView(hero, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(28)
        })

        content.addView(TextView(this).apply {
            text = t("welcome")
            textSize = 24f
            setTextColor(Color.rgb(29, 43, 67))
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        content.addView(TextView(this).apply {
            text = t("choose")
            textSize = 14f
            setTextColor(Color.rgb(112, 123, 143))
            setPadding(0, dp(5), 0, dp(20))
        })

        homeCard(content, "👤", t("customer_login"), t("customer_sub"), t("customer_button"), Color.rgb(24, 119, 210)) { showCustomerLogin() }
        homeCard(content, "🔐", t("admin_login"), t("admin_sub"), t("admin_button"), Color.rgb(104, 78, 190)) { showAdminLogin() }

        content.addView(TextView(this).apply {
            text = t("footer")
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
        recordScreen("customer_login")
        val page = basePage()
        addHeading(page, t("customer_login"), t("customer_login_sub"))
        val mobile = addField(page, t("mobile_hint"), InputType.TYPE_CLASS_NUMBER, 10)
        val password = addField(page, t("password"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        addAction(page, t("login")) {
            val number = mobile.text.toString().trim()
            val pass = password.text.toString()
            if (!validMobile(number)) {
                mobile.error = t("mobile_error")
            } else if (pass.isBlank()) {
                password.error = t("password_error")
            } else {
                auth.signInWithEmailAndPassword(mobileEmail(number), pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) showCustomerWelcome()
                        else Toast.makeText(this, t("login_error"), Toast.LENGTH_LONG).show()
                    }
            }
        }
        addAction(page, t("create_account")) { showCreateCustomerAccount() }
        addAction(page, t("home")) { showHome() }
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun showCreateCustomerAccount() {
        recordScreen("create_account")
        val page = basePage()
        addHeading(page, t("new_account"), t("account_sub"))
        val name = addField(page, t("full_name"))
        val mobile = addField(page, t("mobile_hint"), InputType.TYPE_CLASS_NUMBER, 10)
        val password = addField(page, t("password_min"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val confirm = addField(page, t("confirm_password"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

        addAction(page, t("create")) {
            val customerName = name.text.toString().trim()
            val number = mobile.text.toString().trim()
            val pass = password.text.toString()
            val confirmPass = confirm.text.toString()

            when {
                customerName.isBlank() -> name.error = t("name_error")
                !validMobile(number) -> mobile.error = t("mobile_error")
                pass.length < 8 -> password.error = t("password_min_error")
                pass != confirmPass -> confirm.error = t("password_match_error")
                else -> {
                    auth.createUserWithEmailAndPassword(mobileEmail(number), pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val profile = UserProfileChangeRequest.Builder()
                                    .setDisplayName(customerName)
                                    .build()
                                auth.currentUser?.updateProfile(profile)?.addOnCompleteListener {
                                    Toast.makeText(this, t("account_created"), Toast.LENGTH_LONG).show()
                                    showCustomerWelcome()
                                } ?: showCustomerWelcome()
                            } else {
                                Toast.makeText(this, task.exception?.localizedMessage ?: t("account_failed"), Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }
        addAction(page, t("back_login")) { showCustomerLogin() }
        addAction(page, t("home")) { showHome() }
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun showAdminLogin() {
        recordScreen("admin_login")
        val page = basePage()
        addHeading(page, t("admin_login"), t("admin_sub"))
        val adminId = addField(page, t("admin_id"), InputType.TYPE_CLASS_NUMBER, 12)
        val password = addField(page, t("password"), InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        addAction(page, t("admin_button")) {
            val id = adminId.text.toString().trim()
            val pass = password.text.toString()
            if (id.isBlank()) adminId.error = t("admin_id_error")
            else if (id.length != 12 || id != "636657750019") adminId.error = t("admin_id_invalid")
            else if (pass.isBlank()) password.error = t("password_error")
            else auth.signInWithEmailAndPassword("${id}@gopalcsccenter.app", pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && id == "636657750019") showAdminWelcome()
                    else {
                        auth.signOut()
                        Toast.makeText(this, t("admin_error"), Toast.LENGTH_LONG).show()
                    }
                }
        }
        addAction(page, t("home")) { showHome() }
        setContentView(page)
    }


    private fun initialServiceList(): MutableList<Map<String, String>> = mutableListOf(
        mapOf("mr" to "आधार कार्ड सेवा", "en" to "Aadhaar Card Services", "hi" to "आधार कार्ड सेवा"),
        mapOf("mr" to "पॅन कार्ड सेवा", "en" to "PAN Card Services", "hi" to "पैन कार्ड सेवा"),
        mapOf("mr" to "MahaDBT अर्ज", "en" to "MahaDBT Application", "hi" to "MahaDBT आवेदन"),
        mapOf("mr" to "जात प्रमाणपत्र", "en" to "Caste Certificate", "hi" to "जाति प्रमाणपत्र"),
        mapOf("mr" to "उत्पन्न प्रमाणपत्र", "en" to "Income Certificate", "hi" to "आय प्रमाणपत्र"),
        mapOf("mr" to "रहिवासी प्रमाणपत्र", "en" to "Residence Certificate", "hi" to "निवास प्रमाणपत्र"),
        mapOf("mr" to "रेल्वे तिकीट बुकिंग", "en" to "Railway Ticket Booking", "hi" to "रेलवे टिकट बुकिंग"),
        mapOf("mr" to "बस तिकीट बुकिंग", "en" to "Bus Ticket Booking", "hi" to "बस टिकट बुकिंग"),
        mapOf("mr" to "विमान तिकीट बुकिंग", "en" to "Flight Ticket Booking", "hi" to "हवाई टिकट बुकिंग"),
        mapOf("mr" to "ऑनलाइन फॉर्म", "en" to "Online Forms", "hi" to "ऑनलाइन फॉर्म"),
        mapOf("mr" to "प्रिंट आणि झेरॉक्स", "en" to "Print & Xerox", "hi" to "प्रिंट और ज़ेरॉक्स")
    )

    private fun decodeServiceList(data: Map<String, Any>?): MutableList<Map<String, String>> {
        val raw = data?.get("services") as? List<*> ?: return initialServiceList()
        return raw.mapNotNull { entry ->
            val item = entry as? Map<*, *> ?: return@mapNotNull null
            val mr = item["mr"]?.toString()?.trim().orEmpty()
            if (mr.isBlank()) return@mapNotNull null
            mapOf("mr" to mr, "en" to (item["en"]?.toString() ?: mr), "hi" to (item["hi"]?.toString() ?: mr))
        }.toMutableList()
    }

    private fun serviceLabel(item: Map<String, String>): String =
        item[when (languageIndex) { 1 -> "en"; 2 -> "hi"; else -> "mr" }] ?: item["mr"].orEmpty()

    private fun renderServiceRow(parent: LinearLayout, item: Map<String, String>, index: Int, openAction: (() -> Unit)? = null, deleteAction: (() -> Unit)? = null) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(8), dp(10))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.rgb(246, 248, 253))
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.rgb(230, 234, 243))
            }
        }
        row.addView(TextView(this).apply {
            text = "${index + 1}.  ${serviceLabel(item)}"
            textSize = 16f
            setTextColor(Color.rgb(35, 48, 64))
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        if (deleteAction != null) {
            row.addView(TextView(this).apply {
                text = "हटवा"
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setPadding(dp(12), dp(9), dp(12), dp(9))
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.rgb(198, 55, 65))
                    cornerRadius = dp(9).toFloat()
                }
                setOnClickListener { deleteAction() }
            })
        }
        if (openAction != null) row.setOnClickListener { openAction() }
        parent.addView(row, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, dp(4), 0, dp(4)) })
    }

    private fun showCustomerWelcome() {
        recordScreen("customer_welcome")
        serviceListener?.remove()
        val page = basePage()
        addHeading(page, t("customer_success"), "${t("hello")}, ${auth.currentUser?.displayName ?: t("customer")}")
        val folder = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(18).toFloat()
                setStroke(dp(1), Color.rgb(220, 226, 240))
            }
            elevation = dp(3).toFloat()
        }
        val header = TextView(this).apply {
            text = "📁  उपलब्ध सुविधा                         ▼"
            textSize = 19f
            setTextColor(Color.rgb(35, 48, 64))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(dp(10), dp(16), dp(10), dp(16))
        }
        val items = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = android.view.View.GONE }
        folder.addView(header)
        folder.addView(items)
        header.setOnClickListener {
            val open = items.visibility != android.view.View.VISIBLE
            items.visibility = if (open) android.view.View.VISIBLE else android.view.View.GONE
            header.text = if (open) "📂  उपलब्ध सुविधा                         ▲" else "📁  उपलब्ध सुविधा                         ▼"
        }
        page.addView(folder, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, dp(10), 0, dp(18)) })

        serviceListener = db.collection("app_config").document("services")
            .addSnapshotListener { snapshot, error ->
                items.removeAllViews()
                if (error != null) {
                    items.addView(TextView(this).apply {
                        text = firestoreErrorMessage(error)
                        setTextColor(Color.RED)
                    })
                } else {
                    val list = decodeServiceList(snapshot?.data)
                    if (snapshot == null || !snapshot.exists() || list.isEmpty()) {
                        items.addView(TextView(this).apply {
                            text = "सध्या कोणतीही सेवा उपलब्ध नाही."
                            setTextColor(dark)
                            setPadding(dp(12), dp(12), dp(12), dp(12))
                        })
                    } else list.forEachIndexed { index, service -> renderServiceRow(items, service, index, openAction = { showCustomerDocuments(service["mr"].orEmpty()) }) }
                }
            }
        addAction(page, t("logout")) {
            serviceListener?.remove()
            serviceListener = null
            auth.signOut()
            screenHistory.clear()
            showHome()
        }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(page) })
    }

    private fun showAdminWelcome() {
        recordScreen("admin_welcome")
        serviceListener?.remove()
        val page = basePage()
        addHeading(page, t("admin_success"), "सेवा व्यवस्थापन")
        val folder = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(18).toFloat()
                setStroke(dp(1), Color.rgb(220, 226, 240))
            }
            elevation = dp(3).toFloat()
        }
        val header = TextView(this).apply {
            text = "📁  उपलब्ध सेवा                         ▼"
            textSize = 19f
            setTextColor(Color.rgb(35, 48, 64))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(dp(10), dp(16), dp(10), dp(16))
        }
        val items = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        folder.addView(header)
        folder.addView(items)
        header.setOnClickListener {
            val open = items.visibility != android.view.View.VISIBLE
            items.visibility = if (open) android.view.View.VISIBLE else android.view.View.GONE
            header.text = if (open) "📂  उपलब्ध सेवा                         ▲" else "📁  उपलब्ध सेवा                         ▼"
        }
        page.addView(folder, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, dp(10), 0, dp(18)) })

        val newService = addField(page, "नवीन सेवेचे नाव टाका")
        addAction(page, "＋ सेवा जोडा") {
            val label = newService.text.toString().trim()
            if (label.isBlank()) {
                newService.error = "सेवेचे नाव टाका"
            } else {
                val ref = db.collection("app_config").document("services")
                ref.get().addOnSuccessListener { snap ->
                    val list = if (snap.exists()) decodeServiceList(snap.data) else initialServiceList()
                    if (list.any { it["mr"].equals(label, ignoreCase = true) }) {
                        newService.error = "ही सेवा आधीपासून आहे"
                    } else {
                        list.add(mapOf("mr" to label, "en" to label, "hi" to label))
                        ref.set(mapOf("services" to list))
                            .addOnSuccessListener {
                                newService.text.clear()
                                Toast.makeText(this, "सेवा जोडली", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show()
                            }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show()
                }
            }
        }

        val ref = db.collection("app_config").document("services")
        serviceListener = ref.addSnapshotListener { snapshot, error ->
            items.removeAllViews()
            if (error != null) {
                items.addView(TextView(this).apply {
                    text = firestoreErrorMessage(error)
                    setTextColor(Color.RED)
                })
            } else if (snapshot != null && !snapshot.exists()) {
                ref.set(mapOf("services" to initialServiceList()))
                    .addOnFailureListener {
                        Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show()
                    }
            } else {
                val list = decodeServiceList(snapshot?.data)
                if (list.isEmpty()) {
                    items.addView(TextView(this).apply {
                        text = "सध्या कोणतीही सेवा नाही. नवीन सेवा जोडा."
                        setTextColor(dark)
                        setPadding(dp(10), dp(12), dp(10), dp(12))
                    })
                }
                list.forEachIndexed { index, service ->
                    renderServiceRow(items, service, index, deleteAction = {
                        android.app.AlertDialog.Builder(this)
                            .setTitle("सेवा हटवा")
                            .setMessage("‘${serviceLabel(service)}’ ही सेवा हटवायची आहे? ग्राहकांच्या यादीतूनही हटवली जाईल.")
                            .setNegativeButton("नाही", null)
                            .setPositiveButton("होय") { _, _ ->
                                val updated = list.toMutableList().apply { removeAt(index) }
                                ref.set(mapOf("services" to updated))
                                    .addOnFailureListener {
                                        Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show()
                                    }
                            }.show()
                    })
                    addAction(items, "📄 कागदपत्रे व्यवस्थापन") { showAdminDocuments(service["mr"].orEmpty()) }
                    addAction(items, "₹ सेवा शुल्क सेट करा") { showAdminFee(service["mr"].orEmpty()) }
                }
            }
        }

        addAction(page, t("logout")) {
            serviceListener?.remove()
            serviceListener = null
            auth.signOut()
            screenHistory.clear()
            showHome()
        }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(page) })
    }

    private fun serviceDocumentKey(name: String): String =
        Base64.encodeToString(name.trim().toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

    private fun showAdminDocuments(serviceName: String) {
        if (auth.currentUser?.email != adminEmail) {
            Toast.makeText(this, "फक्त Admin ही यादी बदलू शकतो.", Toast.LENGTH_LONG).show()
            return
        }
        activeServiceName = serviceName
        recordScreen("admin_documents")
        val page = basePage()
        addHeading(page, "कागदपत्रे व्यवस्थापन", serviceName)
        val field = addField(page, "कागदपत्राचे नाव टाका")
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        page.addView(list)
        fun refresh() {
            list.removeAllViews()
            db.collection("service_documents").document(serviceDocumentKey(serviceName)).get()
                .addOnSuccessListener { snap ->
                    val docs = (snap.get("documents") as? List<*>)?.mapNotNull { it?.toString() }?.toMutableList() ?: mutableListOf()
                    docs.forEachIndexed { i, name ->
                        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                        row.addView(TextView(this).apply { text = name; setTextColor(Color.WHITE) },
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                        row.addView(Button(this).apply {
                            text = "बदला"; isAllCaps = false
                            setOnClickListener {
                                val edit = EditText(this@MainActivity).apply { setText(name) }
                                android.app.AlertDialog.Builder(this@MainActivity).setTitle("कागदपत्र बदला").setView(edit)
                                    .setNegativeButton("रद्द", null).setPositiveButton("जतन") { _, _ ->
                                        val v = edit.text.toString().trim()
                                        if (v.isNotBlank()) { docs[i] = v; saveServiceDocuments(serviceName, docs, ::refresh) }
                                    }.show()
                            }
                        })
                        row.addView(Button(this).apply {
                            text = "हटवा"; isAllCaps = false
                            setOnClickListener {
                                docs.removeAt(i); saveServiceDocuments(serviceName, docs, ::refresh)
                            }
                        })
                        list.addView(row)
                    }
                    if (docs.isEmpty()) list.addView(TextView(this).apply { text = "कागदपत्रे जोडलेली नाहीत."; setTextColor(Color.WHITE) })
                }.addOnFailureListener { Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show() }
        }
        addAction(page, "＋ कागदपत्र जोडा") {
            val name = field.text.toString().trim()
            if (name.isBlank()) field.error = "नाव टाका"
            else db.collection("service_documents").document(serviceDocumentKey(serviceName)).get()
                .addOnSuccessListener { snap ->
                    val docs = (snap.get("documents") as? List<*>)?.mapNotNull { it?.toString() }?.toMutableList() ?: mutableListOf()
                    if (docs.any { it.equals(name, true) }) field.error = "हे आधीपासून आहे"
                    else { docs.add(name); saveServiceDocuments(serviceName, docs) { field.text.clear(); refresh() } }
                }.addOnFailureListener { Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show() }
        }
        addAction(page, "मागे जा") { showAdminWelcome() }
        refresh()
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(page) })
    }

    private fun showAdminFee(serviceName: String) {
        if (auth.currentUser?.email != adminEmail) {
            Toast.makeText(this, "फक्त Admin शुल्क बदलू शकतो.", Toast.LENGTH_LONG).show()
            return
        }
        activeServiceName = serviceName
        recordScreen("admin_fee")
        val page = basePage()
        addHeading(page, "सेवा शुल्क व्यवस्थापन", serviceName)
        val feeField = addField(page, "सेवा शुल्क रुपये मध्ये टाका", InputType.TYPE_CLASS_NUMBER)
        val ref = db.collection("service_fees").document(serviceDocumentKey(serviceName))
        ref.get().addOnSuccessListener { snap ->
            val amount = snap.getLong("amount") ?: 0L
            if (amount > 0L) feeField.setText(amount.toString())
        }.addOnFailureListener {
            Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show()
        }
        addAction(page, "शुल्क जतन करा") {
            val amount = feeField.text.toString().trim().toLongOrNull()
            if (amount == null || amount < 0L) {
                feeField.error = "योग्य शुल्क टाका"
            } else {
                ref.set(mapOf("serviceName" to serviceName, "amount" to amount))
                    .addOnSuccessListener {
                        Toast.makeText(this, "सेवा शुल्क जतन झाले.", Toast.LENGTH_SHORT).show()
                        showAdminWelcome()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show()
                    }
            }
        }
        addAction(page, "मागे जा") { showAdminWelcome() }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(page) })
    }

    private fun saveServiceDocuments(serviceName: String, docs: List<String>, done: () -> Unit) {
        db.collection("service_documents").document(serviceDocumentKey(serviceName))
            .set(mapOf("serviceName" to serviceName, "documents" to docs))
            .addOnSuccessListener { Toast.makeText(this, "कागदपत्र यादी जतन झाली.", Toast.LENGTH_SHORT).show(); done() }
            .addOnFailureListener { Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show() }
    }

    private fun showCustomerDocuments(serviceName: String) {
        activeServiceName = serviceName
        recordScreen("customer_documents")
        val page = basePage()
        addHeading(page, serviceName, "सेवा अर्ज व कागदपत्रे")

        fun sectionTitle(title: String) {
            page.addView(TextView(this).apply {
                text = title
                textSize = 19f
                setTextColor(Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, dp(18), 0, dp(8))
            })
        }

        // 1. Required documents checklist
        sectionTitle("1. आवश्यक कागदपत्रे यादी")
        val checklist = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE); cornerRadius = dp(12).toFloat()
            }
        }
        page.addView(checklist)
        db.collection("service_documents").document(serviceDocumentKey(serviceName)).get()
            .addOnSuccessListener { snap ->
                val docs = (snap.get("documents") as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
                checklist.removeAllViews()
                if (docs.isEmpty()) {
                    checklist.addView(TextView(this).apply {
                        text = "Admin ने अद्याप कागदपत्रांची यादी दिलेली नाही."
                        setTextColor(dark)
                    })
                } else docs.forEachIndexed { index, name ->
                    checklist.addView(TextView(this).apply {
                        text = "☐  ${index + 1}. $name"
                        textSize = 16f
                        setTextColor(dark)
                        setPadding(dp(4), dp(7), dp(4), dp(7))
                    })
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show()
            }

        // 2. Separate upload area
        sectionTitle("2. कागदपत्रे अपलोड सुविधा")
        val uploads = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE); cornerRadius = dp(12).toFloat()
            }
        }
        page.addView(uploads)
        db.collection("service_documents").document(serviceDocumentKey(serviceName)).get()
            .addOnSuccessListener { snap ->
                val docs = (snap.get("documents") as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
                uploads.removeAllViews()
                if (docs.isEmpty()) {
                    uploads.addView(TextView(this).apply {
                        text = "अपलोड करण्यासाठी कागदपत्रे उपलब्ध नाहीत."
                        setTextColor(dark)
                    })
                } else docs.forEach { name ->
                    val row = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dp(4), dp(5), dp(4), dp(5))
                    }
                    row.addView(TextView(this).apply {
                        text = "📄 $name"
                        textSize = 15f
                        setTextColor(dark)
                    }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    row.addView(Button(this).apply {
                        text = "अपलोड करा"
                        isAllCaps = false
                        setOnClickListener {
                            pendingDocumentName = name
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/jpeg", "image/png"))
                            }
                            startActivityForResult(intent, filePickerRequestCode)
                        }
                    })
                    uploads.addView(row)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show()
            }

        // 3. Automatically calculated quotation using Admin-configured fee
        sectionTitle("3. कोटेशन बिल")
        val quotation = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), Color.rgb(220, 226, 240))
            }
        }
        page.addView(quotation)
        quotation.addView(TextView(this).apply {
            text = "गोपाल CSC सेंटर"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(dark)
        })
        quotation.addView(TextView(this).apply {
            text = "सेवा: $serviceName"
            textSize = 15f
            setTextColor(dark)
            setPadding(0, dp(8), 0, dp(8))
        })
        val feeLine = TextView(this).apply {
            text = "सेवा शुल्क: शुल्क लोड होत आहे..."
            textSize = 17f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(blue)
        }
        quotation.addView(feeLine)
        quotation.addView(TextView(this).apply {
            text = "एकूण रक्कम"
            textSize = 14f
            setTextColor(dark)
            setPadding(0, dp(12), 0, 0)
        })
        val totalLine = TextView(this).apply {
            text = "₹ --"
            textSize = 25f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(20, 130, 75))
        }
        quotation.addView(totalLine)
        db.collection("service_fees").document(serviceDocumentKey(serviceName)).get()
            .addOnSuccessListener { snap ->
                val amount = snap.getLong("amount") ?: 0L
                feeLine.text = "सेवा शुल्क: ₹ $amount"
                totalLine.text = "₹ $amount"
            }
            .addOnFailureListener {
                feeLine.text = "सेवा शुल्क उपलब्ध नाही."
                totalLine.text = "₹ --"
                Toast.makeText(this, firestoreErrorMessage(it), Toast.LENGTH_LONG).show()
            }

        addAction(page, "मागे जा") { showCustomerWelcome() }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(page) })
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == filePickerRequestCode && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            uploadCustomerDocument(uri, pendingDocumentName, activeServiceName)
        }
    }

    private fun uploadCustomerDocument(uri: Uri, documentName: String, serviceName: String) {
        val user = auth.currentUser ?: return
        val mime = contentResolver.getType(uri).orEmpty()
        if (mime !in listOf("application/pdf", "image/jpeg", "image/png")) {
            Toast.makeText(this, "फक्त PDF, JPG किंवा PNG निवडा.", Toast.LENGTH_LONG).show(); return
        }
        val size = contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
        if (size > 10L * 1024L * 1024L) {
            Toast.makeText(this, "फाइल 10 MB पेक्षा कमी असावी.", Toast.LENGTH_LONG).show(); return
        }
        val extension = when (mime) { "application/pdf" -> "pdf"; "image/png" -> "png"; else -> "jpg" }
        val fileName = "doc_" + System.currentTimeMillis() + "." + extension
        val objectPath = "service_uploads/" + user.uid + "/" + serviceDocumentKey(serviceName) + "/" + fileName
        val ref = storage.reference.child(objectPath)
        Toast.makeText(this, "अपलोड सुरू आहे...", Toast.LENGTH_SHORT).show()
        ref.putFile(uri).continueWithTask { task ->
            if (!task.isSuccessful) throw (task.exception ?: Exception("Upload failed"))
            ref.downloadUrl
        }.addOnSuccessListener { download ->
            db.collection("service_document_uploads").add(mapOf(
                "uid" to user.uid, "serviceName" to serviceName, "documentName" to documentName,
                "storagePath" to objectPath, "downloadUrl" to download.toString(), "contentType" to mime,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )).addOnSuccessListener {
                Toast.makeText(this, "कागदपत्र अपलोड झाले.", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(this, "फाइल अपलोड झाली; नोंद जतन झाली नाही.", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { Toast.makeText(this, "अपलोड अयशस्वी: " + it.localizedMessage, Toast.LENGTH_LONG).show() }
    }

}

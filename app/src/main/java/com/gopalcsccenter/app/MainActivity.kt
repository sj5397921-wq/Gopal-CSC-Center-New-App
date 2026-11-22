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

    private fun showCustomerWelcome() {
        recordScreen("customer_welcome")
        val page = basePage()
        addHeading(page, t("customer_success"), "${t("hello")}, ${auth.currentUser?.displayName ?: t("customer")}")

        val services = listOf(
            listOf("आधार कार्ड सेवा", "Aadhaar Card Services", "आधार कार्ड सेवा"),
            listOf("पॅन कार्ड सेवा", "PAN Card Services", "पैन कार्ड सेवा"),
            listOf("MahaDBT अर्ज", "MahaDBT Application", "MahaDBT आवेदन"),
            listOf("जात प्रमाणपत्र", "Caste Certificate", "जाति प्रमाणपत्र"),
            listOf("उत्पन्न प्रमाणपत्र", "Income Certificate", "आय प्रमाणपत्र"),
            listOf("रहिवासी प्रमाणपत्र", "Residence Certificate", "निवास प्रमाणपत्र"),
            listOf("रेल्वे तिकीट बुकिंग", "Railway Ticket Booking", "रेलवे टिकट बुकिंग"),
            listOf("बस तिकीट बुकिंग", "Bus Ticket Booking", "बस टिकट बुकिंग"),
            listOf("विमान तिकीट बुकिंग", "Flight Ticket Booking", "हवाई टिकट बुकिंग"),
            listOf("ऑनलाइन फॉर्म", "Online Forms", "ऑनलाइन फॉर्म"),
            listOf("प्रिंट आणि झेरॉक्स", "Print & Xerox", "प्रिंट और ज़ेरॉक्स")
        )

        services.forEachIndexed { index, names ->
            page.addView(TextView(this).apply {
                text = "${index + 1}.  ${names[languageIndex]}"
                textSize = 17f
                setTextColor(Color.rgb(35, 48, 64))
                setPadding(dp(16), dp(14), dp(16), dp(14))
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dp(12).toFloat()
                    setStroke(dp(1), Color.rgb(220, 226, 240))
                }
                elevation = dp(2).toFloat()
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(5), 0, dp(5))
            })
        }

        addAction(page, t("logout")) { auth.signOut(); screenHistory.clear(); showHome() }
        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(page)
        })
    }

    private fun showAdminWelcome() {
        recordScreen("admin_welcome")
        val page = basePage()
        addHeading(page, t("admin_success"), t("admin_success_sub"))
        addAction(page, t("logout")) { auth.signOut(); showHome() }
        setContentView(page)
    }
}

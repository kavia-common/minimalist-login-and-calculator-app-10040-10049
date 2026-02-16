package org.example.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlin.math.abs

class MainActivity : Activity() {

    private lateinit var headerTitle: TextView
    private lateinit var headerSubtitle: TextView

    private lateinit var loginContainer: View
    private lateinit var signupContainer: View
    private lateinit var dashboardContainer: View

    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginError: TextView
    private lateinit var loginButton: Button
    private lateinit var goToSignUpButton: Button

    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupConfirmPassword: EditText
    private lateinit var signupError: TextView
    private lateinit var signupButton: Button
    private lateinit var backToLoginButton: Button

    private lateinit var welcomeText: TextView
    private lateinit var numberA: EditText
    private lateinit var numberB: EditText
    private lateinit var calcError: TextView
    private lateinit var resultText: TextView
    private lateinit var addButton: Button
    private lateinit var subButton: Button
    private lateinit var mulButton: Button
    private lateinit var divButton: Button
    private lateinit var logoutButton: Button

    // Very simple local "auth" for demo purposes:
    // - accounts exist only in-memory for the current app process
    // - password validation is minimal
    private val accounts: MutableMap<String, String> = linkedMapOf()
    private var currentUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        wireEvents()

        // Seed an example account for convenience.
        accounts["demo@example.com"] = "password"

        showLogin()
    }

    private fun bindViews() {
        headerTitle = findViewById(R.id.headerTitle)
        headerSubtitle = findViewById(R.id.headerSubtitle)

        loginContainer = findViewById(R.id.loginContainer)
        signupContainer = findViewById(R.id.signupContainer)
        dashboardContainer = findViewById(R.id.dashboardContainer)

        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPassword)
        loginError = findViewById(R.id.loginError)
        loginButton = findViewById(R.id.loginButton)
        goToSignUpButton = findViewById(R.id.goToSignUpButton)

        signupEmail = findViewById(R.id.signupEmail)
        signupPassword = findViewById(R.id.signupPassword)
        signupConfirmPassword = findViewById(R.id.signupConfirmPassword)
        signupError = findViewById(R.id.signupError)
        signupButton = findViewById(R.id.signupButton)
        backToLoginButton = findViewById(R.id.backToLoginButton)

        welcomeText = findViewById(R.id.welcomeText)
        numberA = findViewById(R.id.numberA)
        numberB = findViewById(R.id.numberB)
        calcError = findViewById(R.id.calcError)
        resultText = findViewById(R.id.resultText)
        addButton = findViewById(R.id.addButton)
        subButton = findViewById(R.id.subButton)
        mulButton = findViewById(R.id.mulButton)
        divButton = findViewById(R.id.divButton)
        logoutButton = findViewById(R.id.logoutButton)
    }

    private fun wireEvents() {
        goToSignUpButton.setOnClickListener {
            clearAuthErrors()
            showSignUp()
        }

        backToLoginButton.setOnClickListener {
            clearAuthErrors()
            showLogin()
        }

        loginButton.setOnClickListener {
            clearAuthErrors()
            attemptLogin()
        }

        signupButton.setOnClickListener {
            clearAuthErrors()
            attemptSignUp()
        }

        addButton.setOnClickListener { compute(Operation.ADD) }
        subButton.setOnClickListener { compute(Operation.SUBTRACT) }
        mulButton.setOnClickListener { compute(Operation.MULTIPLY) }
        divButton.setOnClickListener { compute(Operation.DIVIDE) }

        logoutButton.setOnClickListener {
            currentUserEmail = null
            clearCalculatorState()
            showLogin()
        }
    }

    private fun showLogin() {
        headerTitle.text = getString(R.string.app_name)
        headerSubtitle.text = getString(R.string.app_name)

        loginContainer.visibility = View.VISIBLE
        signupContainer.visibility = View.GONE
        dashboardContainer.visibility = View.GONE
    }

    private fun showSignUp() {
        headerTitle.text = getString(R.string.app_name)
        headerSubtitle.text = getString(R.string.app_name)

        loginContainer.visibility = View.GONE
        signupContainer.visibility = View.VISIBLE
        dashboardContainer.visibility = View.GONE
    }

    private fun showDashboard(email: String) {
        headerTitle.text = getString(R.string.app_name)
        headerSubtitle.text = getString(R.string.app_name)

        currentUserEmail = email
        welcomeText.text = "Welcome, $email"
        resultText.text = "Result: —"
        calcError.visibility = View.GONE

        loginContainer.visibility = View.GONE
        signupContainer.visibility = View.GONE
        dashboardContainer.visibility = View.VISIBLE
    }

    private fun clearAuthErrors() {
        loginError.visibility = View.GONE
        signupError.visibility = View.GONE
    }

    private fun setLoginError(message: String) {
        loginError.text = message
        loginError.visibility = View.VISIBLE
    }

    private fun setSignUpError(message: String) {
        signupError.text = message
        signupError.visibility = View.VISIBLE
    }

    private fun attemptLogin() {
        val email = loginEmail.text?.toString()?.trim().orEmpty()
        val password = loginPassword.text?.toString().orEmpty()

        if (!isValidEmail(email)) {
            setLoginError("Please enter a valid email.")
            return
        }
        if (password.isBlank()) {
            setLoginError("Please enter your password.")
            return
        }

        val stored = accounts[email]
        if (stored == null || stored != password) {
            setLoginError("Invalid email or password.")
            return
        }

        // Success: clear password field for privacy.
        loginPassword.setText("")
        showDashboard(email)
    }

    private fun attemptSignUp() {
        val email = signupEmail.text?.toString()?.trim().orEmpty()
        val password = signupPassword.text?.toString().orEmpty()
        val confirm = signupConfirmPassword.text?.toString().orEmpty()

        if (!isValidEmail(email)) {
            setSignUpError("Please enter a valid email.")
            return
        }
        if (accounts.containsKey(email)) {
            setSignUpError("An account with this email already exists.")
            return
        }
        if (password.length < 6) {
            setSignUpError("Password must be at least 6 characters.")
            return
        }
        if (password != confirm) {
            setSignUpError("Passwords do not match.")
            return
        }

        accounts[email] = password

        // Clear sign-up fields then take user to dashboard
        signupPassword.setText("")
        signupConfirmPassword.setText("")
        showDashboard(email)
    }

    private fun clearCalculatorState() {
        numberA.setText("")
        numberB.setText("")
        calcError.visibility = View.GONE
        resultText.text = "Result: —"
    }

    private fun compute(op: Operation) {
        calcError.visibility = View.GONE

        val a = parseDoubleOrNull(numberA.text?.toString())
        val b = parseDoubleOrNull(numberB.text?.toString())

        if (a == null || b == null) {
            showCalcError("Please enter valid numbers for A and B.")
            return
        }

        if (op == Operation.DIVIDE && abs(b) < 1e-12) {
            showCalcError("Cannot divide by zero.")
            return
        }

        val result = when (op) {
            Operation.ADD -> a + b
            Operation.SUBTRACT -> a - b
            Operation.MULTIPLY -> a * b
            Operation.DIVIDE -> a / b
        }

        resultText.text = "Result: ${formatNumber(result)}"
    }

    private fun showCalcError(message: String) {
        calcError.text = message
        calcError.visibility = View.VISIBLE
    }

    private fun parseDoubleOrNull(value: String?): Double? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return trimmed.toDoubleOrNull()
    }

    private fun formatNumber(value: Double): String {
        // Keep UI friendly: show integers without decimal ".0"
        val asLong = value.toLong()
        return if (value == asLong.toDouble()) asLong.toString() else value.toString()
    }

    private fun isValidEmail(email: String): Boolean {
        // Basic local validation (no external deps)
        if (email.isBlank()) return false
        val at = email.indexOf('@')
        if (at <= 0 || at != email.lastIndexOf('@')) return false
        val dot = email.lastIndexOf('.')
        return dot > at + 1 && dot < email.length - 1
    }

    private enum class Operation {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }
}

package com.example.quizappproject.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.example.quizappproject.Activities.MainActivity
import com.example.quizappproject.AppDatabase
import com.example.quizappproject.Entities.UserEntity
import com.example.quizappproject.R
import com.example.quizappproject.ViewModelFactories.UserViewModelFactory
import com.example.quizappproject.ViewModels.UserViewModel
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    private fun saveUserSession(email: String, userId: String) {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_EMAIL", email)
            putString("USER_ID", userId)
            apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.activity_sign_up)

        val database = AppDatabase.getDatabase(this)
        userViewModel = ViewModelProvider(this, UserViewModelFactory(database.userDao()))
            .get(UserViewModel::class.java)

        val signupButton = findViewById<Button>(R.id.btMainS)
        val nameEditText = findViewById<EditText>(R.id.TvName)
        val emailEditText = findViewById<EditText>(R.id.tvEmail)

        signupButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            // Get the input layouts to show error messages
            val nameLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.usernameLayout)
            val emailLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.emailLayout)

            var hasError = false

            // Clear old errors
            nameLayout.error = null
            emailLayout.error = null

            if (name.isEmpty()) {
                nameLayout.error = "Name is required"
                hasError = true
            }

            if (email.isEmpty()) {
                emailLayout.error = "Email is required"
                hasError = true
            }

            if (hasError) return@setOnClickListener

            lifecycleScope.launch {
                val existingUser = userViewModel.getUserByEmail(email)
                val foundUser = userViewModel.getUserByEmail(existingUser.toString().trim())
                if (existingUser == null) {
                    val newUser = UserEntity(name = name, email = email, points = 0)
                    userViewModel.insertUser(newUser)
                    saveUserSession(email, foundUser?.id.toString())

                    runOnUiThread {
                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("USERNAME_KEY", name)
                        editor.apply()

                        val intent = Intent(this@SignUpActivity, SecondActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    runOnUiThread {
                        emailLayout.error = "User with this email already exists"
                    }
                }
            }
        }


        val goToLogin = findViewById<TextView>(R.id.tvGoToSignUp2)
        goToLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        val usernameInput = findViewById<EditText>(R.id.TvName)
        val passwordInput = findViewById<EditText>(R.id.tvEmail)


        rootLayout.setOnTouchListener { _, _ ->
            usernameInput.clearFocus()
            passwordInput.clearFocus()
            false
        }

    }
}

package com.example.quizappproject.Activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.quizappproject.AppDatabase
import com.example.quizappproject.CategoryData.categories
import com.example.quizappproject.Entities.QuestionEntity
import com.example.quizappproject.R
import com.example.quizappproject.ViewModelFactories.UserViewModelFactory
import com.example.quizappproject.ViewModels.UserViewModel
import com.google.android.material.color.DynamicColors
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.common.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    private fun saveUserSession(email: String, userId: String) {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_EMAIL", email)
            putString("USER_ID", userId)
            apply()
        }
    }

    private fun checkUserSession() {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val savedEmail = sharedPref.getString("USER_EMAIL", null)
        if (savedEmail != null) {
            startActivity(Intent(this, SecondActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        checkUserSession()
        createNotificationChannel()
        enableEdgeToEdge()
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.activity_main)
        requestNotificationPermission()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nameLayout = findViewById<TextInputLayout>(R.id.usernameLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val nameInput = findViewById<TextInputEditText>(R.id.TvName)
        val emailInput = findViewById<TextInputEditText>(R.id.tvEmail)
        val loginButton = findViewById<Button>(R.id.btMain)
        val goToSignUp = findViewById<TextView>(R.id.tvGoToSignUp2)

        val database = AppDatabase.getDatabase(this)
        userViewModel = ViewModelProvider(this, UserViewModelFactory(database.userDao()))
            .get(UserViewModel::class.java)

        insertQuestionsIfFirstTime(this, database)

        loginButton.setOnClickListener {
            val enteredName = nameInput.text.toString().trim()
            val enteredEmail = emailInput.text.toString().trim()

            nameLayout.error = null
            emailLayout.error = null

            var isValid = true

            if (enteredName.isEmpty()) {
                nameLayout.error = "Name is required"
                isValid = false
            }

            if (enteredEmail.isEmpty()) {
                emailLayout.error = "Email is required"
                isValid = false
            }

            if (isValid) {
                lifecycleScope.launch {
                    val foundUser = userViewModel.getUserByEmail(enteredEmail)

                    if (foundUser != null && foundUser.name == enteredName) {
                        saveUserSession(enteredEmail, foundUser.id.toString())
                        startActivity(Intent(this@MainActivity, SecondActivity::class.java))
                        finish()
                    } else {
                        emailLayout.error = "Invalid credentials or user not found"
                    }
                }
            }
        }

        goToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("quiz_reminder_channel", "Quiz Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel for daily quiz reminder"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun insertQuestionsIfFirstTime(context: Context, database: AppDatabase) {
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isInserted = prefs.getBoolean("isQuestionsInserted", false)

        if (!isInserted) {
            val questionsJson = context.assets.open("quiz_questions.json")
                .bufferedReader()
                .use { it.readText() }

            val gson = Gson()
            val questionsType = object : TypeToken<List<QuestionEntity>>() {}.type
            val questions: List<QuestionEntity> = gson.fromJson(questionsJson, questionsType)

            CoroutineScope(Dispatchers.IO).launch {
                database.categoryDao().insertAll(categories)
                database.questionDao().insertQuestions(questions)
                prefs.edit().putBoolean("isQuestionsInserted", true).apply()
            }
        }
    }
}

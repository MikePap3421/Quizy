package com.example.quizappproject.fragments

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import com.example.quizappproject.Activities.SecondActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.quizappproject.*
import com.example.quizappproject.Entities.QuestionEntity
import com.example.quizappproject.Entities.QuizResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class   QuestionFragment : Fragment() {

    private var categoryName: String? = null
    private var currentScore = 0
    private var currentIndex = 0
    private lateinit var questionText: TextView
    private lateinit var questionImage: ImageView
    private lateinit var radioGroup: RadioGroup
    private lateinit var answerButton: Button
    private lateinit var counterText: TextView
    private var questions: List<QuestionEntity> = emptyList()

    companion object {
        private const val ARG_CATEGORY_NAME = "category_name"
        private const val NOTIFICATION_CHANNEL_ID = "quiz_reminder_channel"

        fun newInstance(categoryName: String): QuestionFragment {
            val fragment = QuestionFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY_NAME, categoryName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryName = arguments?.getString(ARG_CATEGORY_NAME)
        createNotificationChannel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_question, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        counterText = view.findViewById(R.id.questionCounterTextView)
        questionText = view.findViewById(R.id.questionTextView)
        questionImage = view.findViewById(R.id.question_img)
        radioGroup = view.findViewById(R.id.answersRadioGroup)
        answerButton = view.findViewById(R.id.submitAnswerButton)

        checkNotificationPermission()
        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt("current_index", 0)
            currentScore = savedInstanceState.getInt("current_score", 0)
            categoryName = savedInstanceState.getString("category_name")
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            questions = db.questionDao().getRandomQuestionsByCategory(
                category = categoryName ?: "",
                limit = 10
            )
            if (questions.isNotEmpty()) {
                showQuestion(questions[currentIndex])
            } else {
                questionText.text = "No questions available for this category"
                answerButton.visibility = View.GONE
            }
        }

        answerButton.setOnClickListener {
            var selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Please select an answer", Toast.LENGTH_SHORT).show()
            } else {
                checkAnswer(selectedId)
            }
        }
    }

    private fun showQuestion(question: QuestionEntity) {
        questionText.text = question.questionText
        counterText.text = "${currentIndex + 1} / ${questions.size}"

        if (!question.imageUrl.isNullOrEmpty()) {
            val resId = resources.getIdentifier(question.imageUrl, "drawable", requireContext().packageName)
            if (resId != 0) {
                questionImage.setImageResource(resId)
                questionImage.visibility = View.VISIBLE
            } else {
                questionImage.visibility = View.GONE
            }
        } else {
            questionImage.visibility = View.GONE
        }

        val answers = listOf(question.answer1, question.answer2, question.answer3, question.answer4)
        radioGroup.removeAllViews()

        answers.forEachIndexed { index, answer ->
            val radioButton = RadioButton(requireContext()).apply {
                text = answer
                id = View.generateViewId()
                tag = index
                textSize = 20f
                setTextColor(
                    ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.md_theme_scrim
                    )
                )
                buttonTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.md_theme_scrim
                )
                background = ContextCompat.getDrawable(requireContext(), R.drawable.radio_button_background)
                setPadding(32, 32, 32, 32)
            }
            radioGroup.addView(radioButton)
        }
    }

    private fun checkAnswer(selectedId: Int) {
        val selectedRadioButton = view?.findViewById<RadioButton>(selectedId)
        val selectedIndex = selectedRadioButton?.tag as? Int
        val correctAnswerIndex = questions[currentIndex].correctAnswerIndex - 1

        for (i in 0 until radioGroup.childCount) {
            radioGroup.getChildAt(i).isEnabled = false
        }
        answerButton.isEnabled = false

        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as RadioButton
            when {
                i == correctAnswerIndex -> {
                    radioButton.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_theme_tertiaryContainer
                        )
                    )
                }
                radioButton.id == selectedId && selectedIndex != correctAnswerIndex -> {
                    radioButton.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.md_theme_errorContainer
                        )
                    )
                }
            }
        }

        if (selectedIndex == correctAnswerIndex) {
            currentScore++

            selectedRadioButton?.background = ContextCompat.getDrawable(requireContext(), R.drawable.correct_answer_background)
        } else {
            selectedRadioButton?.background = ContextCompat.getDrawable(requireContext(), R.drawable.wrong_answer_background)
            val correctRadioButton = radioGroup.findViewWithTag<RadioButton>(correctAnswerIndex)
            correctRadioButton?.background = ContextCompat.getDrawable(requireContext(), R.drawable.correct_answer_background)
        }

        answerButton.isEnabled = false

        radioGroup.postDelayed({
            currentIndex++
            if (currentIndex < questions.size) {
                resetRadioButtons()
                showQuestion(questions[currentIndex])
                answerButton.isEnabled = true
            } else {
                onFinishQuiz()
            }
        }, 1500)
    }


    private fun onFinishQuiz() {
        answerButton.isEnabled = false

        val email = requireContext()
            .getSharedPreferences("user_session", Context.MODE_PRIVATE)
            .getString("USER_EMAIL", null)

        if (email != null) {
            val quizResult = QuizResult(userEmail = email, categoryName = categoryName ?: "Unknown", score=currentScore)
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(requireContext())
                db.quizResultDao().insertResult(quizResult)

                val user = db.userDao().getUserByEmail(email)
                if (user != null) {
                    user.points += currentScore
                    db.userDao().updateUser(user)
                    (activity as? SecondActivity)?.updateUserInfo()
                    updateLeaderboard(user.id.toString(), user.name, currentScore)

                }
            }
        }
        showQuizNotification(currentScore)
        scheduleReminderNotification()

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Quiz Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for quiz reminders and updates"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val manager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    private fun showQuizNotification(points: Int) {
        val intent = Intent(requireContext(), SecondActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_info_24)
            .setContentTitle("🎉 Great job!")
            .setContentText("You earned $points points!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
    }




    private fun scheduleReminderNotification() {
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + 20 * 1000
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        }
    }

    fun updateLeaderboard(userId: String, name: String, newPoints: Int) {
        val db = FirebaseFirestore.getInstance()
        val leaderboardRef = db.collection("leaderboard").document(userId)

        leaderboardRef.get().addOnSuccessListener { doc ->
            val currentPoints = doc.getLong("totalPoints") ?: 0
            val updatedEntry = LeaderboardEntry(
                userId = userId,
                name = name,
                totalPoints = currentPoints.toInt() + newPoints,
                lastUpdated = System.currentTimeMillis()
            )

            leaderboardRef.set(updatedEntry)
        }
    }

    private fun resetRadioButtons() {
        radioGroup.clearCheck()
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as RadioButton
            radioButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            radioButton.isEnabled = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current_index", currentIndex)
        outState.putInt("current_score", currentScore)
        categoryName?.let { outState.putString("category_name", it) }
    }


}

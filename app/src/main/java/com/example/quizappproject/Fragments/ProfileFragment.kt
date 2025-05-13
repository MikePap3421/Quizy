package com.example.quizappproject.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizappproject.*
import com.example.quizappproject.Adapters.ProfileCategoryAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: ProfileCategoryAdapter
    private lateinit var userNameTextView: TextView
    private lateinit var userPointsTextView: TextView
    private lateinit var userIconTextView: TextView
    private lateinit var tvUserRank: TextView
    private lateinit var tvSettings: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        recyclerView = view.findViewById(R.id.categoryRecyclerView)
        userNameTextView = view.findViewById(R.id.userName)
        userPointsTextView = view.findViewById(R.id.userPoints)
        userIconTextView = view.findViewById(R.id.userIcon)
        tvUserRank = view.findViewById(R.id.tvUserRank)
        tvSettings = view.findViewById(R.id.SettingsView)

        tvSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        loadProfileData()

        return view
    }

    private fun loadProfileData() {
        val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val email = sharedPrefs.getString("USER_EMAIL", null)

        if (email == null) {
            Toast.makeText(context, "No email found in session", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val userDao = db.userDao()
            val categoryDao = db.categoryDao()
            val resultDao = db.quizResultDao()

            val user = withContext(Dispatchers.IO) { userDao.getUserByEmail(email) }
            val allCategories = withContext(Dispatchers.IO) {
                categoryDao.getAllCategories().map { Category(it.name, it.imageResId) }
            }
            val averagesMap = withContext(Dispatchers.IO) {
                resultDao.getAverageScoresByCategory(user?.id ?: -1).associate { it.categoryName to it.avgScore }
            }

            val averageScores = allCategories.map { averagesMap[it.name] ?: 0.0 }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            categoryAdapter = ProfileCategoryAdapter(allCategories, averageScores)
            recyclerView.adapter = categoryAdapter

            user?.let {
                userNameTextView.text = it.name
                userPointsTextView.text = "Points: ${it.points}"
                userIconTextView.text = it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                updateUserRank(it.id.toString())
            }
        }
    }

    private fun updateUserRank(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("leaderboard")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val rank = snapshot.documents.indexOfFirst { it.id == userId } + 1
                tvUserRank.text = if (rank <= 0) "Not ranked yet" else "Global Rank: #$rank"
            }
            .addOnFailureListener {
                tvUserRank.text = "Rank unavailable"
            }
    }
}

package com.example.quizappproject.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import com.example.quizappproject.AppDatabase
import com.example.quizappproject.Category
import com.example.quizappproject.R
import com.example.quizappproject.Activities.MainActivity
import com.example.quizappproject.Adapters.ProfileCategoryAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    private fun logoutUser(context: Context) {
        val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: ProfileCategoryAdapter
    private lateinit var userNameTextView: TextView
    private lateinit var userPointsTextView: TextView
    private lateinit var userIconTextView: TextView
    private lateinit var tvUserRank: TextView

    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        tvUserRank = view.findViewById(R.id.tvUserRank)

        recyclerView = view.findViewById(R.id.categoryRecyclerView)
            ?: throw IllegalStateException("RecyclerView not found in layout")

        userNameTextView = view.findViewById(R.id.userName)
        userPointsTextView = view.findViewById(R.id.userPoints)
        userIconTextView = view.findViewById(R.id.userIcon)

        drawerLayout = requireActivity().findViewById(R.id.mainDrawer)
        navView = requireActivity().findViewById(R.id.top_nav_view)

        val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val email = sharedPrefs.getString("USER_EMAIL", null)

        lifecycleScope.launch {
            if (email != null) {
                val db = AppDatabase.getDatabase(requireContext())
                val userDao = db.userDao()
                val categoryDao = db.categoryDao()
                val resultDao = db.quizResultDao()

                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email)
                }

                val allCategories = withContext(Dispatchers.IO) {
                    categoryDao.getAllCategories().map { entity ->
                        Category(name = entity.name, imageResId = entity.imageResId)
                    }
                }

                val averagesMap = mutableMapOf<String, Double>()
                val averages = withContext(Dispatchers.IO) {
                    resultDao.getAverageScoresByCategory(email)
                }
                averages.forEach {
                    averagesMap[it.categoryName] = it.avgScore
                }

                val averageScores = allCategories.map { category ->
                    averagesMap[category.name] ?: 0.0
                }

                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                categoryAdapter = ProfileCategoryAdapter(allCategories, averageScores)
                recyclerView.adapter = categoryAdapter
                recyclerView.adapter = categoryAdapter

                user?.let {
                    userNameTextView.text = it.name
                    userPointsTextView.text = "Points: ${it.points}"
                    userIconTextView.text = it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    showUserRank(it.id.toString())

                    getUserRank(it.id.toString()) { rank ->
                        activity?.runOnUiThread {
                            tvUserRank.text = when (rank) {
                                -1 -> "Not ranked yet"
                                else -> "Global Rank: #$rank"
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "No email found in session", Toast.LENGTH_SHORT).show()
            }
        }
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.HomeView -> {
                    val homeFragment = HomeFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, homeFragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.Profileview -> {
                    true
                }
                R.id.Aboutview -> {
                    val AboutFragment = AboutFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AboutFragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.statsView -> {
                    val LeaderboardFragment = LeaderboardFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LeaderboardFragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.LogOutview -> {
                    Toast.makeText(context, "Logging out...", Toast.LENGTH_SHORT).show()
                    context?.let { logoutUser(it) }
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        return view
    }
    private fun showUserRank(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("leaderboard")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val rank = snapshot.documents.indexOfFirst { it.id == userId } + 1
                tvUserRank.text = when {
                    rank == 0 -> "Not ranked yet"
                    else -> "Rank #$rank"
                }
            }
            .addOnFailureListener {
                tvUserRank.text = "Rank unavailable"
            }
    }

    private fun getUserRank(userId: String, onResult: (Int) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("leaderboard")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val rank = snapshot.documents.indexOfFirst { it.id == userId } + 1
                onResult(if (rank == 0) -1 else rank)
            }
            .addOnFailureListener {
                onResult(-1)
            }
    }

}
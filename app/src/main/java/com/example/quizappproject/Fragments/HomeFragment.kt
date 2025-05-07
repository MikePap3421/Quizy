package com.example.quizappproject.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizappproject.*
import com.example.quizappproject.Activities.MainActivity
import com.example.quizappproject.Adapters.CategoryAdapter
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        val recyclerView = view.findViewById<RecyclerView>(R.id.homeRecyclerView)

        val categories = listOf(
            Category("Science", R.drawable.science),
            Category("Flags", R.drawable.flags),
            Category("Geography", R.drawable.geography),
            Category("General Knowledge", R.drawable.general_k),
            Category("History", R.drawable.history),
            Category("Coming soon", R.drawable.comming_soon)
        )
        val adapter = CategoryAdapter(categories) { selectedCategory ->
            if (selectedCategory.name.equals("Coming soon", ignoreCase = true)) {
                Toast.makeText(requireContext(), "Coming Soon! 🚧", Toast.LENGTH_SHORT).show()
            } else {
                val questionFragment = QuestionFragment.newInstance(selectedCategory.name)

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, questionFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }


        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        drawerLayout = requireActivity().findViewById(R.id.mainDrawer)
        navView = requireActivity().findViewById(R.id.top_nav_view)

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
                    val profileFragment = ProfileFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .addToBackStack(null)
                        .commit()

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
                    logoutUser(context)
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }


        val sessionPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val email = sessionPrefs.getString("USER_EMAIL", null)
        val headerView = navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.textView5)

        if (email != null) {
            val userDao = AppDatabase.getDatabase(context).userDao()
            lifecycleScope.launch {
                val user = userDao.getUserByEmail(email)
                user?.let {
                    tvUserName.text = it.name
                }
            }
        } else {
            tvUserName.text = "Guest"
        }

    }
}

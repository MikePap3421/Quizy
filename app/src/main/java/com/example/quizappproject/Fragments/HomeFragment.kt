package com.example.quizappproject.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizappproject.*
import com.example.quizappproject.Adapters.CategoryAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

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
                Toast.makeText(context, "Coming Soon! 🚧", Toast.LENGTH_SHORT).show()
            } else {
                val questionFragment = QuestionFragment.newInstance(selectedCategory.name)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, questionFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        updateDrawerHeader()
    }

    private fun updateDrawerHeader() {
        val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val email = sharedPrefs.getString("USER_EMAIL", null)

        if (email != null) {
            val userDao = AppDatabase.getDatabase(requireContext()).userDao()
            lifecycleScope.launch {
                val user = userDao.getUserByEmail(email)
                user?.let {
                    val navView = requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.top_nav_view)
                    val headerView = navView.getHeaderView(0)
                    val tvUserName = headerView.findViewById<TextView>(R.id.textView5)
                    tvUserName.text = it.name
                }
            }
        }
    }
}

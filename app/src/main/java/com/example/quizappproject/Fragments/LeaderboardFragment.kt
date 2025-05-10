package com.example.quizappproject.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizappproject.Adapters.LeaderboardAdapter
import com.example.quizappproject.LeaderboardEntry
import com.example.quizappproject.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_leaderboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.leaderboardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        fetchLeaderboardData()
    }

    private fun fetchLeaderboardData() {
        FirebaseFirestore.getInstance()
            .collection("leaderboard")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val leaderboard = result.mapNotNull { it.toObject(LeaderboardEntry::class.java) }
                recyclerView.adapter = LeaderboardAdapter(leaderboard)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load leaderboard: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}

package com.example.quizappproject.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.quizappproject.AppDatabase
import com.example.quizappproject.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class ChaseFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: android.content.SharedPreferences

    // Views
    private lateinit var higherRivalNameLetter: TextView
    private lateinit var higherRivalName: TextView
    private lateinit var higherRivalScore: TextView
    private lateinit var lowerRivalNameLetter: TextView
    private lateinit var lowerRivalName: TextView
    private lateinit var UserName: TextView
    private lateinit var lowerRivalScore: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chase, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        UserName = view.findViewById(R.id.user_initial)
        higherRivalNameLetter = view.findViewById(R.id.higher_rival_name)
        higherRivalName = view.findViewById(R.id.chasedview)
        lowerRivalName = view.findViewById(R.id.chaserview)
        lowerRivalNameLetter = view.findViewById(R.id.lower_rival_name)
        higherRivalScore = view.findViewById(R.id.higher_rival_score)
        lowerRivalScore = view.findViewById(R.id.lower_rival_score)



        db = FirebaseFirestore.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)

        fetchRivalsData()
    }

    private fun fetchRivalsData() {
        val currentUserId = sharedPreferences.getString("USER_ID", null) ?: run {
            Toast.makeText(requireContext(), "User not identified", Toast.LENGTH_SHORT).show()
            return
        }
        UserName.text="You"
        db.collection("leaderboard")
            .whereEqualTo("userId", currentUserId)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val currentUserScore = document.getLong("totalPoints") ?: 0L
                    findHigherRival(currentUserScore)
                    findLowerRival(currentUserScore)
                } else {
                    Toast.makeText(requireContext(), "You're not on the leaderboard yet", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load your score: ${e.message}", Toast.LENGTH_LONG).show()
            }


    }

    private fun findHigherRival(currentUserScore: Long) {
        db.collection("leaderboard")
            .whereGreaterThan("totalPoints", currentUserScore)
            .orderBy("totalPoints", Query.Direction.ASCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val higherRival = querySnapshot.documents[0]
                    higherRivalNameLetter.text = higherRival.getString("name")?.take(1)?.uppercase() ?: "?"
                    val text1 = higherRival.getString("name")
                    higherRivalName.text = "You are\nchasing $text1!"
                    higherRivalScore.text = higherRival.getLong("totalPoints")?.toString() ?: "0"
                } else {
                    higherRivalName.text = "You Are On Top!"
                    higherRivalNameLetter.text = ":)"
                    higherRivalScore.text = ""
                }
            }
            .addOnFailureListener { e ->
                higherRivalNameLetter.text = "error"
                Toast.makeText(requireContext(), "Failed to load higher rival: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ChaseFragment", "Error finding higher rival", e)
            }
    }

    private fun findLowerRival(currentUserScore: Long) {
        db.collection("leaderboard")
            .whereLessThan("totalPoints", currentUserScore)
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val lowerRival = querySnapshot.documents[0]
                    lowerRivalNameLetter.text = lowerRival.getString("name")?.take(1)?.uppercase() ?: "?"
                    val text2 = lowerRival.getString("name")
                    lowerRivalName.text = "You are being\n chased by $text2!"
                    lowerRivalScore.text = lowerRival.getLong("totalPoints")?.toString() ?: "0"
                } else {
                    lowerRivalNameLetter.text = ":("
                    lowerRivalName.text = "You Are Last!"
                    lowerRivalScore.text = ""
                }
            }
            .addOnFailureListener { e ->
                lowerRivalNameLetter.text = "error"
                Toast.makeText(
                    requireContext(),
                    "Failed to load lower rival: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("ChaseFragment", "Error finding lower rival", e)
            }

    }

}
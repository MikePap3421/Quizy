package com.example.quizappproject.fragments

import android.app.AlertDialog
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.quizappproject.Activities.MainActivity
import com.example.quizappproject.Activities.SecondActivity
import com.example.quizappproject.AppDatabase
import com.example.quizappproject.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val btnDeleteAccount = view.findViewById<Button>(R.id.btnDeleteAccount)
        val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val email = sharedPrefs.getString("USER_EMAIL", null)

        btnDeleteAccount.setOnClickListener {
            if (email != null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete your account?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            val db = AppDatabase.getDatabase(requireContext())
                            val userDao = db.userDao()
                            val user = userDao.getUserByEmail(email)

                            user?.let {
                                userDao.deleteUser(it)
                                sharedPrefs.edit().clear().apply()

                                val leaderboardRef = FirebaseFirestore.getInstance()
                                    .collection("leaderboard")
                                    .document(user.id.toString())

                                leaderboardRef.get().addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        leaderboardRef.delete()
                                            .addOnSuccessListener {
                                                // Optional: show a toast or log
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(requireContext(), "Failed to delete leaderboard entry", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }


                                val intent = Intent(requireContext(), MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(requireContext(), "No user session found", Toast.LENGTH_SHORT).show()
            }
        }

        val btnEdit = view.findViewById<Button>(R.id.btnEditName)
        val editNameInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextName)

        btnEdit.setOnClickListener {
            val newName = editNameInput.text.toString().trim()
            if (!newName.matches(Regex("^[a-zA-Z0-9 ]{1,12}$"))) {
                editNameInput.error = "Invalid name (only letters, numbers, max 20 chars)"
                return@setOnClickListener
            }
            if (newName.isEmpty()) {
                editNameInput.error = "Name cannot be empty"
                return@setOnClickListener
            }

            if (email != null) {
                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(requireContext())
                    val userDao = db.userDao()
                    val user = userDao.getUserByEmail(email)

                    user?.let {
                        it.name = newName
                        userDao.updateUser(it)

                        updateUserNameInLeaderboard(it.id.toString(), newName)

                        val sharedPrefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        sharedPrefs.edit().putString("USERNAME_KEY", newName).apply()

                        (activity as? SecondActivity)?.updateDrawerHeaderName(newName)


                    }
                }
            } else {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
        val backButton = view.findViewById<FloatingActionButton>(R.id.floatingActionButton)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_from_right,  // ProfileFragment enters from right
                    R.anim.exit_to_left       // SettingsFragment exits to left
                )
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }



        return view
    }

    private fun updateUserNameInLeaderboard(userId: String, newName: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("leaderboard").document(userId)

        docRef.update("name", newName)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
               //nothing
            }
    }


}

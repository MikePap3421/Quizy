package com.example.quizappproject

data class LeaderboardEntry(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val totalPoints: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

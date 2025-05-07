package com.example.quizappproject.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val categoryName: String,
    val score: Int
)

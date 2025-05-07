package com.example.quizappproject.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryName: String,
    val questionText: String,
    val imageUrl: String?,
    val answer1: String,
    val answer2: String,
    val answer3: String,
    val answer4: String,
    val correctAnswerIndex: Int // 1, 2, 3, or 4
)

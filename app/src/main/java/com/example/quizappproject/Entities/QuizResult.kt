package com.example.quizappproject.Entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_results",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val userEmail: String,
    val categoryName: String,
    val score: Int
)

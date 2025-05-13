package com.example.quizappproject.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.*

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["name"],
            childColumns = ["categoryName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryName"])]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryName: String,
    val questionText: String,
    val imageUrl: String?,
    val answer1: String,
    val answer2: String,
    val answer3: String,
    val answer4: String,
    val correctAnswerIndex: Int
)


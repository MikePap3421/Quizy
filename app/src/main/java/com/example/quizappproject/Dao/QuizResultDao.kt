package com.example.quizappproject.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.quizappproject.Entities.QuizResult

data class CategoryAverage(
    val categoryName: String,
    val avgScore: Double
)

@Dao
interface QuizResultDao {
    @Insert
    suspend fun insertResult(result: QuizResult)

    @Query("SELECT * FROM quiz_results WHERE userEmail = :email")
    suspend fun getResultsByUser(email: String): List<QuizResult>

    @Query("""SELECT categoryName, AVG(score) as avgScore FROM quiz_results WHERE userEmail = :email GROUP BY categoryName""")
    suspend fun getAverageScoresByCategory(email: String): List<CategoryAverage>

    @Query("SELECT * FROM quiz_results WHERE userEmail = :email ")
    suspend fun getResultsForUser(email: String): List<QuizResult>

}

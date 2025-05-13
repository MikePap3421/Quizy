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

    @Query("SELECT * FROM quiz_results WHERE userId = :userId")
    fun getResultsByUser(userId: Int): List<QuizResult>

    @Query("""SELECT categoryName, AVG(score) as avgScore FROM quiz_results WHERE userId = :userId GROUP BY categoryName""")
    suspend fun getAverageScoresByCategory(userId: Int): List<CategoryAverage>


    @Query("SELECT * FROM quiz_results WHERE userId = :userId")
    suspend fun getResultsForUser(userId: Int): List<QuizResult>


}

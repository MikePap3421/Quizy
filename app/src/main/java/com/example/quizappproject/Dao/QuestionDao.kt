package com.example.quizappproject.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quizappproject.Entities.QuestionEntity


@Dao
interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Query("SELECT * FROM questions WHERE categoryName = :categoryName")
     suspend fun getQuestionsByCategory(categoryName: String): List<QuestionEntity>

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<QuestionEntity>

    @Query("DELETE FROM questions WHERE categoryName = :category")
    suspend fun deleteQuestionsForCategory(category: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("""SELECT * FROM questions WHERE categoryName = :category ORDER BY RANDOM() LIMIT :limit""")
    suspend fun getRandomQuestionsByCategory(category: String, limit: Int): List<QuestionEntity>

}

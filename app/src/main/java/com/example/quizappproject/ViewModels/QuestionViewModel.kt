package com.example.quizappproject.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizappproject.Dao.QuestionDao
import com.example.quizappproject.Entities.QuestionEntity
import kotlinx.coroutines.launch

class QuestionViewModel(private val questionDao: QuestionDao) : ViewModel() {

    fun insertQuestions(questions: List<QuestionEntity>) {
        viewModelScope.launch {
            questionDao.insertQuestions(questions)
        }
    }

    suspend fun getQuestionsByCategory(category: String): List<QuestionEntity> {
        return questionDao.getQuestionsByCategory(category)
    }
}

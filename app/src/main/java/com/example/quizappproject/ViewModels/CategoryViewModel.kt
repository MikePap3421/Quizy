package com.example.quizappproject.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizappproject.Dao.CategoryDao
import com.example.quizappproject.Entities.CategoryEntity
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryDao: CategoryDao) : ViewModel() {

    fun insertAll(categories: List<CategoryEntity>) {
        viewModelScope.launch {
            categoryDao.insertAll(categories)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            categoryDao.deleteAllCategories()
        }
    }

}

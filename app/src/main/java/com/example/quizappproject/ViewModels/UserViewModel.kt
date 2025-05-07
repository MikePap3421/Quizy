package com.example.quizappproject.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizappproject.Dao.UserDao
import com.example.quizappproject.Entities.UserEntity
import kotlinx.coroutines.launch

class UserViewModel(private val userDao: UserDao) : ViewModel() {

    fun insertUser(user: UserEntity) {
        viewModelScope.launch {
            userDao.insertUser(user)
        }
    }
    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }
    fun getAllUsers(callback: (List<UserEntity>) -> Unit) {
        viewModelScope.launch {
            val users = userDao.getAllUsers()
            callback(users)
        }
    }
    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            userDao.deleteUser(user)
        }
    }
}

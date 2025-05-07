package com.example.quizappproject

import com.example.quizappproject.Entities.CategoryEntity

data class Category(
    val name: String,
    val imageResId: Int
)

object CategoryData {
    val categories = listOf(
        CategoryEntity("Science", R.drawable.science),
        CategoryEntity("Flags", R.drawable.flags),
        CategoryEntity("Geography", R.drawable.geography),
        CategoryEntity("General Knowledge", R.drawable.general_k),
        CategoryEntity("History", R.drawable.history),
    )
}
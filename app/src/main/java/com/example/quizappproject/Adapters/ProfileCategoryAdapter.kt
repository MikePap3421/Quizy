package com.example.quizappproject.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.example.quizappproject.Category
import com.example.quizappproject.R

class ProfileCategoryAdapter(
    private val categories: List<Category>,
    private val averageScores: List<Double>
) : RecyclerView.Adapter<ProfileCategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.categoryLabel)
        val average: TextView = view.findViewById(R.id.categoryAverage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val avg = averageScores.getOrNull(position) ?: 0.0

        holder.name.text = category.name
        holder.average.text = "Avg: %.2f".format(avg)
    }

    @OptIn(UnstableApi::class)
    override fun getItemCount(): Int {
        return categories.size
    }

}




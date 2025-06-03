package com.example.okolog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.okolog.data.Category

class CategoryTabAdapter(
    private val categories: List<Category>,
    private val onItemClick: (Category?) -> Unit
) : RecyclerView.Adapter<CategoryTabAdapter.ViewHolder>() {

    private var selectedId: Int? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.text_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_tab, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = categories.size + 1 // 「すべて」分を追加

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = if (position == 0) null else categories[position - 1]

        holder.text.text = category?.name ?: "すべて"
        holder.text.setBackgroundColor(
            if (category?.id == selectedId || (category == null && selectedId == null)) 0xFFE0E0E0.toInt()
            else 0xFFFFFFFF.toInt()
        )

        holder.itemView.setOnClickListener {
            selectedId = category?.id
            notifyDataSetChanged()
            onItemClick(category)
        }
    }
}
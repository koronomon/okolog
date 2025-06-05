package com.example.okolog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.okolog.R

data class CategorySummary(
    val categoryName: String,
    val totalAmount: Int
)

class CategorySummaryAdapter : RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder>() {

    private var items: List<CategorySummary> = emptyList()

    fun submitList(newList: List<CategorySummary>) {
        items = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.text_category_name)
        private val totalText: TextView = itemView.findViewById(R.id.text_category_total)

        fun bind(item: CategorySummary) {
            nameText.text = item.categoryName

            if (item.totalAmount >= 0) {
                // 収入：青、+表示
                totalText.text = "+¥%,d".format(item.totalAmount)
                totalText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_blue_dark))
            } else {
                // 支出：赤、-表示（符号つき）
                totalText.text = "-¥%,d".format(-item.totalAmount)
                totalText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_summary, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
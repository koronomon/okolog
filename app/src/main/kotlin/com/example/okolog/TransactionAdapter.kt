package com.example.okolog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.okolog.R
import com.example.okolog.data.Transaction

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(
    DiffCallback()
) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateText: TextView = view.findViewById(R.id.text_date)
        private val amountText: TextView = view.findViewById(R.id.text_amount)
        private val typeText: TextView = view.findViewById(R.id.text_type)
        private val memoText: TextView = view.findViewById(R.id.text_memo)

        fun bind(item: Transaction) {
            dateText.text = item.date
            amountText.text = "${item.amount}円"
            typeText.text = if (item.isExpense) "支出" else "収入"
            memoText.text = item.memo ?: ""

            val context = itemView.context
            val color = if (item.isExpense) {
                context.getColor(R.color.red)
            } else {
                context.getColor(R.color.blue)
            }

            typeText.setTextColor(color)
            amountText.setTextColor(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) =
            oldItem == newItem
    }
}
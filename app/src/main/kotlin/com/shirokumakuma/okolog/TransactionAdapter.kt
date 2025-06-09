package com.shirokumakuma.okolog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shirokumakuma.okolog.data.TransactionWithCategory

class TransactionAdapter (
    private val onDeleteClick: (TransactionWithCategory) -> Unit,
    private val onEditClick: (TransactionWithCategory) -> Unit
): ListAdapter<TransactionWithCategory, TransactionAdapter.ViewHolder>(
    DiffCallback()
) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateText: TextView = view.findViewById(R.id.text_date)
        private val amountText: TextView = view.findViewById(R.id.text_amount)
        private val typeText: TextView = view.findViewById(R.id.text_type)
        private val memoText: TextView = view.findViewById(R.id.text_memo)
        private val categoryText: TextView = view.findViewById(R.id.category_text)
        private val deleteButton: View = view.findViewById(R.id.button_delete)
        val editButton: ImageButton = itemView.findViewById(R.id.button_edit)

        fun bind(item: TransactionWithCategory,
                 onDeleteClick: (TransactionWithCategory) -> Unit,
                 onEditClick: (TransactionWithCategory) -> Unit) {
            dateText.text = item.transaction.date
            amountText.text = "${item.transaction.amount}円"
            typeText.text = if (item.transaction.isExpense) "支出" else "収入"
            memoText.text = item.transaction.memo ?: ""
            categoryText.text = item.category.name

            val context = itemView.context
            val color = if (item.transaction.isExpense) {
                context.getColor(R.color.red)
            } else {
                context.getColor(R.color.blue)
            }

            typeText.setTextColor(color)
            amountText.setTextColor(color)

            // 削除ボタンの挙動を登録
            deleteButton.setOnClickListener {
                onDeleteClick(item)
            }

            // 編集ボタンの挙動を登録
            editButton.setOnClickListener {
                onEditClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onDeleteClick, onEditClick)
    }

    class DiffCallback : DiffUtil.ItemCallback<TransactionWithCategory>() {
        override fun areItemsTheSame(oldItem: TransactionWithCategory, newItem: TransactionWithCategory) =
            oldItem.transaction.id == newItem.transaction.id

        override fun areContentsTheSame(oldItem: TransactionWithCategory, newItem: TransactionWithCategory) =
            oldItem == newItem
    }
}
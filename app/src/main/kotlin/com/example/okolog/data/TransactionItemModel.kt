package com.example.okolog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_item_model",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class TransactionItemModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val amount: Int,
    val isExpense: Boolean,  // true = 支出, false = 収入
    val memo: String?,
    val categoryId: Int? // ← カテゴリ名ではなく ID に
)
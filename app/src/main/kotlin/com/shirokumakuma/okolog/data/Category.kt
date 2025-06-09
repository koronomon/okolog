package com.shirokumakuma.okolog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
)
data class Category(
    // キー
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 名前
    val name: String,

    // 無効フラグ
    val isDisabled: Boolean = false
)
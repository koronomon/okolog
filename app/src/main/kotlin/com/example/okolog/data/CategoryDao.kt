package com.example.okolog.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {

    // 無効なカテゴリは除外して取得
    @Query("SELECT * FROM categories WHERE isDisabled = 0 ORDER BY id ASC")
    fun getAll(): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}
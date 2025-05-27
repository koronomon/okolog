package com.example.okolog.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAll(): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Delete
    suspend fun delete(category: Category)
}
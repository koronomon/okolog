package com.example.okolog.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface TransactionItemModelDao {
    @Query("SELECT * FROM transaction_item_model ORDER BY date DESC, id DESC")
    fun getAll(): LiveData<List<TransactionItemModel>>

    @Transaction
    @Query("SELECT * FROM transaction_item_model WHERE date LIKE :month || '%' ORDER BY date DESC, id DESC")
    fun getByMonth(month: String): LiveData<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transaction_item_model WHERE date LIKE :month || '%' ORDER BY date DESC, id DESC")
    fun getMayTransactionsWithCategory(month: String): LiveData<List<TransactionWithCategory>>

    @Insert
    suspend fun insert(transaction: TransactionItemModel)

    @Update
    suspend fun update(transaction: TransactionItemModel)

    @Delete
    suspend fun delete(transaction: TransactionItemModel)
}
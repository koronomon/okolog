package com.shirokumakuma.okolog.data

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
    @Query("""
    SELECT c.name AS name, 
           SUM(CASE WHEN t.isExpense = 1 THEN -t.amount ELSE t.amount END) AS total
    FROM transaction_item_model t
    INNER JOIN categories c ON t.categoryId = c.id
    WHERE t.date LIKE :month || '%'
      AND c.isDisabled = 0
    GROUP BY c.id, c.name
    ORDER BY c.name
""")
    suspend fun getCategoryTotalsByMonth(month: String): List<CategoryTotal>

    @Insert
    suspend fun insert(transaction: TransactionItemModel)

    @Update
    suspend fun update(transaction: TransactionItemModel)

    @Delete
    suspend fun delete(transaction: TransactionItemModel)
}
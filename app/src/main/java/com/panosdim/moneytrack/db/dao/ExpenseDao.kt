package com.panosdim.moneytrack.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.panosdim.moneytrack.model.Expense

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM Expense")
    fun get(): LiveData<List<Expense>>

    @Insert
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Transaction
    suspend fun deleteAndCreate(expense: List<Expense>) {
        deleteAll()
        insertAll(expense)
    }

    @Query("DELETE FROM Expense")
    suspend fun deleteAll()

    @Insert
    suspend fun insertAll(expense: List<Expense>)
}
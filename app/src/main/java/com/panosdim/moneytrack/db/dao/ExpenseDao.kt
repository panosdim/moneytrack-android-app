package com.panosdim.moneytrack.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.utils.currentMonth

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
    suspend fun deleteAndCreateAll(expense: List<Expense>) {
        deleteAll()
        insertAll(expense)
    }

    @Transaction
    suspend fun deleteAndCreateMonth(expense: List<Expense>) {
        deleteMonth(currentMonth())
        insertAll(expense)
    }

    @Query("DELETE FROM Expense")
    suspend fun deleteAll()

    @Query("DELETE FROM Expense WHERE date >= :afterDate")
    suspend fun deleteMonth(afterDate: String)

    @Insert
    suspend fun insertAll(expense: List<Expense>)
}
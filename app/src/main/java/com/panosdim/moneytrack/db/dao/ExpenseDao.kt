package com.panosdim.moneytrack.db.dao

import androidx.room.*
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.utils.oneMonthBefore
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM Expense ORDER BY date DESC")
    fun get(): Flow<List<Expense>>

    @Query("SELECT (SELECT COUNT(*) FROM Expense) == 0")
    fun isEmpty(): Boolean

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
        deleteMonth(oneMonthBefore())
        insertAll(expense)
    }

    @Query("DELETE FROM Expense")
    suspend fun deleteAll()

    @Query("DELETE FROM Expense WHERE date >= :afterDate")
    suspend fun deleteMonth(afterDate: String)

    @Insert
    suspend fun insertAll(expense: List<Expense>)
}
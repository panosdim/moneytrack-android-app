package com.panosdim.moneytrack.db.dao

import androidx.room.*
import com.panosdim.moneytrack.models.Income
import com.panosdim.moneytrack.utils.currentMonth
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM Income")
    fun get(): Flow<List<Income>>

    @Query("SELECT (SELECT COUNT(*) FROM Income) == 0")
    fun isEmpty(): Boolean

    @Insert
    suspend fun insert(income: Income)

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)

    @Transaction
    suspend fun deleteAndCreateAll(income: List<Income>) {
        deleteAll()
        insertAll(income)
    }

    @Transaction
    suspend fun deleteAndCreateMonth(income: List<Income>) {
        deleteMonth(currentMonth())
        insertAll(income)
    }

    @Query("DELETE FROM Income")
    suspend fun deleteAll()

    @Query("DELETE FROM Income WHERE date >= :afterDate")
    suspend fun deleteMonth(afterDate: String)

    @Insert
    suspend fun insertAll(income: List<Income>)
}
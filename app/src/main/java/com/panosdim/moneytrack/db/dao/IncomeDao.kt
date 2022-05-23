package com.panosdim.moneytrack.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.panosdim.moneytrack.model.Income

@Dao
interface IncomeDao {
    @Query("SELECT * FROM Income")
    fun get(): LiveData<List<Income>>

    @Insert
    suspend fun insert(income: Income)

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)

    @Transaction
    suspend fun deleteAndCreate(income: List<Income>) {
        deleteAll()
        insertAll(income)
    }

    @Query("DELETE FROM Income")
    suspend fun deleteAll()

    @Insert
    suspend fun insertAll(income: List<Income>)
}
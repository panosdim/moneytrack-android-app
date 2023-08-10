package com.panosdim.moneytrack.db.dao

import androidx.room.*
import com.panosdim.moneytrack.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM Category")
    fun get(): Flow<List<Category>>

    @Insert
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Transaction
    suspend fun deleteAndCreate(category: List<Category>) {
        deleteAll()
        insertAll(category)
    }

    @Query("DELETE FROM Category")
    suspend fun deleteAll()

    @Insert
    suspend fun insertAll(category: List<Category>)
}
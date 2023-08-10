package com.panosdim.moneytrack.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.panosdim.moneytrack.db.dao.CategoryDao
import com.panosdim.moneytrack.db.dao.ExpenseDao
import com.panosdim.moneytrack.db.dao.IncomeDao
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.models.Income

@Database(
    entities = [Category::class, Income::class, Expense::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
}
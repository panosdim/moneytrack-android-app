package com.panosdim.moneytrack.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("category"),
    )]
)
data class Expense(
    @PrimaryKey var id: Int? = null,
    @ColumnInfo var date: String,
    @ColumnInfo var amount: Float,
    @ColumnInfo var category: Int,
    @ColumnInfo var comment: String
)
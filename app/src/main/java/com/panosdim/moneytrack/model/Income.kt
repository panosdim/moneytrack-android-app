package com.panosdim.moneytrack.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Income(
    @PrimaryKey var id: Int? = null,
    @ColumnInfo var date: String,
    @ColumnInfo var amount: Float,
    @ColumnInfo var comment: String
)
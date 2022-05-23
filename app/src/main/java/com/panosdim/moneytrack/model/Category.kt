package com.panosdim.moneytrack.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    @PrimaryKey var id: Int? = null,
    @ColumnInfo var category: String,
    @ColumnInfo var count: Int
) {
    override fun toString(): String {
        return category
    }
}
package com.panosdim.moneytrack.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Category(
    @PrimaryKey var id: Int? = null,
    @ColumnInfo var category: String,
    @ColumnInfo var count: Int
)
package com.panosdim.moneytrack.utils

import com.panosdim.moneytrack.models.Category

fun getCategoryName(id: Int, categories: List<Category>): String {
    return categories.find { it.id == id }?.category ?: ""
}
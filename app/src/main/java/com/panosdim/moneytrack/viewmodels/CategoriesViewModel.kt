package com.panosdim.moneytrack.viewmodels

import androidx.lifecycle.ViewModel
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.rest.CategoriesRepository
import kotlinx.coroutines.flow.Flow

class CategoriesViewModel : ViewModel() {
    val categories = CategoriesRepository.get()

    fun removeCategory(category: Category): Flow<Response<Unit>> {
        return CategoriesRepository.delete(category)
    }

    fun addCategory(category: Category): Flow<Response<Unit>> {
        return CategoriesRepository.add(category)
    }

    fun updateCategory(category: Category): Flow<Response<Unit>> {
        return CategoriesRepository.update(category)
    }
}
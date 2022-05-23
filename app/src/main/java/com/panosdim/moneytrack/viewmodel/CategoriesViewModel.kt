package com.panosdim.moneytrack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.panosdim.moneytrack.api.CategoriesRepository
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.model.Category

class CategoriesViewModel : ViewModel() {

    private val categoriesRepository = CategoriesRepository()
    var categories: LiveData<List<Category>> = categoriesRepository.get()

    fun removeCategory(category: Category): LiveData<Resource<Category>> {
        return categoriesRepository.delete(category)
    }

    fun addCategory(category: Category): LiveData<Resource<Category>> {
        return categoriesRepository.add(category)
    }

    fun updateCategory(category: Category): LiveData<Resource<Category>> {
        return categoriesRepository.update(category)
    }

    fun refreshCategories() {
        categories = categoriesRepository.get()
    }
}
package com.panosdim.moneytrack.viewmodels

import androidx.lifecycle.ViewModel
import com.panosdim.moneytrack.models.Income
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.rest.IncomeRepository
import kotlinx.coroutines.flow.Flow

class IncomeViewModel : ViewModel() {
    val income = IncomeRepository.get()

    fun fetchAllIncome(): Flow<List<Income>> {
        return IncomeRepository.getAll()
    }

    fun removeIncome(income: Income): Flow<Response<Unit>> {
        return IncomeRepository.delete(income)
    }

    fun addIncome(income: Income): Flow<Response<Unit>> {
        return IncomeRepository.add(income)
    }

    fun updateIncome(income: Income): Flow<Response<Unit>> {
        return IncomeRepository.update(income)
    }
}
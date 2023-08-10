package com.panosdim.moneytrack.viewmodels

import androidx.lifecycle.ViewModel
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.rest.ExpensesRepository
import kotlinx.coroutines.flow.Flow

class ExpensesViewModel : ViewModel() {
    val expenses = ExpensesRepository.get()

    fun removeExpense(expense: Expense): Flow<Response<Unit>> {
        return ExpensesRepository.delete(expense)
    }

    fun addExpense(expense: Expense): Flow<Response<Unit>> {
        return ExpensesRepository.add(expense)
    }

    fun updateExpense(expense: Expense): Flow<Response<Unit>> {
        return ExpensesRepository.update(expense)
    }

    fun years(): Flow<List<Int>> {
        return ExpensesRepository.years()
    }
}
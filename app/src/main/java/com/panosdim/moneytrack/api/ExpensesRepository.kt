package com.panosdim.moneytrack.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.db
import com.panosdim.moneytrack.db.dao.ExpenseDao
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.utils.currentMonth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ExpensesRepository {
    private var client: Webservice = webservice
    private val scope = CoroutineScope(Dispatchers.Main)
    private val expenseDao: ExpenseDao = db.expenseDao()

    suspend fun years(): Flow<List<Int>> {
        return flow {
            emit(webservice.years())
        }
            .flowOn(Dispatchers.IO)
    }

    fun get(fetchAll: Boolean = false): LiveData<List<Expense>> {
        scope.launch {
            try {
                if (fetchAll) {
                    withContext(Dispatchers.IO) {
                        val response = client.expense(null)
                        expenseDao.deleteAndCreateAll(response)
                    }
                } else {
                    withContext(Dispatchers.IO) {
                        val response = client.expense(currentMonth())
                        expenseDao.deleteAndCreateMonth(response)
                    }
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.IO) {
                    expenseDao.get()
                }
            }
        }
        return expenseDao.get()
    }

    fun delete(expense: Expense): LiveData<Resource<Expense>> {
        val result: MutableLiveData<Resource<Expense>> = MutableLiveData()
        result.postValue(Resource.Loading())

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.expense(expense.id!!)
                    when (response.code()) {
                        204 -> {
                            result.postValue(Resource.Success())
                            expenseDao.delete(expense)
                        }
                        404 -> {
                            result.postValue(Resource.Error("Error deleting expense. Expense not found."))
                        }
                    }
                }
            } catch (ex: HttpException) {
                result.postValue(Resource.Error("Error deleting expense."))
            } catch (t: SocketTimeoutException) {
                result.postValue(Resource.Error(App.instance.getString(R.string.connection_timeout)))
            } catch (d: UnknownHostException) {
                result.postValue(Resource.Error(App.instance.getString(R.string.unknown_host)))
            }
        }

        return result
    }

    fun add(expense: Expense): LiveData<Resource<Expense>> {
        val result: MutableLiveData<Resource<Expense>> = MutableLiveData()
        result.postValue(Resource.Loading())

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.expense(expense)
                    result.postValue(Resource.Success())
                    expenseDao.insert(response)
                }
            } catch (e: HttpException) {
                result.postValue(Resource.Error("Error saving expense."))
            } catch (t: SocketTimeoutException) {
                result.postValue(
                    Resource.Error(
                        App.instance.getString(R.string.connection_timeout)
                    )
                )
            } catch (d: UnknownHostException) {
                result.postValue(
                    Resource.Error(
                        App.instance.getString(R.string.unknown_host)
                    )
                )
            }
        }
        return result
    }

    fun update(expense: Expense): LiveData<Resource<Expense>> {
        val result: MutableLiveData<Resource<Expense>> = MutableLiveData()
        result.postValue(Resource.Loading())

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.expense(expense.id!!, expense)
                    result.postValue(Resource.Success())
                    expenseDao.update(response)
                }
            } catch (e: HttpException) {
                result.postValue(Resource.Error("Error updating expense."))
            } catch (t: SocketTimeoutException) {
                result.postValue(
                    Resource.Error(
                        App.instance.getString(R.string.connection_timeout)
                    )
                )
            } catch (d: UnknownHostException) {
                result.postValue(
                    Resource.Error(
                        App.instance.getString(R.string.unknown_host)
                    )
                )
            }
        }
        return result
    }
}
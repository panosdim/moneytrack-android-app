package com.panosdim.moneytrack.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.YEARS_TO_FETCH
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.db
import com.panosdim.moneytrack.db.dao.IncomeDao
import com.panosdim.moneytrack.model.Income
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class IncomeRepository {
    private var client: Webservice = webservice
    private val scope = CoroutineScope(Dispatchers.Main)
    private val incomeDao: IncomeDao = db.incomeDao()

    fun get(): LiveData<List<Income>> {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.income(YEARS_TO_FETCH)
                    incomeDao.deleteAndCreate(response)
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.IO) {
                    incomeDao.get()
                }
            }
        }
        return incomeDao.get()
    }

    fun delete(income: Income): LiveData<Resource<Income>> {
        val result: MutableLiveData<Resource<Income>> = MutableLiveData()
        result.postValue(Resource.Loading())

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.income(income.id!!)
                    when (response.code()) {
                        204 -> {
                            result.postValue(Resource.Success())
                            incomeDao.delete(income)
                        }
                        404 -> {
                            result.postValue(Resource.Error("Error deleting income. Income not found."))
                        }
                    }
                }
            } catch (ex: HttpException) {
                result.postValue(Resource.Error("Error deleting income."))
            } catch (t: SocketTimeoutException) {
                result.postValue(Resource.Error(App.instance.getString(R.string.connection_timeout)))
            } catch (d: UnknownHostException) {
                result.postValue(Resource.Error(App.instance.getString(R.string.unknown_host)))
            }
        }

        return result
    }

    fun add(income: Income): LiveData<Resource<Income>> {
        val result: MutableLiveData<Resource<Income>> = MutableLiveData()
        result.postValue(Resource.Loading())

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.income(income)
                    result.postValue(Resource.Success())
                    incomeDao.insert(response)
                }
            } catch (e: HttpException) {
                result.postValue(Resource.Error("Error saving income."))
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

    fun update(income: Income): LiveData<Resource<Income>> {
        val result: MutableLiveData<Resource<Income>> = MutableLiveData()
        result.postValue(Resource.Loading())

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.income(income.id!!, income)
                    result.postValue(Resource.Success())
                    incomeDao.update(response)
                }
            } catch (e: HttpException) {
                result.postValue(Resource.Error("Error updating income."))
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
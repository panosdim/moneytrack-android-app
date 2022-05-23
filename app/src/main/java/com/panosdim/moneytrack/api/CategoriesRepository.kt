package com.panosdim.moneytrack.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.db
import com.panosdim.moneytrack.db.dao.CategoryDao
import com.panosdim.moneytrack.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CategoriesRepository {
    private var client: Webservice = webservice
    private val scope = CoroutineScope(Dispatchers.Main)
    private val categoryDao: CategoryDao = db.categoryDao()

    fun get(): LiveData<List<Category>> {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.category()
                    categoryDao.deleteAndCreate(response)
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.IO) {
                    categoryDao.get()
                }
            }
        }
        return categoryDao.get()
    }

    fun delete(category: Category): LiveData<Resource<Category>> {
        val result: MutableLiveData<Resource<Category>> = MutableLiveData()
        result.postValue(Resource.Loading())

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.category(category.id!!)
                    when (response.code()) {
                        204 -> {
                            result.postValue(Resource.Success())
                            categoryDao.delete(category)
                        }
                        404 -> {
                            result.postValue(Resource.Error("Error deleting category. Category not found."))
                        }
                    }
                }
            } catch (ex: HttpException) {
                result.postValue(Resource.Error("Error deleting category."))
            } catch (t: SocketTimeoutException) {
                result.postValue(Resource.Error(App.instance.getString(R.string.connection_timeout)))
            } catch (d: UnknownHostException) {
                result.postValue(Resource.Error(App.instance.getString(R.string.unknown_host)))
            }
        }

        return result
    }

    fun add(category: Category): LiveData<Resource<Category>> {
        val result: MutableLiveData<Resource<Category>> = MutableLiveData()
        result.postValue(Resource.Loading())

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.category(category)
                    result.postValue(Resource.Success())
                    categoryDao.insert(response)
                }

            } catch (e: HttpException) {
                result.postValue(Resource.Error("Error saving category."))
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

    fun update(category: Category): LiveData<Resource<Category>> {
        val result: MutableLiveData<Resource<Category>> = MutableLiveData()
        result.postValue(Resource.Loading())

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = client.category(category.id!!, category)
                    result.postValue(Resource.Success())
                    categoryDao.update(response)
                }

            } catch (e: HttpException) {
                result.postValue(Resource.Error("Error updating category."))
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
package com.panosdim.moneytrack.rest

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.db
import com.panosdim.moneytrack.db.dao.CategoryDao
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.ErrorDetails
import com.panosdim.moneytrack.models.Response
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object CategoriesRepository {
    private val categoriesDao: CategoryDao = db.categoryDao()

    fun get(): Flow<Response<List<Category>>> {
        return channelFlow {
            send(Response.Loading)
            coroutineScope {
                launch {
                    categoriesDao.get().collect {
                        send(Response.Success(it))
                    }
                }
                kotlin.runCatching {
                    categoriesDao.deleteAndCreate(
                        client.get("category") {
                            contentType(ContentType.Application.Json)
                        }.body()
                    )
                }.onFailure {
                    Firebase.crashlytics.recordException(it)
                    val errorMessage = when (it) {
                        is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                        is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                        else -> App.instance.getString(R.string.categories_retrieve_error)
                    }
                    send(Response.Error(errorMessage))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun delete(category: Category): Flow<Response<Unit>> {
        return flow {
            emit(Response.Loading)
            kotlin.runCatching {
                client.delete("category") {
                    url {
                        appendPathSegments(category.id.toString())
                    }
                }
                categoriesDao.delete(category)
                emit(Response.Success(Unit))
            }.onFailure {
                val errorMessage = when (it) {
                    is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                    is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                    is ClientRequestException -> {
                        if (it.response.status == HttpStatusCode.NotFound) {
                            App.instance.getString(R.string.category_not_found)
                        } else {
                            App.instance.getString(R.string.category_delete_error)
                        }
                    }

                    else -> App.instance.getString(R.string.category_delete_error)
                }
                emit(Response.Error(errorMessage))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun add(category: Category): Flow<Response<Unit>> {
        return flow {
            emit(Response.Loading)
            kotlin.runCatching {
                val response = client.post("category") {
                    contentType(ContentType.Application.Json)
                    setBody(category)
                }.body<Category>()
                categoriesDao.insert(response)
                emit(Response.Success(Unit))
            }.onFailure {
                val errorMessage = when (it) {
                    is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                    is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                    else -> App.instance.getString(R.string.category_create_error)
                }
                emit(Response.Error(errorMessage))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun update(category: Category): Flow<Response<Unit>> {
        return flow {
            emit(Response.Loading)
            kotlin.runCatching {
                val response = client.put("category") {
                    url {
                        appendPathSegments(category.id.toString())
                    }
                    contentType(ContentType.Application.Json)
                    setBody(category)
                }.body<Category>()
                categoriesDao.update(response)
                emit(Response.Success(Unit))
            }.onFailure {
                val errorMessage = when (it) {
                    is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                    is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                    is ClientRequestException -> {
                        if (it.response.status == HttpStatusCode.Forbidden) {
                            it.response.body<ErrorDetails>().error
                        } else {
                            App.instance.getString(R.string.category_update_error)
                        }
                    }

                    else -> App.instance.getString(R.string.category_update_error)
                }
                emit(Response.Error(errorMessage))
            }
        }.flowOn(Dispatchers.IO)
    }
}
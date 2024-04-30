package com.panosdim.moneytrack.rest

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.db
import com.panosdim.moneytrack.db.dao.ExpenseDao
import com.panosdim.moneytrack.models.ErrorDetails
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.utils.oneMonthBefore
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

object ExpensesRepository {
    private val expenseDao: ExpenseDao = db.expenseDao()

    fun getAll(): Flow<List<Expense>> {
        return flow {
            val expenseList = client.get("expense") {
                contentType(ContentType.Application.Json)
            }.body<List<Expense>>()
            expenseDao.deleteAndCreateAll(
                expenseList
            )
            emit(expenseList)
        }
            .flowOn(Dispatchers.IO)
    }

    fun get(): Flow<Response<List<Expense>>> {
        return channelFlow {
            send(Response.Loading)
            coroutineScope {
                launch {
                    if (!expenseDao.isEmpty()) {
                        expenseDao.get().collect {
                            send(Response.Success(it))
                        }
                    }
                }
                kotlin.runCatching {
                    if (expenseDao.isEmpty()) {
                        val expenseList = client.get("expense") {
                            contentType(ContentType.Application.Json)
                        }.body<List<Expense>>()
                        expenseDao.deleteAndCreateAll(
                            expenseList
                        )
                        send(Response.Success(expenseList))
                    } else {
                        expenseDao.deleteAndCreateMonth(
                            client.get("expense") {
                                contentType(ContentType.Application.Json)
                                url {
                                    parameters.append("after_date", oneMonthBefore())
                                }
                            }.body()
                        )
                    }
                }.onFailure {
                    Firebase.crashlytics.recordException(it)
                    val errorMessage = when (it) {
                        is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                        is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                        else -> App.instance.getString(R.string.expenses_retrieving_error)
                    }
                    send(Response.Error(errorMessage))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun delete(expense: Expense): Flow<Response<Unit>> {
        return flow {
            emit(Response.Loading)
            kotlin.runCatching {
                client.delete("expense") {
                    url {
                        appendPathSegments(expense.id.toString())
                    }
                }
                expenseDao.delete(expense)
                emit(Response.Success(Unit))
            }.onFailure {
                val errorMessage = when (it) {
                    is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                    is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                    is ClientRequestException -> {
                        if (it.response.status == HttpStatusCode.NotFound) {
                            App.instance.getString(R.string.expense_not_found)
                        } else {
                            App.instance.getString(R.string.expense_delete_error)
                        }
                    }

                    else -> App.instance.getString(R.string.expense_delete_error)
                }
                emit(Response.Error(errorMessage))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun add(expense: Expense): Flow<Response<Unit>> {
        return flow {
            emit(Response.Loading)
            kotlin.runCatching {
                val response = client.post("expense") {
                    contentType(ContentType.Application.Json)
                    setBody(expense)
                }.body<Expense>()
                expenseDao.insert(response)
                emit(Response.Success(Unit))
            }.onFailure {
                val errorMessage = when (it) {
                    is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                    is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                    else -> App.instance.getString(R.string.expense_create_error)
                }
                emit(Response.Error(errorMessage))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun update(expense: Expense): Flow<Response<Unit>> {
        return flow {
            emit(Response.Loading)
            kotlin.runCatching {
                val response = client.put("expense") {
                    url {
                        appendPathSegments(expense.id.toString())
                    }
                    contentType(ContentType.Application.Json)
                    setBody(expense)
                }.body<Expense>()
                expenseDao.update(response)
                emit(Response.Success(Unit))
            }.onFailure {
                val errorMessage = when (it) {
                    is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                    is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                    is ClientRequestException -> {
                        if (it.response.status == HttpStatusCode.Forbidden) {
                            it.response.body<ErrorDetails>().error
                        } else {
                            App.instance.getString(R.string.expense_update_error)
                        }
                    }

                    else -> App.instance.getString(R.string.expense_update_error)
                }
                emit(Response.Error(errorMessage))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun years(): Flow<List<Int>> {
        return flow {
            emit(client.get("years") {
                contentType(ContentType.Application.Json)
            }.body<List<Int>>())
        }
            .flowOn(Dispatchers.IO)
    }
}
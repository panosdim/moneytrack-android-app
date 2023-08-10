package com.panosdim.moneytrack.rest

import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.db
import com.panosdim.moneytrack.db.dao.IncomeDao
import com.panosdim.moneytrack.models.ErrorDetails
import com.panosdim.moneytrack.models.Income
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.utils.currentMonth
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

object IncomeRepository {
    private val incomeDao: IncomeDao = db.incomeDao()

    fun get(): Flow<Response<List<Income>>> {
        return channelFlow {
            send(Response.Loading)
            coroutineScope {
                launch {
                    if (!incomeDao.isEmpty()) {
                        incomeDao.get().collect {
                            send(Response.Success(it))
                        }
                    }
                }
                kotlin.runCatching {
                    if (incomeDao.isEmpty()) {
                        val incomeList = client.get("income") {
                            contentType(ContentType.Application.Json)
                        }.body<List<Income>>()
                        incomeDao.deleteAndCreateAll(
                            incomeList
                        )
                        send(Response.Success(incomeList))
                    } else {
                        incomeDao.deleteAndCreateMonth(
                            client.get("income") {
                                contentType(ContentType.Application.Json)
                                url {
                                    parameters.append("after_date", currentMonth())
                                }
                            }.body()
                        )
                    }
                }.onFailure {
                    val errorMessage = when (it) {
                        is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                        is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                        else -> App.instance.getString(R.string.income_retrieving_error)
                    }
                    send(Response.Error(errorMessage))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun delete(income: Income): Flow<Response<Unit>> {
        return flow {
            emit(Response.Loading)
            kotlin.runCatching {
                client.delete("income") {
                    url {
                        appendPathSegments(income.id.toString())
                    }
                }
                incomeDao.delete(income)
                emit(Response.Success(Unit))
            }.onFailure {
                val errorMessage = when (it) {
                    is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                    is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                    is ClientRequestException -> {
                        if (it.response.status == HttpStatusCode.NotFound) {
                            App.instance.getString(R.string.income_not_found)
                        } else {
                            App.instance.getString(R.string.income_delete_error)
                        }
                    }

                    else -> App.instance.getString(R.string.income_delete_error)
                }
                emit(Response.Error(errorMessage))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun add(income: Income): Flow<Response<Unit>> {
        return flow {
            emit(Response.Loading)
            kotlin.runCatching {
                val response = client.post("income") {
                    contentType(ContentType.Application.Json)
                    setBody(income)
                }.body<Income>()
                incomeDao.insert(response)
                emit(Response.Success(Unit))
            }.onFailure {
                val errorMessage = when (it) {
                    is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                    is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                    else -> App.instance.getString(R.string.income_create_error)
                }
                emit(Response.Error(errorMessage))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun update(income: Income): Flow<Response<Unit>> {
        return flow {
            emit(Response.Loading)
            kotlin.runCatching {
                val response = client.put("income") {
                    url {
                        appendPathSegments(income.id.toString())
                    }
                    contentType(ContentType.Application.Json)
                    setBody(income)
                }.body<Income>()
                incomeDao.update(response)
                emit(Response.Success(Unit))
            }.onFailure {
                val errorMessage = when (it) {
                    is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                    is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                    is ClientRequestException -> {
                        if (it.response.status == HttpStatusCode.Forbidden) {
                            it.response.body<ErrorDetails>().error
                        } else {
                            App.instance.getString(R.string.income_update_error)
                        }
                    }

                    else -> App.instance.getString(R.string.income_update_error)
                }
                emit(Response.Error(errorMessage))
            }
        }.flowOn(Dispatchers.IO)
    }
}
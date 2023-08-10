package com.panosdim.moneytrack.viewmodels

import androidx.lifecycle.ViewModel
import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.LoginRequest
import com.panosdim.moneytrack.models.LoginResponse
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.rest.client
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LoginViewModel : ViewModel() {
    fun login(email: String, password: String): Flow<Response<LoginResponse>> {
        return flow {
            emit(Response.Loading)
            kotlin.runCatching {
                client.post("login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(email, password))
                }.body<LoginResponse>()
            }.onSuccess {
                emit(Response.Success(it))
            }.onFailure {
                val errorMessage = when (it) {
                    is SocketTimeoutException -> App.instance.getString(R.string.connection_timeout)
                    is UnknownHostException -> App.instance.getString(R.string.unknown_host)
                    else -> App.instance.getString(R.string.login_failed)
                }
                emit(Response.Error(errorMessage))
            }
        }.flowOn(Dispatchers.IO)
    }
}
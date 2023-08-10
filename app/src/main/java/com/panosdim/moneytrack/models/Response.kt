package com.panosdim.moneytrack.models

sealed class Response<out T> {
    class Success<out T>(val data: T) : Response<T>()
    object Loading : Response<Nothing>()
    class Error(val errorMessage: String) : Response<Nothing>()
}
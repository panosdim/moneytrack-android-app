package com.panosdim.moneytrack.api

import com.panosdim.moneytrack.api.data.LoginRequest
import com.panosdim.moneytrack.api.data.LoginResponse
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.model.Income
import retrofit2.Response
import retrofit2.http.*

interface Webservice {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("income")
    suspend fun income(@Query("years") years: String): List<Income>

    @POST("income")
    suspend fun income(@Body request: Income): Income

    @PUT("income/{id}")
    suspend fun income(@Path("id") id: Int, @Body request: Income): Income

    @DELETE("income/{id}")
    suspend fun income(@Path("id") id: Int): Response<Void>

    @GET("expense")
    suspend fun expense(@Query("years") years: String): List<Expense>

    @POST("expense")
    suspend fun expense(@Body request: Expense): Expense

    @PUT("expense/{id}")
    suspend fun expense(@Path("id") id: Int, @Body request: Expense): Expense

    @DELETE("expense/{id}")
    suspend fun expense(@Path("id") id: Int): Response<Void>

    @GET("category")
    suspend fun category(): List<Category>

    @POST("category")
    suspend fun category(@Body request: Category): Category

    @PUT("category/{id}")
    suspend fun category(@Path("id") id: Int, @Body request: Category): Category

    @DELETE("category/{id}")
    suspend fun category(@Path("id") id: Int): Response<Void>
}
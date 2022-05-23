package com.panosdim.moneytrack.api


import com.google.gson.GsonBuilder
import com.panosdim.moneytrack.BACKEND_URL
import com.panosdim.moneytrack.BuildConfig
import com.panosdim.moneytrack.prefs
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


val webservice: Webservice by lazy {
    val client = OkHttpClient.Builder().addInterceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .addHeader("Authorization", " Bearer " + prefs.token)
            .build()
        chain.proceed(newRequest)
    }

    if (BuildConfig.DEBUG) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.BODY }
        client.addInterceptor(interceptor)
    }

    Retrofit.Builder()
        .baseUrl(BACKEND_URL)
        .client(client.build())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()
        .create(Webservice::class.java)
}
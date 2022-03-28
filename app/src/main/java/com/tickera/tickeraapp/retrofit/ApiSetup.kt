package com.tickera.tickeraapp.retrofit

import com.tickera.tickeraapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiSetup {

    fun getRetrofitForStripe(baseUrl:String): ApiInterface {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient: OkHttpClient = if (BuildConfig.DEBUG)
            OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).addInterceptor(interceptor).build()
        else OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()

        val retrofit = Retrofit.Builder().baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiInterface::class.java)
    }
}
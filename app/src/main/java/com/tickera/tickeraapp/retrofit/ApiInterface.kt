package com.tickera.tickeraapp.retrofit

import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {

    @POST("terminal/connection_tokens")
    fun getSecretKey(@Header("authorization") authorization: String): Call<CommonResponseClass>

    @POST("payment_intents/{id}/capture")
    fun capturePayment(@Header ("authorization") authorization: String, @Path("id") id:String):Call<CommonResponseClass>
}
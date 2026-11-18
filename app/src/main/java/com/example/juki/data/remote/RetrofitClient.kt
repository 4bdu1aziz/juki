package com.example.juki.data.remote

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object RetrofitClient {
    val api: CbrApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://www.cbr.ru/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(CbrApi::class.java)
    }
}
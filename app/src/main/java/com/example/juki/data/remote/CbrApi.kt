package com.example.juki.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CbrApi {
    @GET("scripts/xml_metall.asp")
    suspend fun getGoldRate(
        @Query("date_req1") date1: String,
        @Query("date_req2") date2: String
    ): Response<CbrMetallResponse>
}
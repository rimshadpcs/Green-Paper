package com.rimapps.arqtest.data.remote

import com.rimapps.arqtest.data.remote.dto.TickerDto
import retrofit2.http.GET
import retrofit2.http.Query

interface DollarApi {
    @GET("v1/tickers")
    suspend fun getTickers(
        @Query("currencies") currencies: String
    ): List<TickerDto>

    @GET("v1/tickers-currencies")
    suspend fun getTickerCurrencies(): List<String>
}

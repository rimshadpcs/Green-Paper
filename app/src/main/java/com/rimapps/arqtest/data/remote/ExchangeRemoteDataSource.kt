package com.rimapps.arqtest.data.remote

import com.rimapps.arqtest.data.remote.dto.TickerDto
import javax.inject.Inject

interface ExchangeRemoteDataSource {
    suspend fun getTickerCurrencies(): List<String>

    suspend fun getTickers(currencies: String): List<TickerDto>
}

class DollarExchangeRemoteDataSource @Inject constructor(
    private val api: DollarApi
) : ExchangeRemoteDataSource {
    override suspend fun getTickerCurrencies(): List<String> {
        return api.getTickerCurrencies()
    }

    override suspend fun getTickers(currencies: String): List<TickerDto> {
        return api.getTickers(currencies)
    }
}

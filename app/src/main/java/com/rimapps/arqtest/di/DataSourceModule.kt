package com.rimapps.arqtest.di

import com.rimapps.arqtest.data.remote.DollarExchangeRemoteDataSource
import com.rimapps.arqtest.data.remote.ExchangeRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindExchangeRemoteDataSource(
        implementation: DollarExchangeRemoteDataSource
    ): ExchangeRemoteDataSource
}

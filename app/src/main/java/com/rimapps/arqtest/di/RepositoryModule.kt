package com.rimapps.arqtest.di

import com.rimapps.arqtest.data.repository.ExchangeRepositoryImpl
import com.rimapps.arqtest.domain.repository.ExchangeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindExchangeRepository(
        implementation: ExchangeRepositoryImpl
    ): ExchangeRepository
}

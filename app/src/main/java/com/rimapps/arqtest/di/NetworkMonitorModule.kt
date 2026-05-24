package com.rimapps.arqtest.di

import com.rimapps.arqtest.core.network.AndroidNetworkMonitor
import com.rimapps.arqtest.core.network.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkMonitorModule {
    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        implementation: AndroidNetworkMonitor
    ): NetworkMonitor
}

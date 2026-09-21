package com.studio249.qaapp_realwear.di

import com.studio249.qaapp_realwear.data.Repository
import com.studio249.qaapp_realwear.data.remote.ApiRepository
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
    abstract fun bindRepository(apiRepository: ApiRepository): Repository
}

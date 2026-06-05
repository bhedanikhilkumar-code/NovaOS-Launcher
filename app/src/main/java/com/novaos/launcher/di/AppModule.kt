package com.novaos.launcher.di

import android.content.Context
import androidx.room.Room
import com.novaos.launcher.data.local.room.LauncherDatabase
import com.novaos.launcher.data.local.room.dao.*
import com.novaos.launcher.data.repository.AppRepositoryImpl
import com.novaos.launcher.data.repository.HomeLayoutRepositoryImpl
import com.novaos.launcher.data.repository.SettingsRepositoryImpl
import com.novaos.launcher.domain.repository.AppRepository
import com.novaos.launcher.domain.repository.HomeLayoutRepository
import com.novaos.launcher.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): LauncherDatabase {
        return Room.databaseBuilder(
            context,
            LauncherDatabase::class.java,
            LauncherDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAppDao(database: LauncherDatabase): AppDao = database.appDao()

    @Provides
    fun provideHomeItemDao(database: LauncherDatabase): HomeItemDao = database.homeItemDao()

    @Provides
    fun provideFolderDao(database: LauncherDatabase): FolderDao = database.folderDao()

    @Provides
    fun provideHiddenAppDao(database: LauncherDatabase): HiddenAppDao = database.hiddenAppDao()

    @Provides
    fun provideDockItemDao(database: LauncherDatabase): DockItemDao = database.dockItemDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: AppRepositoryImpl): AppRepository

    @Binds
    @Singleton
    abstract fun bindHomeLayoutRepository(impl: HomeLayoutRepositoryImpl): HomeLayoutRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

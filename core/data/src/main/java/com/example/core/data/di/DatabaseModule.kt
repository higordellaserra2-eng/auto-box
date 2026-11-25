package com.example.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.core.data.local.AutoBoxDatabase
import com.example.core.data.local.AutoBoxDatabaseMigrations
import com.example.core.data.local.VehicleDao
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
  fun provideAutoBoxDatabase(
    @ApplicationContext context: Context,
  ): AutoBoxDatabase =
    Room.databaseBuilder(
      context,
      AutoBoxDatabase::class.java,
      AutoBoxDatabase.DATABASE_NAME,
    )
      .addMigrations(*AutoBoxDatabaseMigrations.all())
      .build()

  @Provides
  fun provideVehicleDao(database: AutoBoxDatabase): VehicleDao = database.vehicleDao()
}

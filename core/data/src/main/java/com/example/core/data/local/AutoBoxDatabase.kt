package com.example.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [VehicleEntity::class],
  version = AutoBoxDatabase.VERSION,
  exportSchema = false,
)
abstract class AutoBoxDatabase : RoomDatabase() {
  abstract fun vehicleDao(): VehicleDao

  companion object {
    const val DATABASE_NAME = "autobox.db"
    const val VERSION = 1
  }
}

object AutoBoxDatabaseMigrations {
  /**
   * Placeholder de migração preparada para a próxima etapa (ex.: histórico de serviços / lembretes).
   * Ao incrementar [AutoBoxDatabase.VERSION], registre as migrations reais aqui e no DatabaseModule.
   */
  val MIGRATION_1_2: Migration =
    object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        // Estrutura v1 preservada; futuras tabelas serão adicionadas aqui sem perda de dados.
      }
    }

  fun all(): Array<Migration> = arrayOf()
}

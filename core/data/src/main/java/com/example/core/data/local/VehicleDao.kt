package com.example.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

  @Query(
    """
    SELECT * FROM vehicles
    ORDER BY is_primary DESC, created_at_epoch_millis DESC
    """,
  )
  fun observeVehicles(): Flow<List<VehicleEntity>>

  @Query(
    """
    SELECT * FROM vehicles
    ORDER BY is_primary DESC, created_at_epoch_millis DESC
    """,
  )
  suspend fun getAllOnce(): List<VehicleEntity>

  @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
  suspend fun getById(id: String): VehicleEntity?

  @Query("SELECT * FROM vehicles WHERE plate = :plate LIMIT 1")
  suspend fun getByPlate(plate: String): VehicleEntity?

  @Query("SELECT COUNT(*) FROM vehicles")
  suspend fun count(): Int

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: VehicleEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: VehicleEntity)

  @Update
  suspend fun update(entity: VehicleEntity)

  @Query("UPDATE vehicles SET is_primary = 0 WHERE is_primary = 1")
  suspend fun clearAllPrimary()

  @Query("UPDATE vehicles SET is_primary = 1, updated_at_epoch_millis = :updatedAt WHERE id = :id")
  suspend fun markPrimary(id: String, updatedAt: Long = System.currentTimeMillis())

  @Query("DELETE FROM vehicles WHERE id = :id")
  suspend fun deleteById(id: String): Int

  @Query("SELECT * FROM vehicles ORDER BY created_at_epoch_millis DESC LIMIT 1")
  suspend fun getMostRecent(): VehicleEntity?

  @Query("SELECT * FROM vehicles WHERE id != :excludedId ORDER BY created_at_epoch_millis DESC LIMIT 1")
  suspend fun getMostRecentExcluding(excludedId: String): VehicleEntity?

  @Transaction
  suspend fun setPrimaryTransaction(id: String) {
    clearAllPrimary()
    markPrimary(id)
  }

  @Transaction
  suspend fun insertVehicleTransaction(entity: VehicleEntity): VehicleEntity {
    val existingCount = count()
    val shouldBePrimary = existingCount == 0 || entity.isPrimary
    if (shouldBePrimary) {
      clearAllPrimary()
    }
    val toInsert = entity.copy(isPrimary = shouldBePrimary)
    insert(toInsert)
    return toInsert
  }

  @Transaction
  suspend fun updateVehicleTransaction(entity: VehicleEntity): VehicleEntity {
    val existing = getById(entity.id)
    val othersExist = getMostRecentExcluding(entity.id) != null
    val resolvedPrimary =
      when {
        entity.isPrimary -> {
          clearAllPrimary()
          true
        }
        existing?.isPrimary == true && !othersExist -> true
        existing?.isPrimary == true && othersExist -> {
          getMostRecentExcluding(entity.id)?.let { candidate ->
            markPrimary(candidate.id)
          }
          false
        }
        else -> false
      }
    val updated = entity.copy(isPrimary = resolvedPrimary)
    update(updated)
    return updated
  }

  @Transaction
  suspend fun deleteAndPromoteTransaction(id: String): VehicleEntity? {
    val target = getById(id) ?: return null
    deleteById(id)
    if (target.isPrimary) {
      getMostRecent()?.let { nextPrimary ->
        markPrimary(nextPrimary.id)
      }
    }
    return target
  }

  @Transaction
  suspend fun restoreVehicleTransaction(entity: VehicleEntity): VehicleEntity {
    val existingCount = count()
    val shouldBePrimary = existingCount == 0 || entity.isPrimary
    if (shouldBePrimary) {
      clearAllPrimary()
    }
    val restored = entity.copy(isPrimary = shouldBePrimary)
    upsert(restored)
    return restored
  }
}

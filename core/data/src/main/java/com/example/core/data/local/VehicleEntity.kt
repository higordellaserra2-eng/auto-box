package com.example.core.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.data.model.Vehicle
import com.example.core.data.model.VehicleType

@Entity(
  tableName = "vehicles",
  indices = [
    Index(value = ["plate"], unique = true),
    Index(value = ["is_primary"]),
  ],
)
data class VehicleEntity(
  @PrimaryKey
  @ColumnInfo(name = "id")
  val id: String,
  @ColumnInfo(name = "plate")
  val plate: String,
  @ColumnInfo(name = "type")
  val type: String,
  @ColumnInfo(name = "brand")
  val brand: String,
  @ColumnInfo(name = "model")
  val model: String,
  @ColumnInfo(name = "year")
  val year: Int,
  @ColumnInfo(name = "color")
  val color: String?,
  @ColumnInfo(name = "nickname")
  val nickname: String?,
  @ColumnInfo(name = "mileage_km")
  val mileageKm: Int?,
  @ColumnInfo(name = "is_primary")
  val isPrimary: Boolean,
  @ColumnInfo(name = "created_at_epoch_millis")
  val createdAtEpochMillis: Long,
  @ColumnInfo(name = "updated_at_epoch_millis")
  val updatedAtEpochMillis: Long,
) {
  fun toDomain(): Vehicle =
    Vehicle(
      id = id,
      plate = plate,
      type = VehicleType.entries.firstOrNull { it.name == type } ?: VehicleType.CAR,
      brand = brand,
      model = model,
      year = year,
      color = color,
      nickname = nickname,
      mileageKm = mileageKm,
      isPrimary = isPrimary,
      createdAtEpochMillis = createdAtEpochMillis,
    )

  companion object {
    fun fromDomain(
      vehicle: Vehicle,
      updatedAtEpochMillis: Long = System.currentTimeMillis(),
    ): VehicleEntity =
      VehicleEntity(
        id = vehicle.id,
        plate = vehicle.plate,
        type = vehicle.type.name,
        brand = vehicle.brand,
        model = vehicle.model,
        year = vehicle.year,
        color = vehicle.color,
        nickname = vehicle.nickname,
        mileageKm = vehicle.mileageKm,
        isPrimary = vehicle.isPrimary,
        createdAtEpochMillis = vehicle.createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
      )
  }
}

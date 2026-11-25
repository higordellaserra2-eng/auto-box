package com.example.core.data.model

import java.time.Year

enum class VehicleType {
  CAR,
  MOTORCYCLE,
  OTHER,
}

data class Vehicle(
  val id: String,
  val plate: String,
  val type: VehicleType,
  val brand: String,
  val model: String,
  val year: Int,
  val color: String? = null,
  val nickname: String? = null,
  val mileageKm: Int? = null,
  val isPrimary: Boolean = false,
  val createdAtEpochMillis: Long = System.currentTimeMillis(),
)

data class PlateLookupInfo(
  val plate: String,
  val type: VehicleType,
  val brand: String,
  val model: String,
  val year: Int,
  val color: String? = null,
)

sealed interface PlateLookupResult {
  data class Found(val info: PlateLookupInfo) : PlateLookupResult

  data object NotFound : PlateLookupResult
}

object VehicleCatalog {
  private val carBrandsAndModels: Map<String, List<String>> =
    linkedMapOf(
      "BYD" to listOf("Dolphin", "Dolphin Mini", "Song Plus", "Yuan Plus"),
      "Chevrolet" to listOf("Onix", "Onix Plus", "Tracker", "Spin", "S10"),
      "Fiat" to listOf("Argo", "Cronos", "Mobi", "Pulse", "Fastback", "Strada", "Toro"),
      "Ford" to listOf("Ka", "EcoSport", "Ranger", "Territory"),
      "Honda" to listOf("City", "Civic", "HR-V", "ZR-V", "Fit"),
      "Hyundai" to listOf("HB20", "HB20S", "Creta", "Tucson"),
      "Jeep" to listOf("Renegade", "Compass", "Commander"),
      "Nissan" to listOf("Kicks", "Versa", "Sentra", "Frontier"),
      "Peugeot" to listOf("208", "2008"),
      "Renault" to listOf("Kwid", "Sandero", "Logan", "Duster", "Kardian"),
      "Toyota" to listOf("Corolla", "Corolla Cross", "Yaris", "Hilux", "SW4"),
      "Volkswagen" to listOf("Polo", "Virtus", "Nivus", "T-Cross", "Gol", "Saveiro"),
    )

  private val motorcycleBrandsAndModels: Map<String, List<String>> =
    linkedMapOf(
      "BMW Motorrad" to listOf("G 310 GS", "F 850 GS", "R 1250 GS"),
      "Dafra" to listOf("Citycom 300i", "NH 190", "Cruisym 150"),
      "Honda" to listOf("CG 160 Titan", "CG 160 Fan", "Biz 125", "PCX 160", "CB 300F Twister", "XRE 300"),
      "Kawasaki" to listOf("Ninja 400", "Z400", "Versys-X 300"),
      "Royal Enfield" to listOf("Meteor 350", "Hunter 350", "Himalayan 450"),
      "Suzuki" to listOf("Burgman 125", "V-Strom 650", "GSX-S750"),
      "Yamaha" to listOf("Fazer FZ25", "Fazer FZ15", "Factor 150", "Lander 250", "NMax 160"),
    )

  private val otherBrandsAndModels: Map<String, List<String>> =
    linkedMapOf(
      "Fiat" to listOf("Fiorino", "Ducato"),
      "Iveco" to listOf("Daily"),
      "Mercedes-Benz" to listOf("Sprinter", "Accelo"),
      "Renault" to listOf("Master", "Kangoo"),
      "Volkswagen" to listOf("Delivery", "Amarok"),
    )

  fun brandsFor(type: VehicleType): List<String> =
    when (type) {
      VehicleType.CAR -> carBrandsAndModels.keys.toList()
      VehicleType.MOTORCYCLE -> motorcycleBrandsAndModels.keys.toList()
      VehicleType.OTHER -> otherBrandsAndModels.keys.toList()
    }

  fun modelsFor(type: VehicleType, brand: String): List<String> {
    val map =
      when (type) {
        VehicleType.CAR -> carBrandsAndModels
        VehicleType.MOTORCYCLE -> motorcycleBrandsAndModels
        VehicleType.OTHER -> otherBrandsAndModels
      }
    val exact = map[brand]
    if (exact != null) return exact
    return map.entries.firstOrNull { it.key.equals(brand, ignoreCase = true) }?.value ?: emptyList()
  }

  fun years(currentYear: Int = Year.now().value + 1): List<Int> =
    (currentYear downTo 1985).toList()

  val commonColors: List<String> =
    listOf("Branco", "Prata", "Preto", "Cinza", "Vermelho", "Azul", "Verde", "Marrom")
}

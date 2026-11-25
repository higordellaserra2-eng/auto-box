package com.example.core.data.model

import java.time.LocalDate

data class HomeSummary(
  val customerFirstName: String,
  val pointsSummary: PointsSummary,
  val primaryVehicle: VehicleSummary?,
  val nextReminder: ServiceReminder?,
  val pendingPaymentAmountCents: Long?,
)

data class PointsSummary(
  val currentPoints: Int,
  val nextRewardPoints: Int,
  val nextRewardTitle: String,
  val cashbackAvailableCents: Long,
) {
  val progressFraction: Float
    get() =
      if (nextRewardPoints <= 0) 1f
      else (currentPoints.toFloat() / nextRewardPoints.toFloat()).coerceIn(0f, 1f)
}

data class VehicleSummary(
  val id: String,
  val plate: String,
  val model: String,
  val year: Int,
)

enum class ReminderPriority {
  INFO,
  UPCOMING,
  DUE_SOON,
}

data class ServiceReminder(
  val id: String,
  val title: String,
  val description: String,
  val vehicleLabel: String,
  val dueDate: LocalDate,
  val priority: ReminderPriority,
)

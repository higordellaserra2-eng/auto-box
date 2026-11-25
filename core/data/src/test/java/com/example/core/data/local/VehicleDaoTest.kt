package com.example.core.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.core.data.model.VehicleType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VehicleDaoTest {

  private lateinit var database: AutoBoxDatabase
  private lateinit var vehicleDao: VehicleDao

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database =
      Room.inMemoryDatabaseBuilder(context, AutoBoxDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    vehicleDao = database.vehicleDao()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `first inserted vehicle automatically becomes primary`() = runTest {
    val first = entity(id = "v1", plate = "BRA2E19", isPrimary = false, createdAt = 100L)
    val saved = vehicleDao.insertVehicleTransaction(first)

    assertTrue(saved.isPrimary)
    vehicleDao.observeVehicles().test {
      val list = awaitItem()
      assertEquals(1, list.size)
      assertTrue(list.first().isPrimary)
    }
  }

  @Test(expected = SQLiteConstraintException::class)
  fun `inserting duplicate plate throws SQLiteConstraintException`() = runTest {
    vehicleDao.insertVehicleTransaction(entity(id = "v1", plate = "BRA2E19", createdAt = 100L))
    vehicleDao.insertVehicleTransaction(entity(id = "v2", plate = "BRA2E19", createdAt = 200L))
  }

  @Test
  fun `deleting primary vehicle promotes most recent remaining vehicle`() = runTest {
    val v1 = entity(id = "v1", plate = "BRA2E19", isPrimary = true, createdAt = 100L)
    val v2 = entity(id = "v2", plate = "ABC1234", isPrimary = false, createdAt = 200L)
    val v3 = entity(id = "v3", plate = "FGH4I56", isPrimary = false, createdAt = 300L)

    vehicleDao.insertVehicleTransaction(v1)
    vehicleDao.insertVehicleTransaction(v2)
    vehicleDao.insertVehicleTransaction(v3)

    val deleted = vehicleDao.deleteAndPromoteTransaction("v1")
    assertNotNull(deleted)
    assertNull(vehicleDao.getById("v1"))

    val promoted = vehicleDao.getById("v3")
    assertNotNull(promoted)
    assertTrue(promoted!!.isPrimary)

    val remaining = vehicleDao.getAllOnce()
    assertEquals(listOf("v3", "v2"), remaining.map { it.id })
  }

  @Test
  fun `setPrimaryTransaction updates primary flag atomically`() = runTest {
    vehicleDao.insertVehicleTransaction(entity(id = "v1", plate = "BRA2E19", isPrimary = true, createdAt = 100L))
    vehicleDao.insertVehicleTransaction(entity(id = "v2", plate = "ABC1234", isPrimary = false, createdAt = 200L))

    vehicleDao.setPrimaryTransaction("v2")

    val all = vehicleDao.getAllOnce()
    assertEquals("v2", all.first { it.isPrimary }.id)
    assertEquals(1, all.count { it.isPrimary })
  }

  private fun entity(
    id: String,
    plate: String,
    isPrimary: Boolean = false,
    createdAt: Long = 1000L,
  ): VehicleEntity =
    VehicleEntity(
      id = id,
      plate = plate,
      type = VehicleType.CAR.name,
      brand = "Hyundai",
      model = "HB20",
      year = 2023,
      color = "Prata",
      nickname = null,
      mileageKm = null,
      isPrimary = isPrimary,
      createdAtEpochMillis = createdAt,
      updatedAtEpochMillis = createdAt,
    )
}

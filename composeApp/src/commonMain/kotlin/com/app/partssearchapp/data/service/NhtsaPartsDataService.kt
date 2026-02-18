package com.app.partssearchapp.data.service

import com.app.partssearchapp.data.models.*
import com.app.partssearchapp.network.nhtsa.NhtsaApiClient

/**
 * PartsDataService implementation that uses:
 * - NHTSA vPIC API for vehicle data (makes, years, models)
 * - InventoryManager for live parts/vendor/listing data
 */
class NhtsaPartsDataService(
  private val nhtsaApiClient: NhtsaApiClient,
  private val inventoryManager: InventoryManager,
) : PartsDataService {

  private var cachedMakes: List<VehicleMake>? = null

  override suspend fun getMakes(): List<VehicleMake> {
    cachedMakes?.let { return it }
    return try {
      val makes = nhtsaApiClient.getAllMakes()
      cachedMakes = makes
      makes
    } catch (e: Exception) {
      MockPartsDataService.makes
    }
  }

  override suspend fun getYearsForMake(makeId: Int): List<Int> {
    return (2025 downTo 2000).toList()
  }

  override suspend fun getModelsForMakeAndYear(makeId: Int, year: Int): List<VehicleModel> {
    return try {
      nhtsaApiClient.getModelsForMakeAndYear(makeId, year)
    } catch (e: Exception) {
      MockPartsDataService.models.filter { it.makeId == makeId && it.year == year }
    }
  }

  override suspend fun getEnginesForModel(modelId: Int): List<VehicleEngine> {
    return getCommonEnginesForModel(modelId)
  }

  override suspend fun getCategoriesForEngine(engineId: Int): List<PartCategory> {
    return inventoryManager.categories.value
  }

  override suspend fun getPartsForCategory(categoryId: Int, engineId: Int): List<Part> {
    return inventoryManager.getAllPartsForCategory(categoryId)
  }

  override suspend fun getListingsForPart(partId: Int): List<VendorListing> {
    return inventoryManager.getAllListingsForPart(partId)
  }

  override suspend fun getVendor(vendorId: Int): Vendor? {
    return inventoryManager.getVendor(vendorId)
  }

  override suspend fun getVendors(): List<Vendor> {
    return inventoryManager.vendors.value
  }

  override suspend fun searchParts(query: String): List<Part> {
    return inventoryManager.searchParts(query)
  }

  private fun getCommonEnginesForModel(modelId: Int): List<VehicleEngine> {
    val baseId = modelId * 100
    return listOf(
      VehicleEngine(baseId + 1, "1.6L L4 DOHC", modelId),
      VehicleEngine(baseId + 2, "2.0L L4 DOHC", modelId),
      VehicleEngine(baseId + 3, "2.5L L4 DOHC", modelId),
    )
  }
}

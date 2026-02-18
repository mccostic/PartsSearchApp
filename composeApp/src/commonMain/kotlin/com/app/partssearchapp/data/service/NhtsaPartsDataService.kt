package com.app.partssearchapp.data.service

import com.app.partssearchapp.data.models.*
import com.app.partssearchapp.network.nhtsa.NhtsaApiClient

/**
 * PartsDataService implementation that uses the NHTSA vPIC API for vehicle data
 * (makes, years, models) and falls back to mock data for parts, vendors, and categories
 * which will come from vendor-sourced data in the real app.
 */
class NhtsaPartsDataService(
  private val nhtsaApiClient: NhtsaApiClient,
) : PartsDataService {

  // Cache makes to avoid repeated API calls
  private var cachedMakes: List<VehicleMake>? = null

  override suspend fun getMakes(): List<VehicleMake> {
    cachedMakes?.let { return it }
    return try {
      val makes = nhtsaApiClient.getAllMakes()
      cachedMakes = makes
      makes
    } catch (e: Exception) {
      // Fallback to mock data if API fails
      MockPartsDataService.makes
    }
  }

  override suspend fun getYearsForMake(makeId: Int): List<Int> {
    // NHTSA doesn't have a year-specific endpoint per make,
    // so we provide a reasonable range. Model year data goes back to 1981.
    return (2025 downTo 2000).toList()
  }

  override suspend fun getModelsForMakeAndYear(makeId: Int, year: Int): List<VehicleModel> {
    return try {
      nhtsaApiClient.getModelsForMakeAndYear(makeId, year)
    } catch (e: Exception) {
      // Fallback to mock data
      MockPartsDataService.models.filter { it.makeId == makeId && it.year == year }
    }
  }

  override suspend fun getEnginesForModel(modelId: Int): List<VehicleEngine> {
    // NHTSA doesn't provide engine data per model in the free API.
    // In production, this would come from TecDoc or vendor-sourced data.
    // For now, provide common engine configurations.
    return getCommonEnginesForModel(modelId)
  }

  override suspend fun getCategoriesForEngine(engineId: Int): List<PartCategory> {
    // Part categories are universal - not sourced from NHTSA.
    // These come from vendor catalog data.
    return MockPartsDataService.categories
  }

  override suspend fun getPartsForCategory(categoryId: Int, engineId: Int): List<Part> {
    // Parts data comes from vendor registrations, not NHTSA.
    // Since NHTSA engine IDs won't match mock data, show all parts for the category.
    return MockPartsDataService.parts.filter { it.categoryId == categoryId }
  }

  override suspend fun getListingsForPart(partId: Int): List<VendorListing> {
    return MockPartsDataService.vendorListings.filter { it.partId == partId }
  }

  override suspend fun getVendor(vendorId: Int): Vendor? {
    return MockPartsDataService.vendors.find { it.id == vendorId }
  }

  override suspend fun getVendors(): List<Vendor> {
    return MockPartsDataService.vendors
  }

  override suspend fun searchParts(query: String): List<Part> {
    val lowerQuery = query.lowercase()
    return MockPartsDataService.parts.filter {
      it.name.lowercase().contains(lowerQuery) ||
        it.partNumber.lowercase().contains(lowerQuery) ||
        it.description.lowercase().contains(lowerQuery)
    }
  }

  /**
   * Generate common engine options for a model.
   * In production, this data would come from TecDoc or a parts compatibility DB.
   */
  private fun getCommonEnginesForModel(modelId: Int): List<VehicleEngine> {
    // Use modelId as a seed to generate plausible engine options.
    // This ensures each model gets unique but consistent engine IDs.
    val baseId = modelId * 100
    return listOf(
      VehicleEngine(baseId + 1, "1.6L L4 DOHC", modelId),
      VehicleEngine(baseId + 2, "2.0L L4 DOHC", modelId),
      VehicleEngine(baseId + 3, "2.5L L4 DOHC", modelId),
    )
  }
}

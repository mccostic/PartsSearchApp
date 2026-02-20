package com.app.partssearchapp.fakes

import com.app.partssearchapp.data.models.*
import com.app.partssearchapp.data.service.PartsDataService

class FakePartsDataService : PartsDataService {

  var makes = listOf(
    VehicleMake(1, "Toyota"),
    VehicleMake(2, "Honda"),
    VehicleMake(3, "Nissan"),
  )
  var modelsForMake = mapOf(
    1 to listOf(
      VehicleModel(1, "Corolla", 1, 2024),
      VehicleModel(2, "Camry", 1, 2024),
    ),
    2 to listOf(
      VehicleModel(3, "Civic", 2, 2024),
    ),
  )
  var yearsForMake = mapOf(
    1 to listOf(2024, 2023, 2022),
    2 to listOf(2024, 2023),
  )
  var enginesForModel = mapOf(
    1 to listOf(
      VehicleEngine(1, "1.8L L4 DOHC", 1),
      VehicleEngine(2, "2.0L L4 DOHC", 1),
    ),
  )
  var categories = listOf(
    PartCategory(1, "Brake & Wheel Hub", subcategories = listOf(
      PartCategory(11, "Brake Pad", parentId = 1),
    )),
    PartCategory(2, "Engine", subcategories = listOf(
      PartCategory(21, "Oil Filter", parentId = 2),
    )),
  )
  var partsForCategory = mapOf(
    11 to listOf(
      Part(1, "Front Brake Pad", "BP-001", 11, "Brake pads", listOf("Ceramic"), listOf(1, 2)),
    ),
  )
  var listingsForPart = mapOf(
    1 to listOf(
      VendorListing(1, 1, 1, "Vendor A", "BRAND", "PN-001", 100.0, "GHS", true, 10),
      VendorListing(2, 1, 2, "Vendor B", "BRAND2", "PN-002", 80.0, "GHS", true, 5),
    ),
  )
  var vendors = listOf(
    Vendor(1, "Vendor A", "Accra", "+233 20 111 2222", 4.5, 100, true),
  )
  var searchResults = listOf(
    Part(1, "Front Brake Pad", "BP-001", 11, "Brake pads", listOf("Ceramic"), listOf(1)),
  )

  var shouldThrow = false
  var errorMessage = "Service error"

  override suspend fun getMakes(): List<VehicleMake> {
    if (shouldThrow) throw Exception(errorMessage)
    return makes
  }

  override suspend fun getYearsForMake(makeId: Int): List<Int> {
    if (shouldThrow) throw Exception(errorMessage)
    return yearsForMake[makeId] ?: emptyList()
  }

  override suspend fun getModelsForMake(makeId: Int): List<VehicleModel> {
    if (shouldThrow) throw Exception(errorMessage)
    return modelsForMake[makeId] ?: emptyList()
  }

  override suspend fun getModelsForMakeAndYear(makeId: Int, year: Int): List<VehicleModel> {
    if (shouldThrow) throw Exception(errorMessage)
    return modelsForMake[makeId]?.filter { it.year == year } ?: emptyList()
  }

  override suspend fun getEnginesForModel(makeId: Int, year: Int, modelId: Int): List<VehicleEngine> {
    if (shouldThrow) throw Exception(errorMessage)
    return enginesForModel[modelId] ?: emptyList()
  }

  override suspend fun getCategoriesForEngine(engineId: Int): List<PartCategory> {
    if (shouldThrow) throw Exception(errorMessage)
    return categories
  }

  override suspend fun getPartsForCategory(categoryId: Int, engineId: Int): List<Part> {
    if (shouldThrow) throw Exception(errorMessage)
    return partsForCategory[categoryId] ?: emptyList()
  }

  override suspend fun getListingsForPart(partId: Int): List<VendorListing> {
    if (shouldThrow) throw Exception(errorMessage)
    return listingsForPart[partId] ?: emptyList()
  }

  override suspend fun getVendor(vendorId: Int): Vendor? {
    if (shouldThrow) throw Exception(errorMessage)
    return vendors.find { it.id == vendorId }
  }

  override suspend fun getVendors(): List<Vendor> {
    if (shouldThrow) throw Exception(errorMessage)
    return vendors
  }

  override suspend fun searchParts(query: String): List<Part> {
    if (shouldThrow) throw Exception(errorMessage)
    return searchResults.filter {
      it.name.contains(query, ignoreCase = true) ||
        it.partNumber.contains(query, ignoreCase = true)
    }
  }
}

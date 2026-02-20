package com.app.partssearchapp.data.service

import com.app.partssearchapp.data.models.*
import com.app.partssearchapp.network.nhtsa.NhtsaApiClient

/**
 * PartsDataService implementation that uses:
 * - VpicLocalDataSource (SQLite/SQLDelight) for vehicle data (makes, years, models, engines)
 * - Falls back to NHTSA API / mock data if local DB is unavailable
 * - InventoryManager for live parts/vendor/listing data
 *
 * Tracks which data source provided makes to ensure consistent ID usage
 * across subsequent lookups (models, years, engines).
 */
class NhtsaPartsDataService(
    private val nhtsaApiClient: NhtsaApiClient,
    private val inventoryManager: InventoryManager,
    private val vpicLocalDataSource: VpicLocalDataSource,
) : PartsDataService {

    private var cachedMakes: List<VehicleMake>? = null

    private enum class DataSource { VPIC, API, MOCK }
    private var activeSource: DataSource = DataSource.MOCK

    override suspend fun getMakes(): List<VehicleMake> {
        cachedMakes?.let { return it }

        // Try local vPIC database first
        if (vpicLocalDataSource.isAvailable) {
            val localMakes = vpicLocalDataSource.getMakes()
            if (localMakes.isNotEmpty()) {
                cachedMakes = localMakes
                activeSource = DataSource.VPIC
                return localMakes
            }
        }

        // Try NHTSA API
        return try {
            val makes = nhtsaApiClient.getAllMakes()
            if (makes.isNotEmpty()) {
                cachedMakes = makes
                activeSource = DataSource.API
                makes
            } else {
                cachedMakes = MockPartsDataService.makes
                activeSource = DataSource.MOCK
                MockPartsDataService.makes
            }
        } catch (e: Exception) {
            cachedMakes = MockPartsDataService.makes
            activeSource = DataSource.MOCK
            MockPartsDataService.makes
        }
    }

    override suspend fun getYearsForMake(makeId: Int): List<Int> {
        if (activeSource == DataSource.VPIC && vpicLocalDataSource.isAvailable) {
            val localYears = vpicLocalDataSource.getYearsForMake(makeId)
            if (localYears.isNotEmpty()) return localYears
        }
        return (2025 downTo 2000).toList()
    }

    override suspend fun getModelsForMake(makeId: Int): List<VehicleModel> {
        // Use the same data source that provided the makes
        when (activeSource) {
            DataSource.VPIC -> {
                val localModels = vpicLocalDataSource.getModels(makeId)
                if (localModels.isNotEmpty()) return localModels
            }
            DataSource.API -> {
                try {
                    val models = nhtsaApiClient.getModelsForMakeAndYear(makeId, 2025)
                    if (models.isNotEmpty()) return models
                } catch (_: Exception) {}
            }
            DataSource.MOCK -> {
                // Fall through to mock below
            }
        }

        return MockPartsDataService.models
            .filter { it.makeId == makeId }
            .distinctBy { it.name }
    }

    override suspend fun getModelsForMakeAndYear(makeId: Int, year: Int): List<VehicleModel> {
        when (activeSource) {
            DataSource.VPIC -> {
                val localModels = vpicLocalDataSource.getModels(makeId)
                if (localModels.isNotEmpty()) return localModels.map { it.copy(year = year) }
            }
            DataSource.API -> {
                try {
                    val models = nhtsaApiClient.getModelsForMakeAndYear(makeId, year)
                    if (models.isNotEmpty()) return models
                } catch (_: Exception) {}
            }
            DataSource.MOCK -> {
                // Fall through to mock below
            }
        }

        return MockPartsDataService.models.filter { it.makeId == makeId && it.year == year }
    }

    override suspend fun getEnginesForModel(makeId: Int, year: Int, modelId: Int): List<VehicleEngine> {
        if (activeSource == DataSource.VPIC && vpicLocalDataSource.isAvailable) {
            val localEngines = vpicLocalDataSource.getEngineSpecs(makeId, year, modelId)
            if (localEngines.isNotEmpty()) return localEngines
        }
        return getCommonEnginesForModel(modelId)
    }

    override suspend fun getCategoriesForEngine(engineId: Int): List<PartCategory> = inventoryManager.categories.value

    override suspend fun getPartsForCategory(categoryId: Int, engineId: Int): List<Part> = inventoryManager.getAllPartsForCategory(categoryId)

    override suspend fun getListingsForPart(partId: Int): List<VendorListing> = inventoryManager.getAllListingsForPart(partId)

    override suspend fun getVendor(vendorId: Int): Vendor? = inventoryManager.getVendor(vendorId)

    override suspend fun getVendors(): List<Vendor> = inventoryManager.vendors.value

    override suspend fun searchParts(query: String): List<Part> = inventoryManager.searchParts(query)

    private fun getCommonEnginesForModel(modelId: Int): List<VehicleEngine> {
        val baseId = modelId * 100
        return listOf(
            VehicleEngine(baseId + 1, "1.6L L4 DOHC", modelId),
            VehicleEngine(baseId + 2, "2.0L L4 DOHC", modelId),
            VehicleEngine(baseId + 3, "2.5L L4 DOHC", modelId),
        )
    }
}

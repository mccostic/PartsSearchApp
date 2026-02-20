package com.app.partssearchapp.data.service

import com.app.partssearchapp.data.models.VehicleEngine
import com.app.partssearchapp.data.models.VehicleMake
import com.app.partssearchapp.data.models.VehicleModel
import com.app.partssearchapp.database.DatabaseDriverFactory
import com.app.partssearchapp.database.VpicDatabase

class VpicLocalDataSource(driverFactory: DatabaseDriverFactory?) {

    private val database: VpicDatabase? = try {
        driverFactory?.let { VpicDatabase(it.createDriver()) }
    } catch (e: Exception) {
        null
    }

    val isAvailable: Boolean get() = database != null

    fun getMakes(): List<VehicleMake> {
        val db = database ?: return emptyList()
        return db.vpicQueries.getMakes().executeAsList().map { row ->
            VehicleMake(id = row.id.toInt(), name = row.name)
        }
    }

    fun getModels(makeId: Int): List<VehicleModel> {
        val db = database ?: return emptyList()
        return db.vpicQueries.getModelsForMake(makeId = makeId.toLong()).executeAsList().map { row ->
            VehicleModel(
                id = row.id.toInt(),
                name = row.name,
                makeId = makeId,
                year = 0,
            )
        }
    }

    fun getYearsForMake(makeId: Int): List<Int> {
        val db = database ?: return emptyList()
        return db.vpicQueries.getYearsForMake(makeId = makeId.toLong())
            .executeAsList()
            .map { it.toInt() }
    }

    fun getEngineSpecs(makeId: Int, year: Int, modelId: Int): List<VehicleEngine> {
        val db = database ?: return emptyList()
        val results = db.vpicQueries.getEngineSpecs(
            makeId = makeId.toLong(),
            year = year.toLong(),
            modelId = modelId.toString(),
        ).executeAsList()

        return results.mapIndexed { index, row ->
            val description = buildEngineDescription(
                displacementL = row.displacement_l,
                cylinders = row.cylinders,
                engineConfig = row.engine_config,
                fuelType = row.fuel_type,
                turbo = row.turbo,
                valveTrain = row.valve_train,
                horsepowerFrom = row.horsepower_from,
                horsepowerTo = row.horsepower_to,
            )
            val engineId = generateEngineId(makeId, year, modelId, index)
            VehicleEngine(
                id = engineId,
                description = description,
                modelId = modelId,
            )
        }
    }

    private fun buildEngineDescription(
        displacementL: String?,
        cylinders: String?,
        engineConfig: String?,
        fuelType: String?,
        turbo: String?,
        valveTrain: String?,
        horsepowerFrom: String?,
        horsepowerTo: String?,
    ): String {
        val parts = mutableListOf<String>()

        displacementL?.let { parts.add("${it}L") }

        val cylinderConfig = when {
            engineConfig != null && cylinders != null -> {
                val prefix = when {
                    engineConfig.contains("In-Line", ignoreCase = true) ||
                        engineConfig.contains("Inline", ignoreCase = true) -> "L"
                    engineConfig.contains("V-Type", ignoreCase = true) ||
                        engineConfig.contains("V-Shaped", ignoreCase = true) -> "V"
                    engineConfig.contains("Flat", ignoreCase = true) ||
                        engineConfig.contains("Opposed", ignoreCase = true) -> "H"
                    engineConfig.contains("Rotary", ignoreCase = true) -> "R"
                    else -> ""
                }
                "$prefix$cylinders"
            }
            cylinders != null -> "${cylinders}cyl"
            else -> null
        }
        cylinderConfig?.let { parts.add(it) }

        valveTrain?.let { parts.add(it) }

        turbo?.takeIf { it.isNotBlank() && !it.equals("NA", true) && !it.equals("N/A", true) }?.let {
            parts.add("Turbo")
        }

        fuelType?.let {
            when {
                it.contains("Diesel", ignoreCase = true) -> parts.add("Diesel")
                it.contains("Electric", ignoreCase = true) ||
                    it.contains("Hybrid", ignoreCase = true) -> parts.add(it)
            }
        }

        val hp = when {
            horsepowerFrom != null &&
                horsepowerTo != null &&
                horsepowerFrom != horsepowerTo -> "$horsepowerFrom-${horsepowerTo}hp"
            horsepowerFrom != null -> "${horsepowerFrom}hp"
            else -> null
        }
        hp?.let { parts.add(it) }

        return if (parts.isEmpty()) "Unknown Engine" else parts.joinToString(" ")
    }

    private fun generateEngineId(
        makeId: Int,
        year: Int,
        modelId: Int,
        index: Int
    ): Int = (makeId * 1000000) +
        ((year % 100) * 10000) +
        ((modelId % 100) * 100) +
        index
}

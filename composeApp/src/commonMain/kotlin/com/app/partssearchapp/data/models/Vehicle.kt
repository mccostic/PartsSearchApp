package com.app.partssearchapp.data.models

import kotlinx.serialization.Serializable

@Serializable
data class VehicleMake(val id: Int, val name: String,)

@Serializable
data class VehicleYear(val year: Int, val makeId: Int,)

@Serializable
data class VehicleModel(val id: Int, val name: String, val makeId: Int, val year: Int,)

@Serializable
data class VehicleEngine(val id: Int, val description: String, val modelId: Int,)

@Serializable
data class VehicleSelection(
    val make: VehicleMake? = null,
    val year: Int? = null,
    val model: VehicleModel? = null,
    val engine: VehicleEngine? = null,
) {
    val breadcrumb: String
        get() {
            val parts = mutableListOf<String>()
            make?.let { parts.add(it.name) }
            model?.let { parts.add(it.name) }
            year?.let { parts.add(it.toString()) }
            engine?.let { parts.add(it.description) }
            return parts.joinToString(" > ")
        }

    val isComplete: Boolean
        get() = make != null && year != null && model != null && engine != null
}

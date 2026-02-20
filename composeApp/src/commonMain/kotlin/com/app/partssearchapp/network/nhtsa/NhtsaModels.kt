package com.app.partssearchapp.network.nhtsa

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NhtsaResponse<T>(
    @SerialName("Count") val count: Int,
    @SerialName("Message") val message: String,
    @SerialName("SearchCriteria") val searchCriteria: String? = null,
    @SerialName("Results") val results: List<T>,
)

@Serializable
data class NhtsaMake(@SerialName("Make_ID") val makeId: Int, @SerialName("Make_Name") val makeName: String,)

@Serializable
data class NhtsaModel(
    @SerialName("Make_ID") val makeId: Int,
    @SerialName("Make_Name") val makeName: String,
    @SerialName("Model_ID") val modelId: Int,
    @SerialName("Model_Name") val modelName: String,
)

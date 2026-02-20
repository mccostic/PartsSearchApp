package com.app.partssearchapp.network.nhtsa

import com.app.partssearchapp.data.models.VehicleMake
import com.app.partssearchapp.data.models.VehicleModel
import com.app.partssearchapp.network.createPlatformHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class NhtsaApiClient {

    private val httpClient: HttpClient = createPlatformHttpClient().config {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }

    private val baseUrl = "https://vpic.nhtsa.dot.gov/api/vehicles"

    // Common makes relevant to Ghana market, filtered from the full 10,000+ makes list
    private val popularMakeNames = setOf(
        "TOYOTA", "HONDA", "NISSAN", "HYUNDAI", "KIA",
        "MERCEDES-BENZ", "BMW", "VOLKSWAGEN", "FORD", "MITSUBISHI",
        "CHEVROLET", "MAZDA", "SUBARU", "SUZUKI", "PEUGEOT",
        "RENAULT", "ISUZU", "LEXUS", "AUDI", "LAND ROVER",
        "JEEP", "DODGE", "VOLVO", "FIAT", "OPEL",
        "DAEWOO", "DAIHATSU", "ACURA", "INFINITI", "CITROEN",
    )

    suspend fun getAllMakes(): List<VehicleMake> {
        val response: NhtsaResponse<NhtsaMake> = httpClient
            .get("$baseUrl/GetAllMakes?format=json")
            .body()

        return response.results
            .filter { it.makeName.uppercase() in popularMakeNames }
            .map { VehicleMake(id = it.makeId, name = formatMakeName(it.makeName)) }
            .sortedBy { it.name }
    }

    suspend fun getModelsForMakeAndYear(makeId: Int, year: Int): List<VehicleModel> {
        val response: NhtsaResponse<NhtsaModel> = httpClient
            .get("$baseUrl/GetModelsForMakeIdYear/makeId/$makeId/modelyear/$year?format=json")
            .body()

        return response.results.map { model ->
            VehicleModel(
                id = model.modelId,
                name = model.modelName,
                makeId = model.makeId,
                year = year,
            )
        }.sortedBy { it.name }
    }

    private fun formatMakeName(name: String): String = name.split(" ", "-").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }.replace(" - ", "-")
}

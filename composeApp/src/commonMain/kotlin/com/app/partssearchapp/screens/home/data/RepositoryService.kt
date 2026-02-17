package com.app.partssearchapp.screens.home.data

import com.app.partssearchapp.network.ApiClient
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Repository data model
 */
@Serializable
data class Repository(
  val id: Int,
  val name: String,
  val description: String,
  val language: String,
  val stars: Int,
  val isPrivate: Boolean,
)

/**
 * Service interface for repository operations
 */
interface RepositoryService {
  suspend fun getRepositories(): List<Repository>
  suspend fun getRepository(id: Int): Repository?
  suspend fun refreshRepositories(): List<Repository>
}

/**
 * Implementation of RepositoryService that depends on global ApiClient
 * This demonstrates how a feature-specific service can depend on global infrastructure
 */
class NetworkRepositoryService(
  private val apiClient: ApiClient,
) : RepositoryService {

  private val json = Json { ignoreUnknownKeys = true }

  override suspend fun getRepositories(): List<Repository> {
    val response = apiClient.get("/api/v1/repositories")
    return parseRepositoriesFromJson(response.data ?: error("No data in response"))
  }

  override suspend fun getRepository(id: Int): Repository? {
    return try {
      val response = apiClient.get("/api/v1/repositories/$id")

      if (response.isSuccess && response.data != null) {
        json.decodeFromString<Repository>(response.data)
      } else {
        null
      }
    } catch (e: Exception) {
      null
    }
  }

  override suspend fun refreshRepositories(): List<Repository> {
    delay(1000)
    return getRepositories()
  }

  private fun parseRepositoriesFromJson(jsonData: String): List<Repository> {
    val jsonObject = json.parseToJsonElement(jsonData)
    val repositoriesArray = jsonObject.jsonObject["repositories"]?.jsonArray

    return repositoriesArray?.map { element ->
      json.decodeFromJsonElement<Repository>(element)
    } ?: error("No repositories found")
  }
}

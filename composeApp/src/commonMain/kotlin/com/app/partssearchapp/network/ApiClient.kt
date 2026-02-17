package com.app.partssearchapp.network

import kotlinx.coroutines.delay

/**
 * Global API client interface for making network requests
 */
interface ApiClient {
  suspend fun get(endpoint: String): ApiResponse
  suspend fun post(endpoint: String, data: Any): ApiResponse
}

/**
 * Response model for API calls
 */
data class ApiResponse(
  val isSuccess: Boolean,
  val data: String?,
  val errorMessage: String? = null,
)

/**
 * Mock implementation of ApiClient for demo purposes
 * In a real app, this would handle actual HTTP requests
 */
class MockApiClient : ApiClient {

  override suspend fun get(endpoint: String): ApiResponse {
    // Simulate network delay
    delay(1000)

    return when {
      endpoint.contains("repositories") -> {
        ApiResponse(
          isSuccess = true,
          data = generateRepositoriesJson()
        )
      }

      endpoint.contains("user") -> {
        ApiResponse(
          isSuccess = true,
          data = """{"user": "active", "status": "authenticated"}"""
        )
      }

      else -> {
        ApiResponse(
          isSuccess = false,
          data = null,
          errorMessage = "Endpoint not found: $endpoint"
        )
      }
    }
  }

  override suspend fun post(endpoint: String, data: Any): ApiResponse {
    // Simulate network delay
    delay(300)

    return ApiResponse(
      isSuccess = true,
      data = """{"status": "success", "message": "Data posted successfully"}"""
    )
  }

  private fun generateRepositoriesJson(): String {
    return """
    {
      "repositories": [
        {
          "id": 1,
          "name": "Android Compose App",
          "description": "Modern Android app built with Jetpack Compose",
          "language": "Kotlin",
          "stars": 142,
          "isPrivate": false
        },
        {
          "id": 2,
          "name": "Weather Dashboard",
          "description": "Real-time weather monitoring dashboard",
          "language": "JavaScript",
          "stars": 89,
          "isPrivate": true
        },
        {
          "id": 3,
          "name": "Task Manager API",
          "description": "RESTful API for task management system",
          "language": "Python",
          "stars": 234,
          "isPrivate": false
        },
        {
          "id": 4,
          "name": "E-commerce Frontend",
          "description": "React-based e-commerce platform frontend",
          "language": "TypeScript",
          "stars": 456,
          "isPrivate": false
        },
        {
          "id": 5,
          "name": "ML Image Classifier",
          "description": "Machine learning model for image classification",
          "language": "Python",
          "stars": 78,
          "isPrivate": true
        },
        {
          "id": 6,
          "name": "iOS Weather App",
          "description": "Native iOS weather application",
          "language": "Swift",
          "stars": 123,
          "isPrivate": false
        }
      ]
    }
    """.trimIndent()
  }
}

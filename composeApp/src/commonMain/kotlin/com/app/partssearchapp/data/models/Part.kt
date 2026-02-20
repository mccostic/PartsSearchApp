package com.app.partssearchapp.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PartCategory(
  val id: Int,
  val name: String,
  val parentId: Int? = null,
  val subcategories: List<PartCategory> = emptyList(),
)

@Serializable
data class Part(
  val id: Int,
  val name: String,
  val partNumber: String,
  val categoryId: Int,
  val description: String,
  val specifications: List<String> = emptyList(),
  val compatibleEngineIds: List<Int> = emptyList(),
)

@Serializable
data class VendorListing(
  val id: Int,
  val partId: Int,
  val vendorId: Int,
  val vendorName: String,
  val brandName: String,
  val partNumber: String,
  val price: Double,
  val currency: String = "GHS",
  val inStock: Boolean = true,
  val stockQuantity: Int = 0,
  val condition: String = "New",
)

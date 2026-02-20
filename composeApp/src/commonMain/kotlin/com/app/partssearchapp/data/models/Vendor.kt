package com.app.partssearchapp.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Vendor(
  val id: Int,
  val name: String,
  val location: String,
  val phone: String,
  val rating: Double = 0.0,
  val totalOrders: Int = 0,
  val isVerified: Boolean = false,
)

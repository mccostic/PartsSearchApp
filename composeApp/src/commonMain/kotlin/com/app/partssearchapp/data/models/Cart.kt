package com.app.partssearchapp.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
  val id: Int,
  val vendorListing: VendorListing,
  val partName: String,
  val quantity: Int = 1,
) {
  val totalPrice: Double get() = vendorListing.price * quantity
}

@Serializable
data class Order(
  val id: Int,
  val items: List<CartItem>,
  val status: OrderStatus = OrderStatus.PENDING,
  val customerName: String = "",
  val customerPhone: String = "",
  val deliveryAddress: String = "",
) {
  val totalAmount: Double get() = items.sumOf { it.totalPrice }
}

@Serializable
enum class OrderStatus {
  PENDING,
  CONFIRMED,
  PROCESSING,
  SHIPPED,
  DELIVERED,
  CANCELLED,
}

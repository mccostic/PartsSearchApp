package com.app.partssearchapp.screens.vendor

import com.app.partssearchapp.data.models.Order
import com.app.partssearchapp.data.models.OrderStatus
import com.app.partssearchapp.data.models.Vendor
import com.app.partssearchapp.data.models.VendorListing

data class VendorDashboardState(
  val vendor: Vendor? = null,
  val listings: List<VendorListing> = emptyList(),
  val orders: List<Order> = emptyList(),
  val selectedTab: VendorTab = VendorTab.OVERVIEW,
  val isLoading: Boolean = false,
)

enum class VendorTab(val label: String) {
  OVERVIEW("Overview"),
  INVENTORY("Inventory"),
  ORDERS("Orders"),
}

sealed class VendorDashboardUIEvent {
  data class TabSelected(val tab: VendorTab) : VendorDashboardUIEvent()
  data class UpdateOrderStatus(val orderId: Int, val status: OrderStatus) : VendorDashboardUIEvent()
  data object BackPressed : VendorDashboardUIEvent()
}

sealed class VendorDashboardNavEvent {
  data object NavigateBack : VendorDashboardNavEvent()
}

sealed class VendorDashboardUIEffect

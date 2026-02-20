package com.app.partssearchapp.screens.vendor

import com.app.partssearchapp.data.models.Order
import com.app.partssearchapp.data.models.OrderStatus
import com.app.partssearchapp.data.models.Part
import com.app.partssearchapp.data.models.Vendor
import com.app.partssearchapp.data.models.VendorListing

data class VendorDashboardState(
  val vendor: Vendor? = null,
  val listings: List<VendorListing> = emptyList(),
  val orders: List<Order> = emptyList(),
  val selectedTab: VendorTab = VendorTab.OVERVIEW,
  val isLoading: Boolean = false,
  // Inventory management
  val showAddListingDialog: Boolean = false,
  val showEditListingDialog: Boolean = false,
  val editingListing: VendorListing? = null,
  val availableParts: List<Part> = emptyList(),
  val inventorySearchQuery: String = "",
  // Revenue stats
  val totalRevenue: Double = 0.0,
  val totalItemsSold: Int = 0,
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
  // Inventory management events
  data object ShowAddListing : VendorDashboardUIEvent()
  data object DismissAddListing : VendorDashboardUIEvent()
  data class AddNewListing(
    val partId: Int,
    val brandName: String,
    val partNumber: String,
    val price: Double,
    val stockQuantity: Int,
    val condition: String,
  ) : VendorDashboardUIEvent()
  data class ShowEditListing(val listing: VendorListing) : VendorDashboardUIEvent()
  data object DismissEditListing : VendorDashboardUIEvent()
  data class UpdateListing(
    val listingId: Int,
    val price: Double,
    val stockQuantity: Int,
    val inStock: Boolean,
  ) : VendorDashboardUIEvent()
  data class RemoveListing(val listingId: Int) : VendorDashboardUIEvent()
  data class InventorySearchChanged(val query: String) : VendorDashboardUIEvent()
}

sealed class VendorDashboardNavEvent {
  data object NavigateBack : VendorDashboardNavEvent()
}

sealed class VendorDashboardUIEffect

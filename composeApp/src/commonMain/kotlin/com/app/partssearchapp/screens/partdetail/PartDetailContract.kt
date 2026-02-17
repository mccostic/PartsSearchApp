package com.app.partssearchapp.screens.partdetail

import com.app.partssearchapp.data.models.Part
import com.app.partssearchapp.data.models.VendorListing

data class PartDetailState(
  val part: Part? = null,
  val listings: List<VendorListing> = emptyList(),
  val partName: String = "",
  val vehicleBreadcrumb: String = "",
  val categoryName: String = "",
  val isLoading: Boolean = false,
  val sortBy: SortOption = SortOption.PRICE_LOW,
)

enum class SortOption(val label: String) {
  PRICE_LOW("Price: Low to High"),
  PRICE_HIGH("Price: High to Low"),
  VENDOR_NAME("Vendor Name"),
  BRAND("Brand"),
}

sealed class PartDetailUIEvent {
  data class AddToCart(val listing: VendorListing) : PartDetailUIEvent()
  data class SortChanged(val sortOption: SortOption) : PartDetailUIEvent()
  data object BackPressed : PartDetailUIEvent()
  data object GoToCart : PartDetailUIEvent()
}

sealed class PartDetailNavEvent {
  data object NavigateBack : PartDetailNavEvent()
  data object NavigateToCart : PartDetailNavEvent()
}

sealed class PartDetailUIEffect

package com.app.partssearchapp.screens.partslisting

import com.app.partssearchapp.data.models.Part
import com.app.partssearchapp.data.models.VendorListing

data class PartsListingState(
  val parts: List<Part> = emptyList(),
  val listingsMap: Map<Int, List<VendorListing>> = emptyMap(),
  val expandedPartId: Int? = null,
  val categoryName: String = "",
  val vehicleBreadcrumb: String = "",
  val engineId: Int = 0,
  val isLoading: Boolean = false,
  val addedToCartPartId: Int? = null,
)

sealed class PartsListingUIEvent {
  data class PartClicked(val part: Part) : PartsListingUIEvent()
  data class AddToCart(val listing: VendorListing, val partName: String) : PartsListingUIEvent()
  data class ViewPartDetail(val partId: Int) : PartsListingUIEvent()
  data object BackPressed : PartsListingUIEvent()
  data object GoToCart : PartsListingUIEvent()
}

sealed class PartsListingNavEvent {
  data class NavigateToPartDetail(
    val partId: Int,
    val partName: String,
    val vehicleBreadcrumb: String,
    val categoryName: String,
  ) : PartsListingNavEvent()
  data object NavigateBack : PartsListingNavEvent()
  data object NavigateToCart : PartsListingNavEvent()
}

sealed class PartsListingUIEffect {
  data class ItemAddedToCart(val partName: String) : PartsListingUIEffect()
}

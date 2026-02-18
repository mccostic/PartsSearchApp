package com.app.partssearchapp.screens.home

import com.app.partssearchapp.data.models.Part
import com.app.partssearchapp.data.models.VehicleMake
import com.app.partssearchapp.screens.profile.ProfileParams

data class HomeState(
  val popularMakes: List<VehicleMake> = emptyList(),
  val searchQuery: String = "",
  val searchResults: List<Part> = emptyList(),
  val isSearching: Boolean = false,
  val isLoading: Boolean = false,
)

sealed class HomeUIEvent {
  data object SearchParts : HomeUIEvent()
  data class SearchQueryChanged(val query: String) : HomeUIEvent()
  data class MakeSelected(val make: VehicleMake) : HomeUIEvent()
  data object NavigateToVehicleSelection : HomeUIEvent()
  data object NavigateToCart : HomeUIEvent()
  data object NavigateToVendorDashboard : HomeUIEvent()
  data class SearchResultClicked(val part: Part) : HomeUIEvent()
}

sealed class HomeNavEvent {
  data class NavigateToVehicleSelection(
    val makeId: Int? = null,
    val makeName: String? = null,
  ) : HomeNavEvent()
  data object NavigateToCart : HomeNavEvent()
  data class NavigateToVendorDashboard(val vendorId: Int) : HomeNavEvent()
  data object NavigateToLogin : HomeNavEvent()
  data class NavigateToProfile(val params: ProfileParams) : HomeNavEvent()
}

sealed class HomeUIEffect

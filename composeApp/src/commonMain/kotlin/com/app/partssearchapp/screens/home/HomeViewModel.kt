package com.app.partssearchapp.screens.home

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.data.service.PartsDataService
import kotlinx.coroutines.flow.filterIsInstance

class HomeViewModel(
  params: HomeParams,
  private val partsDataService: PartsDataService,
) : BaseViewModel<HomeState, HomeUIEvent, HomeNavEvent, HomeUIEffect, HomeParams>(
  params = params,
  initialState = HomeState()
) {

  init {
    setupEventHandlers()
    launch { loadPopularMakes() }
  }

  private fun setupEventHandlers() {
    launch { searchQueryChangedHandler() }
    launch { searchPartsHandler() }
    launch { makeSelectedHandler() }
    launch { navigateToVehicleSelectionHandler() }
    launch { navigateToCartHandler() }
    launch { navigateToVendorDashboardHandler() }
    launch { searchResultClickedHandler() }
  }

  private suspend fun loadPopularMakes() {
    updateState { copy(isLoading = true) }
    try {
      val makes = partsDataService.getMakes()
      updateState { copy(popularMakes = makes, isLoading = false) }
    } catch (e: Exception) {
      updateState { copy(isLoading = false) }
      showErrorSnackbar("Failed to load data")
    }
  }

  private suspend fun searchQueryChangedHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.SearchQueryChanged>()
      .collect { event ->
        updateState { copy(searchQuery = event.query) }
        if (event.query.length >= 2) {
          updateState { copy(isSearching = true) }
          try {
            val results = partsDataService.searchParts(event.query)
            updateState { copy(searchResults = results, isSearching = false) }
          } catch (e: Exception) {
            updateState { copy(isSearching = false) }
          }
        } else {
          updateState { copy(searchResults = emptyList()) }
        }
      }
  }

  private suspend fun searchPartsHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.SearchParts>()
      .collect {
        val query = currentState.searchQuery
        if (query.isNotBlank()) {
          updateState { copy(isSearching = true) }
          try {
            val results = partsDataService.searchParts(query)
            updateState { copy(searchResults = results, isSearching = false) }
          } catch (e: Exception) {
            updateState { copy(isSearching = false) }
            showErrorSnackbar("Search failed")
          }
        }
      }
  }

  private suspend fun makeSelectedHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.MakeSelected>()
      .collect {
        emitNavEvent(HomeNavEvent.NavigateToVehicleSelection())
      }
  }

  private suspend fun navigateToVehicleSelectionHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.NavigateToVehicleSelection>()
      .collect {
        emitNavEvent(HomeNavEvent.NavigateToVehicleSelection())
      }
  }

  private suspend fun navigateToCartHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.NavigateToCart>()
      .collect {
        emitNavEvent(HomeNavEvent.NavigateToCart)
      }
  }

  private suspend fun navigateToVendorDashboardHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.NavigateToVendorDashboard>()
      .collect {
        // Navigate to first vendor for demo
        emitNavEvent(HomeNavEvent.NavigateToVendorDashboard(vendorId = 1))
      }
  }

  private suspend fun searchResultClickedHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.SearchResultClicked>()
      .collect {
        // For now, navigate to vehicle selection
        emitNavEvent(HomeNavEvent.NavigateToVehicleSelection())
      }
  }
}

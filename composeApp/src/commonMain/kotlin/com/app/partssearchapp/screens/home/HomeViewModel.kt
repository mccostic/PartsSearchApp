package com.app.partssearchapp.screens.home

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.data.service.InventoryManager
import com.app.partssearchapp.data.service.PartsDataService
import kotlinx.coroutines.flow.filterIsInstance

class HomeViewModel(
    params: HomeParams,
    private val partsDataService: PartsDataService,
    private val inventoryManager: InventoryManager,
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
        launch { clearSearchHandler() }
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
                        val results = inventoryManager.searchPartsWithListings(event.query)
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
                        val results = inventoryManager.searchPartsWithListings(query)
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
            .collect { event ->
                emitNavEvent(
                    HomeNavEvent.NavigateToVehicleSelection(
                        makeId = event.make.id,
                        makeName = event.make.name,
                    )
                )
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
                emitNavEvent(HomeNavEvent.NavigateToVendorDashboard(vendorId = 1))
            }
    }

    private suspend fun searchResultClickedHandler() {
        uiEvents
            .filterIsInstance<HomeUIEvent.SearchResultClicked>()
            .collect { event ->
                emitNavEvent(
                    HomeNavEvent.NavigateToPartDetail(
                        partId = event.part.id,
                        partName = event.part.name,
                    )
                )
            }
    }

    private suspend fun clearSearchHandler() {
        uiEvents
            .filterIsInstance<HomeUIEvent.ClearSearch>()
            .collect {
                updateState { copy(searchQuery = "", searchResults = emptyList()) }
            }
    }
}

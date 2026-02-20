package com.app.partssearchapp.screens.partslisting

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.data.service.CartManager
import com.app.partssearchapp.data.service.PartsDataService
import com.app.partssearchapp.global.GlobalSnackbarCenter
import kotlinx.coroutines.flow.filterIsInstance

class PartsListingViewModel(
    params: PartsListingParams,
    private val partsDataService: PartsDataService,
    private val cartManager: CartManager,
) : BaseViewModel<PartsListingState, PartsListingUIEvent, PartsListingNavEvent, PartsListingUIEffect, PartsListingParams>(
    params = params,
    initialState = PartsListingState(
        categoryName = params.categoryName,
        vehicleBreadcrumb = params.vehicleBreadcrumb,
        engineId = params.engineId,
    )
) {

    init {
        setupEventHandlers()
        launch { loadParts() }
    }

    private fun setupEventHandlers() {
        launch { partClickedHandler() }
        launch { addToCartHandler() }
        launch { viewPartDetailHandler() }
        launch { backPressedHandler() }
        launch { goToCartHandler() }
    }

    private suspend fun loadParts() {
        updateState { copy(isLoading = true) }
        try {
            val parts = partsDataService.getPartsForCategory(params.categoryId, params.engineId)
            val listingsMap = mutableMapOf<Int, List<com.app.partssearchapp.data.models.VendorListing>>()
            parts.forEach { part ->
                val listings = partsDataService.getListingsForPart(part.id)
                listingsMap[part.id] = listings
            }
            updateState { copy(parts = parts, listingsMap = listingsMap, isLoading = false) }
        } catch (e: Exception) {
            updateState { copy(isLoading = false) }
            showErrorSnackbar("Failed to load parts")
        }
    }

    private suspend fun partClickedHandler() {
        uiEvents
            .filterIsInstance<PartsListingUIEvent.PartClicked>()
            .collect { event ->
                updateState {
                    copy(
                        expandedPartId = if (expandedPartId == event.part.id) null else event.part.id
                    )
                }
            }
    }

    private suspend fun addToCartHandler() {
        uiEvents
            .filterIsInstance<PartsListingUIEvent.AddToCart>()
            .collect { event ->
                cartManager.addToCart(event.listing, event.partName)
                updateState { copy(addedToCartPartId = event.listing.partId) }
                GlobalSnackbarCenter.showSnackbar("${event.partName} added to cart")
            }
    }

    private suspend fun viewPartDetailHandler() {
        uiEvents
            .filterIsInstance<PartsListingUIEvent.ViewPartDetail>()
            .collect { event ->
                val part = currentState.parts.find { it.id == event.partId } ?: return@collect
                emitNavEvent(
                    PartsListingNavEvent.NavigateToPartDetail(
                        partId = part.id,
                        partName = part.name,
                        vehicleBreadcrumb = currentState.vehicleBreadcrumb,
                        categoryName = currentState.categoryName,
                    )
                )
            }
    }

    private suspend fun backPressedHandler() {
        uiEvents
            .filterIsInstance<PartsListingUIEvent.BackPressed>()
            .collect {
                emitNavEvent(PartsListingNavEvent.NavigateBack)
            }
    }

    private suspend fun goToCartHandler() {
        uiEvents
            .filterIsInstance<PartsListingUIEvent.GoToCart>()
            .collect {
                emitNavEvent(PartsListingNavEvent.NavigateToCart)
            }
    }
}

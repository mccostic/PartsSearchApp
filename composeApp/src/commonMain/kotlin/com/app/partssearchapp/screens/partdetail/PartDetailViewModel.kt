package com.app.partssearchapp.screens.partdetail

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.data.service.CartManager
import com.app.partssearchapp.data.service.PartsDataService
import com.app.partssearchapp.global.GlobalSnackbarCenter
import kotlinx.coroutines.flow.filterIsInstance

class PartDetailViewModel(
  params: PartDetailParams,
  private val partsDataService: PartsDataService,
  private val cartManager: CartManager,
) : BaseViewModel<PartDetailState, PartDetailUIEvent, PartDetailNavEvent, PartDetailUIEffect, PartDetailParams>(
  params = params,
  initialState = PartDetailState(
    partName = params.partName,
    vehicleBreadcrumb = params.vehicleBreadcrumb,
    categoryName = params.categoryName,
  )
) {

  init {
    setupEventHandlers()
    launch { loadPartDetail() }
  }

  private fun setupEventHandlers() {
    launch { addToCartHandler() }
    launch { sortChangedHandler() }
    launch { backPressedHandler() }
    launch { goToCartHandler() }
  }

  private suspend fun loadPartDetail() {
    updateState { copy(isLoading = true) }
    try {
      val listings = partsDataService.getListingsForPart(params.partId)
      updateState {
        copy(
          listings = sortListings(listings, sortBy),
          isLoading = false,
        )
      }
    } catch (e: Exception) {
      updateState { copy(isLoading = false) }
      showErrorSnackbar("Failed to load part details")
    }
  }

  private suspend fun addToCartHandler() {
    uiEvents
      .filterIsInstance<PartDetailUIEvent.AddToCart>()
      .collect { event ->
        cartManager.addToCart(event.listing, currentState.partName)
        GlobalSnackbarCenter.showSnackbar("${currentState.partName} added to cart")
      }
  }

  private suspend fun sortChangedHandler() {
    uiEvents
      .filterIsInstance<PartDetailUIEvent.SortChanged>()
      .collect { event ->
        updateState {
          copy(
            sortBy = event.sortOption,
            listings = sortListings(listings, event.sortOption),
          )
        }
      }
  }

  private suspend fun backPressedHandler() {
    uiEvents
      .filterIsInstance<PartDetailUIEvent.BackPressed>()
      .collect {
        emitNavEvent(PartDetailNavEvent.NavigateBack)
      }
  }

  private suspend fun goToCartHandler() {
    uiEvents
      .filterIsInstance<PartDetailUIEvent.GoToCart>()
      .collect {
        emitNavEvent(PartDetailNavEvent.NavigateToCart)
      }
  }

  private fun sortListings(listings: List<com.app.partssearchapp.data.models.VendorListing>, sortOption: SortOption): List<com.app.partssearchapp.data.models.VendorListing> {
    return when (sortOption) {
      SortOption.PRICE_LOW -> listings.sortedBy { it.price }
      SortOption.PRICE_HIGH -> listings.sortedByDescending { it.price }
      SortOption.VENDOR_NAME -> listings.sortedBy { it.vendorName }
      SortOption.BRAND -> listings.sortedBy { it.brandName }
    }
  }
}

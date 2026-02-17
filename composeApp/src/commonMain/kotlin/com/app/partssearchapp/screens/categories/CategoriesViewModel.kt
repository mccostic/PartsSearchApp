package com.app.partssearchapp.screens.categories

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.data.service.PartsDataService
import kotlinx.coroutines.flow.filterIsInstance

class CategoriesViewModel(
  params: CategoriesParams,
  private val partsDataService: PartsDataService,
) : BaseViewModel<CategoriesState, CategoriesUIEvent, CategoriesNavEvent, CategoriesUIEffect, CategoriesParams>(
  params = params,
  initialState = CategoriesState(
    vehicleBreadcrumb = params.vehicleBreadcrumb,
    engineId = params.engineId,
  )
) {

  init {
    setupEventHandlers()
    launch { loadCategories() }
  }

  private fun setupEventHandlers() {
    launch { categoryClickedHandler() }
    launch { subcategoryClickedHandler() }
    launch { toggleCategoryHandler() }
    launch { backPressedHandler() }
    launch { goToCartHandler() }
  }

  private suspend fun loadCategories() {
    updateState { copy(isLoading = true) }
    try {
      val categories = partsDataService.getCategoriesForEngine(params.engineId)
      updateState { copy(categories = categories, isLoading = false) }
    } catch (e: Exception) {
      updateState { copy(isLoading = false) }
      showErrorSnackbar("Failed to load categories")
    }
  }

  private suspend fun categoryClickedHandler() {
    uiEvents
      .filterIsInstance<CategoriesUIEvent.CategoryClicked>()
      .collect { event ->
        if (event.category.subcategories.isEmpty()) {
          emitNavEvent(
            CategoriesNavEvent.NavigateToPartsListing(
              categoryId = event.category.id,
              categoryName = event.category.name,
              engineId = currentState.engineId,
              vehicleBreadcrumb = currentState.vehicleBreadcrumb,
            )
          )
        } else {
          updateState {
            copy(
              expandedCategoryId = if (expandedCategoryId == event.category.id) null
                                   else event.category.id
            )
          }
        }
      }
  }

  private suspend fun subcategoryClickedHandler() {
    uiEvents
      .filterIsInstance<CategoriesUIEvent.SubcategoryClicked>()
      .collect { event ->
        emitNavEvent(
          CategoriesNavEvent.NavigateToPartsListing(
            categoryId = event.subcategory.id,
            categoryName = event.subcategory.name,
            engineId = currentState.engineId,
            vehicleBreadcrumb = currentState.vehicleBreadcrumb,
          )
        )
      }
  }

  private suspend fun toggleCategoryHandler() {
    uiEvents
      .filterIsInstance<CategoriesUIEvent.ToggleCategory>()
      .collect { event ->
        updateState {
          copy(
            expandedCategoryId = if (expandedCategoryId == event.categoryId) null
                                 else event.categoryId
          )
        }
      }
  }

  private suspend fun backPressedHandler() {
    uiEvents
      .filterIsInstance<CategoriesUIEvent.BackPressed>()
      .collect {
        emitNavEvent(CategoriesNavEvent.NavigateBack)
      }
  }

  private suspend fun goToCartHandler() {
    uiEvents
      .filterIsInstance<CategoriesUIEvent.GoToCart>()
      .collect {
        emitNavEvent(CategoriesNavEvent.NavigateToCart)
      }
  }
}

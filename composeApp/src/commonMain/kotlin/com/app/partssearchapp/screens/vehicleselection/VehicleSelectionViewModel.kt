package com.app.partssearchapp.screens.vehicleselection

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.data.service.PartsDataService
import kotlinx.coroutines.flow.filterIsInstance

class VehicleSelectionViewModel(
  params: VehicleSelectionParams,
  private val partsDataService: PartsDataService,
) : BaseViewModel<VehicleSelectionState, VehicleSelectionUIEvent, VehicleSelectionNavEvent, VehicleSelectionUIEffect, VehicleSelectionParams>(
  params = params,
  initialState = VehicleSelectionState()
) {

  init {
    setupEventHandlers()
    launch { loadMakes() }
  }

  private fun setupEventHandlers() {
    launch { makeSelectedHandler() }
    launch { yearSelectedHandler() }
    launch { modelSelectedHandler() }
    launch { engineSelectedHandler() }
    launch { backStepHandler() }
    launch { goHomeHandler() }
  }

  private suspend fun loadMakes() {
    updateState { copy(isLoading = true) }
    try {
      val makes = partsDataService.getMakes()
      updateState { copy(makes = makes, isLoading = false) }
    } catch (e: Exception) {
      updateState { copy(isLoading = false) }
      showErrorSnackbar("Failed to load vehicle makes")
    }
  }

  private suspend fun makeSelectedHandler() {
    uiEvents
      .filterIsInstance<VehicleSelectionUIEvent.MakeSelected>()
      .collect { event ->
        updateState { copy(isLoading = true) }
        try {
          val years = partsDataService.getYearsForMake(event.make.id)
          updateState {
            copy(
              selection = selection.copy(make = event.make, year = null, model = null, engine = null),
              years = years,
              models = emptyList(),
              engines = emptyList(),
              currentStep = SelectionStep.YEAR,
              isLoading = false,
            )
          }
        } catch (e: Exception) {
          updateState { copy(isLoading = false) }
          showErrorSnackbar("Failed to load years")
        }
      }
  }

  private suspend fun yearSelectedHandler() {
    uiEvents
      .filterIsInstance<VehicleSelectionUIEvent.YearSelected>()
      .collect { event ->
        val makeId = currentState.selection.make?.id ?: return@collect
        updateState { copy(isLoading = true) }
        try {
          val models = partsDataService.getModelsForMakeAndYear(makeId, event.year)
          updateState {
            copy(
              selection = selection.copy(year = event.year, model = null, engine = null),
              models = models,
              engines = emptyList(),
              currentStep = SelectionStep.MODEL,
              isLoading = false,
            )
          }
        } catch (e: Exception) {
          updateState { copy(isLoading = false) }
          showErrorSnackbar("Failed to load models")
        }
      }
  }

  private suspend fun modelSelectedHandler() {
    uiEvents
      .filterIsInstance<VehicleSelectionUIEvent.ModelSelected>()
      .collect { event ->
        updateState { copy(isLoading = true) }
        try {
          val engines = partsDataService.getEnginesForModel(event.model.id)
          updateState {
            copy(
              selection = selection.copy(model = event.model, engine = null),
              engines = engines,
              currentStep = SelectionStep.ENGINE,
              isLoading = false,
            )
          }
        } catch (e: Exception) {
          updateState { copy(isLoading = false) }
          showErrorSnackbar("Failed to load engines")
        }
      }
  }

  private suspend fun engineSelectedHandler() {
    uiEvents
      .filterIsInstance<VehicleSelectionUIEvent.EngineSelected>()
      .collect { event ->
        val updatedSelection = currentState.selection.copy(engine = event.engine)
        updateState { copy(selection = updatedSelection) }
        emitNavEvent(
          VehicleSelectionNavEvent.NavigateToCategories(
            CategoriesNavParams(
              engineId = event.engine.id,
              vehicleBreadcrumb = updatedSelection.breadcrumb,
            )
          )
        )
      }
  }

  private suspend fun backStepHandler() {
    uiEvents
      .filterIsInstance<VehicleSelectionUIEvent.BackStep>()
      .collect {
        when (currentState.currentStep) {
          SelectionStep.MAKE -> emitNavEvent(VehicleSelectionNavEvent.NavigateBack)
          SelectionStep.YEAR -> updateState {
            copy(
              selection = selection.copy(make = null),
              currentStep = SelectionStep.MAKE,
              years = emptyList(),
            )
          }
          SelectionStep.MODEL -> updateState {
            copy(
              selection = selection.copy(year = null),
              currentStep = SelectionStep.YEAR,
              models = emptyList(),
            )
          }
          SelectionStep.ENGINE -> updateState {
            copy(
              selection = selection.copy(model = null),
              currentStep = SelectionStep.MODEL,
              engines = emptyList(),
            )
          }
        }
      }
  }

  private suspend fun goHomeHandler() {
    uiEvents
      .filterIsInstance<VehicleSelectionUIEvent.GoHome>()
      .collect {
        emitNavEvent(VehicleSelectionNavEvent.NavigateBack)
      }
  }
}

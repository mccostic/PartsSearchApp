package com.app.partssearchapp.screens.vehicleselection

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.data.models.VehicleMake
import com.app.partssearchapp.data.models.VehicleSelection
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
    if (params.preselectedMakeId > 0) {
      launch { loadModelsForPreselectedMake() }
    } else {
      launch { loadMakes() }
    }
  }

  private fun setupEventHandlers() {
    launch { makeSelectedHandler() }
    launch { modelSelectedHandler() }
    launch { yearSelectedHandler() }
    launch { engineSelectedHandler() }
    launch { makeSearchChangedHandler() }
    launch { backStepHandler() }
    launch { goHomeHandler() }
  }

  private suspend fun loadModelsForPreselectedMake() {
    val make = VehicleMake(id = params.preselectedMakeId, name = params.preselectedMakeName)
    updateState { copy(isLoading = true) }
    try {
      val models = partsDataService.getModelsForMake(make.id)
      updateState {
        copy(
          selection = VehicleSelection(make = make),
          models = models,
          currentStep = SelectionStep.MODEL,
          isLoading = false,
        )
      }
    } catch (e: Exception) {
      updateState { copy(isLoading = false) }
      showErrorSnackbar("Failed to load models")
    }
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
        updateState { copy(isLoading = true, makeSearchQuery = "") }
        try {
          val models = partsDataService.getModelsForMake(event.make.id)
          updateState {
            copy(
              selection = selection.copy(make = event.make, model = null, year = null, engine = null),
              models = models,
              years = emptyList(),
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
        val makeId = currentState.selection.make?.id ?: return@collect
        updateState { copy(isLoading = true) }
        try {
          val years = partsDataService.getYearsForMake(makeId)
          updateState {
            copy(
              selection = selection.copy(model = event.model, year = null, engine = null),
              years = years,
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
        val modelId = currentState.selection.model?.id ?: return@collect
        updateState { copy(isLoading = true) }
        try {
          val engines = partsDataService.getEnginesForModel(makeId, event.year, modelId)
          updateState {
            copy(
              selection = selection.copy(year = event.year, engine = null),
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

  private suspend fun makeSearchChangedHandler() {
    uiEvents
      .filterIsInstance<VehicleSelectionUIEvent.MakeSearchChanged>()
      .collect { event ->
        updateState { copy(makeSearchQuery = event.query) }
      }
  }

  private suspend fun backStepHandler() {
    uiEvents
      .filterIsInstance<VehicleSelectionUIEvent.BackStep>()
      .collect {
        when (currentState.currentStep) {
          SelectionStep.MAKE -> emitNavEvent(VehicleSelectionNavEvent.NavigateBack)
          SelectionStep.MODEL -> updateState {
            copy(
              selection = selection.copy(make = null),
              currentStep = SelectionStep.MAKE,
              models = emptyList(),
              makeSearchQuery = "",
            )
          }
          SelectionStep.YEAR -> updateState {
            copy(
              selection = selection.copy(model = null),
              currentStep = SelectionStep.MODEL,
              years = emptyList(),
            )
          }
          SelectionStep.ENGINE -> updateState {
            copy(
              selection = selection.copy(year = null),
              currentStep = SelectionStep.YEAR,
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

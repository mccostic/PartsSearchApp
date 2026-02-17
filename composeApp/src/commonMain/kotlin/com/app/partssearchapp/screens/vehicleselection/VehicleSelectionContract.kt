package com.app.partssearchapp.screens.vehicleselection

import com.app.partssearchapp.data.models.*

data class VehicleSelectionState(
  val makes: List<VehicleMake> = emptyList(),
  val years: List<Int> = emptyList(),
  val models: List<VehicleModel> = emptyList(),
  val engines: List<VehicleEngine> = emptyList(),
  val selection: VehicleSelection = VehicleSelection(),
  val currentStep: SelectionStep = SelectionStep.MAKE,
  val isLoading: Boolean = false,
)

enum class SelectionStep {
  MAKE, YEAR, MODEL, ENGINE
}

sealed class VehicleSelectionUIEvent {
  data class MakeSelected(val make: VehicleMake) : VehicleSelectionUIEvent()
  data class YearSelected(val year: Int) : VehicleSelectionUIEvent()
  data class ModelSelected(val model: VehicleModel) : VehicleSelectionUIEvent()
  data class EngineSelected(val engine: VehicleEngine) : VehicleSelectionUIEvent()
  data object BackStep : VehicleSelectionUIEvent()
  data object GoHome : VehicleSelectionUIEvent()
}

sealed class VehicleSelectionNavEvent {
  data class NavigateToCategories(val params: CategoriesNavParams) : VehicleSelectionNavEvent()
  data object NavigateBack : VehicleSelectionNavEvent()
}

data class CategoriesNavParams(
  val engineId: Int,
  val vehicleBreadcrumb: String,
)

sealed class VehicleSelectionUIEffect

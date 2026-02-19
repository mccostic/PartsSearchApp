package com.app.partssearchapp.screens.vehicleselection

import com.app.partssearchapp.data.models.*

data class VehicleSelectionState(
  val makes: List<VehicleMake> = emptyList(),
  val models: List<VehicleModel> = emptyList(),
  val years: List<Int> = emptyList(),
  val engines: List<VehicleEngine> = emptyList(),
  val selection: VehicleSelection = VehicleSelection(),
  val currentStep: SelectionStep = SelectionStep.MAKE,
  val isLoading: Boolean = false,
  val makeSearchQuery: String = "",
  val modelSearchQuery: String = "",
  val makeScrollIndex: Int = 0,
  val makeScrollOffset: Int = 0,
  val modelScrollIndex: Int = 0,
  val modelScrollOffset: Int = 0,
)

enum class SelectionStep {
  MAKE, MODEL, YEAR, ENGINE
}

sealed class VehicleSelectionUIEvent {
  data class MakeSelected(val make: VehicleMake) : VehicleSelectionUIEvent()
  data class ModelSelected(val model: VehicleModel) : VehicleSelectionUIEvent()
  data class YearSelected(val year: Int) : VehicleSelectionUIEvent()
  data class EngineSelected(val engine: VehicleEngine) : VehicleSelectionUIEvent()
  data class MakeSearchChanged(val query: String) : VehicleSelectionUIEvent()
  data class ModelSearchChanged(val query: String) : VehicleSelectionUIEvent()
  data class MakeScrollChanged(val index: Int, val offset: Int) : VehicleSelectionUIEvent()
  data class ModelScrollChanged(val index: Int, val offset: Int) : VehicleSelectionUIEvent()
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

package com.app.partssearchapp.screens.vehicleselection

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.app.partssearchapp.arch.BaseRoute
import com.app.partssearchapp.screens.vehicleselection.compose.VehicleSelectionView
import kotlinx.serialization.Serializable

@Serializable
data class VehicleSelectionParams(
  val fromScreen: String = "home",
  val preselectedMakeId: Int = -1,
  val preselectedMakeName: String = "",
)

@Composable
fun VehicleSelectionRoute(backStackEntry: NavBackStackEntry) {
  BaseRoute<VehicleSelectionViewModel, VehicleSelectionState, VehicleSelectionUIEvent, VehicleSelectionNavEvent, VehicleSelectionUIEffect, VehicleSelectionParams>(
    backStackEntry = backStackEntry,
    router = { navEvents ->
      VehicleSelectionRouter(navEvents = navEvents)
    }
  ) { state, onEvent, uiEffects ->
    VehicleSelectionView(
      state = state,
      onEvent = onEvent,
      uiEffects = uiEffects,
    )
  }
}

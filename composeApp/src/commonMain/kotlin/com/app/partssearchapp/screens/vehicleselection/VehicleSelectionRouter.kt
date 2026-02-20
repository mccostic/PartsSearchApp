package com.app.partssearchapp.screens.vehicleselection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.app.partssearchapp.LocalNavController
import com.app.partssearchapp.screens.categories.CategoriesParams
import kotlinx.coroutines.flow.Flow

@Composable
fun VehicleSelectionRouter(
  navEvents: Flow<VehicleSelectionNavEvent>,
) {
  val navController = LocalNavController.current
  LaunchedEffect(Unit) {
    navEvents.collect {
      when (it) {
        is VehicleSelectionNavEvent.NavigateToCategories -> {
          navController.navigate(
            CategoriesParams(
              engineId = it.params.engineId,
              vehicleBreadcrumb = it.params.vehicleBreadcrumb,
            )
          )
        }
        is VehicleSelectionNavEvent.NavigateBack -> {
          navController.popBackStack()
        }
      }
    }
  }
}

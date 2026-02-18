package com.app.partssearchapp.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.app.partssearchapp.LocalNavController
import com.app.partssearchapp.screens.cart.CartParams
import com.app.partssearchapp.screens.login.LoginParams
import com.app.partssearchapp.screens.vehicleselection.VehicleSelectionParams
import com.app.partssearchapp.screens.vendor.VendorDashboardParams
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeRouter(
  navEvents: Flow<HomeNavEvent>,
) {
  val rootNavController = LocalNavController.current
  LaunchedEffect(Unit) {
    navEvents.collect {
      when (it) {
        is HomeNavEvent.NavigateToLogin -> {
          rootNavController.navigate(LoginParams(fromScreen = "home")) {
            popUpTo(0) { inclusive = true }
          }
        }
        is HomeNavEvent.NavigateToProfile -> {
          rootNavController.navigate(it.params)
        }
        is HomeNavEvent.NavigateToVehicleSelection -> {
          rootNavController.navigate(
            VehicleSelectionParams(
              preselectedMakeId = it.makeId ?: -1,
              preselectedMakeName = it.makeName ?: "",
            )
          )
        }
        is HomeNavEvent.NavigateToCart -> {
          rootNavController.navigate(CartParams())
        }
        is HomeNavEvent.NavigateToVendorDashboard -> {
          rootNavController.navigate(
            VendorDashboardParams(vendorId = it.vendorId)
          )
        }
      }
    }
  }
}

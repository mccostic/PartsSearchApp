package com.app.partssearchapp.screens.partslisting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.app.partssearchapp.LocalNavController
import com.app.partssearchapp.screens.partdetail.PartDetailParams
import com.app.partssearchapp.screens.cart.CartParams
import kotlinx.coroutines.flow.Flow

@Composable
fun PartsListingRouter(
  navEvents: Flow<PartsListingNavEvent>,
) {
  val navController = LocalNavController.current
  LaunchedEffect(Unit) {
    navEvents.collect {
      when (it) {
        is PartsListingNavEvent.NavigateToPartDetail -> {
          navController.navigate(
            PartDetailParams(
              partId = it.partId,
              partName = it.partName,
              vehicleBreadcrumb = it.vehicleBreadcrumb,
              categoryName = it.categoryName,
            )
          )
        }
        is PartsListingNavEvent.NavigateBack -> {
          navController.popBackStack()
        }
        is PartsListingNavEvent.NavigateToCart -> {
          navController.navigate(CartParams())
        }
      }
    }
  }
}

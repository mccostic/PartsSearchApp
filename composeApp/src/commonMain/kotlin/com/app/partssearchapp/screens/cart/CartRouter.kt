package com.app.partssearchapp.screens.cart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.app.partssearchapp.LocalNavController
import com.app.partssearchapp.screens.home.HomeParams
import kotlinx.coroutines.flow.Flow

@Composable
fun CartRouter(
  navEvents: Flow<CartNavEvent>,
) {
  val navController = LocalNavController.current
  LaunchedEffect(Unit) {
    navEvents.collect {
      when (it) {
        is CartNavEvent.NavigateBack -> {
          navController.popBackStack()
        }
        is CartNavEvent.NavigateToHome -> {
          navController.navigate(
            HomeParams(userEmail = "", userName = "", loginType = "")
          ) {
            popUpTo(0) { inclusive = true }
          }
        }
      }
    }
  }
}

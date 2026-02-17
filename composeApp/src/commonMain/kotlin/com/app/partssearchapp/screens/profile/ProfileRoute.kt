package com.app.partssearchapp.screens.profile

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.app.partssearchapp.arch.BaseRoute
import com.app.partssearchapp.screens.profile.compose.ProfileView
import com.app.partssearchapp.screens.profile.di.ProfileComponent
import com.punith.mediumarticle.screens.profile.di.create
import kotlinx.serialization.Serializable

/**
 * Navigation parameters for Profile screen
 */
@Serializable
data class ProfileParams(
  val userId: String,
  val userEmail: String,
  val userName: String = "",
  val fromScreen: String = "home",
  val listenerToken: String = "",
)

@Composable
fun ProfileRoute(backStackEntry: NavBackStackEntry) {
  BaseRoute<ProfileViewModel, ProfileState, ProfileUIEvent, ProfileNavEvent, ProfileUIEffect, ProfileParams>(
    backStackEntry = backStackEntry,
    componentProvider = { params, applicationComponent ->
      ProfileComponent::class.create(params, applicationComponent).profileViewModel
    },
    router = { navEvents ->
        ProfileRouter(navEvents = navEvents)
    }
  ) { state, onEvent, uiEffects ->
    ProfileView(
      state = state,
      onEvent = onEvent,
      uiEffects = uiEffects
    )
  }
}

package com.app.partssearchapp.screens.home

import com.app.partssearchapp.screens.profile.ProfileParams
import com.app.partssearchapp.screens.home.data.Repository

/**
 * Home screen state
 */
data class HomeState(
  val userInfo: UserInfo = UserInfo(),
  val uiState: HomeUiState = HomeUiState(),
  val processState: HomeProcessState = HomeProcessState(),
  val repositories: List<Repository> = emptyList(),
)

/**
 * User information from login
 */
data class UserInfo(
  val email: String = "",
  val name: String = "",
  val loginType: String = "",
  val loginTime: String = "",
)

/**
 * UI-specific state
 */
data class HomeUiState(
  val showWelcomeDialog: Boolean = false,
  val selectedTab: Int = 0,
)

/**
 * Process-related state
 */
data class HomeProcessState(
  val isLoading: Boolean = false,
  val refreshing: Boolean = false,
)

/**
 * UI Events
 */
sealed class HomeUIEvent {
  data object RefreshData : HomeUIEvent()
  data object ShowWelcomeDialog : HomeUIEvent()
  data object DismissWelcomeDialog : HomeUIEvent()
  data class TabSelected(val index: Int) : HomeUIEvent()
  data object LogoutClicked : HomeUIEvent()
  data object NavigateToProfile : HomeUIEvent()
  data object LoadRepositories : HomeUIEvent()
  data class RepositoryClicked(val repository: Repository) : HomeUIEvent()
}

/**
 * Navigation Events (Navigation only)
 */
sealed class HomeNavEvent {
  data object NavigateToLogin : HomeNavEvent()
  data class NavigateToProfile(val params: ProfileParams) : HomeNavEvent()
}

/**
 * Home-specific UI Effects
 */
sealed class HomeUIEffect {
  data class ShowWelcomeDialog(val userName: String) : HomeUIEffect()
}

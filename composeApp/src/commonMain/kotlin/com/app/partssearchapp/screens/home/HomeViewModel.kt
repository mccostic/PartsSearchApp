package com.app.partssearchapp.screens.home

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.global.GlobalPopupCenter
import com.app.partssearchapp.global.overlayPopup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import com.app.partssearchapp.global.GlobalSnackbarCenter
import com.app.partssearchapp.screens.home.data.RepositoryService
import com.app.partssearchapp.screens.profile.ProfileListener
import com.app.partssearchapp.screens.profile.ProfileParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ViewModel for Home screen that manages user info and home state
 * Implements ProfileScreenListener to handle events from Profile screen
 */
@Inject
class HomeViewModel(
    params: HomeParams,
    private val repositoryService: RepositoryService,
) : BaseViewModel<HomeState, HomeUIEvent, HomeNavEvent, HomeUIEffect, HomeParams>(
  params = params,
  initialState = HomeState()
) {

  init {
    setupEventHandlers()
    initializeUserInfo()
    launch { loadRepositories() }
  }

  private fun setupEventHandlers() {
    launch { refreshDataHandler() }
    launch { showWelcomeDialogHandler() }
    launch { dismissWelcomeDialogHandler() }
    launch { tabSelectedHandler() }
    launch { logoutHandler() }
    launch { navigateToProfileHandler() }
    launch { loadRepositoriesHandler() }
    launch { repositoryClickedHandler() }
  }

  @OptIn(ExperimentalTime::class)
  private fun initializeUserInfo() {
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val timeString = "${currentTime.hour}:${currentTime.minute.toString().padStart(2, '0')}"

    updateState {
      copy(
        userInfo = UserInfo(
            email = params.userEmail,
            name = params.userName.ifEmpty { params.userEmail.substringBefore("@") },
            loginType = params.loginType,
            loginTime = timeString
        ),
        uiState = uiState.copy(showWelcomeDialog = params.loginType == "sign_up")
      )
    }
  }

  private suspend fun refreshDataHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.RefreshData>()
      .collect {
        updateState {
          copy(processState = processState.copy(refreshing = true))
        }

        try {
          val repositories = repositoryService.refreshRepositories()
          updateState {
            copy(
              repositories = repositories,
              processState = processState.copy(refreshing = false)
            )
          }
          GlobalSnackbarCenter.showSnackbar("Data refreshed successfully!")
        } catch (e: Exception) {
          updateState {
            copy(processState = processState.copy(refreshing = false))
          }
          showErrorSnackbar("Failed to refresh data")
        }
      }
  }

  private suspend fun loadRepositories() {
    try {
      updateState {
        copy(processState = processState.copy(isLoading = true))
      }
      // Show global loading overlay
      GlobalPopupCenter.showNow(
        overlayPopup(
          scrimColor = Color(0x33000000),
          dedupKey = "home_loading_repos"
        ) {
          CircularProgressIndicator()
        }
      )

      val repositories = repositoryService.getRepositories()
      updateState {
        copy(
          repositories = repositories,
          processState = processState.copy(isLoading = false)
        )
      }
      GlobalPopupCenter.dismiss(dedupKey = "home_loading_repos")
    } catch (e: Exception) {
      updateState {
        copy(processState = processState.copy(isLoading = false))
      }
      GlobalPopupCenter.dismiss(dedupKey = "home_loading_repos")
      showErrorSnackbar("Failed to load repositories")
    }
  }

  private suspend fun loadRepositoriesHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.LoadRepositories>()
      .collect {
        loadRepositories()
      }
  }

  private suspend fun repositoryClickedHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.RepositoryClicked>()
      .collect { event ->
        reportGlobalError(
          "Something went wrong while opening ${event.repository.name}",
          Throwable("Something Went wrong")
        )
      }
  }

  private suspend fun showWelcomeDialogHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.ShowWelcomeDialog>()
      .collect {
        updateState {
          copy(uiState = uiState.copy(showWelcomeDialog = true))
        }
      }
  }

  private suspend fun dismissWelcomeDialogHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.DismissWelcomeDialog>()
      .collect {
        updateState {
          copy(uiState = uiState.copy(showWelcomeDialog = false))
        }
      }
  }

  private suspend fun tabSelectedHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.TabSelected>()
      .collect { event ->
        updateState {
          copy(uiState = uiState.copy(selectedTab = event.index))
        }
      }
  }

  private suspend fun logoutHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.LogoutClicked>()
      .collect {
        updateState {
          copy(processState = processState.copy(isLoading = true))
        }

        // Simulate logout delay
        delay(500)

        emitNavEvent(HomeNavEvent.NavigateToLogin)
      }
  }

  val profileListenerToken: String = registerListener(
    object : ProfileListener {
      override fun onNameUpdated(name: String) {
        onProfileUpdated(name)
      }

      override fun onLogout() {
        onLogoutClicked()
      }

      override fun onError(message: String) {
        onErrorClicked(message)
      }
    }
  )

  private suspend fun navigateToProfileHandler() {
    uiEvents
      .filterIsInstance<HomeUIEvent.NavigateToProfile>()
      .collect {
        val profileParams = ProfileParams(
          userId = currentState.userInfo.email,
          userEmail = currentState.userInfo.email,
          userName = currentState.userInfo.name,
          listenerToken = profileListenerToken,
        )
        emitNavEvent(HomeNavEvent.NavigateToProfile(profileParams))
      }
  }

  fun onProfileUpdated(name: String) {
    showSuccessSnackbar("Profile updated successfully!")
    updateState {
      copy(
        userInfo = userInfo.copy(
          name = name
        )
      )
    }
  }

  fun onLogoutClicked() {
    emitNavEvent(HomeNavEvent.NavigateToLogin)
  }

  fun onErrorClicked(error: String) {
    showErrorSnackbar("Error: $error")
  }
}

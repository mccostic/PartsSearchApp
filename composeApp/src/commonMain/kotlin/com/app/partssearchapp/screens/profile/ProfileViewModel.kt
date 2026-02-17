package com.app.partssearchapp.screens.profile

import com.app.partssearchapp.arch.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import me.tatarka.inject.annotations.Inject
import kotlin.time.Clock

/**
 * ViewModel for Profile screen that manages user profile and settings
 */
@Inject
class ProfileViewModel(
    params: ProfileParams,
) : BaseViewModel<ProfileState, ProfileUIEvent, ProfileNavEvent, ProfileUIEffect, ProfileParams>(
  params = params,
  initialState = ProfileState()
) {
  private val profileListener
    get() = getListener<ProfileListener>(params.listenerToken)

  init {
    setupEventHandlers()
    initializeProfile()
  }

  private fun setupEventHandlers() {
    launch { startEditingHandler() }
    launch { cancelEditingHandler() }
    launch { saveProfileHandler() }
    launch { updateNameHandler() }
    launch { updateBioHandler() }
    launch { tabSelectedHandler() }
    launch { showDeleteDialogHandler() }
    launch { dismissDeleteDialogHandler() }
    launch { deleteAccountHandler() }
    launch { refreshProfileHandler() }
    launch { backPressedHandler() }
    launch { logoutClickedHandler() }
    launch { triggerErrorHandler() }
  }

  @OptIn(ExperimentalTime::class)
  private fun initializeProfile() {
    val currentTime =
      Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val timeString = "${currentTime.hour}:${currentTime.minute.toString().padStart(2, '0')}"
    val dateString = "${currentTime.day}/${currentTime.month.number}/${currentTime.year}"

    updateState {
      copy(
        userProfile = UserProfile(
            id = params.userId,
            email = params.userEmail,
            name = params.userName.ifEmpty { params.userEmail.substringBefore("@") },
            bio = "Welcome to my profile! I'm exploring this amazing app.",
            profilePicture = "",
            joinDate = dateString,
            totalLogins = (15..100).random(),
            lastLoginTime = timeString
        )
      )
    }
  }

  private suspend fun startEditingHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.StartEditing>()
      .collect {
        updateState {
          copy(uiState = uiState.copy(isEditing = true))
        }
      }
  }

  private suspend fun cancelEditingHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.CancelEditing>()
      .collect {
        updateState {
          copy(uiState = uiState.copy(isEditing = false))
        }
        initializeProfile()
      }
  }

  private suspend fun saveProfileHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.SaveProfile>()
      .collect {
        updateState {
          copy(processState = processState.copy(isSaving = true))
        }

        // Simulate API call
        delay(1000)

        updateState {
          copy(
            uiState = uiState.copy(isEditing = false),
            processState = processState.copy(isSaving = false)
          )
        }

        showSuccessSnackbar("Profile saved successfully!")
        profileListener.onNameUpdated(currentState.userProfile.name)
        emitNavEvent(ProfileNavEvent.NavigateBack)
      }
  }

  private suspend fun updateNameHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.UpdateName>()
      .collect { event ->
        updateState {
          copy(
            userProfile = userProfile.copy(name = event.name)
          )
        }
      }
  }

  private suspend fun updateBioHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.UpdateBio>()
      .collect { event ->
        updateState {
          copy(
            userProfile = userProfile.copy(bio = event.bio)
          )
        }
      }
  }

  private suspend fun tabSelectedHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.TabSelected>()
      .collect { event ->
        updateState {
          copy(uiState = uiState.copy(selectedTab = event.tab))
        }
      }
  }

  private suspend fun showDeleteDialogHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.ShowDeleteDialog>()
      .collect {
        updateState {
          copy(uiState = uiState.copy(showDeleteDialog = true))
        }
      }
  }

  private suspend fun dismissDeleteDialogHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.DismissDeleteDialog>()
      .collect {
        updateState {
          copy(uiState = uiState.copy(showDeleteDialog = false))
        }
      }
  }

  private suspend fun deleteAccountHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.DeleteAccount>()
      .collect {
        updateState {
          copy(
            processState = processState.copy(isDeleting = true),
            uiState = uiState.copy(showDeleteDialog = false)
          )
        }
        delay(1500)
        profileListener.onNameUpdated(currentState.userProfile.email)
        emitNavEvent(ProfileNavEvent.NavigateToLogin)
      }
  }

  private suspend fun refreshProfileHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.RefreshProfile>()
      .collect {
        updateState {
          copy(processState = processState.copy(isLoading = true))
        }

        // Simulate data refresh
        delay(1000)

        updateState {
          copy(processState = processState.copy(isLoading = false))
        }

        showSuccessSnackbar("Profile refreshed!")
      }
  }

  private suspend fun backPressedHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.BackPressed>()
      .collect {
        if (currentState.uiState.isEditing) {
          // If editing, just cancel editing instead of navigating back
          updateState {
            copy(uiState = uiState.copy(isEditing = false))
          }
        } else {
          emitNavEvent(ProfileNavEvent.NavigateBack)
        }
      }
  }

  private suspend fun logoutClickedHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.LogoutClicked>()
      .collect {
        profileListener.onLogout()
        emitNavEvent(ProfileNavEvent.NavigateToLogin)
      }
  }

  private suspend fun triggerErrorHandler() {
    uiEvents
      .filterIsInstance<ProfileUIEvent.TriggerError>()
      .collect {
        profileListener.onError("Something went wrong in profile screen!")
        emitNavEvent(ProfileNavEvent.NavigateBack)
      }
  }
}

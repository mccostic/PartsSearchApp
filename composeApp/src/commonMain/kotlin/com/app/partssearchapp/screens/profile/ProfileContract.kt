package com.app.partssearchapp.screens.profile

/**
 * Profile screen state
 */
data class ProfileState(
    val userProfile: UserProfile = UserProfile(),
    val uiState: ProfileUiState = ProfileUiState(),
    val processState: ProfileProcessState = ProfileProcessState(),
)

/**
 * User profile information
 */
data class UserProfile(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val bio: String = "",
    val profilePicture: String = "",
    val joinDate: String = "",
    val totalLogins: Int = 0,
    val lastLoginTime: String = "",
)

/**
 * UI-specific state
 */
data class ProfileUiState(
    val isEditing: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedTab: ProfileTab = ProfileTab.SETTINGS,
)

/**
 * Process-related state
 */
data class ProfileProcessState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
)

enum class ProfileTab {
    SETTINGS,
    ACTIVITY,
    PREFERENCES
}

/**
 * UI Events
 */
sealed class ProfileUIEvent {
    data object StartEditing : ProfileUIEvent()
    data object CancelEditing : ProfileUIEvent()
    data object SaveProfile : ProfileUIEvent()
    data class UpdateName(val name: String) : ProfileUIEvent()
    data class UpdateBio(val bio: String) : ProfileUIEvent()
    data class TabSelected(val tab: ProfileTab) : ProfileUIEvent()
    data object ShowDeleteDialog : ProfileUIEvent()
    data object DismissDeleteDialog : ProfileUIEvent()
    data object DeleteAccount : ProfileUIEvent()
    data object RefreshProfile : ProfileUIEvent()
    data object BackPressed : ProfileUIEvent()
    data object LogoutClicked : ProfileUIEvent()
    data object TriggerError : ProfileUIEvent()
}

/**
 * Navigation Events (Navigation only)
 */
sealed class ProfileNavEvent {
    data object NavigateBack : ProfileNavEvent()
    data object NavigateToLogin : ProfileNavEvent()
}

/**
 * Profile-specific UI Effects
 */
sealed class ProfileUIEffect {
    data class ShowConfirmationDialog(val title: String, val message: String, val onConfirm: () -> Unit) : ProfileUIEffect()
}

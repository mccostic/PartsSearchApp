package com.app.partssearchapp.screens.login

import com.app.partssearchapp.screens.home.HomeParams

/**
 * UI State that represents LoginScreen with a structured approach
 * - AuthData: Contains actual authentication form fields
 * - UiState: UI-specific state like dialogs
 * - ProcessState: Process-related state like loading indicators
 **/
data class LoginState(
    val authData: AuthData = AuthData(),
    val uiState: UiState = UiState(),
    val processState: ProcessState = ProcessState(),
)

/**
 * Contains the actual authentication data fields
 * with validation functions
 */
data class AuthData(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSignInMode: Boolean = true,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
) {
    /**
     * Determines if the current form has valid data for submission
     */
    val isValid: Boolean
        get() = email.isNotEmpty() &&
            password.isNotEmpty() &&
            emailError == null &&
            passwordError == null &&
            (isSignInMode || (confirmPassword.isNotEmpty() && confirmPasswordError == null))

    /**
     * Creates a copy of the form for the opposite authentication mode
     */
    fun toggleMode(): AuthData = copy(
        isSignInMode = !isSignInMode,
        confirmPassword = "",
        confirmPasswordError = null
    )
}

/**
 * Contains UI-specific state
 */
data class UiState(val dialogState: DialogState = DialogState.NONE,)

/**
 * Contains process-related state like loading indicators
 */
data class ProcessState(
    val loginButtonState: ButtonState = ButtonState.DISABLED,
    val resetPasswordButtonState: ButtonState = ButtonState.DISABLED,
)

enum class ButtonState {
    ENABLED,
    DISABLED,
    LOADING
}

enum class DialogState {
    NONE,
    FORGOT_PASSWORD,
    RESET_EMAIL_SENT
}

sealed class LoginUIEvent {
    data class EmailChanged(val email: String) : LoginUIEvent()
    data class PasswordChanged(val password: String) : LoginUIEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : LoginUIEvent()
    object LoginClicked : LoginUIEvent()
    object ToggleMode : LoginUIEvent()
    object ForgotPasswordClicked : LoginUIEvent()
    object DismissDialog : LoginUIEvent()
    object ResetPasswordClicked : LoginUIEvent()
}

sealed class LoginNavEvent {
    data class NavigateToHome(val homeParams: HomeParams) : LoginNavEvent()
}

/**
 * Login-specific UI Effects
 */
sealed class LoginUIEffect {
    data class ShowSnackbar(val message: String) : LoginUIEffect()
    data class ShowErrorDialog(val title: String, val message: String) : LoginUIEffect()
}

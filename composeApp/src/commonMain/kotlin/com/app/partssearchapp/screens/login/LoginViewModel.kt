package com.app.partssearchapp.screens.login

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.screens.home.HomeParams
import com.app.partssearchapp.screens.login.usecases.AuthUseCase
import com.app.partssearchapp.utils.debounce300
import kotlinx.coroutines.flow.filterIsInstance
/**
 * ViewModel for Login/Registration screen that handles authentication
 * with improved state management and clean separation of concerns
 */
class LoginViewModel(params: LoginParams, private val authUseCase: AuthUseCase,) :
    BaseViewModel<LoginState, LoginUIEvent, LoginNavEvent, LoginUIEffect, LoginParams>(
        params = params,
        initialState = LoginState(
            authData = AuthData(
                email = params.prefillEmail
            )
        )
    ) {

    init {
        setupEventHandlers()
    }

    private fun setupEventHandlers() {
        // Form input handlers
        launch { emailChangeHandler() }
        launch { passwordChangeHandler() }
        launch { confirmPasswordChangeHandler() }

        // Action handlers
        launch { loginClickHandler() }
        launch { toggleModeHandler() }

        // Dialog-related handlers
        launch { forgotPasswordHandler() }
        launch { resetPasswordHandler() }
        launch { dismissDialogHandler() }
    }

    /**
     * Handles email input changes and validates the email format
     */
    private suspend fun emailChangeHandler() {
        uiEvents
            .filterIsInstance<LoginUIEvent.EmailChanged>()
            .collect { event ->
                val email = event.email
                val error = authUseCase.validateEmail(email)

                updateState {
                    val updatedAuthData = authData.copy(email = email, emailError = error)
                    copy(
                        authData = updatedAuthData,
                        processState = determineProcessState(updatedAuthData)
                    )
                }
            }
    }

    /**
     * Handles password input changes and validates the password format
     * Also updates confirm password validation if in signup mode
     */
    private suspend fun passwordChangeHandler() {
        uiEvents
            .filterIsInstance<LoginUIEvent.PasswordChanged>()
            .collect { event ->
                val password = event.password
                val error = authUseCase.validatePassword(password)

                // Also validate confirm password if in signup mode
                val confirmPasswordError = if (!currentState.authData.isSignInMode) {
                    authUseCase.validateConfirmPassword(password, currentState.authData.confirmPassword)
                } else {
                    null
                }

                updateState {
                    val updatedAuthData = authData.copy(
                        password = password,
                        passwordError = error,
                        confirmPasswordError = confirmPasswordError
                    )

                    copy(
                        authData = updatedAuthData,
                        processState = determineProcessState(updatedAuthData)
                    )
                }
            }
    }

    /**
     * Handles confirm password input changes and validates it matches with password
     */
    private suspend fun confirmPasswordChangeHandler() {
        uiEvents
            .filterIsInstance<LoginUIEvent.ConfirmPasswordChanged>()
            .collect { event ->
                val confirmPassword = event.confirmPassword
                val error =
                    authUseCase.validateConfirmPassword(currentState.authData.password, confirmPassword)

                updateState {
                    val updatedAuthData = authData.copy(
                        confirmPassword = confirmPassword,
                        confirmPasswordError = error
                    )

                    copy(
                        authData = updatedAuthData,
                        processState = determineProcessState(updatedAuthData)
                    )
                }
            }
    }

    /**
     * Handles login button click and processes authentication
     */
    private suspend fun loginClickHandler() {
        uiEvents
            .filterIsInstance<LoginUIEvent.LoginClicked>()
            .debounce300()
            .collect {
                updateState {
                    copy(
                        processState = processState.copy(loginButtonState = ButtonState.LOADING)
                    )
                }

                if (currentState.authData.isSignInMode) {
                    handleSignIn()
                } else {
                    handleSignUp()
                }
            }
    }

    /**
     * Handles toggling between sign-in and sign-up modes
     */
    private suspend fun toggleModeHandler() {
        uiEvents
            .filterIsInstance<LoginUIEvent.ToggleMode>()
            .collect {
                updateState {
                    val updatedAuthData = authData.toggleMode()
                    copy(
                        authData = updatedAuthData,
                        processState = determineProcessState(updatedAuthData)
                    )
                }
            }
    }

    /**
     * Handles forgot password button click
     */
    private suspend fun forgotPasswordHandler() {
        uiEvents
            .filterIsInstance<LoginUIEvent.ForgotPasswordClicked>()
            .collect {
                val email = currentState.authData.email
                val emailError = authUseCase.validateEmail(email)

                updateState {
                    val updatedAuthData = if (emailError != null) {
                        authData.copy(emailError = emailError)
                    } else {
                        authData
                    }

                    copy(
                        uiState = uiState.copy(dialogState = DialogState.FORGOT_PASSWORD),
                        authData = updatedAuthData,
                        processState = determineProcessState(updatedAuthData)
                    )
                }
            }
    }

    /**
     * Handles dismissing any active dialog
     */
    private suspend fun dismissDialogHandler() {
        uiEvents
            .filterIsInstance<LoginUIEvent.DismissDialog>()
            .collect {
                updateState {
                    copy(
                        uiState = uiState.copy(dialogState = DialogState.NONE)
                    )
                }
            }
    }

    /**
     * Handles password reset request
     */
    private suspend fun resetPasswordHandler() {
        uiEvents
            .filterIsInstance<LoginUIEvent.ResetPasswordClicked>()
            .collect {
                val email = currentState.authData.email

                updateState {
                    copy(
                        processState = processState.copy(resetPasswordButtonState = ButtonState.LOADING)
                    )
                }

                val result = authUseCase.resetPassword(email)
                result.fold(
                    onSuccess = {
                        updateState {
                            copy(
                                uiState = uiState.copy(dialogState = DialogState.RESET_EMAIL_SENT)
                            )
                        }
                    },
                    onFailure = { error ->
                        val errorMessage = authUseCase.getUserFriendlyAuthError(error as Exception)
                        emitUIEffect(LoginUIEffect.ShowSnackbar(errorMessage))
                        updateState {
                            copy(
                                processState = processState.copy(resetPasswordButtonState = ButtonState.ENABLED)
                            )
                        }
                    }
                )
            }
    }

    /**
     * Determines the current process state based on form validity
     */
    private fun determineProcessState(authData: AuthData): ProcessState {
        val loginButtonState = if (authData.isValid) {
            ButtonState.ENABLED
        } else {
            ButtonState.DISABLED
        }

        val resetPasswordButtonState = if (
            authData.email.isNotEmpty() &&
            authData.emailError == null
        ) {
            ButtonState.ENABLED
        } else {
            ButtonState.DISABLED
        }

        return ProcessState(
            loginButtonState = loginButtonState,
            resetPasswordButtonState = resetPasswordButtonState
        )
    }

    /**
     * Processes sign-in with the provided credentials
     */
    private fun handleSignIn() {
        launch {
            val result = authUseCase.signIn(
                email = currentState.authData.email,
                password = currentState.authData.password
            )

            result.fold(
                onSuccess = {
                    val state = currentState
                    val homeParams = HomeParams(
                        userEmail = state.authData.email,
                        userName = state.authData.email.substringBefore("@")
                            .replaceFirstChar { it.uppercase() },
                        loginType = "sign_in"
                    )
                    emitNavEvent(LoginNavEvent.NavigateToHome(homeParams))
                },
                onFailure = { error ->
                    val errorMessage = authUseCase.getUserFriendlyAuthError(error as Exception)
                    emitUIEffect(LoginUIEffect.ShowSnackbar(errorMessage))
                    updateState {
                        copy(
                            processState = processState.copy(loginButtonState = ButtonState.ENABLED)
                        )
                    }
                }
            )
        }
    }

    /**
     * Processes sign-up with the provided credentials
     */
    private fun handleSignUp() {
        launch {
            val result = authUseCase.signUp(
                email = currentState.authData.email,
                password = currentState.authData.password
            )

            result.fold(
                onSuccess = {
                    val state = currentState
                    val homeParams = HomeParams(
                        userEmail = state.authData.email,
                        userName = state.authData.email.substringBefore("@")
                            .replaceFirstChar { it.uppercase() },
                        loginType = "sign_up"
                    )
                    emitNavEvent(LoginNavEvent.NavigateToHome(homeParams))
                },
                onFailure = { error ->
                    val errorMessage = authUseCase.getUserFriendlyAuthError(error as Exception)
                    emitUIEffect(LoginUIEffect.ShowSnackbar(errorMessage))
                    updateState {
                        copy(
                            processState = processState.copy(loginButtonState = ButtonState.ENABLED)
                        )
                    }
                }
            )
        }
    }
}

package com.app.partssearchapp.screens.login.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.app.partssearchapp.screens.login.*
import com.app.partssearchapp.screens.login.ButtonState
import com.app.partssearchapp.screens.login.DialogState
import com.app.partssearchapp.screens.login.LoginState
import com.app.partssearchapp.screens.login.LoginUIEffect
import com.app.partssearchapp.screens.login.LoginUIEvent
import kotlinx.coroutines.flow.Flow

/**
 * Main login view that displays the authentication form
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(state: LoginState, onEvent: (LoginUIEvent) -> Unit, uiEffects: Flow<LoginUIEffect>,) {
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle UI Effects
    LaunchedEffect(Unit) {
        uiEffects.collect { effect ->
            when (effect) {
                is LoginUIEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                is LoginUIEffect.ShowErrorDialog -> {
                    // Handle error dialog if needed
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (state.authData.isSignInMode) "Sign In" else "Sign Up") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Email Field
                OutlinedTextField(
                    value = state.authData.email,
                    onValueChange = { onEvent(LoginUIEvent.EmailChanged(it)) },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    isError = state.authData.emailError != null,
                    supportingText = state.authData.emailError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                OutlinedTextField(
                    value = state.authData.password,
                    onValueChange = { onEvent(LoginUIEvent.PasswordChanged(it)) },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    isError = state.authData.passwordError != null,
                    supportingText = state.authData.passwordError?.let { { Text(it) } },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (state.authData.isSignInMode) ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        onDone = {
                            if (state.authData.isSignInMode &&
                                state.processState.loginButtonState == ButtonState.ENABLED
                            ) {
                                onEvent(
                                    LoginUIEvent.LoginClicked
                                )
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirm Password Field (only in sign up mode)
                if (!state.authData.isSignInMode) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.authData.confirmPassword,
                        onValueChange = { onEvent(LoginUIEvent.ConfirmPasswordChanged(it)) },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        isError = state.authData.confirmPasswordError != null,
                        supportingText = state.authData.confirmPasswordError?.let { { Text(it) } },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (state.processState.loginButtonState == ButtonState.ENABLED) {
                                    onEvent(
                                        LoginUIEvent.LoginClicked
                                    )
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login/Sign Up Button
                Button(
                    onClick = { onEvent(LoginUIEvent.LoginClicked) },
                    enabled = state.processState.loginButtonState == ButtonState.ENABLED,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.processState.loginButtonState == ButtonState.LOADING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (state.authData.isSignInMode) "Sign In" else "Sign Up")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Mode Button
                TextButton(
                    onClick = { onEvent(LoginUIEvent.ToggleMode) }
                ) {
                    Text(
                        if (state.authData.isSignInMode) {
                            "Don't have an account? Sign Up"
                        } else {
                            "Already have an account? Sign In"
                        }
                    )
                }

                // Forgot Password (only in sign in mode)
                if (state.authData.isSignInMode) {
                    TextButton(
                        onClick = { onEvent(LoginUIEvent.ForgotPasswordClicked) }
                    ) {
                        Text("Forgot Password?")
                    }
                }
            }
        }

        // Dialogs
        when (state.uiState.dialogState) {
            DialogState.FORGOT_PASSWORD -> {
                ForgotPasswordDialog(
                    email = state.authData.email,
                    onDismiss = { onEvent(LoginUIEvent.DismissDialog) },
                    onResetPassword = { onEvent(LoginUIEvent.ResetPasswordClicked) },
                    resetButtonState = state.processState.resetPasswordButtonState
                )
            }

            DialogState.RESET_EMAIL_SENT -> {
                AlertDialog(
                    onDismissRequest = { onEvent(LoginUIEvent.DismissDialog) },
                    title = { Text("Email Sent") },
                    text = { Text("Password reset email has been sent to ${state.authData.email}") },
                    confirmButton = {
                        TextButton(onClick = { onEvent(LoginUIEvent.DismissDialog) }) {
                            Text("OK")
                        }
                    }
                )
            }

            DialogState.NONE -> {
                /* No dialog */
            }
        }
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onDismiss: () -> Unit,
    onResetPassword: () -> Unit,
    resetButtonState: ButtonState
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Text("Send password reset email to: $email?")
        },
        confirmButton = {
            Button(
                onClick = onResetPassword,
                enabled = resetButtonState == ButtonState.ENABLED
            ) {
                if (resetButtonState == ButtonState.LOADING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

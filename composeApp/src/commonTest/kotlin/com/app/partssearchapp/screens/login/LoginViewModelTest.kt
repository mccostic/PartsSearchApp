package com.app.partssearchapp.screens.login

import com.app.partssearchapp.*
import com.app.partssearchapp.screens.login.usecases.AuthUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

  private lateinit var authUseCase: AuthUseCase

  @BeforeTest
  fun setup() {
    setupTestDispatchers()
    authUseCase = AuthUseCase()
  }

  @AfterTest
  fun tearDown() {
    tearDownTestDispatchers()
  }

  private fun createVm(prefillEmail: String = "") = LoginViewModel(
    params = LoginParams(prefillEmail = prefillEmail),
    authUseCase = authUseCase,
  )

  @Test
  fun initialStateHasPrefillEmail() = runTest {
    val vm = createVm(prefillEmail = "test@test.com")

    assertEquals("test@test.com", vm.stateFlow.value.authData.email)
    assertTrue(vm.stateFlow.value.authData.isSignInMode)
  }

  @Test
  fun emailChangedValidatesEmail() = runTest {
    val vm = createVm()

    vm.emitUIEvent(LoginUIEvent.EmailChanged("invalid"))
    awaitIdle()

    assertNotNull(vm.stateFlow.value.authData.emailError)

    vm.emitUIEvent(LoginUIEvent.EmailChanged("valid@email.com"))
    awaitIdle()

    assertNull(vm.stateFlow.value.authData.emailError)
  }

  @Test
  fun passwordChangedValidatesPassword() = runTest {
    val vm = createVm()

    vm.emitUIEvent(LoginUIEvent.PasswordChanged("12"))
    awaitIdle()

    assertNotNull(vm.stateFlow.value.authData.passwordError)

    vm.emitUIEvent(LoginUIEvent.PasswordChanged("validpass"))
    awaitIdle()

    assertNull(vm.stateFlow.value.authData.passwordError)
  }

  @Test
  fun confirmPasswordValidatesMatch() = runTest {
    val vm = createVm()

    // Switch to sign-up mode
    vm.emitUIEvent(LoginUIEvent.ToggleMode)
    awaitIdle()
    assertFalse(vm.stateFlow.value.authData.isSignInMode)

    vm.emitUIEvent(LoginUIEvent.PasswordChanged("password"))
    awaitIdle()
    vm.emitUIEvent(LoginUIEvent.ConfirmPasswordChanged("different"))
    awaitIdle()

    assertNotNull(vm.stateFlow.value.authData.confirmPasswordError)

    vm.emitUIEvent(LoginUIEvent.ConfirmPasswordChanged("password"))
    awaitIdle()

    assertNull(vm.stateFlow.value.authData.confirmPasswordError)
  }

  @Test
  fun toggleModeSwitchesBetweenSignInAndSignUp() = runTest {
    val vm = createVm()

    assertTrue(vm.stateFlow.value.authData.isSignInMode)

    vm.emitUIEvent(LoginUIEvent.ToggleMode)
    awaitIdle()

    assertFalse(vm.stateFlow.value.authData.isSignInMode)

    vm.emitUIEvent(LoginUIEvent.ToggleMode)
    awaitIdle()

    assertTrue(vm.stateFlow.value.authData.isSignInMode)
  }

  @Test
  fun forgotPasswordShowsDialog() = runTest {
    val vm = createVm()

    vm.emitUIEvent(LoginUIEvent.ForgotPasswordClicked)
    awaitIdle()

    assertEquals(DialogState.FORGOT_PASSWORD, vm.stateFlow.value.uiState.dialogState)
  }

  @Test
  fun dismissDialogHidesDialog() = runTest {
    val vm = createVm()

    vm.emitUIEvent(LoginUIEvent.ForgotPasswordClicked)
    awaitIdle()
    assertEquals(DialogState.FORGOT_PASSWORD, vm.stateFlow.value.uiState.dialogState)

    vm.emitUIEvent(LoginUIEvent.DismissDialog)
    awaitIdle()

    assertEquals(DialogState.NONE, vm.stateFlow.value.uiState.dialogState)
  }

  @Test
  fun validFormEnablesLoginButton() = runTest {
    val vm = createVm()

    assertEquals(ButtonState.DISABLED, vm.stateFlow.value.processState.loginButtonState)

    vm.emitUIEvent(LoginUIEvent.EmailChanged("test@test.com"))
    awaitIdle()
    vm.emitUIEvent(LoginUIEvent.PasswordChanged("password"))
    awaitIdle()

    assertEquals(ButtonState.ENABLED, vm.stateFlow.value.processState.loginButtonState)
  }

  @Test
  fun validEmailEnablesResetPasswordButton() = runTest {
    val vm = createVm()

    assertEquals(ButtonState.DISABLED, vm.stateFlow.value.processState.resetPasswordButtonState)

    vm.emitUIEvent(LoginUIEvent.EmailChanged("test@test.com"))
    awaitIdle()

    assertEquals(ButtonState.ENABLED, vm.stateFlow.value.processState.resetPasswordButtonState)
  }

  @Test
  fun loginClickedWithValidCredentialsNavigatesHome() = runTest {
    val vm = createVm()

    val (navEvents, job) = collectEvents(vm.navEvents)

    vm.emitUIEvent(LoginUIEvent.EmailChanged("test@test.com"))
    awaitIdle()
    vm.emitUIEvent(LoginUIEvent.PasswordChanged("password"))
    awaitIdle()
    vm.emitUIEvent(LoginUIEvent.LoginClicked)
    awaitIdle()

    assertTrue(navEvents.any { it is LoginNavEvent.NavigateToHome })
    job.cancel()
  }
}

package com.app.partssearchapp.screens.profile

import com.app.partssearchapp.*
import com.app.partssearchapp.arch.GlobalListenerRegistry
import kotlin.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private var lastUpdatedName: String? = null
    private var logoutCalled = false
    private var lastError: String? = null
    private lateinit var listenerToken: String

    @BeforeTest
    fun setup() {
        setupTestDispatchers()
        lastUpdatedName = null
        logoutCalled = false
        lastError = null
        listenerToken = GlobalListenerRegistry.register(
            object : ProfileListener {
                override fun onNameUpdated(name: String) {
                    lastUpdatedName = name
                }
                override fun onLogout() {
                    logoutCalled = true
                }
                override fun onError(message: String) {
                    lastError = message
                }
            },
        )
    }

    @AfterTest
    fun tearDown() {
        GlobalListenerRegistry.unregister(listenerToken)
        tearDownTestDispatchers()
    }

    private fun createVm() = ProfileViewModel(
        params = ProfileParams(
            userId = "user-1",
            userEmail = "test@test.com",
            userName = "Test User",
            listenerToken = listenerToken,
        ),
    )

    @Test
    fun initialStateInitializesProfile() = runTest {
        val vm = createVm()

        val state = vm.stateFlow.value
        assertEquals("user-1", state.userProfile.id)
        assertEquals("test@test.com", state.userProfile.email)
        assertEquals("Test User", state.userProfile.name)
        assertFalse(state.uiState.isEditing)
        assertFalse(state.processState.isLoading)
    }

    @Test
    fun startEditingSetsEditingMode() = runTest {
        val vm = createVm()

        vm.emitUIEvent(ProfileUIEvent.StartEditing)
        awaitIdle()

        assertTrue(vm.stateFlow.value.uiState.isEditing)
    }

    @Test
    fun cancelEditingExitsEditMode() = runTest {
        val vm = createVm()

        vm.emitUIEvent(ProfileUIEvent.StartEditing)
        awaitIdle()
        assertTrue(vm.stateFlow.value.uiState.isEditing)

        vm.emitUIEvent(ProfileUIEvent.CancelEditing)
        awaitIdle()

        assertFalse(vm.stateFlow.value.uiState.isEditing)
    }

    @Test
    fun updateNameUpdatesProfile() = runTest {
        val vm = createVm()

        vm.emitUIEvent(ProfileUIEvent.UpdateName("New Name"))
        awaitIdle()

        assertEquals("New Name", vm.stateFlow.value.userProfile.name)
    }

    @Test
    fun updateBioUpdatesProfile() = runTest {
        val vm = createVm()

        vm.emitUIEvent(ProfileUIEvent.UpdateBio("New Bio"))
        awaitIdle()

        assertEquals("New Bio", vm.stateFlow.value.userProfile.bio)
    }

    @Test
    fun tabSelectedChangesTab() = runTest {
        val vm = createVm()

        assertEquals(ProfileTab.SETTINGS, vm.stateFlow.value.uiState.selectedTab)

        vm.emitUIEvent(ProfileUIEvent.TabSelected(ProfileTab.ACTIVITY))
        awaitIdle()

        assertEquals(ProfileTab.ACTIVITY, vm.stateFlow.value.uiState.selectedTab)
    }

    @Test
    fun showDeleteDialogShowsDialog() = runTest {
        val vm = createVm()

        vm.emitUIEvent(ProfileUIEvent.ShowDeleteDialog)
        awaitIdle()

        assertTrue(vm.stateFlow.value.uiState.showDeleteDialog)
    }

    @Test
    fun dismissDeleteDialogHidesDialog() = runTest {
        val vm = createVm()

        vm.emitUIEvent(ProfileUIEvent.ShowDeleteDialog)
        awaitIdle()
        assertTrue(vm.stateFlow.value.uiState.showDeleteDialog)

        vm.emitUIEvent(ProfileUIEvent.DismissDeleteDialog)
        awaitIdle()

        assertFalse(vm.stateFlow.value.uiState.showDeleteDialog)
    }

    @Test
    fun backPressedDuringEditingCancelsEdit() = runTest {
        val vm = createVm()

        vm.emitUIEvent(ProfileUIEvent.StartEditing)
        awaitIdle()
        assertTrue(vm.stateFlow.value.uiState.isEditing)

        vm.emitUIEvent(ProfileUIEvent.BackPressed)
        awaitIdle()

        assertFalse(vm.stateFlow.value.uiState.isEditing)
    }

    @Test
    fun backPressedNavigatesBack() = runTest {
        val vm = createVm()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(ProfileUIEvent.BackPressed)
        awaitIdle()

        assertTrue(navEvents.any { it is ProfileNavEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun logoutNavigatesToLogin() = runTest {
        val vm = createVm()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(ProfileUIEvent.LogoutClicked)
        awaitIdle()

        assertTrue(logoutCalled)
        assertTrue(navEvents.any { it is ProfileNavEvent.NavigateToLogin })
        job.cancel()
    }

    @Test
    fun triggerErrorCallsListenerAndNavigatesBack() = runTest {
        val vm = createVm()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(ProfileUIEvent.TriggerError)
        awaitIdle()

        assertNotNull(lastError)
        assertTrue(navEvents.any { it is ProfileNavEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun saveProfileNotifiesListener() = runTest {
        val vm = createVm()

        vm.emitUIEvent(ProfileUIEvent.StartEditing)
        awaitIdle()
        vm.emitUIEvent(ProfileUIEvent.UpdateName("Updated Name"))
        awaitIdle()
        vm.emitUIEvent(ProfileUIEvent.SaveProfile)
        awaitIdle()

        assertEquals("Updated Name", lastUpdatedName)
        assertFalse(vm.stateFlow.value.uiState.isEditing)
    }
}

package com.app.partssearchapp.screens.home

import com.app.partssearchapp.*
import com.app.partssearchapp.data.models.Part
import com.app.partssearchapp.data.models.VehicleMake
import com.app.partssearchapp.data.service.InventoryManager
import com.app.partssearchapp.fakes.FakePartsDataService
import kotlin.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var fakeService: FakePartsDataService
    private lateinit var inventoryManager: InventoryManager

    @BeforeTest
    fun setup() {
        setupTestDispatchers()
        fakeService = FakePartsDataService()
        inventoryManager = InventoryManager()
    }

    @AfterTest
    fun tearDown() {
        tearDownTestDispatchers()
    }

    private fun createVm() = HomeViewModel(
        params = HomeParams(
            userEmail = "test@test.com",
            userName = "Test",
            loginType = "sign_in",
        ),
        partsDataService = fakeService,
        inventoryManager = inventoryManager,
    )

    @Test
    fun initialStateLoadsPopularMakes() = runTest {
        val vm = createVm()
        awaitIdle()

        val state = vm.stateFlow.value
        assertEquals(3, state.popularMakes.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun searchQueryChangedUpdatesState() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(HomeUIEvent.SearchQueryChanged("brake"))
        awaitIdle()

        assertEquals("brake", vm.stateFlow.value.searchQuery)
    }

    @Test
    fun makeSelectedEmitsNavEvent() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(HomeUIEvent.MakeSelected(VehicleMake(1, "Toyota")))
        awaitIdle()

        assertTrue(navEvents.any { it is HomeNavEvent.NavigateToVehicleSelection })
        val nav = navEvents.filterIsInstance<HomeNavEvent.NavigateToVehicleSelection>().first()
        assertEquals(1, nav.makeId)
        assertEquals("Toyota", nav.makeName)
        job.cancel()
    }

    @Test
    fun navigateToVehicleSelectionEmitsNavEvent() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(HomeUIEvent.NavigateToVehicleSelection)
        awaitIdle()

        assertTrue(navEvents.any { it is HomeNavEvent.NavigateToVehicleSelection })
        job.cancel()
    }

    @Test
    fun navigateToCartEmitsNavEvent() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(HomeUIEvent.NavigateToCart)
        awaitIdle()

        assertTrue(navEvents.any { it is HomeNavEvent.NavigateToCart })
        job.cancel()
    }

    @Test
    fun navigateToVendorDashboardEmitsNavEvent() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(HomeUIEvent.NavigateToVendorDashboard)
        awaitIdle()

        assertTrue(navEvents.any { it is HomeNavEvent.NavigateToVendorDashboard })
        job.cancel()
    }

    @Test
    fun searchResultClickedEmitsNavEvent() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        val part = Part(1, "Brake Pad", "BP-001", 11, "Desc", emptyList(), emptyList())
        vm.emitUIEvent(HomeUIEvent.SearchResultClicked(part))
        awaitIdle()

        assertTrue(navEvents.any { it is HomeNavEvent.NavigateToPartDetail })
        val nav = navEvents.filterIsInstance<HomeNavEvent.NavigateToPartDetail>().first()
        assertEquals(1, nav.partId)
        assertEquals("Brake Pad", nav.partName)
        job.cancel()
    }

    @Test
    fun clearSearchResetsSearchState() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(HomeUIEvent.SearchQueryChanged("brake"))
        awaitIdle()
        assertEquals("brake", vm.stateFlow.value.searchQuery)

        vm.emitUIEvent(HomeUIEvent.ClearSearch)
        awaitIdle()

        assertEquals("", vm.stateFlow.value.searchQuery)
        assertTrue(vm.stateFlow.value.searchResults.isEmpty())
    }

    @Test
    fun errorLoadingMakesSetsLoadingFalse() = runTest {
        fakeService.shouldThrow = true
        val vm = createVm()
        awaitIdle()

        assertFalse(vm.stateFlow.value.isLoading)
        assertTrue(vm.stateFlow.value.popularMakes.isEmpty())
    }
}

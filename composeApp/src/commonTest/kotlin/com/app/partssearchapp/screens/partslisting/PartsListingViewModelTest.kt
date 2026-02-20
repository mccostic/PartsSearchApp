package com.app.partssearchapp.screens.partslisting

import com.app.partssearchapp.*
import com.app.partssearchapp.data.models.VendorListing
import com.app.partssearchapp.data.service.CartManager
import com.app.partssearchapp.data.service.InventoryManager
import com.app.partssearchapp.fakes.FakePartsDataService
import kotlin.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class PartsListingViewModelTest {

    private lateinit var fakeService: FakePartsDataService
    private lateinit var cartManager: CartManager

    @BeforeTest
    fun setup() {
        setupTestDispatchers()
        fakeService = FakePartsDataService()
        cartManager = CartManager(InventoryManager())
    }

    @AfterTest
    fun tearDown() {
        tearDownTestDispatchers()
    }

    private fun createVm() = PartsListingViewModel(
        params = PartsListingParams(
            categoryId = 11,
            categoryName = "Brake Pad",
            engineId = 1,
            vehicleBreadcrumb = "Toyota > Corolla > 2024",
        ),
        partsDataService = fakeService,
        cartManager = cartManager,
    )

    @Test
    fun initialStateLoadsPartsAndListings() = runTest {
        val vm = createVm()
        awaitIdle()

        val state = vm.stateFlow.value
        assertEquals(1, state.parts.size)
        assertEquals("Brake Pad", state.categoryName)
        assertEquals("Toyota > Corolla > 2024", state.vehicleBreadcrumb)
        assertTrue(state.listingsMap.isNotEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun partClickedTogglesExpanded() = runTest {
        val vm = createVm()
        awaitIdle()

        val part = vm.stateFlow.value.parts.first()
        vm.emitUIEvent(PartsListingUIEvent.PartClicked(part))
        awaitIdle()

        assertEquals(part.id, vm.stateFlow.value.expandedPartId)

        // Click again to collapse
        vm.emitUIEvent(PartsListingUIEvent.PartClicked(part))
        awaitIdle()

        assertNull(vm.stateFlow.value.expandedPartId)
    }

    @Test
    fun addToCartAddsItem() = runTest {
        val vm = createVm()
        awaitIdle()

        val listing = VendorListing(1, 1, 1, "Vendor A", "BRAND", "PN-001", 100.0, "GHS", true, 10)
        vm.emitUIEvent(PartsListingUIEvent.AddToCart(listing, "Brake Pad"))
        awaitIdle()

        assertEquals(1, cartManager.itemCount)
        assertEquals(1, vm.stateFlow.value.addedToCartPartId)
    }

    @Test
    fun viewPartDetailEmitsNavEvent() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(PartsListingUIEvent.ViewPartDetail(1))
        awaitIdle()

        assertTrue(navEvents.any { it is PartsListingNavEvent.NavigateToPartDetail })
        val nav = navEvents.filterIsInstance<PartsListingNavEvent.NavigateToPartDetail>().first()
        assertEquals(1, nav.partId)
        assertEquals("Front Brake Pad", nav.partName)
        job.cancel()
    }

    @Test
    fun backPressedEmitsNavigateBack() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(PartsListingUIEvent.BackPressed)
        awaitIdle()

        assertTrue(navEvents.any { it is PartsListingNavEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun goToCartEmitsNavigateToCart() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(PartsListingUIEvent.GoToCart)
        awaitIdle()

        assertTrue(navEvents.any { it is PartsListingNavEvent.NavigateToCart })
        job.cancel()
    }

    @Test
    fun errorLoadingPartsSetsLoadingFalse() = runTest {
        fakeService.shouldThrow = true
        val vm = createVm()
        awaitIdle()

        assertFalse(vm.stateFlow.value.isLoading)
        assertTrue(vm.stateFlow.value.parts.isEmpty())
    }
}

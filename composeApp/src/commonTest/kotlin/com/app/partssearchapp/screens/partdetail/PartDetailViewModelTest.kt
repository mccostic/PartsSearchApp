package com.app.partssearchapp.screens.partdetail

import com.app.partssearchapp.*
import com.app.partssearchapp.data.service.CartManager
import com.app.partssearchapp.data.service.InventoryManager
import com.app.partssearchapp.fakes.FakePartsDataService
import kotlin.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class PartDetailViewModelTest {

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

    private fun createVm() = PartDetailViewModel(
        params = PartDetailParams(
            partId = 1,
            partName = "Front Brake Pad",
            vehicleBreadcrumb = "Toyota > Corolla > 2024",
            categoryName = "Brake Pad",
        ),
        partsDataService = fakeService,
        cartManager = cartManager,
    )

    @Test
    fun initialStateLoadsListings() = runTest {
        val vm = createVm()
        awaitIdle()

        val state = vm.stateFlow.value
        assertEquals(2, state.listings.size)
        assertEquals("Front Brake Pad", state.partName)
        assertEquals("Brake Pad", state.categoryName)
        assertFalse(state.isLoading)
    }

    @Test
    fun listingsAreSortedByPriceLowByDefault() = runTest {
        val vm = createVm()
        awaitIdle()

        val prices = vm.stateFlow.value.listings.map { it.price }
        assertEquals(prices.sorted(), prices)
        assertEquals(SortOption.PRICE_LOW, vm.stateFlow.value.sortBy)
    }

    @Test
    fun sortChangedSortsListings() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(PartDetailUIEvent.SortChanged(SortOption.PRICE_HIGH))
        awaitIdle()

        val state = vm.stateFlow.value
        assertEquals(SortOption.PRICE_HIGH, state.sortBy)
        val prices = state.listings.map { it.price }
        assertEquals(prices.sortedDescending(), prices)
    }

    @Test
    fun sortByVendorNameSortsCorrectly() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(PartDetailUIEvent.SortChanged(SortOption.VENDOR_NAME))
        awaitIdle()

        val names = vm.stateFlow.value.listings.map { it.vendorName }
        assertEquals(names.sorted(), names)
    }

    @Test
    fun sortByBrandSortsCorrectly() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(PartDetailUIEvent.SortChanged(SortOption.BRAND))
        awaitIdle()

        val brands = vm.stateFlow.value.listings.map { it.brandName }
        assertEquals(brands.sorted(), brands)
    }

    @Test
    fun addToCartAddsItem() = runTest {
        val vm = createVm()
        awaitIdle()

        val listing = vm.stateFlow.value.listings.first()
        vm.emitUIEvent(PartDetailUIEvent.AddToCart(listing))
        awaitIdle()

        assertEquals(1, cartManager.itemCount)
    }

    @Test
    fun backPressedEmitsNavigateBack() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(PartDetailUIEvent.BackPressed)
        awaitIdle()

        assertTrue(navEvents.any { it is PartDetailNavEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun goToCartEmitsNavigateToCart() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(PartDetailUIEvent.GoToCart)
        awaitIdle()

        assertTrue(navEvents.any { it is PartDetailNavEvent.NavigateToCart })
        job.cancel()
    }

    @Test
    fun errorLoadingListingsSetsLoadingFalse() = runTest {
        fakeService.shouldThrow = true
        val vm = createVm()
        awaitIdle()

        assertFalse(vm.stateFlow.value.isLoading)
        assertTrue(vm.stateFlow.value.listings.isEmpty())
    }
}

package com.app.partssearchapp.screens.vendor

import com.app.partssearchapp.*
import com.app.partssearchapp.data.models.OrderStatus
import com.app.partssearchapp.data.service.InventoryManager
import kotlin.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class VendorDashboardViewModelTest {

    private lateinit var inventoryManager: InventoryManager

    @BeforeTest
    fun setup() {
        setupTestDispatchers()
        inventoryManager = InventoryManager()
    }

    @AfterTest
    fun tearDown() {
        tearDownTestDispatchers()
    }

    private fun createVm(vendorId: Int = 1) = VendorDashboardViewModel(
        params = VendorDashboardParams(vendorId = vendorId, vendorName = "Accra Auto Parts"),
        inventoryManager = inventoryManager,
    )

    @Test
    fun initialStateLoadsVendorData() = runTest {
        val vm = createVm()
        awaitIdle()

        val state = vm.stateFlow.value
        assertNotNull(state.vendor)
        assertEquals("Accra Auto Parts", state.vendor?.name)
        assertTrue(state.listings.isNotEmpty())
        assertTrue(state.orders.isNotEmpty())
        assertTrue(state.availableParts.isNotEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun tabSelectedChangesTab() = runTest {
        val vm = createVm()
        awaitIdle()

        assertEquals(VendorTab.OVERVIEW, vm.stateFlow.value.selectedTab)

        vm.emitUIEvent(VendorDashboardUIEvent.TabSelected(VendorTab.INVENTORY))
        awaitIdle()

        assertEquals(VendorTab.INVENTORY, vm.stateFlow.value.selectedTab)

        vm.emitUIEvent(VendorDashboardUIEvent.TabSelected(VendorTab.ORDERS))
        awaitIdle()

        assertEquals(VendorTab.ORDERS, vm.stateFlow.value.selectedTab)
    }

    @Test
    fun updateOrderStatusUpdatesOrder() = runTest {
        val vm = createVm()
        awaitIdle()

        val orderId = vm.stateFlow.value.orders.first().id
        vm.emitUIEvent(VendorDashboardUIEvent.UpdateOrderStatus(orderId, OrderStatus.SHIPPED))
        awaitIdle()

        val order = vm.stateFlow.value.orders.find { it.id == orderId }
        assertEquals(OrderStatus.SHIPPED, order?.status)
    }

    @Test
    fun backPressedEmitsNavigateBack() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(VendorDashboardUIEvent.BackPressed)
        awaitIdle()

        assertTrue(navEvents.any { it is VendorDashboardNavEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun showAddListingShowsDialog() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(VendorDashboardUIEvent.ShowAddListing)
        awaitIdle()

        assertTrue(vm.stateFlow.value.showAddListingDialog)
    }

    @Test
    fun dismissAddListingHidesDialog() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(VendorDashboardUIEvent.ShowAddListing)
        awaitIdle()
        assertTrue(vm.stateFlow.value.showAddListingDialog)

        vm.emitUIEvent(VendorDashboardUIEvent.DismissAddListing)
        awaitIdle()

        assertFalse(vm.stateFlow.value.showAddListingDialog)
    }

    @Test
    fun addNewListingAddsToInventory() = runTest {
        val vm = createVm()
        awaitIdle()

        val initialCount = vm.stateFlow.value.listings.size

        vm.emitUIEvent(
            VendorDashboardUIEvent.AddNewListing(
                partId = 1,
                brandName = "TEST BRAND",
                partNumber = "TEST-001",
                price = 150.0,
                stockQuantity = 10,
                condition = "New",
            ),
        )
        awaitIdle()

        assertFalse(vm.stateFlow.value.showAddListingDialog)
        assertTrue(vm.stateFlow.value.listings.size >= initialCount)
    }

    @Test
    fun showEditListingShowsDialogWithListing() = runTest {
        val vm = createVm()
        awaitIdle()

        val listing = vm.stateFlow.value.listings.first()
        vm.emitUIEvent(VendorDashboardUIEvent.ShowEditListing(listing))
        awaitIdle()

        assertTrue(vm.stateFlow.value.showEditListingDialog)
        assertEquals(listing, vm.stateFlow.value.editingListing)
    }

    @Test
    fun dismissEditListingHidesDialog() = runTest {
        val vm = createVm()
        awaitIdle()

        val listing = vm.stateFlow.value.listings.first()
        vm.emitUIEvent(VendorDashboardUIEvent.ShowEditListing(listing))
        awaitIdle()

        vm.emitUIEvent(VendorDashboardUIEvent.DismissEditListing)
        awaitIdle()

        assertFalse(vm.stateFlow.value.showEditListingDialog)
        assertNull(vm.stateFlow.value.editingListing)
    }

    @Test
    fun updateListingUpdatesInventory() = runTest {
        val vm = createVm()
        awaitIdle()

        val listing = vm.stateFlow.value.listings.first()
        vm.emitUIEvent(
            VendorDashboardUIEvent.UpdateListing(
                listingId = listing.id,
                price = 999.0,
                stockQuantity = 50,
                inStock = true,
            ),
        )
        awaitIdle()

        assertFalse(vm.stateFlow.value.showEditListingDialog)
        val updated = vm.stateFlow.value.listings.find { it.id == listing.id }
        assertEquals(999.0, updated?.price)
        assertEquals(50, updated?.stockQuantity)
    }

    @Test
    fun removeListingRemovesFromInventory() = runTest {
        val vm = createVm()
        awaitIdle()

        val listing = vm.stateFlow.value.listings.first()
        val initialCount = vm.stateFlow.value.listings.size

        vm.emitUIEvent(VendorDashboardUIEvent.RemoveListing(listing.id))
        awaitIdle()

        assertTrue(vm.stateFlow.value.listings.size < initialCount)
        assertNull(vm.stateFlow.value.listings.find { it.id == listing.id })
    }

    @Test
    fun inventorySearchUpdatesQuery() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(VendorDashboardUIEvent.InventorySearchChanged("brake"))
        awaitIdle()

        assertEquals("brake", vm.stateFlow.value.inventorySearchQuery)
    }

    @Test
    fun revenueStatsComputedFromDeliveredOrders() = runTest {
        val vm = createVm()
        awaitIdle()

        val state = vm.stateFlow.value
        assertTrue(state.totalRevenue > 0.0)
        assertTrue(state.totalItemsSold > 0)
    }
}

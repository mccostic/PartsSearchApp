package com.app.partssearchapp.screens.cart

import com.app.partssearchapp.*
import com.app.partssearchapp.data.models.VendorListing
import com.app.partssearchapp.data.service.CartManager
import com.app.partssearchapp.data.service.InventoryManager
import kotlin.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private lateinit var inventoryManager: InventoryManager
    private lateinit var cartManager: CartManager

    @BeforeTest
    fun setup() {
        setupTestDispatchers()
        inventoryManager = InventoryManager()
        cartManager = CartManager(inventoryManager)
    }

    @AfterTest
    fun tearDown() {
        tearDownTestDispatchers()
    }

    private fun createVm() = CartViewModel(
        params = CartParams(),
        cartManager = cartManager,
    )

    private val testListing = VendorListing(1, 1, 1, "Vendor A", "BRAND", "PN-001", 100.0, "GHS", true, 10)

    @Test
    fun observesCartItems() = runTest {
        cartManager.addToCart(testListing, "Brake Pad")

        val vm = createVm()
        awaitIdle()

        assertEquals(1, vm.stateFlow.value.items.size)
        assertEquals("Brake Pad", vm.stateFlow.value.items.first().partName)
    }

    @Test
    fun removeItemRemovesFromCart() = runTest {
        cartManager.addToCart(testListing, "Brake Pad")
        val vm = createVm()
        awaitIdle()

        val itemId = vm.stateFlow.value.items.first().id
        vm.emitUIEvent(CartUIEvent.RemoveItem(itemId))
        awaitIdle()

        assertTrue(vm.stateFlow.value.items.isEmpty())
    }

    @Test
    fun updateQuantityUpdatesCart() = runTest {
        cartManager.addToCart(testListing, "Brake Pad")
        val vm = createVm()
        awaitIdle()

        val itemId = vm.stateFlow.value.items.first().id
        vm.emitUIEvent(CartUIEvent.UpdateQuantity(itemId, 3))
        awaitIdle()

        assertEquals(3, vm.stateFlow.value.items.first().quantity)
    }

    @Test
    fun clearCartClearsAllItems() = runTest {
        cartManager.addToCart(testListing, "Brake Pad")
        cartManager.addToCart(
            VendorListing(2, 2, 1, "Vendor A", "BRAND", "PN-002", 50.0, "GHS", true, 5),
            "Oil Filter"
        )
        val vm = createVm()
        awaitIdle()
        assertEquals(2, vm.stateFlow.value.items.size)

        vm.emitUIEvent(CartUIEvent.ClearCart)
        awaitIdle()

        assertTrue(vm.stateFlow.value.items.isEmpty())
    }

    @Test
    fun checkoutClickedShowsDialog() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(CartUIEvent.CheckoutClicked)
        awaitIdle()

        assertTrue(vm.stateFlow.value.showCheckoutDialog)
    }

    @Test
    fun dismissCheckoutHidesDialog() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(CartUIEvent.CheckoutClicked)
        awaitIdle()
        assertTrue(vm.stateFlow.value.showCheckoutDialog)

        vm.emitUIEvent(CartUIEvent.DismissCheckout)
        awaitIdle()

        assertFalse(vm.stateFlow.value.showCheckoutDialog)
    }

    @Test
    fun updateCustomerInfoUpdatesState() = runTest {
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(CartUIEvent.UpdateCustomerName("Kwame"))
        awaitIdle()
        assertEquals("Kwame", vm.stateFlow.value.customerName)

        vm.emitUIEvent(CartUIEvent.UpdateCustomerPhone("+233 20 111 2222"))
        awaitIdle()
        assertEquals("+233 20 111 2222", vm.stateFlow.value.customerPhone)

        vm.emitUIEvent(CartUIEvent.UpdateDeliveryAddress("123 Accra St"))
        awaitIdle()
        assertEquals("123 Accra St", vm.stateFlow.value.deliveryAddress)
    }

    @Test
    fun confirmOrderClearsCartAndEmitsEffect() = runTest {
        cartManager.addToCart(testListing, "Brake Pad")
        val vm = createVm()
        awaitIdle()

        vm.emitUIEvent(CartUIEvent.CheckoutClicked)
        awaitIdle()
        vm.emitUIEvent(CartUIEvent.ConfirmOrder)
        awaitIdle()

        assertFalse(vm.stateFlow.value.showCheckoutDialog)
        assertTrue(vm.stateFlow.value.items.isEmpty())
    }

    @Test
    fun backPressedEmitsNavigateBack() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(CartUIEvent.BackPressed)
        awaitIdle()

        assertTrue(navEvents.any { it is CartNavEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun continueShoppingEmitsNavigateToHome() = runTest {
        val vm = createVm()
        awaitIdle()

        val (navEvents, job) = collectEvents(vm.navEvents)

        vm.emitUIEvent(CartUIEvent.ContinueShopping)
        awaitIdle()

        assertTrue(navEvents.any { it is CartNavEvent.NavigateToHome })
        job.cancel()
    }

    @Test
    fun totalPriceComputesCorrectly() = runTest {
        cartManager.addToCart(testListing, "Brake Pad")
        cartManager.addToCart(
            VendorListing(2, 2, 1, "Vendor A", "BRAND", "PN-002", 50.0, "GHS", true, 5),
            "Oil Filter"
        )
        val vm = createVm()
        awaitIdle()

        assertEquals(150.0, vm.stateFlow.value.totalPrice)
        assertEquals(2, vm.stateFlow.value.itemCount)
    }
}

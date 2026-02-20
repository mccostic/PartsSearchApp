package com.app.partssearchapp.data.service

import com.app.partssearchapp.data.models.VendorListing
import kotlin.test.*

class CartManagerTest {

    private lateinit var inventoryManager: InventoryManager
    private lateinit var cartManager: CartManager

    private val listing1 =
        VendorListing(1, 1, 1, "Vendor A", "BRAND", "PN-001", 100.0, "GHS", true, 10)
    private val listing2 =
        VendorListing(2, 2, 1, "Vendor A", "BRAND", "PN-002", 50.0, "GHS", true, 5)

    @BeforeTest
    fun setup() {
        inventoryManager = InventoryManager()
        cartManager = CartManager(inventoryManager)
    }

    @Test
    fun addToCartAddsNewItem() {
        cartManager.addToCart(listing1, "Brake Pad")

        assertEquals(1, cartManager.cartItems.value.size)
        assertEquals("Brake Pad", cartManager.cartItems.value.first().partName)
        assertEquals(1, cartManager.cartItems.value.first().quantity)
    }

    @Test
    fun addToCartIncreasesQuantityForExistingItem() {
        cartManager.addToCart(listing1, "Brake Pad")
        cartManager.addToCart(listing1, "Brake Pad")

        assertEquals(1, cartManager.cartItems.value.size)
        assertEquals(2, cartManager.cartItems.value.first().quantity)
    }

    @Test
    fun addDifferentItemsAddsMultiple() {
        cartManager.addToCart(listing1, "Brake Pad")
        cartManager.addToCart(listing2, "Oil Filter")

        assertEquals(2, cartManager.cartItems.value.size)
    }

    @Test
    fun removeFromCartRemovesItem() {
        cartManager.addToCart(listing1, "Brake Pad")
        val itemId = cartManager.cartItems.value.first().id

        cartManager.removeFromCart(itemId)

        assertTrue(cartManager.cartItems.value.isEmpty())
    }

    @Test
    fun removeNonExistentItemDoesNothing() {
        cartManager.addToCart(listing1, "Brake Pad")

        cartManager.removeFromCart(999)

        assertEquals(1, cartManager.cartItems.value.size)
    }

    @Test
    fun updateQuantityUpdatesItem() {
        cartManager.addToCart(listing1, "Brake Pad")
        val itemId = cartManager.cartItems.value.first().id

        cartManager.updateQuantity(itemId, 5)

        assertEquals(5, cartManager.cartItems.value.first().quantity)
    }

    @Test
    fun updateQuantityToZeroRemovesItem() {
        cartManager.addToCart(listing1, "Brake Pad")
        val itemId = cartManager.cartItems.value.first().id

        cartManager.updateQuantity(itemId, 0)

        assertTrue(cartManager.cartItems.value.isEmpty())
    }

    @Test
    fun updateQuantityToNegativeRemovesItem() {
        cartManager.addToCart(listing1, "Brake Pad")
        val itemId = cartManager.cartItems.value.first().id

        cartManager.updateQuantity(itemId, -1)

        assertTrue(cartManager.cartItems.value.isEmpty())
    }

    @Test
    fun clearCartRemovesAllItems() {
        cartManager.addToCart(listing1, "Brake Pad")
        cartManager.addToCart(listing2, "Oil Filter")
        assertEquals(2, cartManager.cartItems.value.size)

        cartManager.clearCart()

        assertTrue(cartManager.cartItems.value.isEmpty())
    }

    @Test
    fun itemCountSumsQuantities() {
        cartManager.addToCart(listing1, "Brake Pad")
        cartManager.addToCart(listing1, "Brake Pad") // quantity 2
        cartManager.addToCart(listing2, "Oil Filter") // quantity 1

        assertEquals(3, cartManager.itemCount)
    }

    @Test
    fun totalPriceSumsCorrectly() {
        cartManager.addToCart(listing1, "Brake Pad") // 100.0
        cartManager.addToCart(listing2, "Oil Filter") // 50.0

        assertEquals(150.0, cartManager.totalPrice)
    }

    @Test
    fun totalPriceWithMultipleQuantities() {
        cartManager.addToCart(listing1, "Brake Pad")
        cartManager.addToCart(listing1, "Brake Pad") // 100 * 2 = 200
        cartManager.addToCart(listing2, "Oil Filter") // 50 * 1 = 50

        assertEquals(250.0, cartManager.totalPrice)
    }

    @Test
    fun checkoutClearsCartAndDeductsStock() {
        cartManager.addToCart(listing1, "Brake Pad")

        val initialStock = inventoryManager.listings.value.find { it.id == 1 }?.stockQuantity ?: 0

        cartManager.checkout()

        assertTrue(cartManager.cartItems.value.isEmpty())
        val updatedStock = inventoryManager.listings.value.find { it.id == 1 }?.stockQuantity ?: 0
        assertEquals(initialStock - 1, updatedStock)
    }
}

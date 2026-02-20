package com.app.partssearchapp.data.service

import com.app.partssearchapp.data.models.CartItem
import com.app.partssearchapp.data.models.VendorListing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartManager(private val inventoryManager: InventoryManager,) {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private var nextId = 1

    val itemCount: Int get() = _cartItems.value.sumOf { it.quantity }
    val totalPrice: Double get() = _cartItems.value.sumOf { it.totalPrice }

    fun addToCart(listing: VendorListing, partName: String) {
        _cartItems.update { items ->
            val existing = items.find { it.vendorListing.id == listing.id }
            if (existing != null) {
                items.map {
                    if (it.vendorListing.id == listing.id) {
                        it.copy(quantity = it.quantity + 1)
                    } else {
                        it
                    }
                }
            } else {
                items + CartItem(id = nextId++, vendorListing = listing, partName = partName)
            }
        }
    }

    fun removeFromCart(itemId: Int) {
        _cartItems.update { items -> items.filter { it.id != itemId } }
    }

    fun updateQuantity(itemId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(itemId)
            return
        }
        _cartItems.update { items ->
            items.map { if (it.id == itemId) it.copy(quantity = quantity) else it }
        }
    }

    fun checkout() {
        // Deduct stock from inventory for each cart item
        _cartItems.value.forEach { item ->
            inventoryManager.updateStockAfterPurchase(item.vendorListing.id, item.quantity)
        }
        clearCart()
    }

    fun clearCart() {
        _cartItems.update { emptyList() }
    }
}

package com.app.partssearchapp.screens.cart

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.data.service.CartManager
import com.app.partssearchapp.global.GlobalSnackbarCenter
import kotlinx.coroutines.flow.filterIsInstance

class CartViewModel(
  params: CartParams,
  private val cartManager: CartManager,
) : BaseViewModel<CartState, CartUIEvent, CartNavEvent, CartUIEffect, CartParams>(
  params = params,
  initialState = CartState()
) {

  init {
    setupEventHandlers()
    launch { observeCart() }
  }

  private fun setupEventHandlers() {
    launch { removeItemHandler() }
    launch { updateQuantityHandler() }
    launch { clearCartHandler() }
    launch { checkoutClickedHandler() }
    launch { dismissCheckoutHandler() }
    launch { updateCustomerNameHandler() }
    launch { updateCustomerPhoneHandler() }
    launch { updateDeliveryAddressHandler() }
    launch { confirmOrderHandler() }
    launch { backPressedHandler() }
    launch { continueShoppingHandler() }
  }

  private suspend fun observeCart() {
    cartManager.cartItems.collect { items ->
      updateState { copy(items = items) }
    }
  }

  private suspend fun removeItemHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.RemoveItem>()
      .collect { event ->
        cartManager.removeFromCart(event.itemId)
      }
  }

  private suspend fun updateQuantityHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.UpdateQuantity>()
      .collect { event ->
        cartManager.updateQuantity(event.itemId, event.quantity)
      }
  }

  private suspend fun clearCartHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.ClearCart>()
      .collect {
        cartManager.clearCart()
        GlobalSnackbarCenter.showSnackbar("Cart cleared")
      }
  }

  private suspend fun checkoutClickedHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.CheckoutClicked>()
      .collect {
        updateState { copy(showCheckoutDialog = true) }
      }
  }

  private suspend fun dismissCheckoutHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.DismissCheckout>()
      .collect {
        updateState { copy(showCheckoutDialog = false) }
      }
  }

  private suspend fun updateCustomerNameHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.UpdateCustomerName>()
      .collect { event ->
        updateState { copy(customerName = event.name) }
      }
  }

  private suspend fun updateCustomerPhoneHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.UpdateCustomerPhone>()
      .collect { event ->
        updateState { copy(customerPhone = event.phone) }
      }
  }

  private suspend fun updateDeliveryAddressHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.UpdateDeliveryAddress>()
      .collect { event ->
        updateState { copy(deliveryAddress = event.address) }
      }
  }

  private suspend fun confirmOrderHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.ConfirmOrder>()
      .collect {
        cartManager.checkout()
        updateState { copy(showCheckoutDialog = false) }
        showSuccessSnackbar("Order placed successfully! Vendors have been notified.")
        emitUIEffect(CartUIEffect.OrderPlaced)
      }
  }

  private suspend fun backPressedHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.BackPressed>()
      .collect {
        emitNavEvent(CartNavEvent.NavigateBack)
      }
  }

  private suspend fun continueShoppingHandler() {
    uiEvents
      .filterIsInstance<CartUIEvent.ContinueShopping>()
      .collect {
        emitNavEvent(CartNavEvent.NavigateToHome)
      }
  }
}

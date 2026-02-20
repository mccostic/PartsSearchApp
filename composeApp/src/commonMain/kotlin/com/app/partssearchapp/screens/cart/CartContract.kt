package com.app.partssearchapp.screens.cart

import com.app.partssearchapp.data.models.CartItem

data class CartState(
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val showCheckoutDialog: Boolean = false,
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
) {
    val totalPrice: Double get() = items.sumOf { it.totalPrice }
    val itemCount: Int get() = items.sumOf { it.quantity }
}

sealed class CartUIEvent {
    data class RemoveItem(val itemId: Int) : CartUIEvent()
    data class UpdateQuantity(val itemId: Int, val quantity: Int) : CartUIEvent()
    data object ClearCart : CartUIEvent()
    data object CheckoutClicked : CartUIEvent()
    data object DismissCheckout : CartUIEvent()
    data class UpdateCustomerName(val name: String) : CartUIEvent()
    data class UpdateCustomerPhone(val phone: String) : CartUIEvent()
    data class UpdateDeliveryAddress(val address: String) : CartUIEvent()
    data object ConfirmOrder : CartUIEvent()
    data object BackPressed : CartUIEvent()
    data object ContinueShopping : CartUIEvent()
}

sealed class CartNavEvent {
    data object NavigateBack : CartNavEvent()
    data object NavigateToHome : CartNavEvent()
}

sealed class CartUIEffect {
    data object OrderPlaced : CartUIEffect()
}

package com.app.partssearchapp.screens.cart.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.partssearchapp.data.models.CartItem
import com.app.partssearchapp.screens.cart.*
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartView(state: CartState, onEvent: (CartUIEvent) -> Unit, uiEffects: Flow<CartUIEffect>,) {
    LaunchedEffect(Unit) {
        uiEffects.collect { effect ->
            when (effect) {
                is CartUIEffect.OrderPlaced -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Cart (${state.itemCount})") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(CartUIEvent.BackPressed) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.items.isNotEmpty()) {
                        IconButton(onClick = { onEvent(CartUIEvent.ClearCart) }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Cart")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                Surface(
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "Total (${state.itemCount} items):",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                "GHS ${state.totalPrice.formatPrice()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onEvent(CartUIEvent.CheckoutClicked) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Proceed to Checkout")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Your cart is empty",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Search for parts to add to your cart",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(onClick = { onEvent(CartUIEvent.ContinueShopping) }) {
                        Text("Browse Parts")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onRemove = { onEvent(CartUIEvent.RemoveItem(item.id)) },
                        onQuantityChange = { qty -> onEvent(CartUIEvent.UpdateQuantity(item.id, qty)) },
                    )
                }
            }
        }
    }

    // Checkout dialog
    if (state.showCheckoutDialog) {
        CheckoutDialog(
            customerName = state.customerName,
            customerPhone = state.customerPhone,
            deliveryAddress = state.deliveryAddress,
            totalPrice = state.totalPrice,
            onNameChange = { onEvent(CartUIEvent.UpdateCustomerName(it)) },
            onPhoneChange = { onEvent(CartUIEvent.UpdateCustomerPhone(it)) },
            onAddressChange = { onEvent(CartUIEvent.UpdateDeliveryAddress(it)) },
            onConfirm = { onEvent(CartUIEvent.ConfirmOrder) },
            onDismiss = { onEvent(CartUIEvent.DismissCheckout) },
        )
    }
}

@Composable
private fun CartItemCard(item: CartItem, onRemove: () -> Unit, onQuantityChange: (Int) -> Unit,) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.partName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${item.vendorListing.brandName} - ${item.vendorListing.partNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = item.vendorListing.vendorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Quantity controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${item.vendorListing.currency} ${item.vendorListing.price.formatPrice()} each",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${item.vendorListing.currency} ${item.totalPrice.formatPrice()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckoutDialog(
    customerName: String,
    customerPhone: String,
    deliveryAddress: String,
    totalPrice: Double,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Checkout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Total: GHS ${totalPrice.formatPrice()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                OutlinedTextField(
                    value = customerName,
                    onValueChange = onNameChange,
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = onAddressChange,
                    label = { Text("Delivery Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = customerName.isNotBlank() && customerPhone.isNotBlank() && deliveryAddress.isNotBlank(),
            ) {
                Text("Place Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun Double.formatPrice(): String {
    val intPart = this.toLong()
    val decPart = ((this - intPart) * 100).toLong()
    return "$intPart.${decPart.toString().padStart(2, '0')}"
}

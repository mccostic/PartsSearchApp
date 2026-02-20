package com.app.partssearchapp.screens.vendor.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.app.partssearchapp.data.models.Order
import com.app.partssearchapp.data.models.OrderStatus
import com.app.partssearchapp.data.models.Part
import com.app.partssearchapp.data.models.VendorListing
import com.app.partssearchapp.screens.vendor.*
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardView(
    state: VendorDashboardState,
    onEvent: (VendorDashboardUIEvent) -> Unit,
    uiEffects: Flow<VendorDashboardUIEffect>,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.vendor?.name ?: "Vendor Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(VendorDashboardUIEvent.BackPressed) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = state.selectedTab.ordinal
            ) {
                VendorTab.entries.forEach { tab ->
                    Tab(
                        selected = tab == state.selectedTab,
                        onClick = {
                            onEvent(VendorDashboardUIEvent.TabSelected(tab))
                        },
                        text = { Text(tab.label) }
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (state.selectedTab) {
                    VendorTab.OVERVIEW -> OverviewTab(state)
                    VendorTab.INVENTORY -> InventoryTab(
                        listings = state.listings,
                        searchQuery = state.inventorySearchQuery,
                        onSearchChanged = { onEvent(VendorDashboardUIEvent.InventorySearchChanged(it)) },
                        onAddListing = { onEvent(VendorDashboardUIEvent.ShowAddListing) },
                        onEditListing = { onEvent(VendorDashboardUIEvent.ShowEditListing(it)) },
                        onRemoveListing = { onEvent(VendorDashboardUIEvent.RemoveListing(it)) },
                    )
                    VendorTab.ORDERS -> OrdersTab(
                        orders = state.orders,
                        onUpdateStatus = { orderId, status ->
                            onEvent(VendorDashboardUIEvent.UpdateOrderStatus(orderId, status))
                        },
                    )
                }
            }
        }

        // Add listing dialog
        if (state.showAddListingDialog) {
            AddListingDialog(
                availableParts = state.availableParts,
                onDismiss = { onEvent(VendorDashboardUIEvent.DismissAddListing) },
                onAdd = { partId, brandName, partNumber, price, stockQty, condition ->
                    onEvent(
                        VendorDashboardUIEvent.AddNewListing(
                            partId = partId,
                            brandName = brandName,
                            partNumber = partNumber,
                            price = price,
                            stockQuantity = stockQty,
                            condition = condition,
                        )
                    )
                },
            )
        }

        // Edit listing dialog
        if (state.showEditListingDialog && state.editingListing != null) {
            EditListingDialog(
                listing = state.editingListing,
                onDismiss = { onEvent(VendorDashboardUIEvent.DismissEditListing) },
                onSave = { listingId, price, stockQty, inStock ->
                    onEvent(
                        VendorDashboardUIEvent.UpdateListing(
                            listingId = listingId,
                            price = price,
                            stockQuantity = stockQty,
                            inStock = inStock,
                        )
                    )
                },
            )
        }
    }
}

@Composable
private fun OverviewTab(state: VendorDashboardState) {
    val vendor = state.vendor ?: return

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                vendor.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                vendor.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(vendor.phone, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (vendor.isVerified) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Verified Vendor",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard("Rating", "${vendor.rating}/5.0", Icons.Default.Star, Modifier.weight(1f))
                StatCard("Total Orders", vendor.totalOrders.toString(), Icons.Default.ShoppingBag, Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard("Listed Parts", state.listings.size.toString(), Icons.Default.Inventory, Modifier.weight(1f))
                StatCard(
                    "In Stock",
                    state.listings.count {
                        it.inStock
                    }.toString(),
                    Icons.Default.CheckCircle,
                    Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    "Revenue",
                    "GHS ${state.totalRevenue.formatPrice()}",
                    Icons.Default.AttachMoney,
                    Modifier.weight(1f)
                )
                StatCard(
                    "Pending",
                    state.orders.count {
                        it.status == OrderStatus.PENDING
                    }.toString(),
                    Icons.Default.PendingActions,
                    Modifier.weight(1f)
                )
            }
        }

        // Stock alerts
        val lowStock = state.listings.filter { it.inStock && it.stockQuantity <= 5 }
        if (lowStock.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Low Stock Alert",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        lowStock.forEach { listing ->
                            Text(
                                "${listing.brandName} ${listing.partNumber} - Only ${listing.stockQuantity} left",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            }
        }

        val outOfStock = state.listings.filter { !it.inStock }
        if (outOfStock.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.RemoveShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${outOfStock.size} item${if (outOfStock.size > 1) "s" else ""} out of stock",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InventoryTab(
    listings: List<VendorListing>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onAddListing: () -> Unit,
    onEditListing: (VendorListing) -> Unit,
    onRemoveListing: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search and add bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                placeholder = { Text("Search inventory...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(onClick = onAddListing) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        // Summary row
        val inStock = listings.count { it.inStock }
        val totalValue = listings.sumOf { it.price * it.stockQuantity }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "${listings.size} listings ($inStock in stock)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Value: GHS ${totalValue.formatPrice()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        val filteredListings = if (searchQuery.isBlank()) {
            listings
        } else {
            val q = searchQuery.lowercase()
            listings.filter {
                it.brandName.lowercase().contains(q) ||
                    it.partNumber.lowercase().contains(q)
            }
        }

        if (filteredListings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (searchQuery.isBlank()) "No inventory items yet" else "No matches found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (searchQuery.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap + Add to list your first part",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredListings) { listing ->
                    InventoryItemCard(
                        listing = listing,
                        onEdit = { onEditListing(listing) },
                        onRemove = { onRemoveListing(listing.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryItemCard(listing: VendorListing, onEdit: () -> Unit, onRemove: () -> Unit,) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        listing.brandName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        listing.partNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (listing.inStock) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (listing.inStock) {
                                if (listing.stockQuantity <= 5) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.tertiary
                                }
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (listing.inStock) {
                                if (listing.stockQuantity <= 5) {
                                    "Low Stock: ${listing.stockQuantity}"
                                } else {
                                    "In Stock: ${listing.stockQuantity}"
                                }
                            } else {
                                "Out of Stock"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (listing.inStock && listing.stockQuantity > 5) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                        )
                    }
                    Text(
                        "Condition: ${listing.condition}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${listing.currency} ${listing.price.formatPrice()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove Listing") },
            text = { Text("Remove ${listing.brandName} ${listing.partNumber} from your inventory?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onRemove()
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddListingDialog(
    availableParts: List<Part>,
    onDismiss: () -> Unit,
    onAdd: (
        partId: Int,
        brandName: String,
        partNumber: String,
        price: Double,
        stockQty: Int,
        condition: String
    ) -> Unit,
) {
    var selectedPart by remember { mutableStateOf<Part?>(null) }
    var brandName by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stockQty by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("New") }
    var partSearchQuery by remember { mutableStateOf("") }
    var showPartDropdown by remember { mutableStateOf(false) }

    val filteredParts = if (partSearchQuery.isBlank()) {
        availableParts.take(20)
    } else {
        val q = partSearchQuery.lowercase()
        availableParts.filter {
            it.name.lowercase().contains(q) || it.partNumber.lowercase().contains(q)
        }.take(20)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Listing") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Part selector
                ExposedDropdownMenuBox(
                    expanded = showPartDropdown,
                    onExpandedChange = { showPartDropdown = it },
                ) {
                    val fillMaxWidth = Modifier.fillMaxWidth()
                    OutlinedTextField(
                        value = selectedPart?.let { "${it.name} (${it.partNumber})" } ?: partSearchQuery,
                        onValueChange = {
                            partSearchQuery = it
                            selectedPart = null
                            showPartDropdown = true
                        },
                        label = { Text("Select Part") },
                        modifier = fillMaxWidth.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPartDropdown) },
                    )
                    ExposedDropdownMenu(
                        expanded = showPartDropdown,
                        onDismissRequest = { showPartDropdown = false },
                    ) {
                        filteredParts.forEach { part ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(part.name, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            part.partNumber,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedPart = part
                                    partSearchQuery = ""
                                    showPartDropdown = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    label = { Text("Brand Name") },
                    placeholder = { Text("e.g. BOSCH, DENSO, OEM...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = partNumber,
                    onValueChange = { partNumber = it },
                    label = { Text("Your Part Number") },
                    placeholder = { Text("e.g. BP-3547") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (GHS)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = stockQty,
                        onValueChange = { stockQty = it },
                        label = { Text("Stock Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }

                // Condition selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Condition:", style = MaterialTheme.typography.bodyMedium)
                    listOf("New", "Refurbished", "Used").forEach { c ->
                        FilterChip(
                            selected = condition == c,
                            onClick = { condition = c },
                            label = { Text(c) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: return@Button
                    val qty = stockQty.toIntOrNull() ?: return@Button
                    val part = selectedPart ?: return@Button
                    onAdd(part.id, brandName, partNumber, p, qty, condition)
                },
                enabled = selectedPart != null &&
                    brandName.isNotBlank() &&
                    partNumber.isNotBlank() &&
                    price.toDoubleOrNull() != null &&
                    stockQty.toIntOrNull() != null,
            ) {
                Text("Add Listing")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun EditListingDialog(
    listing: VendorListing,
    onDismiss: () -> Unit,
    onSave: (listingId: Int, price: Double, stockQty: Int, inStock: Boolean) -> Unit,
) {
    var price by remember { mutableStateOf(listing.price.formatPrice()) }
    var stockQty by remember { mutableStateOf(listing.stockQuantity.toString()) }
    var inStock by remember { mutableStateOf(listing.inStock) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Listing") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(listing.brandName, fontWeight = FontWeight.Bold)
                        Text(
                            listing.partNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (GHS)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = stockQty,
                    onValueChange = {
                        stockQty = it
                        val qty = it.toIntOrNull() ?: 0
                        inStock = qty > 0
                    },
                    label = { Text("Stock Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = inStock, onCheckedChange = { inStock = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (inStock) "In Stock" else "Out of Stock",
                        color = if (inStock) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: return@Button
                    val qty = stockQty.toIntOrNull() ?: return@Button
                    onSave(listing.id, p, qty, inStock)
                },
                enabled = price.toDoubleOrNull() != null && stockQty.toIntOrNull() != null,
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun OrdersTab(orders: List<Order>, onUpdateStatus: (Int, OrderStatus) -> Unit,) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No orders yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(orders) { order ->
                OrderCard(
                    order = order,
                    onUpdateStatus = { status -> onUpdateStatus(order.id, status) },
                )
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onUpdateStatus: (OrderStatus) -> Unit,) {
    val statusColor = when (order.status) {
        OrderStatus.PENDING -> MaterialTheme.colorScheme.tertiary
        OrderStatus.CONFIRMED -> MaterialTheme.colorScheme.primary
        OrderStatus.PROCESSING -> MaterialTheme.colorScheme.secondary
        OrderStatus.SHIPPED -> MaterialTheme.colorScheme.primary
        OrderStatus.DELIVERED -> MaterialTheme.colorScheme.tertiary
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

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
            ) {
                Text(
                    "Order #${order.id}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = statusColor.copy(alpha = 0.1f),
                ) {
                    Text(
                        order.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Customer: ${order.customerName}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Phone: ${order.customerPhone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Address: ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))
            order.items.forEach { item ->
                Text(
                    "${item.quantity}x ${item.partName} - ${item.vendorListing.currency} ${item.totalPrice.formatPrice()}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Total: GHS ${order.totalAmount.formatPrice()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val nextStatus = when (order.status) {
                        OrderStatus.PENDING -> OrderStatus.CONFIRMED
                        OrderStatus.CONFIRMED -> OrderStatus.PROCESSING
                        OrderStatus.PROCESSING -> OrderStatus.SHIPPED
                        OrderStatus.SHIPPED -> OrderStatus.DELIVERED
                    }
                    nextStatus.let { status ->
                        Button(onClick = { onUpdateStatus(status) }, modifier = Modifier.weight(1f)) {
                            Text("Mark as ${status.name}")
                        }
                    }
                    OutlinedButton(onClick = { onUpdateStatus(OrderStatus.CANCELLED) }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

private fun Double.formatPrice(): String {
    val intPart = this.toLong()
    val decPart = ((this - intPart) * 100).toLong()
    return "$intPart.${decPart.toString().padStart(2, '0')}"
}

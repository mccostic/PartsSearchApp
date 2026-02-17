package com.app.partssearchapp.screens.vendor.compose

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
import com.app.partssearchapp.data.models.Order
import com.app.partssearchapp.data.models.OrderStatus
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
      // Tabs
      TabRow(
        selectedTabIndex = state.selectedTab.ordinal,
      ) {
        VendorTab.entries.forEach { tab ->
          Tab(
            selected = state.selectedTab == tab,
            onClick = { onEvent(VendorDashboardUIEvent.TabSelected(tab)) },
            text = { Text(tab.label) },
          )
        }
      }

      if (state.isLoading) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      } else {
        when (state.selectedTab) {
          VendorTab.OVERVIEW -> OverviewTab(state)
          VendorTab.INVENTORY -> InventoryTab(state.listings)
          VendorTab.ORDERS -> OrdersTab(
            orders = state.orders,
            onUpdateStatus = { orderId, status ->
              onEvent(VendorDashboardUIEvent.UpdateOrderStatus(orderId, status))
            },
          )
        }
      }
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
      // Vendor info card
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
              Text(
                vendor.phone,
                style = MaterialTheme.typography.bodySmall,
              )
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

    // Stats cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        StatCard(
          title = "Rating",
          value = "${vendor.rating}/5.0",
          icon = Icons.Default.Star,
          modifier = Modifier.weight(1f),
        )
        StatCard(
          title = "Total Orders",
          value = vendor.totalOrders.toString(),
          icon = Icons.Default.ShoppingBag,
          modifier = Modifier.weight(1f),
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        StatCard(
          title = "Listed Parts",
          value = state.listings.size.toString(),
          icon = Icons.Default.Inventory,
          modifier = Modifier.weight(1f),
        )
        StatCard(
          title = "Pending Orders",
          value = state.orders.count { it.status == OrderStatus.PENDING }.toString(),
          icon = Icons.Default.PendingActions,
          modifier = Modifier.weight(1f),
        )
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
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun InventoryTab(listings: List<VendorListing>) {
  if (listings.isEmpty()) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      Text("No inventory items", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  } else {
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(listings) { listing ->
        Card(
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  if (listing.inStock) Icons.Default.CheckCircle else Icons.Default.Cancel,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = if (listing.inStock) MaterialTheme.colorScheme.tertiary
                         else MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  "Stock: ${listing.stockQuantity}",
                  style = MaterialTheme.typography.bodySmall,
                )
              }
            }
            Text(
              "${listing.currency} ${listing.price.formatPrice()}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun OrdersTab(
  orders: List<Order>,
  onUpdateStatus: (Int, OrderStatus) -> Unit,
) {
  if (orders.isEmpty()) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
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
private fun OrderCard(
  order: Order,
  onUpdateStatus: (OrderStatus) -> Unit,
) {
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

      Text(
        "Customer: ${order.customerName}",
        style = MaterialTheme.typography.bodyMedium,
      )
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

      // Action buttons based on status
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
            else -> null
          }
          nextStatus?.let { status ->
            Button(
              onClick = { onUpdateStatus(status) },
              modifier = Modifier.weight(1f),
            ) {
              Text("Mark as ${status.name}")
            }
          }
          OutlinedButton(
            onClick = { onUpdateStatus(OrderStatus.CANCELLED) },
          ) {
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

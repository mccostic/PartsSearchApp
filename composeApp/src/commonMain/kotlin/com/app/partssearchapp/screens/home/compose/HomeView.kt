package com.app.partssearchapp.screens.home.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.partssearchapp.data.models.VehicleMake
import com.app.partssearchapp.data.service.PartWithListings
import com.app.partssearchapp.screens.home.HomeState
import com.app.partssearchapp.screens.home.HomeUIEffect
import com.app.partssearchapp.screens.home.HomeUIEvent
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
  state: HomeState,
  onEvent: (HomeUIEvent) -> Unit,
  uiEffects: Flow<HomeUIEffect>,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("PartsSearch GH")
        },
        actions = {
          IconButton(onClick = { onEvent(HomeUIEvent.NavigateToCart) }) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
          }
          IconButton(onClick = { onEvent(HomeUIEvent.NavigateToVendorDashboard) }) {
            Icon(Icons.Default.Store, contentDescription = "Vendor Dashboard")
          }
        }
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(bottom = 16.dp),
    ) {
      // Hero section with search
      item {
        HeroSection(
          searchQuery = state.searchQuery,
          isSearching = state.isSearching,
          onSearchQueryChanged = { onEvent(HomeUIEvent.SearchQueryChanged(it)) },
          onSearch = { onEvent(HomeUIEvent.SearchParts) },
          onSelectVehicle = { onEvent(HomeUIEvent.NavigateToVehicleSelection) },
          onClearSearch = { onEvent(HomeUIEvent.ClearSearch) },
        )
      }

      // Search results
      if (state.searchResults.isNotEmpty()) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "${state.searchResults.size} part${if (state.searchResults.size > 1) "s" else ""} found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = { onEvent(HomeUIEvent.ClearSearch) }) {
              Text("Clear")
            }
          }
        }
        items(state.searchResults) { result ->
          SearchResultCard(
            result = result,
            onClick = { onEvent(HomeUIEvent.SearchResultClicked(result.part)) },
          )
        }
      } else if (state.searchQuery.length >= 2 && !state.isSearching) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "No parts found for \"${state.searchQuery}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
              )
              Text(
                "Try searching by part name, number, or brand",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      // Only show the rest when not searching
      if (state.searchResults.isEmpty() && state.searchQuery.length < 2) {
        // Popular Makes
        item {
          Text(
            text = "Popular Makes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
          )
        }

        item {
          if (state.isLoading) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator()
            }
          } else {
            LazyRow(
              contentPadding = PaddingValues(horizontal = 16.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              items(state.popularMakes) { make ->
                MakeChip(
                  make = make,
                  onClick = { onEvent(HomeUIEvent.MakeSelected(make)) },
                )
              }
            }
          }
        }

        // How it works
        item {
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = "How It Works",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          )
        }

        item { HowItWorksSection() }

        // Quick actions
        item {
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          )
        }

        item {
          QuickActionsSection(
            onSelectVehicle = { onEvent(HomeUIEvent.NavigateToVehicleSelection) },
            onViewCart = { onEvent(HomeUIEvent.NavigateToCart) },
            onVendorDashboard = { onEvent(HomeUIEvent.NavigateToVendorDashboard) },
          )
        }
      }
    }
  }
}

@Composable
private fun HeroSection(
  searchQuery: String,
  isSearching: Boolean,
  onSearchQueryChanged: (String) -> Unit,
  onSearch: () -> Unit,
  onSelectVehicle: () -> Unit,
  onClearSearch: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.primaryContainer,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = "Find Auto Parts in Ghana",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Compare prices from multiple vendors across Ghana",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Search bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = { Text("Search by part name, number, or brand...") },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
          if (isSearching) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
          } else if (searchQuery.isNotEmpty()) {
            IconButton(onClick = onClearSearch) {
              Icon(Icons.Default.Close, contentDescription = "Clear")
            }
          }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          focusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
      )

      // Search hint chips
      if (searchQuery.isEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf("brake pad", "oil filter", "battery", "spark plug").forEach { hint ->
            SuggestionChip(
              onClick = { onSearchQueryChanged(hint) },
              label = { Text(hint, style = MaterialTheme.typography.labelSmall) },
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Select vehicle button
      Button(
        onClick = onSelectVehicle,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.Default.DirectionsCar, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Select Your Vehicle")
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Year > Make > Model > Engine",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
      )
    }
  }
}

@Composable
private fun SearchResultCard(
  result: PartWithListings,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp),
    onClick = onClick,
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
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
            text = result.part.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = result.part.partNumber,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = result.part.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        if (result.lowestPrice != null) {
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "from",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = "GHS ${result.lowestPrice.formatPrice()}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Vendor info row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Store,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${result.vendorCount} vendor${if (result.vendorCount > 1) "s" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.tertiary,
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "In Stock",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary,
          )
        }

        // Brands available
        val brands = result.listings.map { it.brandName }.distinct().take(3)
        Text(
          text = brands.joinToString(" / "),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun MakeChip(
  make: VehicleMake,
  onClick: () -> Unit,
) {
  Card(
    onClick = onClick,
    modifier = Modifier.width(100.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        Icons.Default.DirectionsCar,
        contentDescription = null,
        modifier = Modifier.size(32.dp),
        tint = MaterialTheme.colorScheme.primary,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = make.name,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun HowItWorksSection() {
  Column(
    modifier = Modifier.padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    StepCard(
      stepNumber = 1,
      title = "Select Your Vehicle",
      description = "Choose your car's make, year, model, and engine type",
      icon = Icons.Default.DirectionsCar,
    )
    StepCard(
      stepNumber = 2,
      title = "Browse Parts",
      description = "Find parts from categories like Brakes, Engine, Electrical, and more",
      icon = Icons.Default.Category,
    )
    StepCard(
      stepNumber = 3,
      title = "Compare Vendors",
      description = "See prices from multiple vendors across Accra, Kumasi, Tema, and more",
      icon = Icons.Default.CompareArrows,
    )
    StepCard(
      stepNumber = 4,
      title = "Order & Deliver",
      description = "Add to cart, checkout, and have parts delivered to your location",
      icon = Icons.Default.LocalShipping,
    )
  }
}

@Composable
private fun StepCard(
  stepNumber: Int,
  title: String,
  description: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(40.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            "$stepNumber",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
          )
        }
      }
      Spacer(modifier = Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
        )
        Text(
          description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
  }
}

@Composable
private fun QuickActionsSection(
  onSelectVehicle: () -> Unit,
  onViewCart: () -> Unit,
  onVendorDashboard: () -> Unit,
) {
  Column(
    modifier = Modifier.padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      QuickActionCard(
        title = "Part Catalog",
        icon = Icons.Default.Search,
        onClick = onSelectVehicle,
        modifier = Modifier.weight(1f),
      )
      QuickActionCard(
        title = "Shopping Cart",
        icon = Icons.Default.ShoppingCart,
        onClick = onViewCart,
        modifier = Modifier.weight(1f),
      )
    }
    Card(
      onClick = onVendorDashboard,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          Icons.Default.Store,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            "Vendor Dashboard",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
          )
          Text(
            "Manage your parts inventory and orders",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
private fun QuickActionCard(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    onClick = onClick,
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        icon,
        contentDescription = null,
        modifier = Modifier.size(32.dp),
        tint = MaterialTheme.colorScheme.primary,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium,
      )
    }
  }
}

private fun Double.formatPrice(): String {
  val intPart = this.toLong()
  val decPart = ((this - intPart) * 100).toLong()
  return "$intPart.${decPart.toString().padStart(2, '0')}"
}

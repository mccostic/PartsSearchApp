package com.app.partssearchapp.screens.home.compose

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.app.partssearchapp.data.models.Part
import com.app.partssearchapp.data.models.VehicleMake
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
      // Hero section
      item {
        HeroSection(
          searchQuery = state.searchQuery,
          isSearching = state.isSearching,
          onSearchQueryChanged = { onEvent(HomeUIEvent.SearchQueryChanged(it)) },
          onSearch = { onEvent(HomeUIEvent.SearchParts) },
          onSelectVehicle = { onEvent(HomeUIEvent.NavigateToVehicleSelection) },
        )
      }

      // Search results
      if (state.searchResults.isNotEmpty()) {
        item {
          Text(
            text = "Search Results (${state.searchResults.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          )
        }
        items(state.searchResults) { part ->
          SearchResultItem(
            part = part,
            onClick = { onEvent(HomeUIEvent.SearchResultClicked(part)) },
          )
        }
      }

      // Popular Makes section
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

      // How it works section
      item {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
          text = "How It Works",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }

      item {
        HowItWorksSection()
      }

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

@Composable
private fun HeroSection(
  searchQuery: String,
  isSearching: Boolean,
  onSearchQueryChanged: (String) -> Unit,
  onSearch: () -> Unit,
  onSelectVehicle: () -> Unit,
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
        placeholder = { Text("Search by part name or number...") },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
          if (isSearching) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
          }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          focusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
      )

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
private fun SearchResultItem(
  part: Part,
  onClick: () -> Unit,
) {
  ListItem(
    headlineContent = { Text(part.name) },
    supportingContent = {
      Column {
        Text(part.description)
        Text(
          "Part #: ${part.partNumber}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    },
    leadingContent = {
      Icon(Icons.Default.Build, contentDescription = null)
    },
    trailingContent = {
      Icon(Icons.Default.ChevronRight, contentDescription = null)
    },
    modifier = Modifier.clickable { onClick() },
  )
  HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
      description = "Find the parts you need from categories like Brakes, Engine, Electrical, and more",
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
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ),
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
      Icon(
        icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
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

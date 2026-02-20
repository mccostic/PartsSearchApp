package com.app.partssearchapp.screens.partslisting.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.app.partssearchapp.data.models.Part
import com.app.partssearchapp.data.models.VendorListing
import com.app.partssearchapp.screens.partslisting.*
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartsListingView(
    state: PartsListingState,
    onEvent: (PartsListingUIEvent) -> Unit,
    uiEffects: Flow<PartsListingUIEffect>,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.categoryName) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PartsListingUIEvent.BackPressed) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(PartsListingUIEvent.GoToCart) }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
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
            // Breadcrumb
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Part Catalog > ${state.vehicleBreadcrumb} > ${state.categoryName}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.parts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No parts found for this category",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.parts) { part ->
                        PartCard(
                            part = part,
                            listings = state.listingsMap[part.id] ?: emptyList(),
                            isExpanded = state.expandedPartId == part.id,
                            onPartClick = { onEvent(PartsListingUIEvent.PartClicked(part)) },
                            onAddToCart = { listing ->
                                onEvent(PartsListingUIEvent.AddToCart(listing, part.name))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartCard(
    part: Part,
    listings: List<VendorListing>,
    isExpanded: Boolean,
    onPartClick: () -> Unit,
    onAddToCart: (VendorListing) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Part header
            ListItem(
                headlineContent = {
                    Text(part.name, fontWeight = FontWeight.Bold)
                },
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
                trailingContent = {
                    Column(horizontalAlignment = Alignment.End) {
                        if (listings.isNotEmpty()) {
                            Text(
                                "From GHS ${listings.minOf { it.price }.formatPrice()}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "${listings.size} vendor${if (listings.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                        )
                    }
                },
                modifier = Modifier.clickable { onPartClick() },
            )

            // Expanded vendor listings
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Specifications
                    if (part.specifications.isNotEmpty()) {
                        Text(
                            "Specifications",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        part.specifications.forEach { spec ->
                            Text(
                                "- $spec",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        "Available from vendors:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    listings.sortedBy { it.price }.forEach { listing ->
                        VendorListingCard(
                            listing = listing,
                            onAddToCart = { onAddToCart(listing) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorListingCard(listing: VendorListing, onAddToCart: () -> Unit,) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.brandName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = listing.partNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                )
                Text(
                    text = listing.vendorName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (listing.inStock) {
                    Text(
                        text = "In Stock (${listing.stockQuantity})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                } else {
                    Text(
                        text = "Out of Stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${listing.currency} ${listing.price.formatPrice()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onAddToCart,
                    enabled = listing.inStock,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Cart", style = MaterialTheme.typography.labelSmall)
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

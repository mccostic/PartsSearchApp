package com.app.partssearchapp.screens.vendor

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.data.models.*
import com.app.partssearchapp.data.service.InventoryManager
import kotlinx.coroutines.flow.filterIsInstance

class VendorDashboardViewModel(params: VendorDashboardParams, private val inventoryManager: InventoryManager,) :
    BaseViewModel<VendorDashboardState, VendorDashboardUIEvent, VendorDashboardNavEvent, VendorDashboardUIEffect, VendorDashboardParams>(
        params = params,
        initialState = VendorDashboardState()
    ) {

    init {
        setupEventHandlers()
        launch { loadVendorData() }
        launch { observeListings() }
    }

    private fun setupEventHandlers() {
        launch { tabSelectedHandler() }
        launch { updateOrderStatusHandler() }
        launch { backPressedHandler() }
        launch { showAddListingHandler() }
        launch { dismissAddListingHandler() }
        launch { addNewListingHandler() }
        launch { showEditListingHandler() }
        launch { dismissEditListingHandler() }
        launch { updateListingHandler() }
        launch { removeListingHandler() }
        launch { inventorySearchHandler() }
    }

    private suspend fun loadVendorData() {
        updateState { copy(isLoading = true) }
        try {
            val vendor = inventoryManager.getVendor(params.vendorId)
            val listings = inventoryManager.getVendorListings(params.vendorId)
            val availableParts = inventoryManager.getAvailablePartsForVendor()
            val orders = getMockOrders()

            val totalRevenue = orders
                .filter { it.status == OrderStatus.DELIVERED }
                .sumOf { it.totalAmount }
            val totalItemsSold = orders
                .filter { it.status == OrderStatus.DELIVERED }
                .sumOf { it.items.sumOf { item -> item.quantity } }

            updateState {
                copy(
                    vendor = vendor,
                    listings = listings,
                    orders = orders,
                    availableParts = availableParts,
                    isLoading = false,
                    totalRevenue = totalRevenue,
                    totalItemsSold = totalItemsSold,
                )
            }
        } catch (e: Exception) {
            updateState { copy(isLoading = false) }
            showErrorSnackbar("Failed to load vendor data")
        }
    }

    private suspend fun observeListings() {
        inventoryManager.listings.collect { allListings ->
            val vendorListings = allListings.filter { it.vendorId == params.vendorId }
            updateState { copy(listings = vendorListings) }
        }
    }

    private suspend fun tabSelectedHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.TabSelected>()
            .collect { event ->
                updateState { copy(selectedTab = event.tab) }
            }
    }

    private suspend fun updateOrderStatusHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.UpdateOrderStatus>()
            .collect { event ->
                updateState {
                    copy(
                        orders = orders.map { order ->
                            if (order.id == event.orderId) {
                                order.copy(status = event.status)
                            } else {
                                order
                            }
                        }
                    )
                }
                showSuccessSnackbar("Order #${event.orderId} updated to ${event.status.name}")
            }
    }

    private suspend fun backPressedHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.BackPressed>()
            .collect {
                emitNavEvent(VendorDashboardNavEvent.NavigateBack)
            }
    }

    private suspend fun showAddListingHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.ShowAddListing>()
            .collect {
                updateState { copy(showAddListingDialog = true) }
            }
    }

    private suspend fun dismissAddListingHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.DismissAddListing>()
            .collect {
                updateState { copy(showAddListingDialog = false) }
            }
    }

    private suspend fun addNewListingHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.AddNewListing>()
            .collect { event ->
                val vendor = currentState.vendor ?: return@collect
                val newListing = VendorListing(
                    id = 0,
                    partId = event.partId,
                    vendorId = params.vendorId,
                    vendorName = vendor.name,
                    brandName = event.brandName,
                    partNumber = event.partNumber,
                    price = event.price,
                    currency = "GHS",
                    inStock = event.stockQuantity > 0,
                    stockQuantity = event.stockQuantity,
                    condition = event.condition,
                )
                inventoryManager.addListing(newListing)
                updateState { copy(showAddListingDialog = false) }
                showSuccessSnackbar("Listing added successfully")
            }
    }

    private suspend fun showEditListingHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.ShowEditListing>()
            .collect { event ->
                updateState {
                    copy(showEditListingDialog = true, editingListing = event.listing)
                }
            }
    }

    private suspend fun dismissEditListingHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.DismissEditListing>()
            .collect {
                updateState { copy(showEditListingDialog = false, editingListing = null) }
            }
    }

    private suspend fun updateListingHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.UpdateListing>()
            .collect { event ->
                inventoryManager.updateListing(
                    listingId = event.listingId,
                    price = event.price,
                    stockQuantity = event.stockQuantity,
                    inStock = event.inStock,
                )
                updateState { copy(showEditListingDialog = false, editingListing = null) }
                showSuccessSnackbar("Listing updated")
            }
    }

    private suspend fun removeListingHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.RemoveListing>()
            .collect { event ->
                inventoryManager.removeListing(event.listingId)
                showSuccessSnackbar("Listing removed")
            }
    }

    private suspend fun inventorySearchHandler() {
        uiEvents
            .filterIsInstance<VendorDashboardUIEvent.InventorySearchChanged>()
            .collect { event ->
                updateState { copy(inventorySearchQuery = event.query) }
            }
    }

    private fun getMockOrders(): List<Order> = listOf(
        Order(
            id = 1001,
            items = listOf(
                CartItem(
                    1,
                    VendorListing(1, 1, params.vendorId, "Self", "TOYOTA GENUINE", "04465-02220", 185.0, "GHS", true, 25),
                    "Front Brake Pad Set",
                    2
                )
            ),
            status = OrderStatus.PENDING,
            customerName = "Kwame Asante",
            customerPhone = "+233 20 123 4567",
            deliveryAddress = "123 Independence Ave, Accra",
        ),
        Order(
            id = 1002,
            items = listOf(
                CartItem(
                    2,
                    VendorListing(23, 10, params.vendorId, "Self", "TOYOTA GENUINE", "90915-YZZD4", 35.0, "GHS", true, 100),
                    "Oil Filter",
                    3
                )
            ),
            status = OrderStatus.CONFIRMED,
            customerName = "Ama Mensah",
            customerPhone = "+233 24 987 6543",
            deliveryAddress = "456 Kwame Nkrumah Circle, Accra",
        ),
        Order(
            id = 1003,
            items = listOf(
                CartItem(
                    3,
                    VendorListing(64, 30, params.vendorId, "Self", "VARTA", "D24", 450.0, "GHS", true, 10),
                    "Car Battery 12V 60Ah",
                    1
                )
            ),
            status = OrderStatus.DELIVERED,
            customerName = "Kofi Adjei",
            customerPhone = "+233 55 222 3333",
            deliveryAddress = "789 Oxford St, Osu, Accra",
        ),
        Order(
            id = 1004,
            items = listOf(
                CartItem(
                    4,
                    VendorListing(39, 16, params.vendorId, "Self", "DENSO", "SK20R11", 95.0, "GHS", true, 40),
                    "Iridium Spark Plug Set",
                    2
                )
            ),
            status = OrderStatus.DELIVERED,
            customerName = "Abena Owusu",
            customerPhone = "+233 20 555 6666",
            deliveryAddress = "12 Ring Road East, Accra",
        ),
        Order(
            id = 1005,
            items = listOf(
                CartItem(
                    5,
                    VendorListing(52, 21, params.vendorId, "Self", "DENSO", "DRM50034", 650.0, "GHS", true, 3),
                    "Radiator Assembly",
                    1
                )
            ),
            status = OrderStatus.PROCESSING,
            customerName = "Yaw Boateng",
            customerPhone = "+233 27 888 9999",
            deliveryAddress = "34 Osu Badu St, Accra",
        ),
    )
}

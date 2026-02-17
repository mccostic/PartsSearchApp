package com.app.partssearchapp.screens.vendor

import com.app.partssearchapp.arch.BaseViewModel
import com.app.partssearchapp.data.models.*
import com.app.partssearchapp.data.service.PartsDataService
import kotlinx.coroutines.flow.filterIsInstance

class VendorDashboardViewModel(
  params: VendorDashboardParams,
  private val partsDataService: PartsDataService,
) : BaseViewModel<VendorDashboardState, VendorDashboardUIEvent, VendorDashboardNavEvent, VendorDashboardUIEffect, VendorDashboardParams>(
  params = params,
  initialState = VendorDashboardState()
) {

  init {
    setupEventHandlers()
    launch { loadVendorData() }
  }

  private fun setupEventHandlers() {
    launch { tabSelectedHandler() }
    launch { updateOrderStatusHandler() }
    launch { backPressedHandler() }
  }

  private suspend fun loadVendorData() {
    updateState { copy(isLoading = true) }
    try {
      val vendor = partsDataService.getVendor(params.vendorId)
      val allVendors = partsDataService.getVendors()
      // Get mock listings for this vendor
      val listings = getMockVendorListings(params.vendorId)
      val orders = getMockOrders()
      updateState {
        copy(
          vendor = vendor,
          listings = listings,
          orders = orders,
          isLoading = false,
        )
      }
    } catch (e: Exception) {
      updateState { copy(isLoading = false) }
      showErrorSnackbar("Failed to load vendor data")
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
              if (order.id == event.orderId) order.copy(status = event.status)
              else order
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

  private fun getMockVendorListings(vendorId: Int): List<VendorListing> {
    return com.app.partssearchapp.data.service.MockPartsDataService.vendorListings
      .filter { it.vendorId == vendorId }
  }

  private fun getMockOrders(): List<Order> {
    return listOf(
      Order(
        id = 1001,
        items = listOf(
          CartItem(1, VendorListing(1, 1, params.vendorId, "Self", "TOYOTA GENUINE", "04465-02220", 185.0, "GHS", true, 25), "Front Brake Pad Set", 2)
        ),
        status = OrderStatus.PENDING,
        customerName = "Kwame Asante",
        customerPhone = "+233 20 123 4567",
        deliveryAddress = "123 Independence Ave, Accra",
      ),
      Order(
        id = 1002,
        items = listOf(
          CartItem(2, VendorListing(11, 7, params.vendorId, "Self", "TOYOTA GENUINE", "90915-YZZD4", 35.0, "GHS", true, 100), "Oil Filter", 3)
        ),
        status = OrderStatus.CONFIRMED,
        customerName = "Ama Mensah",
        customerPhone = "+233 24 987 6543",
        deliveryAddress = "456 Kwame Nkrumah Circle, Accra",
      ),
      Order(
        id = 1003,
        items = listOf(
          CartItem(3, VendorListing(20, 17, params.vendorId, "Self", "VARTA", "D24", 450.0, "GHS", true, 10), "Car Battery 12V 60Ah", 1)
        ),
        status = OrderStatus.DELIVERED,
        customerName = "Kofi Adjei",
        customerPhone = "+233 55 222 3333",
        deliveryAddress = "789 Oxford St, Osu, Accra",
      ),
    )
  }
}

package com.app.partssearchapp.data.service

import com.app.partssearchapp.data.models.VendorListing
import kotlin.test.*

class InventoryManagerTest {

  private lateinit var manager: InventoryManager

  @BeforeTest
  fun setup() {
    manager = InventoryManager()
  }

  @Test
  fun getVendorReturnsCorrectVendor() {
    val vendor = manager.getVendor(1)
    assertNotNull(vendor)
    assertEquals("Accra Auto Parts", vendor.name)
  }

  @Test
  fun getVendorReturnsNullForInvalidId() {
    assertNull(manager.getVendor(999))
  }

  @Test
  fun getVendorListingsReturnsListingsForVendor() {
    val listings = manager.getVendorListings(1)
    assertTrue(listings.isNotEmpty())
    assertTrue(listings.all { it.vendorId == 1 })
  }

  @Test
  fun addListingAddsToInventory() {
    val initialCount = manager.listings.value.size

    val newListing = VendorListing(0, 1, 1, "Test Vendor", "BRAND", "PN-NEW", 99.0, "GHS", true, 5)
    val added = manager.addListing(newListing)

    assertEquals(initialCount + 1, manager.listings.value.size)
    assertTrue(added.id > 0)
    assertNotNull(manager.listings.value.find { it.id == added.id })
  }

  @Test
  fun updateListingUpdatesValues() {
    val listing = manager.listings.value.first()

    manager.updateListing(
      listingId = listing.id,
      price = 999.0,
      stockQuantity = 50,
      inStock = true,
    )

    val updated = manager.listings.value.find { it.id == listing.id }
    assertNotNull(updated)
    assertEquals(999.0, updated.price)
    assertEquals(50, updated.stockQuantity)
    assertTrue(updated.inStock)
  }

  @Test
  fun removeListingRemovesFromInventory() {
    val listing = manager.listings.value.first()
    val initialCount = manager.listings.value.size

    manager.removeListing(listing.id)

    assertEquals(initialCount - 1, manager.listings.value.size)
    assertNull(manager.listings.value.find { it.id == listing.id })
  }

  @Test
  fun updateStockAfterPurchaseDeductsStock() {
    val listing = manager.listings.value.first()
    val initialStock = listing.stockQuantity

    manager.updateStockAfterPurchase(listing.id, 2)

    val updated = manager.listings.value.find { it.id == listing.id }
    assertNotNull(updated)
    assertEquals(initialStock - 2, updated.stockQuantity)
  }

  @Test
  fun updateStockAfterPurchaseSetsOutOfStock() {
    val listing = manager.listings.value.first()

    manager.updateStockAfterPurchase(listing.id, listing.stockQuantity + 5)

    val updated = manager.listings.value.find { it.id == listing.id }
    assertNotNull(updated)
    assertEquals(0, updated.stockQuantity)
    assertFalse(updated.inStock)
  }

  @Test
  fun getListingsForPartReturnsInStockOnly() {
    val listings = manager.getListingsForPart(1)
    assertTrue(listings.isNotEmpty())
    assertTrue(listings.all { it.partId == 1 && it.inStock })
  }

  @Test
  fun getAllListingsForPartReturnsAll() {
    val all = manager.getAllListingsForPart(1)
    val inStock = manager.getListingsForPart(1)
    assertTrue(all.size >= inStock.size)
  }

  @Test
  fun searchPartsReturnsMatchingResults() {
    val results = manager.searchParts("brake")
    assertTrue(results.isNotEmpty())
    assertTrue(results.all {
      it.name.contains("brake", ignoreCase = true) ||
        it.description.contains("brake", ignoreCase = true)
    })
  }

  @Test
  fun searchPartsWithBlankQueryReturnsEmpty() {
    assertTrue(manager.searchParts("").isEmpty())
    assertTrue(manager.searchParts("  ").isEmpty())
  }

  @Test
  fun searchPartsWithListingsReturnsPartWithListingInfo() {
    val results = manager.searchPartsWithListings("brake")
    assertTrue(results.isNotEmpty())
    results.forEach { pwl ->
      assertTrue(pwl.listings.isNotEmpty())
      assertNotNull(pwl.lowestPrice)
      assertTrue(pwl.vendorCount > 0)
    }
  }

  @Test
  fun getPartsForCategoryReturnsMatchingParts() {
    val parts = manager.getPartsForCategory(11) // Brake Pad
    assertTrue(parts.isNotEmpty())
    assertTrue(parts.all { it.categoryId == 11 })
  }

  @Test
  fun getAvailablePartsForVendorReturnsAllParts() {
    val parts = manager.getAvailablePartsForVendor()
    assertTrue(parts.isNotEmpty())
    assertEquals(manager.parts.value.size, parts.size)
  }

  @Test
  fun getPartByIdReturnsCorrectPart() {
    val part = manager.getPartById(1)
    assertNotNull(part)
    assertEquals(1, part.id)
  }

  @Test
  fun getPartByIdReturnsNullForInvalidId() {
    assertNull(manager.getPartById(9999))
  }
}

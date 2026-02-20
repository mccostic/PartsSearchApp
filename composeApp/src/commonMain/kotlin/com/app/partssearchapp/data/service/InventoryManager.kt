package com.app.partssearchapp.data.service

import com.app.partssearchapp.data.models.Part
import com.app.partssearchapp.data.models.PartCategory
import com.app.partssearchapp.data.models.Vendor
import com.app.partssearchapp.data.models.VendorListing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Central inventory manager that holds ALL vendor inventory data.
 * Vendors add/edit/remove their listings here. Buyers see this same live data.
 * This replaces the disconnected mock data approach.
 */
class InventoryManager {

    private val _vendors = MutableStateFlow(initialVendors)
    val vendors: StateFlow<List<Vendor>> = _vendors.asStateFlow()

    private val _parts = MutableStateFlow(initialParts)
    val parts: StateFlow<List<Part>> = _parts.asStateFlow()

    private val _listings = MutableStateFlow(initialListings)
    val listings: StateFlow<List<VendorListing>> = _listings.asStateFlow()

    private val _categories = MutableStateFlow(initialCategories)
    val categories: StateFlow<List<PartCategory>> = _categories.asStateFlow()

    private var nextListingId = initialListings.maxOf { it.id } + 1

    // --- Vendor operations ---

    fun getVendor(vendorId: Int): Vendor? = _vendors.value.find { it.id == vendorId }

    fun getVendorListings(vendorId: Int): List<VendorListing> = _listings.value.filter { it.vendorId == vendorId }

    // --- Inventory management (vendor actions) ---

    fun addListing(listing: VendorListing): VendorListing {
        val newListing = listing.copy(id = nextListingId++)
        _listings.update { it + newListing }
        return newListing
    }

    fun updateListing(listingId: Int, price: Double, stockQuantity: Int, inStock: Boolean) {
        _listings.update { listings ->
            listings.map {
                if (it.id == listingId) {
                    it.copy(
                        price = price,
                        stockQuantity = stockQuantity,
                        inStock = inStock,
                    )
                } else {
                    it
                }
            }
        }
    }

    fun removeListing(listingId: Int) {
        _listings.update { listings -> listings.filter { it.id != listingId } }
    }

    fun updateStockAfterPurchase(listingId: Int, quantityPurchased: Int) {
        _listings.update { listings ->
            listings.map {
                if (it.id == listingId) {
                    val newQty = (it.stockQuantity - quantityPurchased).coerceAtLeast(0)
                    it.copy(stockQuantity = newQty, inStock = newQty > 0)
                } else {
                    it
                }
            }
        }
    }

    // --- Buyer queries ---

    fun getListingsForPart(partId: Int): List<VendorListing> = _listings.value.filter {
        it.partId == partId && it.inStock
    }

    fun getAllListingsForPart(partId: Int): List<VendorListing> = _listings.value.filter { it.partId == partId }

    fun getPartsForCategory(categoryId: Int): List<Part> {
        val partIds = _listings.value.filter { it.inStock }.map { it.partId }.toSet()
        return _parts.value.filter { it.categoryId == categoryId && it.id in partIds }
    }

    fun getAllPartsForCategory(categoryId: Int): List<Part> = _parts.value.filter { it.categoryId == categoryId }

    fun searchParts(query: String): List<Part> {
        if (query.isBlank()) return emptyList()
        val lowerQuery = query.lowercase()
        val inStockPartIds = _listings.value.filter { it.inStock }.map { it.partId }.toSet()

        // Search parts by name, part number, or description
        val matchingParts = _parts.value.filter {
            (
                it.name.lowercase().contains(lowerQuery) ||
                    it.partNumber.lowercase().contains(lowerQuery) ||
                    it.description.lowercase().contains(lowerQuery)
                ) &&
                it.id in inStockPartIds
        }

        // Also match by vendor listing part numbers and brand names
        val listingMatchPartIds = _listings.value.filter {
            it.inStock &&
                (
                    it.partNumber.lowercase().contains(lowerQuery) ||
                        it.brandName.lowercase().contains(lowerQuery)
                    )
        }.map { it.partId }.toSet()

        val extraParts = _parts.value.filter {
            it.id in listingMatchPartIds &&
                it.id !in matchingParts.map { p -> p.id }.toSet()
        }

        return matchingParts + extraParts
    }

    fun searchPartsWithListings(query: String): List<PartWithListings> {
        if (query.isBlank()) return emptyList()
        val matchingParts = searchParts(query)
        return matchingParts.map { part ->
            val partListings = _listings.value.filter { it.partId == part.id && it.inStock }
            PartWithListings(
                part = part,
                listings = partListings,
                lowestPrice = partListings.minOfOrNull { it.price },
                vendorCount = partListings.map { it.vendorId }.distinct().size,
            )
        }.filter { it.listings.isNotEmpty() }
    }

    fun getPartById(partId: Int): Part? = _parts.value.find { it.id == partId }

    fun getAvailablePartsForVendor(): List<Part> = _parts.value

    companion object {
        // Ghana-based vendors
        val initialVendors = listOf(
            Vendor(1, "Accra Auto Parts", "Accra, Abossey Okai", "+233 20 111 2222", 4.5, 1250, true),
            Vendor(2, "Kumasi Parts Hub", "Kumasi, Suame Magazine", "+233 24 333 4444", 4.2, 890, true),
            Vendor(3, "Tema Auto Supplies", "Tema, Community 1", "+233 27 555 6666", 4.7, 2100, true),
            Vendor(4, "Cape Coast Motors", "Cape Coast, Kotokuraba", "+233 20 777 8888", 3.9, 450, false),
            Vendor(5, "Takoradi Spares", "Takoradi, Market Circle", "+233 24 999 0000", 4.1, 670, true),
            Vendor(6, "Bright's Auto Center", "Accra, Spintex Road", "+233 55 111 3333", 4.8, 3200, true),
            Vendor(7, "Koforidua Car Parts", "Koforidua, New Juaben", "+233 20 444 5555", 4.0, 320, true),
            Vendor(8, "Tamale Auto World", "Tamale, Central Market", "+233 26 666 7777", 3.8, 210, false),
        )

        val initialCategories = listOf(
            PartCategory(
                1,
                "Brake & Wheel Hub",
                subcategories = listOf(
                    PartCategory(11, "Brake Pad", parentId = 1),
                    PartCategory(12, "Brake Rotor / Disc", parentId = 1),
                    PartCategory(13, "Brake Caliper", parentId = 1),
                    PartCategory(14, "Brake Fluid", parentId = 1),
                    PartCategory(15, "Wheel Bearing", parentId = 1),
                )
            ),
            PartCategory(
                2,
                "Engine",
                subcategories = listOf(
                    PartCategory(21, "Oil Filter", parentId = 2),
                    PartCategory(22, "Air Filter", parentId = 2),
                    PartCategory(23, "Spark Plug", parentId = 2),
                    PartCategory(24, "Timing Belt / Chain", parentId = 2),
                    PartCategory(25, "Engine Mount", parentId = 2),
                    PartCategory(26, "Valve Cover Gasket", parentId = 2),
                )
            ),
            PartCategory(
                3,
                "Cooling System",
                subcategories = listOf(
                    PartCategory(31, "Radiator", parentId = 3),
                    PartCategory(32, "Water Pump", parentId = 3),
                    PartCategory(33, "Thermostat", parentId = 3),
                    PartCategory(34, "Coolant / Antifreeze", parentId = 3),
                    PartCategory(35, "Radiator Hose", parentId = 3),
                    PartCategory(36, "Radiator Fan", parentId = 3),
                )
            ),
            PartCategory(
                4,
                "Fuel & Air",
                subcategories = listOf(
                    PartCategory(41, "Fuel Filter", parentId = 4),
                    PartCategory(42, "Fuel Pump", parentId = 4),
                    PartCategory(43, "Fuel Injector", parentId = 4),
                    PartCategory(44, "Throttle Body", parentId = 4),
                )
            ),
            PartCategory(
                5,
                "Electrical",
                subcategories = listOf(
                    PartCategory(51, "Battery", parentId = 5),
                    PartCategory(52, "Alternator", parentId = 5),
                    PartCategory(53, "Starter Motor", parentId = 5),
                    PartCategory(54, "Headlight Bulb", parentId = 5),
                    PartCategory(55, "Ignition Coil", parentId = 5),
                    PartCategory(56, "Fuse Box", parentId = 5),
                )
            ),
            PartCategory(
                6,
                "Suspension & Steering",
                subcategories = listOf(
                    PartCategory(61, "Shock Absorber", parentId = 6),
                    PartCategory(62, "Strut", parentId = 6),
                    PartCategory(63, "Control Arm", parentId = 6),
                    PartCategory(64, "Tie Rod End", parentId = 6),
                    PartCategory(65, "Ball Joint", parentId = 6),
                    PartCategory(66, "Sway Bar Link", parentId = 6),
                )
            ),
            PartCategory(
                7,
                "Transmission",
                subcategories = listOf(
                    PartCategory(71, "Transmission Fluid", parentId = 7),
                    PartCategory(72, "Clutch Kit", parentId = 7),
                    PartCategory(73, "CV Joint / Axle", parentId = 7),
                    PartCategory(74, "Drive Belt", parentId = 7),
                )
            ),
            PartCategory(
                8,
                "Exhaust",
                subcategories = listOf(
                    PartCategory(81, "Muffler", parentId = 8),
                    PartCategory(82, "Catalytic Converter", parentId = 8),
                    PartCategory(83, "Exhaust Gasket", parentId = 8),
                    PartCategory(84, "Oxygen Sensor", parentId = 8),
                )
            ),
            PartCategory(
                9,
                "Body & Interior",
                subcategories = listOf(
                    PartCategory(91, "Side Mirror", parentId = 9),
                    PartCategory(92, "Wiper Blade", parentId = 9),
                    PartCategory(93, "Door Handle", parentId = 9),
                    PartCategory(94, "Bumper", parentId = 9),
                    PartCategory(95, "Fender", parentId = 9),
                )
            ),
            PartCategory(
                10,
                "Lighting",
                subcategories = listOf(
                    PartCategory(101, "Headlight Assembly", parentId = 10),
                    PartCategory(102, "Tail Light Assembly", parentId = 10),
                    PartCategory(103, "Fog Light", parentId = 10),
                    PartCategory(104, "Turn Signal", parentId = 10),
                )
            ),
        )

        // All engine IDs for compatibility
        private val toyotaEngineIds = (1..10).toList()
        private val hondaEngineIds = (11..15).toList()
        private val allEngineIds = (1..20).toList()

        val initialParts = listOf(
            // === BRAKE PADS (11) ===
            Part(
                1,
                "Front Brake Pad Set",
                "BP-FRT-001",
                11,
                "Premium ceramic front brake pads for Toyota",
                listOf("Ceramic compound", "Low dust", "Anti-squeal shim"),
                toyotaEngineIds
            ),
            Part(
                2,
                "Rear Brake Pad Set",
                "BP-RR-001",
                11,
                "OEM quality rear brake pads for Toyota",
                listOf("Semi-metallic", "Built-in wear indicator"),
                toyotaEngineIds
            ),
            Part(
                3,
                "Front Brake Pad Set",
                "BP-FRT-002",
                11,
                "High-performance front brake pads for Honda",
                listOf("Carbon-ceramic", "Track-rated"),
                hondaEngineIds
            ),
            Part(
                4,
                "Front Brake Pad Set - Universal",
                "BP-FRT-UNI",
                11,
                "Economy front brake pads - multi-vehicle fit",
                listOf("Economy range", "Standard compound"),
                allEngineIds
            ),
            Part(
                5,
                "Rear Brake Pad Set - Universal",
                "BP-RR-UNI",
                11,
                "Economy rear brake pads - multi-vehicle fit",
                listOf("Standard compound", "Multi-vehicle"),
                allEngineIds
            ),

            // === BRAKE ROTORS (12) ===
            Part(
                6,
                "Front Brake Rotor",
                "BR-FRT-001",
                12,
                "Ventilated front brake disc for Toyota",
                listOf("280mm diameter", "Ventilated"),
                toyotaEngineIds
            ),
            Part(
                7,
                "Rear Brake Rotor",
                "BR-RR-001",
                12,
                "Solid rear brake disc for Toyota",
                listOf("260mm diameter", "Solid"),
                toyotaEngineIds
            ),
            Part(
                8,
                "Front Brake Rotor - Drilled",
                "BR-FRT-DRL",
                12,
                "Drilled & slotted performance rotor",
                listOf("Cross-drilled", "Zinc coated", "290mm"),
                allEngineIds
            ),

            // === OIL FILTERS (21) ===
            Part(
                10,
                "Oil Filter - Toyota",
                "OF-TY-001",
                21,
                "Spin-on oil filter for Toyota engines",
                listOf("Micron rating: 20", "Anti-drain back valve"),
                toyotaEngineIds
            ),
            Part(
                11,
                "Oil Filter - Honda",
                "OF-HN-001",
                21,
                "Oil filter for Honda engines",
                listOf("Micron rating: 15", "Silicone anti-drain valve"),
                hondaEngineIds
            ),
            Part(
                12,
                "Oil Filter - Universal",
                "OF-UNI-001",
                21,
                "Universal spin-on oil filter",
                listOf("Multi-vehicle fit", "Standard filtration"),
                allEngineIds
            ),

            // === AIR FILTERS (22) ===
            Part(
                13,
                "Engine Air Filter - Toyota",
                "AF-TY-001",
                22,
                "Panel-type engine air filter",
                listOf("High-flow", "20,000km service life"),
                toyotaEngineIds
            ),
            Part(
                14,
                "Engine Air Filter - Honda",
                "AF-HN-001",
                22,
                "OEM replacement air filter",
                listOf("Standard flow", "Paper element"),
                hondaEngineIds
            ),
            Part(
                15,
                "Performance Air Filter",
                "AF-PERF-001",
                22,
                "Washable high-flow air filter",
                listOf("K&N style", "Reusable", "Million km warranty"),
                allEngineIds
            ),

            // === SPARK PLUGS (23) ===
            Part(
                16,
                "Iridium Spark Plug Set (4x)",
                "SP-IR-001",
                23,
                "Iridium spark plugs for 4-cylinder",
                listOf("Iridium tip", "Pre-gapped", "60,000km life"),
                allEngineIds
            ),
            Part(
                17,
                "Copper Spark Plug Set (4x)",
                "SP-CU-001",
                23,
                "Standard copper core spark plugs",
                listOf("Copper core", "Pre-gapped", "20,000km life"),
                allEngineIds
            ),

            // === TIMING BELT (24) ===
            Part(
                18,
                "Timing Belt Kit",
                "TB-KIT-001",
                24,
                "Complete timing belt kit with tensioner",
                listOf("Belt + tensioner + idler", "OEM spec"),
                toyotaEngineIds
            ),

            // === ENGINE MOUNT (25) ===
            Part(
                19,
                "Front Engine Mount",
                "EM-FRT-001",
                25,
                "Hydraulic front engine mount",
                listOf("Hydraulic", "OEM replacement"),
                toyotaEngineIds
            ),
            Part(
                20,
                "Rear Engine Mount",
                "EM-RR-001",
                25,
                "Rear transmission mount",
                listOf("Rubber", "OEM spec"),
                allEngineIds
            ),

            // === RADIATOR (31) ===
            Part(
                21,
                "Radiator Assembly",
                "RD-ASSY-001",
                31,
                "Complete radiator assembly",
                listOf("Aluminum core", "Plastic tanks", "Includes cap"),
                toyotaEngineIds
            ),
            Part(
                22,
                "Radiator Assembly - Universal",
                "RD-ASSY-UNI",
                31,
                "Universal fit radiator",
                listOf("Aluminum/plastic", "Check fitment"),
                allEngineIds
            ),

            // === WATER PUMP (32) ===
            Part(
                23,
                "Water Pump",
                "WP-001",
                32,
                "Engine water pump with gasket",
                listOf("Cast iron body", "Gasket included"),
                toyotaEngineIds
            ),

            // === THERMOSTAT (33) ===
            Part(
                24,
                "Thermostat",
                "TH-001",
                33,
                "Standard thermostat with housing",
                listOf("82°C opening temp", "Housing included"),
                allEngineIds
            ),

            // === FUEL FILTER (41) ===
            Part(
                25,
                "Fuel Filter",
                "FF-001",
                41,
                "In-line fuel filter",
                listOf("10 micron", "Plastic housing"),
                allEngineIds
            ),

            // === FUEL PUMP (42) ===
            Part(
                26,
                "Fuel Pump Assembly",
                "FP-ASSY-001",
                42,
                "Electric fuel pump with sender unit",
                listOf("In-tank", "Includes strainer"),
                toyotaEngineIds
            ),

            // === BATTERY (51) ===
            Part(
                30,
                "Car Battery 12V 60Ah",
                "BT-60AH",
                51,
                "Maintenance-free car battery",
                listOf("12V", "60Ah", "540 CCA", "24-month warranty"),
                allEngineIds
            ),
            Part(
                31,
                "Car Battery 12V 75Ah",
                "BT-75AH",
                51,
                "Heavy-duty car battery for SUVs/trucks",
                listOf("12V", "75Ah", "680 CCA", "36-month warranty"),
                allEngineIds
            ),
            Part(
                32,
                "Car Battery 12V 45Ah",
                "BT-45AH",
                51,
                "Compact battery for small vehicles",
                listOf("12V", "45Ah", "400 CCA", "18-month warranty"),
                allEngineIds
            ),

            // === ALTERNATOR (52) ===
            Part(
                33,
                "Alternator - Toyota",
                "AL-TY-001",
                52,
                "Remanufactured alternator 100A",
                listOf("100A output", "Internal regulator"),
                toyotaEngineIds
            ),
            Part(
                34,
                "Alternator - Universal",
                "AL-UNI-001",
                52,
                "Aftermarket alternator 90A",
                listOf("90A output", "External regulator"),
                allEngineIds
            ),

            // === STARTER MOTOR (53) ===
            Part(
                35,
                "Starter Motor",
                "SM-001",
                53,
                "Remanufactured starter motor",
                listOf("12V", "1.4kW output"),
                toyotaEngineIds
            ),

            // === HEADLIGHT BULB (54) ===
            Part(
                36,
                "Headlight Bulb H4",
                "HB-H4-001",
                54,
                "Halogen H4 headlight bulb pair",
                listOf("H4", "12V 60/55W", "Pair"),
                allEngineIds
            ),
            Part(
                37,
                "Headlight Bulb H7",
                "HB-H7-001",
                54,
                "Halogen H7 headlight bulb pair",
                listOf("H7", "12V 55W", "Pair"),
                allEngineIds
            ),
            Part(
                38,
                "LED Headlight H4",
                "HB-LED-H4",
                54,
                "LED conversion kit H4",
                listOf("6000K white", "30W per bulb", "Pair"),
                allEngineIds
            ),

            // === SHOCK ABSORBER (61) ===
            Part(
                40,
                "Front Shock Absorber (Pair)",
                "SA-FRT-001",
                61,
                "Gas-charged front shocks",
                listOf("Twin-tube", "Gas-charged", "Pair"),
                toyotaEngineIds
            ),
            Part(
                41,
                "Rear Shock Absorber (Pair)",
                "SA-RR-001",
                61,
                "Gas-charged rear shocks",
                listOf("Twin-tube", "Gas-charged", "Pair"),
                toyotaEngineIds
            ),
            Part(
                42,
                "Front Shock Absorber - Universal",
                "SA-FRT-UNI",
                61,
                "Aftermarket front shocks",
                listOf("Oil-filled", "Standard duty", "Pair"),
                allEngineIds
            ),

            // === CONTROL ARM (63) ===
            Part(
                43,
                "Front Lower Control Arm",
                "CA-FRT-LO",
                63,
                "Front lower control arm with ball joint",
                listOf("Steel", "Ball joint included", "Left or Right"),
                toyotaEngineIds
            ),

            // === TIE ROD END (64) ===
            Part(
                44,
                "Outer Tie Rod End",
                "TR-OUT-001",
                64,
                "Outer tie rod end",
                listOf("Greasable", "Boot included"),
                allEngineIds
            ),

            // === CLUTCH KIT (72) ===
            Part(
                45,
                "Clutch Kit 3-Piece",
                "CK-3PC-001",
                72,
                "Complete clutch kit for manual transmission",
                listOf("Disc + pressure plate + bearing", "OEM spec"),
                toyotaEngineIds
            ),

            // === CV JOINT (73) ===
            Part(
                46,
                "CV Axle Assembly",
                "CV-ASSY-001",
                73,
                "Complete CV axle shaft assembly",
                listOf("New (not rebuilt)", "Inner + outer joints", "Left or Right"),
                toyotaEngineIds
            ),

            // === WIPER BLADES (92) ===
            Part(
                50,
                "Wiper Blade Set",
                "WB-SET-001",
                92,
                "All-season beam wiper blade set",
                listOf("Beam style", "Driver + Passenger"),
                allEngineIds
            ),
            Part(
                51,
                "Rear Wiper Blade",
                "WB-RR-001",
                92,
                "Rear window wiper blade",
                listOf("Snap-on type", "300mm"),
                allEngineIds
            ),

            // === SIDE MIRROR (91) ===
            Part(
                52,
                "Side Mirror - Left",
                "SM-LF-001",
                91,
                "Manual side mirror left side",
                listOf("Manual adjust", "Non-heated", "Black"),
                allEngineIds
            ),

            // === HEADLIGHT ASSEMBLY (101) ===
            Part(
                55,
                "Headlight Assembly - Left",
                "HL-LF-001",
                101,
                "Complete headlight assembly left side",
                listOf("Halogen", "Clear lens", "DOT approved"),
                toyotaEngineIds
            ),
            Part(
                56,
                "Headlight Assembly - Right",
                "HL-RT-001",
                101,
                "Complete headlight assembly right side",
                listOf("Halogen", "Clear lens", "DOT approved"),
                toyotaEngineIds
            ),

            // === TAIL LIGHT (102) ===
            Part(
                57,
                "Tail Light Assembly - Left",
                "TL-LF-001",
                102,
                "Tail light assembly left side",
                listOf("OEM style", "Includes bulbs"),
                toyotaEngineIds
            ),

            // === MUFFLER (81) ===
            Part(
                60,
                "Rear Muffler",
                "MF-RR-001",
                81,
                "Rear exhaust muffler",
                listOf("Stainless steel", "Direct fit"),
                toyotaEngineIds
            ),

            // === OXYGEN SENSOR (84) ===
            Part(
                61,
                "Oxygen Sensor - Upstream",
                "O2-UP-001",
                84,
                "Upstream oxygen sensor (pre-cat)",
                listOf("4-wire", "Heated"),
                allEngineIds
            ),

            // === IGNITION COIL (55 category) ===
            Part(
                62,
                "Ignition Coil",
                "IC-001",
                55,
                "Direct ignition coil",
                listOf("Coil-on-plug", "OEM spec"),
                toyotaEngineIds
            ),
            Part(
                63,
                "Ignition Coil Pack",
                "IC-PACK-001",
                55,
                "Ignition coil pack for 4-cylinder",
                listOf("4-pack", "OEM replacement"),
                allEngineIds
            ),
        )

        val initialListings = listOf(
            // === FRONT BRAKE PAD SET (partId=1) - Toyota, 4 vendors ===
            VendorListing(1, 1, 1, "Accra Auto Parts", "TOYOTA GENUINE", "04465-02220", 185.0, "GHS", true, 25),
            VendorListing(2, 1, 2, "Kumasi Parts Hub", "BOSCH", "BP-3547", 145.0, "GHS", true, 15),
            VendorListing(3, 1, 3, "Tema Auto Supplies", "BREMBO", "P-83-099", 220.0, "GHS", true, 8),
            VendorListing(4, 1, 6, "Bright's Auto Center", "AKEBONO", "ACT-1210", 165.0, "GHS", true, 30),

            // === REAR BRAKE PAD SET (partId=2) ===
            VendorListing(5, 2, 1, "Accra Auto Parts", "TOYOTA GENUINE", "04466-02260", 155.0, "GHS", true, 20),
            VendorListing(6, 2, 3, "Tema Auto Supplies", "TRW", "GDB-3448", 125.0, "GHS", true, 12),
            VendorListing(7, 2, 6, "Bright's Auto Center", "BOSCH", "BP-3548", 138.0, "GHS", true, 18),

            // === FRONT BRAKE PAD - HONDA (partId=3) ===
            VendorListing(8, 3, 1, "Accra Auto Parts", "HONDA GENUINE", "45022-TBA-A01", 175.0, "GHS", true, 10),
            VendorListing(9, 3, 3, "Tema Auto Supplies", "FERODO", "FDB4393", 140.0, "GHS", true, 14),

            // === UNIVERSAL BRAKE PAD (partId=4) ===
            VendorListing(10, 4, 4, "Cape Coast Motors", "ECONOMY PARTS", "EP-BP-001", 75.0, "GHS", true, 50),
            VendorListing(11, 4, 5, "Takoradi Spares", "VALUE BRAKE", "VB-200", 85.0, "GHS", true, 40),
            VendorListing(12, 4, 7, "Koforidua Car Parts", "FRICO", "FR-BP-100", 70.0, "GHS", true, 35),
            VendorListing(13, 4, 8, "Tamale Auto World", "ASIMCO", "AS-BP-200", 65.0, "GHS", true, 60),

            // === REAR UNIVERSAL BRAKE PAD (partId=5) ===
            VendorListing(14, 5, 4, "Cape Coast Motors", "ECONOMY PARTS", "EP-BP-002", 68.0, "GHS", true, 45),
            VendorListing(15, 5, 7, "Koforidua Car Parts", "FRICO", "FR-BP-101", 62.0, "GHS", true, 30),

            // === FRONT BRAKE ROTOR (partId=6) ===
            VendorListing(16, 6, 1, "Accra Auto Parts", "TOYOTA GENUINE", "43512-02330", 350.0, "GHS", true, 6),
            VendorListing(17, 6, 6, "Bright's Auto Center", "BREMBO", "09.A820.11", 295.0, "GHS", true, 10),
            VendorListing(18, 6, 3, "Tema Auto Supplies", "TRW", "DF6136", 270.0, "GHS", true, 5),

            // === REAR BRAKE ROTOR (partId=7) ===
            VendorListing(19, 7, 1, "Accra Auto Parts", "TOYOTA GENUINE", "42431-02190", 280.0, "GHS", true, 4),
            VendorListing(20, 7, 6, "Bright's Auto Center", "BREMBO", "08.A429.11", 240.0, "GHS", true, 7),

            // === DRILLED ROTOR (partId=8) ===
            VendorListing(21, 8, 6, "Bright's Auto Center", "EBC", "GD7555", 420.0, "GHS", true, 3),
            VendorListing(22, 8, 3, "Tema Auto Supplies", "POWER STOP", "JBR1554XPR", 380.0, "GHS", true, 4),

            // === OIL FILTER TOYOTA (partId=10) ===
            VendorListing(23, 10, 1, "Accra Auto Parts", "TOYOTA GENUINE", "90915-YZZD4", 35.0, "GHS", true, 100),
            VendorListing(24, 10, 2, "Kumasi Parts Hub", "MANN FILTER", "W68/3", 28.0, "GHS", true, 60),
            VendorListing(25, 10, 3, "Tema Auto Supplies", "BOSCH", "F-026-407-156", 32.0, "GHS", true, 45),
            VendorListing(26, 10, 6, "Bright's Auto Center", "DENSO", "DXE-1007", 30.0, "GHS", true, 80),
            VendorListing(27, 10, 7, "Koforidua Car Parts", "SAKURA", "C-1141", 22.0, "GHS", true, 50),

            // === OIL FILTER HONDA (partId=11) ===
            VendorListing(28, 11, 1, "Accra Auto Parts", "HONDA GENUINE", "15400-RTA-003", 38.0, "GHS", true, 40),
            VendorListing(29, 11, 3, "Tema Auto Supplies", "MANN FILTER", "W610/6", 26.0, "GHS", true, 30),

            // === OIL FILTER UNIVERSAL (partId=12) ===
            VendorListing(30, 12, 4, "Cape Coast Motors", "ECONOMY PARTS", "EP-OF-001", 18.0, "GHS", true, 80),
            VendorListing(31, 12, 5, "Takoradi Spares", "SAKURA", "C-1003", 20.0, "GHS", true, 65),
            VendorListing(32, 12, 8, "Tamale Auto World", "CHAMPION", "COF100110S", 15.0, "GHS", true, 90),

            // === AIR FILTER TOYOTA (partId=13) ===
            VendorListing(33, 13, 1, "Accra Auto Parts", "TOYOTA GENUINE", "17801-21050", 55.0, "GHS", true, 35),
            VendorListing(34, 13, 6, "Bright's Auto Center", "MANN FILTER", "C2524", 48.0, "GHS", true, 20),
            VendorListing(35, 13, 2, "Kumasi Parts Hub", "BOSCH", "F-026-400-391", 42.0, "GHS", true, 25),

            // === AIR FILTER HONDA (partId=14) ===
            VendorListing(36, 14, 1, "Accra Auto Parts", "HONDA GENUINE", "17220-5AA-A00", 52.0, "GHS", true, 20),

            // === PERFORMANCE AIR FILTER (partId=15) ===
            VendorListing(37, 15, 6, "Bright's Auto Center", "K&N", "33-2360", 120.0, "GHS", true, 5),
            VendorListing(38, 15, 3, "Tema Auto Supplies", "BMC", "FB502/20", 110.0, "GHS", true, 3),

            // === IRIDIUM SPARK PLUG (partId=16) ===
            VendorListing(39, 16, 1, "Accra Auto Parts", "DENSO", "SK20R11", 95.0, "GHS", true, 40),
            VendorListing(40, 16, 3, "Tema Auto Supplies", "NGK", "ILKAR7B11", 85.0, "GHS", true, 50),
            VendorListing(41, 16, 6, "Bright's Auto Center", "BOSCH", "FR7KPP33U+", 78.0, "GHS", true, 25),
            VendorListing(42, 16, 2, "Kumasi Parts Hub", "CHAMPION", "RER8WYCB4", 72.0, "GHS", true, 35),

            // === COPPER SPARK PLUG (partId=17) ===
            VendorListing(43, 17, 4, "Cape Coast Motors", "NGK", "BKR6E-11", 35.0, "GHS", true, 80),
            VendorListing(44, 17, 7, "Koforidua Car Parts", "CHAMPION", "RN9YC", 30.0, "GHS", true, 60),
            VendorListing(45, 17, 8, "Tamale Auto World", "BOSCH", "WR7DC+", 28.0, "GHS", true, 70),

            // === TIMING BELT KIT (partId=18) ===
            VendorListing(46, 18, 1, "Accra Auto Parts", "TOYOTA GENUINE", "13568-09130-KIT", 280.0, "GHS", true, 5),
            VendorListing(47, 18, 6, "Bright's Auto Center", "GATES", "TCK-331", 220.0, "GHS", true, 8),
            VendorListing(48, 18, 3, "Tema Auto Supplies", "CONTINENTAL", "CT-1075K1", 240.0, "GHS", true, 4),

            // === ENGINE MOUNT FRONT (partId=19) ===
            VendorListing(49, 19, 1, "Accra Auto Parts", "TOYOTA GENUINE", "12361-0T010", 180.0, "GHS", true, 8),
            VendorListing(50, 19, 6, "Bright's Auto Center", "HUTCHINSON", "594440", 145.0, "GHS", true, 6),

            // === ENGINE MOUNT REAR (partId=20) ===
            VendorListing(51, 20, 2, "Kumasi Parts Hub", "OPTIMAL", "F8-8076", 95.0, "GHS", true, 12),

            // === RADIATOR (partId=21) ===
            VendorListing(52, 21, 1, "Accra Auto Parts", "DENSO", "DRM50034", 650.0, "GHS", true, 3),
            VendorListing(53, 21, 3, "Tema Auto Supplies", "NISSENS", "64652", 580.0, "GHS", true, 5),
            VendorListing(54, 21, 6, "Bright's Auto Center", "KOYO", "PL012161", 520.0, "GHS", true, 4),

            // === WATER PUMP (partId=23) ===
            VendorListing(55, 23, 1, "Accra Auto Parts", "AISIN", "WPT-190", 195.0, "GHS", true, 6),
            VendorListing(56, 23, 6, "Bright's Auto Center", "GMB", "GWT-121A", 155.0, "GHS", true, 10),

            // === THERMOSTAT (partId=24) ===
            VendorListing(57, 24, 1, "Accra Auto Parts", "TOYOTA GENUINE", "90916-03100", 45.0, "GHS", true, 30),
            VendorListing(58, 24, 2, "Kumasi Parts Hub", "GATES", "TH44982G1", 35.0, "GHS", true, 25),
            VendorListing(59, 24, 7, "Koforidua Car Parts", "WAHLER", "4174.82D", 32.0, "GHS", true, 20),

            // === FUEL FILTER (partId=25) ===
            VendorListing(60, 25, 2, "Kumasi Parts Hub", "BOSCH", "0450905318", 25.0, "GHS", true, 40),
            VendorListing(61, 25, 5, "Takoradi Spares", "MANN FILTER", "WK822/1", 22.0, "GHS", true, 35),

            // === FUEL PUMP (partId=26) ===
            VendorListing(62, 26, 1, "Accra Auto Parts", "DENSO", "195130-7030", 420.0, "GHS", true, 4),
            VendorListing(63, 26, 6, "Bright's Auto Center", "BOSCH", "0580314068", 380.0, "GHS", true, 5),

            // === BATTERY 60AH (partId=30) ===
            VendorListing(64, 30, 1, "Accra Auto Parts", "VARTA", "D24", 450.0, "GHS", true, 10),
            VendorListing(65, 30, 2, "Kumasi Parts Hub", "BOSCH", "S4-005", 420.0, "GHS", true, 8),
            VendorListing(66, 30, 6, "Bright's Auto Center", "EXIDE", "EA640", 380.0, "GHS", true, 15),
            VendorListing(67, 30, 3, "Tema Auto Supplies", "AMARON", "AAM-FL-560114042", 360.0, "GHS", true, 12),
            VendorListing(68, 30, 5, "Takoradi Spares", "CHLORIDE EXIDE", "N60", 320.0, "GHS", true, 20),

            // === BATTERY 75AH (partId=31) ===
            VendorListing(69, 31, 1, "Accra Auto Parts", "VARTA", "E44", 580.0, "GHS", true, 6),
            VendorListing(70, 31, 6, "Bright's Auto Center", "BOSCH", "S5-010", 550.0, "GHS", true, 8),
            VendorListing(71, 31, 3, "Tema Auto Supplies", "EXIDE", "EB740", 520.0, "GHS", true, 5),

            // === BATTERY 45AH (partId=32) ===
            VendorListing(72, 32, 4, "Cape Coast Motors", "CHLORIDE EXIDE", "N45", 250.0, "GHS", true, 15),
            VendorListing(73, 32, 7, "Koforidua Car Parts", "AMARON", "AAM-FL-545106036", 280.0, "GHS", true, 10),

            // === ALTERNATOR TOYOTA (partId=33) ===
            VendorListing(74, 33, 1, "Accra Auto Parts", "DENSO", "104210-3960", 750.0, "GHS", true, 3),
            VendorListing(75, 33, 6, "Bright's Auto Center", "BOSCH", "0124525121", 680.0, "GHS", true, 4),

            // === STARTER MOTOR (partId=35) ===
            VendorListing(76, 35, 1, "Accra Auto Parts", "DENSO", "428000-7770", 650.0, "GHS", true, 3),
            VendorListing(77, 35, 3, "Tema Auto Supplies", "BOSCH", "0001121411", 580.0, "GHS", true, 4),

            // === HEADLIGHT BULB H4 (partId=36) ===
            VendorListing(78, 36, 1, "Accra Auto Parts", "PHILIPS", "12342VPS2", 45.0, "GHS", true, 50),
            VendorListing(79, 36, 2, "Kumasi Parts Hub", "OSRAM", "64193NL-HCB", 40.0, "GHS", true, 40),
            VendorListing(80, 36, 4, "Cape Coast Motors", "NARVA", "48889", 25.0, "GHS", true, 80),

            // === LED HEADLIGHT H4 (partId=38) ===
            VendorListing(81, 38, 6, "Bright's Auto Center", "PHILIPS", "11342ULWX2", 180.0, "GHS", true, 10),
            VendorListing(82, 38, 3, "Tema Auto Supplies", "OSRAM", "65204CW", 160.0, "GHS", true, 8),

            // === FRONT SHOCK ABSORBER (partId=40) ===
            VendorListing(83, 40, 3, "Tema Auto Supplies", "KYB", "339372", 550.0, "GHS", true, 4),
            VendorListing(84, 40, 6, "Bright's Auto Center", "MONROE", "G16481", 480.0, "GHS", true, 6),
            VendorListing(85, 40, 1, "Accra Auto Parts", "TOKICO", "B3168", 440.0, "GHS", true, 5),

            // === REAR SHOCK ABSORBER (partId=41) ===
            VendorListing(86, 41, 3, "Tema Auto Supplies", "KYB", "343460", 420.0, "GHS", true, 6),
            VendorListing(87, 41, 6, "Bright's Auto Center", "MONROE", "G1089", 380.0, "GHS", true, 5),

            // === UNIVERSAL SHOCK (partId=42) ===
            VendorListing(88, 42, 4, "Cape Coast Motors", "MAGNUM", "MA-SA-001", 280.0, "GHS", true, 10),
            VendorListing(89, 42, 7, "Koforidua Car Parts", "RECORD", "RC-SA-200", 260.0, "GHS", true, 8),

            // === CONTROL ARM (partId=43) ===
            VendorListing(90, 43, 1, "Accra Auto Parts", "TRW", "JTC1408", 320.0, "GHS", true, 4),
            VendorListing(91, 43, 6, "Bright's Auto Center", "LEMFORDER", "35621", 350.0, "GHS", true, 3),

            // === TIE ROD END (partId=44) ===
            VendorListing(92, 44, 2, "Kumasi Parts Hub", "CTR", "CEKH-28R", 55.0, "GHS", true, 20),
            VendorListing(93, 44, 5, "Takoradi Spares", "555", "SE-T291R", 48.0, "GHS", true, 15),

            // === CLUTCH KIT (partId=45) ===
            VendorListing(94, 45, 1, "Accra Auto Parts", "AISIN", "KT-316", 850.0, "GHS", true, 2),
            VendorListing(95, 45, 6, "Bright's Auto Center", "EXEDY", "TYK2253", 780.0, "GHS", true, 3),
            VendorListing(96, 45, 3, "Tema Auto Supplies", "VALEO", "826865", 720.0, "GHS", true, 2),

            // === CV AXLE (partId=46) ===
            VendorListing(97, 46, 1, "Accra Auto Parts", "GSP", "218335", 450.0, "GHS", true, 3),
            VendorListing(98, 46, 6, "Bright's Auto Center", "SKF", "VKJC-6070", 520.0, "GHS", true, 2),

            // === WIPER BLADE SET (partId=50) ===
            VendorListing(99, 50, 1, "Accra Auto Parts", "BOSCH", "AR604S", 65.0, "GHS", true, 30),
            VendorListing(100, 50, 4, "Cape Coast Motors", "VALEO", "VM309", 45.0, "GHS", true, 20),
            VendorListing(101, 50, 2, "Kumasi Parts Hub", "DENSO", "DU-060L", 55.0, "GHS", true, 25),

            // === HEADLIGHT ASSEMBLY LEFT (partId=55) ===
            VendorListing(102, 55, 1, "Accra Auto Parts", "DEPO", "212-11M5L-EM", 450.0, "GHS", true, 3),
            VendorListing(103, 55, 6, "Bright's Auto Center", "TYC", "20-9596-90", 380.0, "GHS", true, 4),

            // === MUFFLER (partId=60) ===
            VendorListing(104, 60, 2, "Kumasi Parts Hub", "WALKER", "22580", 280.0, "GHS", true, 3),
            VendorListing(105, 60, 6, "Bright's Auto Center", "BOSAL", "185-483", 320.0, "GHS", true, 2),

            // === OXYGEN SENSOR (partId=61) ===
            VendorListing(106, 61, 1, "Accra Auto Parts", "DENSO", "234-4622", 180.0, "GHS", true, 8),
            VendorListing(107, 61, 3, "Tema Auto Supplies", "BOSCH", "15717", 165.0, "GHS", true, 6),

            // === IGNITION COIL (partId=62) ===
            VendorListing(108, 62, 1, "Accra Auto Parts", "DENSO", "673-1307", 85.0, "GHS", true, 15),
            VendorListing(109, 62, 6, "Bright's Auto Center", "NGK", "U5065", 75.0, "GHS", true, 12),

            // === IGNITION COIL PACK (partId=63) ===
            VendorListing(110, 63, 3, "Tema Auto Supplies", "BOSCH", "0221504470", 280.0, "GHS", true, 5),
        )
    }
}

data class PartWithListings(
    val part: Part,
    val listings: List<VendorListing>,
    val lowestPrice: Double?,
    val vendorCount: Int,
)

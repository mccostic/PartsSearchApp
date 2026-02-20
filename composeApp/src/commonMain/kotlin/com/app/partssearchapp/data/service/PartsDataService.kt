package com.app.partssearchapp.data.service

import com.app.partssearchapp.data.models.*
import kotlinx.coroutines.delay

interface PartsDataService {
    suspend fun getMakes(): List<VehicleMake>
    suspend fun getYearsForMake(makeId: Int): List<Int>
    suspend fun getModelsForMake(makeId: Int): List<VehicleModel>
    suspend fun getModelsForMakeAndYear(makeId: Int, year: Int): List<VehicleModel>
    suspend fun getEnginesForModel(makeId: Int, year: Int, modelId: Int): List<VehicleEngine>
    suspend fun getCategoriesForEngine(engineId: Int): List<PartCategory>
    suspend fun getPartsForCategory(categoryId: Int, engineId: Int): List<Part>
    suspend fun getListingsForPart(partId: Int): List<VendorListing>
    suspend fun getVendor(vendorId: Int): Vendor?
    suspend fun getVendors(): List<Vendor>
    suspend fun searchParts(query: String): List<Part>
}

class MockPartsDataService : PartsDataService {

    override suspend fun getMakes(): List<VehicleMake> {
        delay(300)
        return makes
    }

    override suspend fun getYearsForMake(makeId: Int): List<Int> {
        delay(200)
        return yearsForMake[makeId] ?: emptyList()
    }

    override suspend fun getModelsForMake(makeId: Int): List<VehicleModel> {
        delay(200)
        return models.filter { it.makeId == makeId }.distinctBy { it.name }
    }

    override suspend fun getModelsForMakeAndYear(makeId: Int, year: Int): List<VehicleModel> {
        delay(200)
        return models.filter { it.makeId == makeId && it.year == year }
    }

    override suspend fun getEnginesForModel(makeId: Int, year: Int, modelId: Int): List<VehicleEngine> {
        delay(200)
        return engines.filter { it.modelId == modelId }
    }

    override suspend fun getCategoriesForEngine(engineId: Int): List<PartCategory> {
        delay(300)
        return categories
    }

    override suspend fun getPartsForCategory(categoryId: Int, engineId: Int): List<Part> {
        delay(300)
        return parts.filter { it.categoryId == categoryId && it.compatibleEngineIds.contains(engineId) }
    }

    override suspend fun getListingsForPart(partId: Int): List<VendorListing> {
        delay(200)
        return vendorListings.filter { it.partId == partId }
    }

    override suspend fun getVendor(vendorId: Int): Vendor? {
        delay(100)
        return vendors.find { it.id == vendorId }
    }

    override suspend fun getVendors(): List<Vendor> {
        delay(200)
        return vendors
    }

    override suspend fun searchParts(query: String): List<Part> {
        delay(300)
        val lowerQuery = query.lowercase()
        return parts.filter {
            it.name.lowercase().contains(lowerQuery) ||
                it.partNumber.lowercase().contains(lowerQuery) ||
                it.description.lowercase().contains(lowerQuery)
        }
    }

    companion object {
        val makes = listOf(
            VehicleMake(1, "Toyota"),
            VehicleMake(2, "Honda"),
            VehicleMake(3, "Nissan"),
            VehicleMake(4, "Hyundai"),
            VehicleMake(5, "Kia"),
            VehicleMake(6, "Mercedes-Benz"),
            VehicleMake(7, "BMW"),
            VehicleMake(8, "Volkswagen"),
            VehicleMake(9, "Ford"),
            VehicleMake(10, "Mitsubishi"),
        )

        val yearsForMake = mapOf(
            1 to (2024 downTo 2010).toList(),
            2 to (2024 downTo 2010).toList(),
            3 to (2024 downTo 2012).toList(),
            4 to (2024 downTo 2012).toList(),
            5 to (2024 downTo 2014).toList(),
            6 to (2024 downTo 2010).toList(),
            7 to (2024 downTo 2010).toList(),
            8 to (2024 downTo 2012).toList(),
            9 to (2024 downTo 2010).toList(),
            10 to (2024 downTo 2012).toList(),
        )

        val models = listOf(
            // Toyota models
            VehicleModel(1, "Corolla", 1, 2024),
            VehicleModel(2, "Camry", 1, 2024),
            VehicleModel(3, "RAV4", 1, 2024),
            VehicleModel(4, "Hilux", 1, 2024),
            VehicleModel(5, "Land Cruiser", 1, 2024),
            VehicleModel(6, "Corolla", 1, 2023),
            VehicleModel(7, "Camry", 1, 2023),
            VehicleModel(8, "Hilux", 1, 2023),
            VehicleModel(9, "Corolla", 1, 2022),
            VehicleModel(10, "Camry", 1, 2022),
            // Honda models
            VehicleModel(11, "Civic", 2, 2024),
            VehicleModel(12, "Accord", 2, 2024),
            VehicleModel(13, "CR-V", 2, 2024),
            VehicleModel(14, "Fit", 2, 2024),
            VehicleModel(15, "Civic", 2, 2023),
            VehicleModel(16, "Accord", 2, 2023),
            // Nissan models
            VehicleModel(17, "Altima", 3, 2024),
            VehicleModel(18, "Sentra", 3, 2024),
            VehicleModel(19, "Pathfinder", 3, 2024),
            // Hyundai models
            VehicleModel(20, "Elantra", 4, 2024),
            VehicleModel(21, "Tucson", 4, 2024),
            VehicleModel(22, "Accent", 4, 2024),
            // Kia models
            VehicleModel(23, "Sportage", 5, 2024),
            VehicleModel(24, "Rio", 5, 2024),
            // Mercedes models
            VehicleModel(25, "C-Class", 6, 2024),
            VehicleModel(26, "E-Class", 6, 2024),
            // BMW models
            VehicleModel(27, "3 Series", 7, 2024),
            VehicleModel(28, "5 Series", 7, 2024),
            // VW models
            VehicleModel(29, "Golf", 8, 2024),
            VehicleModel(30, "Passat", 8, 2024),
            // Ford models
            VehicleModel(31, "Ranger", 9, 2024),
            VehicleModel(32, "EcoSport", 9, 2024),
            // Mitsubishi models
            VehicleModel(33, "L200", 10, 2024),
            VehicleModel(34, "Outlander", 10, 2024),
        )

        val engines = listOf(
            // Toyota Corolla 2024
            VehicleEngine(1, "1.8L L4 DOHC", 1),
            VehicleEngine(2, "2.0L L4 DOHC", 1),
            // Toyota Camry 2024
            VehicleEngine(3, "2.5L L4 DOHC", 2),
            VehicleEngine(4, "2.5L L4 Hybrid", 2),
            // Toyota RAV4 2024
            VehicleEngine(5, "2.5L L4 DOHC", 3),
            // Toyota Hilux 2024
            VehicleEngine(6, "2.4L L4 Turbo Diesel", 4),
            VehicleEngine(7, "2.7L L4 DOHC", 4),
            // Toyota Land Cruiser 2024
            VehicleEngine(8, "3.3L V6 Twin Turbo Diesel", 5),
            // Toyota Corolla 2023
            VehicleEngine(9, "1.8L L4 DOHC", 6),
            VehicleEngine(10, "2.0L L4 DOHC", 6),
            // Honda Civic 2024
            VehicleEngine(11, "2.0L L4 DOHC", 11),
            VehicleEngine(12, "1.5L L4 Turbocharged", 11),
            // Honda Accord 2024
            VehicleEngine(13, "1.5L L4 Turbocharged", 12),
            VehicleEngine(14, "2.0L L4 Hybrid", 12),
            // Honda CR-V 2024
            VehicleEngine(15, "1.5L L4 Turbocharged", 13),
            // Nissan Altima 2024
            VehicleEngine(16, "2.5L L4 DOHC", 17),
            // Hyundai Elantra 2024
            VehicleEngine(17, "2.0L L4 DOHC", 20),
            // Hyundai Tucson 2024
            VehicleEngine(18, "2.5L L4 DOHC", 21),
            // BMW 3 Series 2024
            VehicleEngine(19, "2.0L L4 Turbocharged", 27),
            // Mercedes C-Class 2024
            VehicleEngine(20, "1.5L L4 Turbocharged", 25),
        )

        val categories = listOf(
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
                )
            ),
            PartCategory(
                4,
                "Fuel & Air",
                subcategories = listOf(
                    PartCategory(41, "Fuel Filter", parentId = 4),
                    PartCategory(42, "Fuel Pump", parentId = 4),
                    PartCategory(43, "Fuel Injector", parentId = 4),
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
                )
            ),
            PartCategory(
                7,
                "Transmission",
                subcategories = listOf(
                    PartCategory(71, "Transmission Fluid", parentId = 7),
                    PartCategory(72, "Clutch Kit", parentId = 7),
                    PartCategory(73, "CV Joint / Axle", parentId = 7),
                )
            ),
            PartCategory(
                8,
                "Exhaust",
                subcategories = listOf(
                    PartCategory(81, "Muffler", parentId = 8),
                    PartCategory(82, "Catalytic Converter", parentId = 8),
                    PartCategory(83, "Exhaust Gasket", parentId = 8),
                )
            ),
            PartCategory(
                9,
                "Body & Interior",
                subcategories = listOf(
                    PartCategory(91, "Side Mirror", parentId = 9),
                    PartCategory(92, "Wiper Blade", parentId = 9),
                    PartCategory(93, "Door Handle", parentId = 9),
                )
            ),
        )

        // All engine IDs for broad compatibility
        private val allEngineIds = engines.map { it.id }
        private val toyotaEngineIds = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        private val hondaEngineIds = listOf(11, 12, 13, 14, 15)
        private val universalEngineIds = allEngineIds

        val parts = listOf(
            // Brake Pads (categoryId = 11)
            Part(
                1,
                "Front Brake Pad Set",
                "BP-TY-001",
                11,
                "Premium ceramic front brake pads",
                listOf("Ceramic compound", "Low dust", "Anti-squeal shim included"),
                toyotaEngineIds
            ),
            Part(
                2,
                "Rear Brake Pad Set",
                "BP-TY-002",
                11,
                "OEM quality rear brake pads",
                listOf("Semi-metallic", "Built-in wear indicator"),
                toyotaEngineIds
            ),
            Part(
                3,
                "Front Brake Pad Set",
                "BP-HN-001",
                11,
                "High performance front brake pads",
                listOf("Carbon-ceramic", "Track-rated"),
                hondaEngineIds
            ),
            Part(
                4,
                "Front Brake Pad Set",
                "BP-UN-001",
                11,
                "Universal fit front brake pads",
                listOf("Economy range", "Standard compound"),
                universalEngineIds
            ),

            // Brake Rotors (categoryId = 12)
            Part(
                5,
                "Front Brake Rotor",
                "BR-TY-001",
                12,
                "Ventilated front brake disc",
                listOf("280mm diameter", "Ventilated"),
                toyotaEngineIds
            ),
            Part(
                6,
                "Rear Brake Rotor",
                "BR-TY-002",
                12,
                "Solid rear brake disc",
                listOf("260mm diameter", "Solid"),
                toyotaEngineIds
            ),

            // Oil Filters (categoryId = 21)
            Part(
                7,
                "Oil Filter",
                "OF-TY-001",
                21,
                "Spin-on oil filter for Toyota engines",
                listOf("Micron rating: 20", "Anti-drain back valve"),
                toyotaEngineIds
            ),
            Part(
                8,
                "Oil Filter",
                "OF-HN-001",
                21,
                "Oil filter for Honda engines",
                listOf("Micron rating: 15", "Silicone anti-drain valve"),
                hondaEngineIds
            ),
            Part(
                9,
                "Oil Filter",
                "OF-UN-001",
                21,
                "Universal spin-on oil filter",
                listOf("Multi-vehicle fit", "Standard filtration"),
                universalEngineIds
            ),

            // Air Filters (categoryId = 22)
            Part(
                10,
                "Engine Air Filter",
                "AF-TY-001",
                22,
                "Panel-type engine air filter",
                listOf("High-flow", "Washable option"),
                toyotaEngineIds
            ),
            Part(
                11,
                "Engine Air Filter",
                "AF-HN-001",
                22,
                "OEM replacement air filter",
                listOf("Standard flow", "Paper element"),
                hondaEngineIds
            ),

            // Spark Plugs (categoryId = 23)
            Part(
                12,
                "Spark Plug (Set of 4)",
                "SP-TY-001",
                23,
                "Iridium spark plugs for 4-cylinder Toyota",
                listOf("Iridium tip", "Pre-gapped", "60,000km life"),
                toyotaEngineIds
            ),
            Part(
                13,
                "Spark Plug (Set of 4)",
                "SP-UN-001",
                23,
                "Standard copper spark plugs",
                listOf("Copper core", "20,000km life"),
                universalEngineIds
            ),

            // Radiator (categoryId = 31)
            Part(
                14,
                "Radiator Assembly",
                "RD-TY-001",
                31,
                "Complete radiator assembly",
                listOf("Aluminum core", "Plastic tanks", "Includes cap"),
                toyotaEngineIds
            ),

            // Water Pump (categoryId = 32)
            Part(
                15,
                "Water Pump",
                "WP-TY-001",
                32,
                "Engine water pump with gasket",
                listOf("Cast iron body", "Gasket included"),
                toyotaEngineIds
            ),

            // Thermostat (categoryId = 33)
            Part(
                16,
                "Thermostat",
                "TH-UN-001",
                33,
                "Standard thermostat with housing",
                listOf("82 degrees Celsius", "Housing included"),
                universalEngineIds
            ),

            // Battery (categoryId = 51)
            Part(
                17,
                "Car Battery 12V 60Ah",
                "BT-UN-001",
                51,
                "Maintenance-free car battery",
                listOf("12V", "60Ah", "540 CCA", "24-month warranty"),
                universalEngineIds
            ),
            Part(
                18,
                "Car Battery 12V 75Ah",
                "BT-UN-002",
                51,
                "Heavy-duty car battery",
                listOf("12V", "75Ah", "680 CCA", "36-month warranty"),
                universalEngineIds
            ),

            // Alternator (categoryId = 52)
            Part(
                19,
                "Alternator",
                "AL-TY-001",
                52,
                "Remanufactured alternator",
                listOf("100A output", "Internal regulator"),
                toyotaEngineIds
            ),

            // Shock Absorber (categoryId = 61)
            Part(
                20,
                "Front Shock Absorber (Pair)",
                "SA-TY-001",
                61,
                "Gas-charged front shock absorbers",
                listOf("Twin-tube", "Gas-charged", "Pair"),
                toyotaEngineIds
            ),
            Part(
                21,
                "Rear Shock Absorber (Pair)",
                "SA-TY-002",
                61,
                "Gas-charged rear shock absorbers",
                listOf("Twin-tube", "Gas-charged", "Pair"),
                toyotaEngineIds
            ),

            // Wiper Blades (categoryId = 92)
            Part(
                22,
                "Wiper Blade Set",
                "WB-UN-001",
                92,
                "All-season wiper blade set",
                listOf("Beam style", "Driver + Passenger", "Universal fit"),
                universalEngineIds
            ),
        )

        val vendors = listOf(
            Vendor(1, "Accra Auto Parts", "Accra, Abossey Okai", "+233 20 111 2222", 4.5, 1250, true),
            Vendor(2, "Kumasi Parts Hub", "Kumasi, Suame Magazine", "+233 24 333 4444", 4.2, 890, true),
            Vendor(3, "Tema Auto Supplies", "Tema, Community 1", "+233 27 555 6666", 4.7, 2100, true),
            Vendor(4, "Cape Coast Motors", "Cape Coast, Kotokuraba", "+233 20 777 8888", 3.9, 450, false),
            Vendor(5, "Takoradi Spares", "Takoradi, Market Circle", "+233 24 999 0000", 4.1, 670, true),
            Vendor(6, "Bright's Auto Center", "Accra, Spintex Road", "+233 55 111 3333", 4.8, 3200, true),
        )

        val vendorListings = listOf(
            // Brake Pad listings for Part 1 (Front Brake Pad Set - Toyota)
            VendorListing(1, 1, 1, "Accra Auto Parts", "TOYOTA GENUINE", "04465-02220", 185.0, "GHS", true, 25),
            VendorListing(2, 1, 2, "Kumasi Parts Hub", "BOSCH", "BP-3547", 145.0, "GHS", true, 15),
            VendorListing(3, 1, 3, "Tema Auto Supplies", "BREMBO", "P-83-099", 220.0, "GHS", true, 8),
            VendorListing(4, 1, 6, "Bright's Auto Center", "AKEBONO", "ACT-1210", 165.0, "GHS", true, 30),
            // Brake Pad Part 2 (Rear)
            VendorListing(5, 2, 1, "Accra Auto Parts", "TOYOTA GENUINE", "04466-02260", 155.0, "GHS", true, 20),
            VendorListing(6, 2, 3, "Tema Auto Supplies", "TRW", "GDB-3448", 125.0, "GHS", true, 12),
            // Brake Pad Part 4 (Universal)
            VendorListing(7, 4, 4, "Cape Coast Motors", "ECONOMY PARTS", "EP-BP-001", 75.0, "GHS", true, 50),
            VendorListing(8, 4, 5, "Takoradi Spares", "VALUE BRAKE", "VB-200", 85.0, "GHS", true, 40),
            // Brake Rotor Part 5
            VendorListing(9, 5, 1, "Accra Auto Parts", "TOYOTA GENUINE", "43512-02330", 350.0, "GHS", true, 6),
            VendorListing(10, 5, 6, "Bright's Auto Center", "BREMBO", "09.A820.11", 295.0, "GHS", true, 10),
            // Oil Filter Part 7
            VendorListing(11, 7, 1, "Accra Auto Parts", "TOYOTA GENUINE", "90915-YZZD4", 35.0, "GHS", true, 100),
            VendorListing(12, 7, 2, "Kumasi Parts Hub", "MANN FILTER", "W68/3", 28.0, "GHS", true, 60),
            VendorListing(13, 7, 3, "Tema Auto Supplies", "BOSCH", "F-026-407-156", 32.0, "GHS", true, 45),
            VendorListing(14, 7, 6, "Bright's Auto Center", "DENSO", "DXE-1007", 30.0, "GHS", true, 80),
            // Air Filter Part 10
            VendorListing(15, 10, 1, "Accra Auto Parts", "TOYOTA GENUINE", "17801-21050", 55.0, "GHS", true, 35),
            VendorListing(16, 10, 6, "Bright's Auto Center", "K&N", "33-2360", 120.0, "GHS", true, 5),
            // Spark Plug Part 12
            VendorListing(17, 12, 1, "Accra Auto Parts", "DENSO", "SK20R11", 95.0, "GHS", true, 40),
            VendorListing(18, 12, 3, "Tema Auto Supplies", "NGK", "ILKAR7B11", 85.0, "GHS", true, 50),
            VendorListing(19, 12, 6, "Bright's Auto Center", "BOSCH", "FR7KPP33U+", 78.0, "GHS", true, 25),
            // Battery Part 17
            VendorListing(20, 17, 1, "Accra Auto Parts", "VARTA", "D24", 450.0, "GHS", true, 10),
            VendorListing(21, 17, 2, "Kumasi Parts Hub", "BOSCH", "S4-005", 420.0, "GHS", true, 8),
            VendorListing(22, 17, 6, "Bright's Auto Center", "EXIDE", "EA640", 380.0, "GHS", true, 15),
            // Shock Absorber Part 20
            VendorListing(23, 20, 3, "Tema Auto Supplies", "KYB", "339-372", 550.0, "GHS", true, 4),
            VendorListing(24, 20, 6, "Bright's Auto Center", "MONROE", "G-16481", 480.0, "GHS", true, 6),
            // Wiper Blades Part 22
            VendorListing(25, 22, 1, "Accra Auto Parts", "BOSCH", "AR-604-S", 65.0, "GHS", true, 30),
            VendorListing(26, 22, 4, "Cape Coast Motors", "VALEO", "VM-309", 45.0, "GHS", true, 20),
        )
    }
}

package com.app.partssearchapp.di

import com.app.partssearchapp.data.service.CartManager
import com.app.partssearchapp.data.service.InventoryManager
import com.app.partssearchapp.data.service.NhtsaPartsDataService
import com.app.partssearchapp.data.service.PartsDataService
import com.app.partssearchapp.data.service.VpicLocalDataSource
import com.app.partssearchapp.network.ApiClient
import com.app.partssearchapp.network.MockApiClient
import com.app.partssearchapp.network.nhtsa.NhtsaApiClient
import com.app.partssearchapp.screens.cart.CartParams
import com.app.partssearchapp.screens.cart.CartViewModel
import com.app.partssearchapp.screens.categories.CategoriesParams
import com.app.partssearchapp.screens.categories.CategoriesViewModel
import com.app.partssearchapp.screens.home.HomeParams
import com.app.partssearchapp.screens.home.HomeViewModel
import com.app.partssearchapp.screens.login.LoginParams
import com.app.partssearchapp.screens.login.LoginViewModel
import com.app.partssearchapp.screens.login.usecases.AuthUseCase
import com.app.partssearchapp.screens.partdetail.PartDetailParams
import com.app.partssearchapp.screens.partdetail.PartDetailViewModel
import com.app.partssearchapp.screens.partslisting.PartsListingParams
import com.app.partssearchapp.screens.partslisting.PartsListingViewModel
import com.app.partssearchapp.screens.profile.ProfileParams
import com.app.partssearchapp.screens.profile.ProfileViewModel
import com.app.partssearchapp.screens.vehicleselection.VehicleSelectionParams
import com.app.partssearchapp.screens.vehicleselection.VehicleSelectionViewModel
import com.app.partssearchapp.screens.vendor.VendorDashboardParams
import com.app.partssearchapp.screens.vendor.VendorDashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Global singleton services
    single<ApiClient> { MockApiClient() }
    single { NhtsaApiClient() }
    single { InventoryManager() }
    single { VpicLocalDataSource(getOrNull()) }
    single<PartsDataService> { NhtsaPartsDataService(get(), get(), get()) }
    single { CartManager(get()) }
    factory { AuthUseCase() }

    // Screen ViewModels
    viewModel { (params: LoginParams) -> LoginViewModel(params, get()) }
    viewModel { (params: HomeParams) -> HomeViewModel(params, get(), get()) }
    viewModel { (params: ProfileParams) -> ProfileViewModel(params) }
    viewModel { (params: VehicleSelectionParams) -> VehicleSelectionViewModel(params, get()) }
    viewModel { (params: CategoriesParams) -> CategoriesViewModel(params, get()) }
    viewModel { (params: PartsListingParams) -> PartsListingViewModel(params, get(), get()) }
    viewModel { (params: PartDetailParams) -> PartDetailViewModel(params, get(), get()) }
    viewModel { (params: CartParams) -> CartViewModel(params, get()) }
    viewModel { (params: VendorDashboardParams) -> VendorDashboardViewModel(params, get()) }
}

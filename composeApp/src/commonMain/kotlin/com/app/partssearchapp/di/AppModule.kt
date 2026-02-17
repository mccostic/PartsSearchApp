package com.app.partssearchapp.di

import com.app.partssearchapp.network.ApiClient
import com.app.partssearchapp.network.MockApiClient
import com.app.partssearchapp.screens.home.HomeParams
import com.app.partssearchapp.screens.home.HomeViewModel
import com.app.partssearchapp.screens.home.data.NetworkRepositoryService
import com.app.partssearchapp.screens.home.data.RepositoryService
import com.app.partssearchapp.screens.login.LoginParams
import com.app.partssearchapp.screens.login.LoginViewModel
import com.app.partssearchapp.screens.login.usecases.AuthUseCase
import com.app.partssearchapp.screens.profile.ProfileParams
import com.app.partssearchapp.screens.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Global singleton services
    single<ApiClient> { MockApiClient() }
    single<RepositoryService> { NetworkRepositoryService(get()) }
    factory { AuthUseCase() }

    // Screen ViewModels (created per navigation entry, params injected at call site)
    viewModel { (params: LoginParams) -> LoginViewModel(params, get()) }
    viewModel { (params: HomeParams) -> HomeViewModel(params, get()) }
    viewModel { (params: ProfileParams) -> ProfileViewModel(params) }
}

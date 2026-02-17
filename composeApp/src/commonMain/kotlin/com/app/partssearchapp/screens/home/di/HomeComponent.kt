package com.app.partssearchapp.screens.home.di

import com.app.partssearchapp.di.ApplicationComponent
import com.app.partssearchapp.network.ApiClient
import com.app.partssearchapp.screens.home.HomeParams
import com.app.partssearchapp.screens.home.HomeViewModel
import com.app.partssearchapp.screens.home.data.NetworkRepositoryService
import com.app.partssearchapp.screens.home.data.RepositoryService
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class HomeScope

@HomeScope
@Component
abstract class HomeComponent(
  @get:Provides val params: HomeParams,
  @Component val applicationComponent: ApplicationComponent,
) {
  abstract val homeViewModel: HomeViewModel

  @HomeScope
  @Provides
  fun provideRepositoryService(apiClient: ApiClient): RepositoryService {
    return NetworkRepositoryService(apiClient)
  }

  companion object
}

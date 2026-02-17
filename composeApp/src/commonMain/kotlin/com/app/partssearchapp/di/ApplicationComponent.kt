package com.app.partssearchapp.di

import com.app.partssearchapp.network.ApiClient
import com.app.partssearchapp.network.MockApiClient
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ApplicationScope

@ApplicationScope
@Component
abstract class ApplicationComponent {
  
  // Global API client that can be used across the application
  @ApplicationScope
  @Provides
  fun provideApiClient(): ApiClient = MockApiClient()
  
  companion object
}

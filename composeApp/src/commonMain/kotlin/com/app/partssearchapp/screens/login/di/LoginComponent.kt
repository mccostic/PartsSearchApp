package com.app.partssearchapp.screens.login.di

import com.app.partssearchapp.di.ApplicationComponent
import com.app.partssearchapp.screens.login.LoginParams
import com.app.partssearchapp.screens.login.LoginViewModel
import com.app.partssearchapp.screens.login.usecases.AuthUseCase
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class LoginScope

@LoginScope
@Component
abstract class LoginComponent(
    @get:Provides val params: LoginParams,
    @Component val applicationComponent: ApplicationComponent,
) {
    abstract val authUseCase: AuthUseCase
    abstract val loginViewModel: LoginViewModel
    
    companion object
}

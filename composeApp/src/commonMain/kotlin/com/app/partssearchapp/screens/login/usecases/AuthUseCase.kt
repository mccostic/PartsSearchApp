package com.app.partssearchapp.screens.login.usecases

import kotlinx.coroutines.delay

/**
 * UseCase for handling authentication operations
 */
class AuthUseCase {

    /**
     * Validates email format
     */
    fun validateEmail(email: String): String? = when {
        email.isEmpty() -> "Email is required"
        !email.contains("@") || !email.contains(".") -> "Invalid email format"
        else -> null
    }

    /**
     * Validates password format
     */
    fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }

    /**
     * Validates confirm password matches password
     */
    fun validateConfirmPassword(password: String, confirmPassword: String): String? = when {
        confirmPassword.isEmpty() -> "Confirm password is required"
        confirmPassword != password -> "Passwords do not match"
        else -> null
    }

    /**
     * Mock sign in operation
     */
    suspend fun signIn(email: String, password: String): Result<Unit> {
        // Simulate network delay
        delay(1000)

        // Mock validation - accept any email with password "password"
        return if (password == "password") {
            println("Sign in successful for: $email")
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    /**
     * Mock sign up operation
     */
    suspend fun signUp(email: String, password: String): Result<Unit> {
        // Simulate network delay
        delay(1000)

        // Mock success for all valid inputs
        println("Sign up successful for: $email with password length: ${password.length}")
        return Result.success(Unit)
    }

    /**
     * Mock password reset operation
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        // Simulate network delay
        delay(1000)

        println("Password reset sent to: $email")
        return Result.success(Unit)
    }

    /**
     * Converts exceptions to user-friendly error messages
     */
    fun getUserFriendlyAuthError(exception: Exception): String = when {
        exception.message?.contains("Invalid credentials") == true ->
            "Invalid email or password. Please try again."
        exception.message?.contains("network") == true ->
            "Network error. Please check your connection."
        else -> "An error occurred. Please try again."
    }
}

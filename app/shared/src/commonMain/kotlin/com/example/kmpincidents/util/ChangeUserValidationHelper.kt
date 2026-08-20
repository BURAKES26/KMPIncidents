package com.example.kmpincidents.util

object ChangeUserValidationHelper {

    private val emailRegex = Regex(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
        RegexOption.IGNORE_CASE
    )

    /** Returns null if valid, or an error message to display otherwise. */
    fun validateUserProfile(
        username: String,
        email: String,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): String? {
        if (username.isBlank()) return "Username cannot be empty"
        if (username.length < 3) return "Username must be at least 3 characters"

        if (email.isBlank()) return "Email cannot be empty"
        if (!emailRegex.matches(email)) return "Please enter a valid email address"

        val isChangingPassword = currentPassword.isNotBlank() ||
                newPassword.isNotBlank() ||
                confirmPassword.isNotBlank()

        if (isChangingPassword) {
            if (currentPassword.isBlank()) return "Current password is required"
            if (newPassword.isBlank()) return "New password cannot be empty"
            if (newPassword.length < 6) return "Password must be at least 6 characters"
            if (confirmPassword.isBlank()) return "Please confirm your password"
            if (newPassword != confirmPassword) return "Passwords do not match"
        }

        return null
    }
}
package com.secretstash.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Request for user registration")
data class UserRegistrationRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @field:Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, and ._-")
    @Schema(
        description = "User's username", 
        example = "john_doe", 
        required = true, 
        minLength = 3, 
        maxLength = 50, 
        pattern = "^[a-zA-Z0-9._-]+$"
    )
    val username: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, one special character, and no whitespace"
    )
    @Schema(
        description = "User's password", 
        example = "StrongP@ss123", 
        required = true, 
        minLength = 8,
        format = "password"
    )
    val password: String
)

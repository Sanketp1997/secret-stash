package com.secretstash.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request for user authentication")
data class AuthRequest(
    @Schema(description = "User's username", example = "john_doe", required = true)
    val username: String,
    
    @Schema(description = "User's password", example = "Password123!", required = true)
    val password: String
)

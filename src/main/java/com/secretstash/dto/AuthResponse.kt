package com.secretstash.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response after successful authentication")
data class AuthResponse(
    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    val token: String,
    
    @Schema(description = "Authenticated username", example = "john_doe", required = true)
    val username: String,
    
    @Schema(description = "Status message", example = "Authentication successful", required = true)
    val message: String = "Authentication successful"
)

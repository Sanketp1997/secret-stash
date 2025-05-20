package com.secretstash.exception

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Response returned when an error occurs")
data class ErrorResponse(
    @Schema(description = "Time when the error occurred", example = "2025-05-20T11:45:30", required = true)
    val timestamp: LocalDateTime,
    
    @Schema(description = "HTTP status code", example = "400", required = true)
    val status: Int,
    
    @Schema(description = "Error type", example = "Bad Request", required = true)
    val error: String,
    
    @Schema(description = "Detailed error message", example = "Note not found with ID: 123", required = true)
    val message: String,
    
    @Schema(description = "Request path that caused the error", example = "/api/notes/123", required = true)
    val path: String
)

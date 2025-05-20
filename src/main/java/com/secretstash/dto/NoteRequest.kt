package com.secretstash.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Schema(description = "Request for creating or updating a note")
data class NoteRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must be less than 255 characters")
    @Schema(description = "Title of the note", example = "My Secret Note", required = true, maxLength = 255)
    val title: String,
    
    @field:NotBlank(message = "Content is required")
    @Schema(description = "Content of the note", example = "This is my secret information that needs to be stored securely.", required = true)
    val content: String,
    
    // Self-destruction timer (optional)
    @Schema(description = "Optional expiry time for the note (self-destruction)", example = "2025-06-30T23:59:59", required = false)
    val expiryTime: LocalDateTime? = null
)

package com.secretstash.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Response containing note details")
data class NoteResponse(
    @Schema(description = "Unique identifier of the note", example = "1", required = true)
    val id: Long,
    
    @Schema(description = "Title of the note", example = "My Secret Note", required = true)
    val title: String,
    
    @Schema(description = "Content of the note", example = "This is my secret information stored securely.", required = true)
    val content: String,
    
    @Schema(description = "Date and time when the note was created", example = "2025-05-20T10:30:00", required = true)
    val createdAt: LocalDateTime,
    
    @Schema(description = "Date and time when the note was last updated", example = "2025-05-20T11:45:00", required = true)
    val updatedAt: LocalDateTime,
    
    @Schema(description = "Optional expiry time for the note (self-destruction)", example = "2025-06-30T23:59:59", required = false)
    val expiryTime: LocalDateTime?,
    
    @Schema(description = "Version number of the note", example = "1", required = true)
    val version: Int
)

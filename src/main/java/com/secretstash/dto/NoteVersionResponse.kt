package com.secretstash.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Response containing details of a specific note version")
data class NoteVersionResponse(
    @Schema(description = "Unique identifier of the note version", example = "1", required = true)
    val id: Long,
    
    @Schema(description = "Title of the note at this version", example = "My Secret Note (Draft)", required = true)
    val title: String,
    
    @Schema(description = "Content of the note at this version", example = "This is an earlier version of my secret information.", required = true)
    val content: String,
    
    @Schema(description = "Date and time when this version was created", example = "2025-05-19T15:20:00", required = true)
    val createdAt: LocalDateTime,
    
    @Schema(description = "Version number", example = "1", required = true)
    val versionNumber: Int,
    
    @Schema(description = "ID of the parent note", example = "42", required = true)
    val noteId: Long
)

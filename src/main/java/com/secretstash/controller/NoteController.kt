package com.secretstash.controller

import com.secretstash.dto.NoteRequest
import com.secretstash.dto.NoteResponse
import com.secretstash.dto.NoteVersionResponse
import com.secretstash.service.NoteService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notes", description = "The Notes API")
@SecurityRequirement(name = "JWT")
class NoteController(private val noteService: NoteService) {

    @Operation(
        summary = "Create a new note",
        description = "Creates a new note with the provided details"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Note created successfully",
                content = [Content(schema = Schema(implementation = NoteResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid input"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    @PostMapping
    fun createNote(@Valid @RequestBody noteRequest: NoteRequest): ResponseEntity<NoteResponse> {
        val createdNote = noteService.createNote(noteRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote)
    }

    @Operation(
        summary = "Get a note by ID",
        description = "Returns a note based on the provided ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Note found",
                content = [Content(schema = Schema(implementation = NoteResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Note not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    @GetMapping("/{id}")
    fun getNoteById(
        @Parameter(description = "ID of the note to retrieve") 
        @PathVariable id: Long
    ): ResponseEntity<NoteResponse> {
        val note = noteService.getNoteById(id)
        return ResponseEntity.ok(note)
    }

    @Operation(
        summary = "Get all notes with pagination",
        description = "Returns a paginated list of notes"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved notes"
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    @GetMapping
    fun getNotes(
        @Parameter(description = "Page number (0-based)") 
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "Number of items per page") 
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<NoteResponse>> {
        val notes = noteService.getNotes(page, size)
        return ResponseEntity.ok(notes)
    }

    @Operation(
        summary = "Update a note",
        description = "Updates an existing note with new information"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Note updated successfully",
                content = [Content(schema = Schema(implementation = NoteResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid input"),
            ApiResponse(responseCode = "404", description = "Note not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    @PutMapping("/{id}")
    fun updateNote(
        @Parameter(description = "ID of the note to update") 
        @PathVariable id: Long,
        
        @Valid @RequestBody noteRequest: NoteRequest
    ): ResponseEntity<NoteResponse> {
        val updatedNote = noteService.updateNote(id, noteRequest)
        return ResponseEntity.ok(updatedNote)
    }

    @Operation(
        summary = "Delete a note",
        description = "Deletes a note based on the provided ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Note deleted successfully"),
            ApiResponse(responseCode = "404", description = "Note not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    @DeleteMapping("/{id}")
    fun deleteNote(
        @Parameter(description = "ID of the note to delete") 
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        noteService.deleteNote(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Get note version history",
        description = "Returns the version history of a note"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Version history retrieved successfully"
            ),
            ApiResponse(responseCode = "404", description = "Note not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    @GetMapping("/{id}/versions")
    fun getNoteVersions(
        @Parameter(description = "ID of the note") 
        @PathVariable id: Long
    ): ResponseEntity<List<NoteVersionResponse>> {
        val versions = noteService.getNoteVersions(id)
        return ResponseEntity.ok(versions)
    }
}

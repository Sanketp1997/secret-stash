package com.secretstash.service

import com.secretstash.dto.NoteRequest
import com.secretstash.dto.NoteResponse
import com.secretstash.dto.NoteVersionResponse
import com.secretstash.model.Note
import com.secretstash.model.NoteVersion
import com.secretstash.model.User
import com.secretstash.repository.NoteRepository
import com.secretstash.repository.NoteVersionRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NoteService(
    private val noteRepository: NoteRepository,
    private val noteVersionRepository: NoteVersionRepository,
    private val authService: AuthService
) {

    @Transactional
    fun createNote(noteRequest: NoteRequest): NoteResponse {
        val currentUser = authService.getCurrentUser()
        
        // Check if user has reached the limit of 5,000 notes
        val noteCount = noteRepository.countByUser(currentUser)
        if (noteCount >= 5000) {
            throw IllegalStateException("You have reached the maximum limit of 5,000 notes")
        }
        
        val note = Note(
            title = noteRequest.title,
            content = noteRequest.content,
            user = currentUser,
            expiryTime = noteRequest.expiryTime
        )
        
        val savedNote = noteRepository.save(note)
        return mapToNoteResponse(savedNote)
    }
    
    @Transactional(readOnly = true)
    fun getNoteById(noteId: Long): NoteResponse {
        val currentUser = authService.getCurrentUser()
        val note = findNoteByIdAndUser(noteId, currentUser)
        return mapToNoteResponse(note)
    }
    
    @Transactional(readOnly = true)
    fun getNotes(page: Int, size: Int): Page<NoteResponse> {
        val currentUser = authService.getCurrentUser()
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val now = LocalDateTime.now()
        
        // Fetch only active notes (not expired)
        return noteRepository.findActiveNotesByUser(currentUser, now, pageable)
            .map { this.mapToNoteResponse(it) }
    }
    
    @Transactional
    fun updateNote(noteId: Long, noteRequest: NoteRequest): NoteResponse {
        val currentUser = authService.getCurrentUser()
        val note = findNoteByIdAndUser(noteId, currentUser)
        
        // Create a version of the current note before updating
        createNoteVersion(note)
        
        // Update the note
        note.title = noteRequest.title
        note.content = noteRequest.content
        note.updatedAt = LocalDateTime.now()
        note.expiryTime = noteRequest.expiryTime
        note.version += 1
        
        val updatedNote = noteRepository.save(note)
        return mapToNoteResponse(updatedNote)
    }
    
    @Transactional
    fun deleteNote(noteId: Long) {
        val currentUser = authService.getCurrentUser()
        val note = findNoteByIdAndUser(noteId, currentUser)
        
        // Delete all versions first to avoid foreign key constraints
        val versions = noteVersionRepository.findByNoteOrderByVersionNumberDesc(note)
        noteVersionRepository.deleteAll(versions)
        
        // Delete the note
        noteRepository.delete(note)
    }
    
    @Transactional(readOnly = true)
    fun getNoteVersions(noteId: Long): List<NoteVersionResponse> {
        val currentUser = authService.getCurrentUser()
        val note = findNoteByIdAndUser(noteId, currentUser)
        
        return noteVersionRepository.findByNoteOrderByVersionNumberDesc(note)
            .map { mapToNoteVersionResponse(it) }
    }
    
    @Transactional
    fun deleteExpiredNotes() {
        val now = LocalDateTime.now()
        val expiredNotes = noteRepository.findExpiredNotes(now)
        noteRepository.deleteAll(expiredNotes)
    }
    
    private fun createNoteVersion(note: Note) {
        val noteVersion = NoteVersion(
            title = note.title,
            content = note.content,
            versionNumber = note.version,
            note = note
        )
        noteVersionRepository.save(noteVersion)
    }
    
    private fun findNoteByIdAndUser(noteId: Long, user: User): Note {
        return noteRepository.findByIdAndUser(noteId, user)
            .orElseThrow { EntityNotFoundException("Note not found with id: $noteId") }
    }
    
    private fun mapToNoteResponse(note: Note): NoteResponse {
        return NoteResponse(
            id = note.id,
            title = note.title,
            content = note.content,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            expiryTime = note.expiryTime,
            version = note.version
        )
    }
    
    private fun mapToNoteVersionResponse(noteVersion: NoteVersion): NoteVersionResponse {
        return NoteVersionResponse(
            id = noteVersion.id,
            title = noteVersion.title,
            content = noteVersion.content,
            createdAt = noteVersion.createdAt,
            versionNumber = noteVersion.versionNumber,
            noteId = noteVersion.note.id
        )
    }
}

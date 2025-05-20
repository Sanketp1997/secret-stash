package com.secretstash.service

import com.secretstash.dto.NoteRequest
import com.secretstash.dto.NoteVersionResponse
import com.secretstash.model.Note
import com.secretstash.model.NoteVersion
import com.secretstash.model.User
import com.secretstash.repository.NoteRepository
import com.secretstash.repository.NoteVersionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.Page
import java.time.LocalDateTime
import java.util.*

class NoteServiceTest {

    private val noteRepository = mock<NoteRepository>()
    private val noteVersionRepository = mock<NoteVersionRepository>()
    private val authService = mock<AuthService>()
    private val noteService = NoteService(noteRepository, noteVersionRepository, authService)

    private val user = User(id = 1, username = "user", password = "pass")
    private val note = Note(id = 1, title = "title", content = "content", user = user)

    @Test
    fun `createNote should save note and return NoteResponse`() {
        val request = NoteRequest("title", "content", null)
        whenever(authService.getCurrentUser()).thenReturn(user)
        whenever(noteRepository.countByUser(user)).thenReturn(0)
        whenever(noteRepository.save(any())).thenReturn(note)

        val response = noteService.createNote(request)

        assertEquals("title", response.title)
        assertEquals("content", response.content)
        verify(noteRepository).save(any())
    }

//    Add more test cases

    @Test
    fun `updateNote should update note and return NoteResponse`() {
        val request = NoteRequest("title", "content", null)
        whenever(authService.getCurrentUser()).thenReturn(user)
        whenever(noteRepository.findByIdAndUser(note.id, user)).thenReturn(Optional.of(note))
        whenever(noteRepository.save(any())).thenReturn(note)

        val response = noteService.updateNote(note.id, request)

        assertEquals("title", response.title)
        assertEquals("content", response.content)
        verify(noteRepository).save(any())
    }

    @Test
    fun `getNoteById should return NoteResponse`() {
        whenever(authService.getCurrentUser()).thenReturn(user)
        whenever(noteRepository.findByIdAndUser(note.id, user)).thenReturn(Optional.of(note))

        val response = noteService.getNoteById(note.id)

        assertEquals("title", response.title)
        assertEquals("content", response.content)
    }

    @Test
    fun `getNotes should return Page of NoteResponse`() {
        whenever(authService.getCurrentUser()).thenReturn(user)
        whenever(noteRepository.findActiveNotesByUser(eq(user), any(), any())).thenReturn(Page.empty())
        val page = noteService.getNotes(0, 10)

        assert(page.isEmpty)
    }

    @Test
    fun `deleteNote should delete note and all versions`() {
        whenever(authService.getCurrentUser()).thenReturn(user)
        whenever(noteRepository.findByIdAndUser(note.id, user)).thenReturn(Optional.of(note))
        whenever(noteVersionRepository.findByNoteOrderByVersionNumberDesc(any())).thenReturn(listOf())

        noteService.deleteNote(note.id)

        verify(noteRepository).delete(note)
        verify(noteVersionRepository).deleteAll(any())
    }


    @Test
    fun `getNoteVersions should return list of NoteVersionResponse`() {
        val note = Note(id = 1, title = "title", content = "content", user = user)
        val version1 = NoteVersion(1, "title", "content", LocalDateTime.now(), 1, note)
        val version2 = NoteVersion(2, "title", "content", LocalDateTime.now(), 2, note)
        whenever(authService.getCurrentUser()).thenReturn(user)
        whenever(noteRepository.findByIdAndUser(note.id, user)).thenReturn(Optional.of(note))
        whenever(noteVersionRepository.findByNoteOrderByVersionNumberDesc(note)).thenReturn(listOf(version1, version2))

        val versions = noteService.getNoteVersions(note.id)


        val noteVersionResponse1 = NoteVersionResponse(version1.id, version1.title, version1.content, version1.createdAt, version1.versionNumber, version1.note.id)
        val noteVersionResponse2 = NoteVersionResponse(version2.id, version2.title, version2.content, version2.createdAt, version2.versionNumber, version2.note.id)
        assertEquals((listOf(noteVersionResponse1, noteVersionResponse2)), versions)
    }


    @Test
    fun `deleteExpiredNotes should delete expired notes and all versions`() {
        val expiredNote = Note(id = 1, title = "title", content = "content", user = user, expiryTime = LocalDateTime.now().minusDays(1))
        val notExpiredNote = Note(id = 2, title = "title", content = "content", user = user, expiryTime = LocalDateTime.now().plusDays(1))
        whenever(noteRepository.findExpiredNotes(any())).thenReturn(listOf(expiredNote))

        noteService.deleteExpiredNotes()

        verify(noteRepository).deleteAll(listOf(expiredNote))
        verify(noteRepository, never()).delete(notExpiredNote)
    }


    @Test
    fun `createNote should throw IllegalStateException if user has reached the limit`() {
        whenever(authService.getCurrentUser()).thenReturn(user)
        whenever(noteRepository.countByUser(user)).thenReturn(5000)

        val request = NoteRequest("title", "content", null)
        assertThrows(IllegalStateException::class.java) {
            noteService.createNote(request)
        }
    }

}
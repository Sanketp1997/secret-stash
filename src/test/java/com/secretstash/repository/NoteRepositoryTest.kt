package com.secretstash.repository

import com.secretstash.model.Note
import com.secretstash.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

@DataJpaTest
class NoteRepositoryTest @Autowired constructor(
    val noteRepository: NoteRepository,
    val userRepository: UserRepository
) {

    @Test
    fun `findByUser returns notes for user`() {
        val user = userRepository.save(User(username = "repoUser", password = "pass"))
        val note = noteRepository.save(Note(title = "Note1", content = "Content", user = user))
        val notes = noteRepository.findByUser(user, PageRequest.of(0, 10))
        assertTrue(notes.content.contains(note))
    }

    @Test
    fun `findByIdAndUser returns note if owned by user`() {
        val user = userRepository.save(User(username = "repoUser2", password = "pass"))
        val note = noteRepository.save(Note(title = "Note2", content = "Content", user = user))
        val found = noteRepository.findByIdAndUser(note.id, user)
        assertTrue(found.isPresent)
        assertEquals(note, found.get())
    }

    @Test
    fun `findActiveNotesByUser returns only active notes`() {
        val user = userRepository.save(User(username = "repoUser3", password = "pass"))
        val now = LocalDateTime.now()
        val activeNote = noteRepository.save(Note(title = "Active", content = "C", user = user, expiryTime = now.plusDays(1)))
        val expiredNote = noteRepository.save(Note(title = "Expired", content = "C", user = user, expiryTime = now.minusDays(1)))
        val activeNotes = noteRepository.findActiveNotesByUser(user, now, PageRequest.of(0, 10))
        assertTrue(activeNotes.content.any { it.id == activeNote.id })
        assertFalse(activeNotes.content.any { it.id == expiredNote.id })
    }

    @Test
    fun `countByUser returns correct count`() {
        val user = userRepository.save(User(username = "repoUser4", password = "pass"))
        noteRepository.save(Note(title = "N1", content = "C", user = user))
        noteRepository.save(Note(title = "N2", content = "C", user = user))
        assertEquals(2, noteRepository.countByUser(user))
    }
}
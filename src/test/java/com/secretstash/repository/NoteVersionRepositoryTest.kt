package com.secretstash.repository

import com.secretstash.model.Note
import com.secretstash.model.NoteVersion
import com.secretstash.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class NoteVersionRepositoryTest @Autowired constructor(
    val noteVersionRepository: NoteVersionRepository,
    val noteRepository: NoteRepository,
    val userRepository: UserRepository
) {

    @Test
    fun `findByNoteOrderByVersionNumberDesc returns versions in descending order`() {
        val user = userRepository.save(User(username = "verUser", password = "pass"))
        val note = noteRepository.save(Note(title = "N", content = "C", user = user))
        val v1 = noteVersionRepository.save(NoteVersion(title = "V1", content = "C", note = note, versionNumber = 1))
        val v2 = noteVersionRepository.save(NoteVersion(title = "V2", content = "C", note = note, versionNumber = 2))
        val versions = noteVersionRepository.findByNoteOrderByVersionNumberDesc(note)
        assertEquals(listOf(v2, v1), versions)
    }

    @Test
    fun `countByNoteId returns correct count`() {
        val user = userRepository.save(User(username = "verUser2", password = "pass"))
        val note = noteRepository.save(Note(title = "N", content = "C", user = user))
        noteVersionRepository.save(NoteVersion(title = "V1", content = "C", note = note, versionNumber = 1))
        noteVersionRepository.save(NoteVersion(title = "V2", content = "C", note = note, versionNumber = 2))
        assertEquals(2, noteVersionRepository.countByNoteId(note.id))
    }
}
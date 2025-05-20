package com.secretstash.repository

import com.secretstash.model.Note
import com.secretstash.model.NoteVersion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NoteVersionRepository : JpaRepository<NoteVersion, Long> {
    fun findByNoteOrderByVersionNumberDesc(note: Note): List<NoteVersion>

    fun countByNoteId(noteId: Long): Long
}

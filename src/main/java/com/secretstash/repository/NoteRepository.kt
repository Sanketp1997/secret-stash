package com.secretstash.repository

import com.secretstash.model.Note
import com.secretstash.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface NoteRepository : JpaRepository<Note, Long> {
    fun findByIdAndUser(id: Long, user: User): Optional<Note>
    
    fun findByUser(user: User, pageable: Pageable): Page<Note>

    @Query("SELECT n FROM Note n WHERE n.expiryTime IS NOT NULL AND n.expiryTime <= :now")
    fun findExpiredNotes(now: LocalDateTime): List<Note>
    
    @Query("SELECT n FROM Note n WHERE n.user = :user AND (n.expiryTime IS NULL OR n.expiryTime > :now)")
    fun findActiveNotesByUser(user: User, now: LocalDateTime, pageable: Pageable): Page<Note>
    
    fun countByUser(user: User): Long
}

package com.secretstash.config

import com.secretstash.service.NoteService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
class SchedulingConfig(private val noteService: NoteService) {

    private val logger = LoggerFactory.getLogger(SchedulingConfig::class.java)

    // Run every minute
    @Scheduled(fixedRate = 60 * 1000)
    fun scheduleExpiredNotesDeletion() {
        logger.info("Starting scheduled task: Delete expired notes")
        try {
            noteService.deleteExpiredNotes()
            logger.info("Completed scheduled task: Delete expired notes")
        } catch (e: Exception) {
            logger.error("Error in scheduled task: Delete expired notes", e)
        }
    }
}

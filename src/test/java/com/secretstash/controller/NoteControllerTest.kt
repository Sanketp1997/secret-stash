package com.secretstash.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.secretstash.dto.NoteRequest
import com.secretstash.dto.NoteResponse
import com.secretstash.dto.NoteVersionResponse
import com.secretstash.security.JwtTokenProvider
import com.secretstash.service.NoteService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@WebMvcTest(NoteController::class)
class NoteControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var noteService: NoteService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider


    @Test
    fun `createNote returns 201 on success`() {
        val request = NoteRequest("title", "content", null)
        val response = NoteResponse(1, "title", "content", LocalDateTime.now(), LocalDateTime.now(), null, 1)
        whenever(noteService.createNote(eq(request))).thenReturn(response)

        mockMvc.perform(
            post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    fun `createNote returns 400 on invalid input`() {
        val request = NoteRequest("", "", null) // invalid

        mockMvc.perform(
            post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getNotes returns 200 with notes`() {
        val response = NoteResponse(1, "title", "content", LocalDateTime.now(), LocalDateTime.now(), null, 1)
        whenever(noteService.getNotes(eq(0), eq(10))).thenReturn(PageImpl(listOf(response), PageRequest.of(0, 10), 1))

        mockMvc.perform(get("/api/notes?page=0&size=10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1))
    }

    @Test
    fun `getNoteById returns 200 on success`() {
        val response = NoteResponse(1, "title", "content", LocalDateTime.now(), LocalDateTime.now(), null, 1)
        whenever(noteService.getNoteById(eq(1L))).thenReturn(response)

        mockMvc.perform(get("/api/notes/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    fun `getNoteById returns 404 if not found`() {
        whenever(noteService.getNoteById(eq(99L))).thenThrow(
            ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
        )

        mockMvc.perform(get("/api/notes/99"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `updateNote returns 200 on success`() {
        val request = NoteRequest("updated", "content", null)
        val response = NoteResponse(1, "updated", "content", LocalDateTime.now(), LocalDateTime.now(), null, 2)
        whenever(noteService.updateNote(eq(1L), eq(request))).thenReturn(response)

        mockMvc.perform(
            put("/api/notes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("updated"))
    }

    @Test
    fun `updateNote returns 404 if not found`() {
        val request = NoteRequest("title", "content", null)
        whenever(noteService.updateNote(eq(99L), eq(request))).thenThrow(
            ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
        )

        mockMvc.perform(
            put("/api/notes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `deleteNote returns 204 on success`() {
        doNothing().whenever(noteService).deleteNote(eq(1L))

        mockMvc.perform(delete("/api/notes/1"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `deleteNote returns 404 if not found`() {
        doThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"))
            .whenever(noteService).deleteNote(eq(99L))

        mockMvc.perform(delete("/api/notes/99"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getNoteVersions returns 200 with versions`() {
        val version = NoteVersionResponse(1, "title", "content", LocalDateTime.now(), 1, 1)
        whenever(noteService.getNoteVersions(eq(1L))).thenReturn(listOf(version))

        mockMvc.perform(get("/api/notes/1/versions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
    }

    @Test
    fun `getNoteVersions returns 404 if note not found`() {
        whenever(noteService.getNoteVersions(eq(99L))).thenThrow(
            ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
        )

        mockMvc.perform(get("/api/notes/99/versions"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getNoteVersions returns 200 with multiple versions`() {
        val version1 = NoteVersionResponse(1, "title", "content", LocalDateTime.now(), 1, 1)
        val version2 = NoteVersionResponse(2, "title", "content", LocalDateTime.now(), 2, 1)
        whenever(noteService.getNoteVersions(eq(1L))).thenReturn(listOf(version1, version2))

        mockMvc.perform(get("/api/notes/1/versions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
    }

    @TestConfiguration
    class TestSecurityConfig {
        @Bean
        fun webSecurityCustomizer(): WebSecurityCustomizer {
            return WebSecurityCustomizer { web: WebSecurity ->
                web.ignoring().anyRequest()
            }
        }
    }
}